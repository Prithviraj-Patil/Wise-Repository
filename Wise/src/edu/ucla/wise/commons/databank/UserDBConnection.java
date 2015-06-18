/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.commons.databank;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.persistence.data.Answer;
import edu.ucla.wise.persistence.data.WiseTables;
import edu.ucla.wise.utils.SQLTemplateUtil;

/**
 * Class UserDBConnection -- a customized interface to encapsulate single-user
 * interface to data storage.
 */
public class UserDBConnection implements DataBankInterface {
	public User theUser = null;
	private final String surveyID;
	private final DataBank db;
	private final WiseTables wiseTables;
	private Connection conn = null;
	private static final Logger LOGGER = Logger.getLogger(UserDBConnection.class);

	private final ResultDataProvider resultDataProvider;
	private final UserDataStorer userDataStorer;

	/**
	 * Constructor for the class.
	 * 
	 * @param usr
	 *            User for which this object is associated to.
	 * @param dbk
	 *            Data bank for getting details to contact database.
	 */
	public UserDBConnection(User usr, DataBank dbk) {
		this.theUser = usr;
		this.db = dbk;
		this.wiseTables = this.db.getWiseTables();
		this.surveyID = usr.getCurrentSurvey().getId();

		this.resultDataProvider = new ResultDataProvider(this);
		this.userDataStorer = new UserDataStorer(this);
		try {

			/*
			 * open a database connection to hold for the user ultimately closed
			 * by finalize() below
			 */
			this.conn = this.db.getDBConnection();
		} catch (SQLException e) {
			LOGGER.error("User " + this.theUser.getId()
					+ " unable to make its DB connection. Err: " + e.toString(), null);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			this.conn.close();
		} catch (SQLException e) {
			LOGGER.error("Exception for user " + this.theUser.getId()
					+ " closing DB connection w/: " + e.toString(), null);
		} finally {
			super.finalize();
		}
	}

	/**
	 * Retrieves values for a list of fields from the invitee table
	 * 
	 * @param fieldNames
	 *            Array of field name whose values are to be retrieved from the
	 *            database.
	 * @return String[] Array of the values from the database.
	 */
	public String[] getInviteeAttrs(String[] fieldNames) {
		HashSet<String> nonEncodedFieldSet = new HashSet<String>();
		nonEncodedFieldSet.add("firstname");
		nonEncodedFieldSet.add("lastname");
		nonEncodedFieldSet.add("salutation");
		nonEncodedFieldSet.add("phone");
		nonEncodedFieldSet.add("irb_id");

		String userid = this.theUser.getId();
		String[] values = new String[fieldNames.length];
		if (fieldNames.length < 1) {
			return values;
		}
		String fieldString = "";
		for (int i = 0; i < (fieldNames.length - 1); i++) {
			fieldString += (!nonEncodedFieldSet.contains(fieldNames[i].toLowerCase())) ? "AES_DECRYPT("
					+ fieldNames[i].toLowerCase() + ",'" + this.db.getEmailEncryptionKey() + "')"
					: fieldNames[i];
			fieldString += ",";
		}
		// fieldString += fieldNames[fieldNames.length - 1];
		fieldString += (!nonEncodedFieldSet.contains(fieldNames[fieldNames.length - 1]
				.toLowerCase())) ? "AES_DECRYPT(" + fieldNames[fieldNames.length - 1].toLowerCase()
				+ ",'" + this.db.getEmailEncryptionKey() + "')" : fieldNames[fieldNames.length - 1];

		String sql = "SELECT " + fieldString + " FROM " + this.wiseTables.getInvitee()
				+ " WHERE id = " + userid;
		try {

			// TODO: Change to Prepared Statement.
			/* connect to the database */
			Statement stmt = this.conn.createStatement();

			/* get the status' value from survey data table */
			LOGGER.info("\n #####The SQl being executed for the extraction of invitee fields is :"
					+ sql);
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();

			/*
			 * update the current page by searching with the status' value (page
			 * ID)
			 */
			if (rs.next()) {
				for (int i = 0; i < fieldNames.length; i++) {
					values[i] = rs.getString(i + 1);
					values[i] = values[i].replaceAll("^\"|\"$", "");
				}
			}
			stmt.close();
		} catch (SQLException e) {
			LOGGER.error("DataBank - Invitee attr retrieval fail: " + e.toString(), null);
			return null; // signal failure to retrieve
		}
		return values;
	}

	public void recordPageStore() {
		/*
		 * note proper storage of "status" field relies on User object having
		 * advanced page before call;
		 */
		String nextPage = "null";
		if (this.theUser.getCurrentPage() != null) {

			/* null val means finished */
			nextPage = "'" + this.theUser.getCurrentPage().getId() + "'";
		}
		this.setInviteeStatus(nextPage);

	}

	public void storeMainData(String[] names, char[] valTypes, String[] vals) {
		this.userDataStorer.storeMainData(this.theUser.getCurrentSurvey().getId(),
				this.theUser.getId(), names, valTypes, vals);
		this.recordPageStore();
	}

	public String getInviteeStatus() {
		String status = null;
		try {

			String sql = "SELECT state FROM " + this.wiseTables.getSurveyUserState()
					+ " WHERE invitee = ?";
			PreparedStatement stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				status = rs.getString(1);
			}

			if (!status.equals("completed")) {

				sql = "SELECT status FROM " + this.wiseTables.getSurveyUserPageStatus()
						+ " WHERE invitee = ?";
				stmt = this.conn.prepareStatement(sql);
				stmt.setInt(1, Integer.parseInt(this.theUser.getId()));

				rs = stmt.executeQuery();
				if (rs.next()) {
					status = rs.getString(1);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("SQL error while getting invitee status for invitee '"
					+ this.theUser.getId() + "'");
		}
		return status;
	}

	public void setInviteeStatus(String pageId) {
		try {
			String sqlToInsertStatus = "INSERT INTO "
					+ this.wiseTables.getSurveyUserPageStatus()
					+ " (invitee, status) VALUES ( ?, ?) ON DUPLICATE KEY UPDATE status=VALUES(status)";
			PreparedStatement stmt = this.conn.prepareStatement(sqlToInsertStatus);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.setString(2, pageId);
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Could not set status for invitee '" + this.theUser.getId()
					+ "' and page '" + pageId + "'");
		}

	}

	/**
	 * sets up user's status entry in survey data table
	 * 
	 * @param pageID
	 *            Page ID whose status has to be updated to
	 */
	public void beginSurvey(String pageID) {

		PreparedStatement stmt3 = null;
		try {

			String inviteeStatus = this.getInviteeStatus();

			/*
			 * if the user doesn't exist, insert a new user record in to the
			 * data table and set the status value to be the ID of the 1st
			 * survey page - (starting from the beginning)
			 */
			if (Strings.isNullOrEmpty(inviteeStatus)) {
				this.setInviteeStatus(pageID);
			}

			/* update user state to be started (consented) */
			String sql = "update survey_user_state set state='started', state_count=1, entry_time=now() where invitee= ?"
					+ " AND survey= ?";
			stmt3 = this.conn.prepareStatement(sql);
			stmt3.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt3.setString(2, this.surveyID);
			stmt3.executeUpdate();

		} catch (SQLException e) {
			LOGGER.error("Databank SETUP STATUS:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("Databank SETUP STATUS:" + e.toString(), null);
		} finally {
			try {
				if (stmt3 != null) {
					stmt3.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns the current page the user is on from the database.
	 * 
	 * @return String The Page on which user is on. Returns null if none
	 */
	public String getCurrentPageName() {
		return this.getInviteeStatus();
	}

	public Map<String, String> getMainData(int questionLevel) {
		return this.resultDataProvider.getAnswersForInvitee(
				this.theUser.getCurrentSurvey().getId(), Integer.parseInt(this.theUser.getId()), 0);
	}

	public String getAllDataForRepeatingSet(String repeatingSetName) {
		return this.resultDataProvider.getAnswersInRepeatingSetForInvitee(this.theUser
				.getCurrentSurvey().getId(), Integer.parseInt(this.theUser.getId()),
				repeatingSetName);
	}

	public void insertRepeatSetInstance(String repeatSetName, String instanceName,
			Map<String, Answer> answers) {
		this.userDataStorer.insertRepeatSetInstance(this.theUser.getCurrentSurvey().getId(),
				this.theUser.getId(), repeatSetName, instanceName, answers);
	}

	/**
	 * Updates the database to record user's current page.
	 */
	public void recordCurrentPage() {
		this.setInviteeStatus(this.theUser.getCurrentPage().getId());
	}

	/**
	 * Updates the users current submitted page into the database.
	 */
	public void recordPageSubmit() {
		String sql = "INSERT INTO page_submit (invitee, survey, page) " + "VALUES (?,?,?)";
		PreparedStatement stmt = null;
		try {
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.setString(2, this.surveyID);
			stmt.setString(3, this.theUser.getCurrentPage().getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Record page submit error:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("Record page submit Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("Record page submit error:" + e.toString(), null);
				}
			}
		}
	}

	/**
	 * Generates a random string which is used while link generation for sending
	 * email to users. This link is used by the users to get access to the
	 * survey system.
	 * 
	 * @param messageID
	 *            The message name that is send to the user as email.
	 * @return String New random message Id that is used for link generation
	 *         while sending emails.
	 */
	public String recordMessageUse(String messageId) {
		String uid = this.theUser.getId();
		String randMessageId = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(22);
		String sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES (?,?,?,?)";
		PreparedStatement stmt = null;
		try {

			/* connect to database */
			stmt = this.conn.prepareStatement(sql);

			stmt.setString(1, randMessageId);
			stmt.setInt(2, Integer.parseInt(uid));
			stmt.setString(3, this.surveyID);
			stmt.setString(4, messageId);

			/* check if the user has already existed in the survey data table */
			stmt.executeUpdate();

			stmt.close();
		} catch (SQLException e) {
			LOGGER.error("Error recording new message using " + sql + ": " + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("Error recording new message Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("Error recording new message" + e.toString(), null);
				}
			}
		}
		return randMessageId;
	}

	/**
	 * Saves the user's new state into database.
	 * 
	 * @param newState
	 *            New state to which user is to be updated.
	 */
	public void setUserState(String newState) {
		String sql = "update survey_user_state set state = ?" + ", state_count=1 " + // reset
																						// to
																						// 1
																						// on
																						// entering
																						// new
																						// state
				"where invitee = ? AND survey = ?";
		PreparedStatement stmt = null;

		/* Assumes user/survey has a state */
		try {

			/* connect to database */
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, newState);
			stmt.setInt(2, Integer.parseInt(this.theUser.getId()));
			stmt.setString(3, this.surveyID);
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("UDB setUserState:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB setUserState Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("UDB setUserState:" + e.toString(), null);
				}
			}
		}
	}

	/**
	 * Returns users current state of the user in the survey weather it is
	 * completed/interrupted.
	 * 
	 * @return String Current state of the user.
	 */
	public String getUserState() {
		String sql = "SELECT state FROM survey_user_state " + "where invitee= ?" + " AND survey= ?";

		PreparedStatement stmt = null;

		/* Assumes user/survey has a state */
		String theState = null;
		try {

			/* connect to database */
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.setString(2, this.surveyID);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				theState = rs.getString(1);
			}
		} catch (SQLException e) {
			LOGGER.error("UDB getUserState:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB getUserState Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("UDB getUserState:" + e.toString(), null);
				}
			}
		}
		return theState;
	}

	/**
	 * Returns current message sequence of the user this is used to know which
	 * email has to be sent to the user.
	 * 
	 * @return String The user's current message sequence.
	 */
	public String getCurrentMessageSequence() {
		String sql = "SELECT message_sequence FROM survey_user_state "
				+ "where invitee= ? AND survey= ?";
		PreparedStatement stmt = null;

		/* Assumes user/survey has a sequence */
		String theSeq = null;
		try {

			/* connect to database */
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.setString(2, this.surveyID);

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				theSeq = rs.getString(1);
			}
		} catch (SQLException e) {
			LOGGER.error("UDB getCurrentMessageSequence:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB getCurrentMessageSequence Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("UDB getCurrentMessageSequence:" + e.toString(), null);
				}
			}
		}
		return theSeq;
	}

	/**
	 * Saves the user's consent answer - accept or decline
	 * 
	 * @param answer
	 *            accept or decline.
	 */
	public void setConsent(String answer) {
		String sql = "INSERT INTO consent_response (invitee, answer, survey) VALUES (?, ?, ?)";
		PreparedStatement stmt = null;
		try {

			/* connect to database */
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.setString(2, answer);
			stmt.setString(3, this.surveyID);
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("UDB setConsent:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB setConsent Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("UDB setConsent:" + e.toString(), null);
				}
			}
		}
	}

	/**
	 * Checks if user has consented - accepted the consent form.
	 * 
	 * @return boolean If user has accepted the consent or not.
	 */
	public boolean checkConsent() {
		boolean resultp = false;
		String sql = "SELECT * FROM consent_response WHERE invitee = ?"
				+ " AND survey= ? AND answer = 'Y'";
		PreparedStatement stmt = null;

		try {
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.setString(2, this.surveyID);

			/*
			 * if user accepted the consent form, the record can be found from
			 * the consent_response table
			 */
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				resultp = true;
			}
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("UDB checkConsent:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB checkConsent Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("UDB checkConsent:" + e.toString(), null);
				}
			}
		}
		return resultp;
	}

	/**
	 * Creates user's survey session and captures the ip address of user into
	 * the survey_user_session
	 * 
	 * @param browserUseragent
	 *            User browser's User Agent.
	 * @param ipAddress
	 *            Ip address of the user's machine from where he is accessing
	 *            the survey.
	 * @param surveyMsgId
	 *            Message Id.
	 * @return String Session ID.
	 */
	public String createSurveySession(String browserUseragent, String ipAddress, String surveyMsgId) {
		String sessionid = "";
		String sql1 = "INSERT INTO survey_user_session (from_message, endtime, starttime, browser_info, ip_address) "
				+ "VALUES (?, 0, now(), ?, ?)";
		String sql2 = "SELECT LAST_INSERT_ID()";
		PreparedStatement statement1 = null;
		PreparedStatement statement2 = null;
		try {

			/* connect to the database */
			statement1 = this.conn.prepareStatement(sql1);
			statement1.setString(1, surveyMsgId);
			statement1.setString(2, browserUseragent);
			statement1.setString(3, ipAddress);

			/*
			 * add a new session record and save the startime & user's browser
			 * info and ip address
			 */
			statement1.executeUpdate();

			/* get the new session id */
			statement2 = this.conn.prepareStatement(sql2);
			ResultSet rs = statement2.executeQuery();
			if (rs.next()) {
				sessionid = rs.getString(1);
			}
		} catch (SQLException e) {
			LOGGER.error("USER CREATE DB SESSION:" + e.toString(), null);
		} finally {
			if ((statement1 != null) || (statement2 != null)) {
				try {
					if (statement1 != null) {
						statement1.close();
					}
					if (statement2 != null) {
						statement2.close();
					}
				} catch (SQLException e) {
					LOGGER.error("USER CREATE DB SESSION:" + e.toString(), null);
				}
			}
		}
		return sessionid;
	}

	/**
	 * close the user's survey session by setting endtime to now().
	 */
	public void closeSurveySession() {
		String sql = "UPDATE survey_user_session SET endtime = now() WHERE id = ?";
		PreparedStatement stmt = null;
		try {
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt.executeUpdate();

		} catch (SQLException e) {
			LOGGER.error("USER CLOSE SURVEY SESSION :" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB setDone Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("USER CLOSE SURVEY SESSION :" + e.toString(), null);
				}
			}
		}
	}

	/**
	 * update the user's status to be done in the survey data table
	 */
	public void setDone() {

		/* set endtime for the current survey session */
		String sql1 = "UPDATE survey_user_session SET endtime = now() WHERE id = ?";

		/* set status = null, which means the user has completed the survey */
		this.setInviteeStatus(DBConstants.SURVEY_COMPLETED_STATUS);

		PreparedStatement stmt1 = null;
		try {
			stmt1 = this.conn.prepareStatement(sql1);
			stmt1.setInt(1, Integer.parseInt(this.theUser.getSession()));
			stmt1.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("UDB setDone:" + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("UDB setDone Invalid User ID" + e.toString(), e);
		} finally {
			if (stmt1 != null) {
				try {
					stmt1.close();
				} catch (SQLException e) {
					LOGGER.error("UDB setDone:" + e.toString(), null);
				}
			}
		}
	}

	/**
	 * Gets a hashtable of all the page IDs which the user has completed and
	 * currently working on
	 * 
	 * @return Hashtable Status of user with respect to each page.
	 */
	public Hashtable<String, String> getCompletedPages() {
		Hashtable<String, String> pages = new Hashtable<String, String>();
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		String sql1 = "select status from " + this.wiseTables.getSurveyUserPageStatus()
				+ " where invitee = ?";
		String sql2 = "select distinct page from page_submit where invitee = ?" + " and survey = ?";

		try {
			stmt1 = this.conn.prepareStatement(sql1);

			stmt1.setInt(1, Integer.parseInt(this.theUser.getId()));

			/* get the status' value from the survey data table */
			ResultSet rs = stmt1.executeQuery();

			/* add it into the hashtable */
			while (rs.next()) {
				pages.put(rs.getString(1), "Current");
			}

			// get the submitted page IDs from page submit table
			stmt2 = this.conn.prepareStatement(sql2);
			stmt2.setInt(1, Integer.parseInt(this.theUser.getId()));
			stmt2.setString(2, this.surveyID);

			rs = stmt2.executeQuery();

			/* input them into the hashtable */
			while (rs.next()) {
				pages.put(rs.getString(1), "Completed");
			}

		} catch (SQLException e) {
			LOGGER.error("USER DB getCompletedPages:" + e.toString(), e);
		} catch (NumberFormatException e) {
			LOGGER.error("getCompletedPages Invalid User ID" + e.toString(), e);
		} finally {
			if ((stmt1 != null) || (stmt2 != null)) {
				try {
					if (stmt1 != null) {
						stmt1.close();
					}
					if (stmt2 != null) {
						stmt2.close();
					}
				} catch (SQLException e) {
					LOGGER.error("getCompletedPages:" + e.toString(), null);
				}
			}
		}
		return pages;
	}

	/**
	 * Records the new invitee who has accessed the survey for the first time.
	 * 
	 * @param inviteeId
	 *            Id of the new invitee.
	 * @param surveyId
	 *            Survey that the user is accessing.
	 * @return boolean If the record is successful or not.
	 */
	public boolean recordWelcomeHit(String inviteeId, String surveyId) {

		/* insert a new accessment record */
		String sql = "INSERT INTO welcome_hits (invitee, survey) VALUES (?, ?)";

		PreparedStatement stmt = null;
		boolean resultp = false;
		try {
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(inviteeId));
			stmt.setString(2, this.surveyID);
			int temp = stmt.executeUpdate();
			if (temp > 0) {
				resultp = true;
			}

		} catch (NumberFormatException e) {
			LOGGER.error("USER RECORD WELCOME HIT:" + e.toString(), e);
		} catch (SQLException e) {
			LOGGER.error("USER RECORD WELCOME HIT:" + e.toString(), e);
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
		return resultp;
	}

	/**
	 * Records the details about a user who has declined taking the survey.
	 * 
	 * @param msgId
	 *            The Id in the URL which is used to access the system.
	 * @param studyId
	 *            Study space name which the user is declining.
	 * @param inviteeId
	 *            The Id of user.
	 * @param surveryId
	 *            Survey name which the user is declining.
	 * @return boolean If the operation of recording was successful or not.
	 */
	public boolean recordDeclineHit(String msgId, String studyId, String inviteeId, String surveryId) {

		boolean resultp = false;
		// Statement stmt = null;
		String sql1 = "INSERT INTO decline_hits (msg_id, survey) VALUES (?, ?)";
		String sql2 = "update survey_user_state set state='declined', state_count=1, "
				+ "entry_time=now() where invitee= ? AND survey= ?";
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		try {
			/* connect to the database */
			stmt1 = this.conn.prepareStatement(sql1);
			stmt2 = this.conn.prepareStatement(sql2);

			/* add a new decline hits record */
			stmt1.setString(1, msgId);
			stmt1.setString(2, studyId);
			int out1 = stmt1.executeUpdate();

			/* update the user state */
			stmt2.setInt(1, Integer.parseInt(inviteeId));
			stmt2.setString(2, surveryId);
			int out2 = stmt2.executeUpdate();
			if ((out1 > 0) && (out2 > 0)) {
				resultp = true;
			}
		} catch (NumberFormatException e) {
			LOGGER.error("USER RECORD DECLINE HIT:" + e.toString(), e);
		} catch (SQLException e) {
			LOGGER.error("USER RECORD DECLINE HIT:" + e.toString(), e);
		} finally {
			try {
				if (stmt1 != null) {
					stmt1.close();
				}
				if (stmt2 != null) {
					stmt2.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
		return resultp;
	}

	/**
	 * Records the reason for declining to taking the survey.
	 * 
	 * @param inviteeId
	 *            User who wants to decline the survey.
	 * @param reason
	 *            Reason for declining
	 * @return boolean If the reason has successfully updated or not.
	 */
	public boolean setDeclineReason(String inviteeId, String reason) {

		PreparedStatement stmt = null;
		String sql = "INSERT INTO decline_reason (invitee, reason) VALUES (?,?)";
		boolean retVal = false;
		try {

			/* connect to the database */
			stmt = this.conn.prepareStatement(sql);
			stmt.setInt(1, Integer.parseInt(inviteeId));
			stmt.setString(2, reason);

			/* save the user's decline reason */
			int out = stmt.executeUpdate();
			if (out > 0) {
				retVal = true;
			}
		} catch (NumberFormatException e) {
			LOGGER.error("USER SET DECLINE REASON:" + e.toString(), e);
		} catch (SQLException e) {
			LOGGER.error("USER SET DECLINE REASON:" + e.toString(), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
		return retVal;
	}

	/**
	 * Outputs number of users who have finished taking the survey as of now.
	 * 
	 * @param surveyId
	 *            Survey Name whose status has to be checked.
	 * @return int Number of people who have finished taking the survey.
	 */
	public int checkCompletionNumber(String surveyId) {
		int numCompleters = 0;
		String surveyDataTable = surveyId + "_data";
		String sql = "SELECT count(distinct invitee) FROM " + surveyDataTable
				+ "WHERE status IS NULL";
		PreparedStatement stmt = null;
		try {

			/* connect to the database */
			stmt = this.conn.prepareStatement(sql);

			/* count the completers */
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				numCompleters = rs.getInt("count(distinct invitee)");
			}
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("USER CHECK COMPLETION NUMBER:" + e.toString(), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
		return numCompleters;
	}

	/**
	 * Deletes repeating item sets.
	 * 
	 * @param inviteeId
	 *            Invitee whose repeating item has to be deleted.
	 * @param tableName
	 *            Repeating item table name from which the item has to be
	 *            deleted.
	 * @param instanceName
	 *            Row's instance name which has to be deleted.
	 * @return boolean If the delete was successful or not.
	 */
	public boolean deleteRowFromTable(String itemSetName, String instanceName) {
		return this.userDataStorer.deleteRowFromTable(this.theUser.getId(), itemSetName,
				instanceName, this.surveyID);
	}

	@Override
	public Connection getDBConnection() throws SQLException {
		return this.conn;
	}

	@Override
	public String getStudySpace() {
		return this.db.getStudySpace();
	}

	@Override
	public WiseTables getWiseTables() {
		return this.db.getWiseTables();
	}

	@Override
	public SQLTemplateUtil getSqlTemplateUtil() {
		return new SQLTemplateUtil(SurveyorApplication.getInstance().getSQLTemplateConfiguration());
	}
}
