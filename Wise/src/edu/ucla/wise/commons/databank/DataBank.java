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

import static edu.ucla.wise.commons.databank.DBConstants.MainTableExtension;
import static edu.ucla.wise.commons.databank.DBConstants.dbDriver;
import static edu.ucla.wise.commons.databank.DBConstants.mysqlServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.oreilly.servlet.MultipartRequest;

import edu.ucla.wise.admin.healthmon.HealthStatus;
import edu.ucla.wise.admin.view.SurveyInformation;
import edu.ucla.wise.client.interview.InterviewManager;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.ClosedQuestion;
import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.InviteeMetadata;
import edu.ucla.wise.commons.InviteeMetadata.Values;
import edu.ucla.wise.commons.Message;
import edu.ucla.wise.commons.MessageSender;
import edu.ucla.wise.commons.MessageSequence;
import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.QuestionBlock;
import edu.ucla.wise.commons.QuestionBlockforSubjectSet;
import edu.ucla.wise.commons.Reminder;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.User.INVITEE_FIELDS;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.email.EmailMessage;
import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.persistence.data.WiseTables;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;
import edu.ucla.wise.utils.SQLTemplateUtil;
import edu.ucla.wise.web.WebResponseMessage;
import edu.ucla.wise.web.WebResponseMessageType;

/**
 * This class encapsulates the database interface for a Study Space. The static
 * part represents the MySQL interface in general
 * 
 * Also provides group-level update of the valid survey_user_states: invited,
 * declined, start_reminder_x, non_responder, started, interrupted,
 * completion_reminder_x, incompleter TODO: (low) abstract valid User-state
 * progression into static final strings, either in Data_Bank or User class
 */
public class DataBank implements DataBankInterface {

	// TODO (med) add mechanism to use decimalPlaces and maxSize rather than
	// this default

	private static final Logger LOGGER = Logger.getLogger(DataBank.class);

	/** Instance Variables */
	private final StudySpace studySpace;
	private final WiseTables wiseTables;
	private final String dbdata;
	private final String dbuser;
	private final String dbpwd;
	private final String emailEncryptionKey;

	/**
	 * Sets the Database driver from the properties file.
	 * 
	 * @param props
	 *            Resource bundle object from which the details of the data base
	 *            driver are obtained.
	 */
	public static void SetupDB(WiseProperties properties) {
		mysqlServer = properties.getDatabaseServer();
		try {
			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
		} catch (SQLException e) {
			LOGGER.error("DataBank init Error: " + e, e);
		}
	}

	/**
	 * Constructor setting up data storage for a survey session. All the
	 * parameters are now obtained from database
	 * 
	 * @param ss
	 *            Study space to which the data bank class is linked to.
	 * 
	 */
	public DataBank(StudySpace ss, StudySpaceParameters params) {
		this.studySpace = ss;
		this.wiseTables = new WiseTables(ss.studyName);
		this.dbuser = params.getDatabaseUsername();
		this.dbdata = params.getDatabaseName();
		this.dbpwd = params.getDatabasePassword();
		this.emailEncryptionKey = params.getDatabaseEncryptionKey();
	}

	/**
	 * Gets list of current survey xml files and request loading of each by
	 * studySpace
	 */
	public void readSurveys() {
		/* Read all the surveys in the current database */
		String sql = "SELECT filename from " + this.wiseTables.getSurveys() + ", "
				+ "(SELECT max(internal_id) as maxint FROM surveys group by id) maxes "
				+ "WHERE maxes.maxint = surveys.internal_id";

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				String filename = rs.getString("filename");
				this.studySpace.loadSurvey(filename);
			}
		} catch (SQLException e) {
			LOGGER.error("DataBank survey file loading error:" + e.toString(), e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("DataBank survey file loading error:" + e.toString(), e);
			}
		}
	}

	/**
	 * Creates a User object form the message ID that is obtained from the URL
	 * whenever a user tries to use WISE system.
	 * 
	 * @param msgId
	 * @return
	 */
	public User makeUserFromMsgID(String msgId) {
		/* get the user's ID and the survey ID being responded to */
		String sql = "select invitee, survey from " + this.wiseTables.getSurveyMessageUse()
				+ " where messageId= ?";
		LOGGER.debug("SQL:" + sql);

		User theUser = null;
		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			String usrID, surveyID;
			Survey survey;
			stmt.setString(1, msgId);
			ResultSet rs = stmt.executeQuery();

			/* if message id not found, result set will be empty */
			if (rs.next()) {
				usrID = rs.getString("invitee");
				surveyID = rs.getString("survey");
				survey = this.getSurvey(surveyID);
				LOGGER.info("surveyId:'" + surveyID + "'");
				if ((usrID == null) || (survey == null)) {
					throw new Exception("Can't get user " + usrID + " or survey ID " + surveyID);
				}
				theUser = new User(usrID, survey, msgId, this);
			}
			rs.close();
		} catch (SQLException e) {
			LOGGER.error("Data_Bank user creation error:" + e.toString(), e);
		} catch (Exception e) {
			LOGGER.error("Data_Bank user creation error:" + e.toString(), e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("DataBank survey file loading error:" + e.toString(), e);
			}
		}
		return theUser;
	}

	/**
	 * Establishes data base connection and returns a Connection object for
	 * other functions.
	 * 
	 * @return Connection Database connection object.
	 * @throws SQLException
	 */
	@Override
	public Connection getDBConnection() throws SQLException {

		return DriverManager.getConnection(dbDriver + mysqlServer + "/" + this.getDbdata()
				+ "?user=" + this.getDbuser() + "&password=" + this.dbpwd + "&autoReconnect=true");
	}

	/**
	 * Searches by the survey ID and returns a specific survey.
	 * 
	 * @param surveyId
	 *            Survey Id which has to be returned.
	 * @return Survey Survey object associated with the surveyId.
	 */
	public Survey getSurvey(String sid) {
		return this.studySpace.getSurvey(sid);
	}

	/**
	 * Creates the tables for storing the responses for the survey questions.
	 * This function is called while loading the survey.
	 * 
	 * @param survey
	 *            Survey for which tables have to be created.
	 * @throws SQLException
	 */
	public void setupSurvey(Survey survey) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		Statement stmtM = null;

		String createSql = "";
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			stmtM = conn.createStatement();

			/*
			 * get the temporary survey record inserted by admin tool in the
			 * SURVEYS table
			 */
			String sql = "select internal_id, filename, title, uploaded, status " + "from "
					+ this.wiseTables.getSurveys()
					+ " where internal_id=(select max(internal_id) from "
					+ this.wiseTables.getSurveys() + " where id='" + survey.getId() + "')";
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();
			String internalId, filename, title, uploaded, status;

			if (rs.next()) {

				/* save the data of the newly inserted survey record */
				internalId = rs.getString("internal_id");
				filename = rs.getString("filename");
				title = rs.getString("title");
				uploaded = rs.getString("uploaded");
				status = rs.getString("status");

				/* delete the newly inserted survey record */
				String sqlM = "delete from surveys where internal_id=" + internalId;
				stmtM.execute(sqlM);

				/*
				 * add the new survey record back in the table of surveys, and
				 * save the new table creation syntax and set the archive date
				 * to be current - (it's the current survey, has not been
				 * archived yet)
				 */
				sqlM = "insert into " + this.wiseTables.getSurveys()
						+ "(internal_id, id, filename, title, uploaded, status, archive_date) "
						+ "values(" + internalId + ",'" + survey.getId() + "','" + filename
						+ "',\"" + title + "\",'" + uploaded + "','" + status + "','current')";
				stmtM.execute(sqlM);

				/*
				 * append the data from the old data table to the new created
				 * one if in production mode, status.equalsIgnoreCase("P") but
				 * taking that out of criteria for user trust
				 */
			} // end of if
		} catch (SQLException e) {
			LOGGER.error("SURVEY - CREATE TABLE: " + createSql, e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (stmtM != null) {
					stmtM.close();
				}
			} catch (SQLException e) {
				LOGGER.error("DataBank survey table creation error:" + e.toString(), e);
			}
		}
		return;
	}

	/**
	 * Updates the value of archive date in table of surveys
	 * 
	 * @param survey
	 *            Survey for which tables have to be updated.
	 */
	public void updateArchiveDate(Survey survey) {
		Connection conn = null;
		Statement stmt = null;
		Statement stmtM = null;
		Statement stmtN = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			stmtM = conn.createStatement();
			stmtN = conn.createStatement();

			/* get the internal id of the old survey record */
			String sql = "select max(internal_id) from " + "(select * from "
					+ this.wiseTables.getSurveys() + " where id='" + survey.getId()
					+ "' and internal_id <> " + "(select max(internal_id) from "
					+ this.wiseTables.getSurveys() + " where id='" + survey.getId()
					+ "')) as a group by a.id;";
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();

			/* get the uploaded date */
			if (rs.next()) {
				String sqlM = "select internal_id, uploaded from " + this.wiseTables.getSurveys()
						+ " where internal_id=" + rs.getString(1);
				stmtM.execute(sqlM);
				ResultSet rsM = stmtM.getResultSet();

				/*
				 * keep the value of uploaded date - (mysql tends to
				 * automatically update this value and set the archive date to
				 * be none - (no need to do the archive since data sets are
				 * identical)
				 */
				if (rsM.next()) {
					String sqlN = "update " + this.wiseTables.getSurveys() + " set uploaded='"
							+ rsM.getString(2) + "', archive_date='' where internal_id="
							+ rsM.getString(1);
					stmtN.execute(sqlN);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("SURVEY - UPDATE ARCHIVE DATE", e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (stmtM != null) {
					stmtM.close();
				}
				if (stmtN != null) {
					stmtN.close();
				}
			} catch (SQLException e) {
				LOGGER.error("DataBank survey table creation error", e);
			}
		}
		return;
	}

	/**
	 * Archives old survey tables.
	 * 
	 * @param tableName
	 *            Table that has to be archived.
	 * @return String The date when the table is archived.
	 */
	public String archiveTable(String tableName) {
		String archiveString = "";
		Connection connection = null;
		Statement statement = null;
		try {
			connection = this.getDBConnection();
			statement = connection.createStatement();

			boolean oldTableFound = false;
			String archiveDate = "";

			ResultSet resultSet = statement.executeQuery("show tables");

			while (resultSet.next()) {
				if (resultSet.getString(1).equalsIgnoreCase(tableName)) {
					oldTableFound = true;
					break;
				}
			}

			if (oldTableFound) {
				String sqlToCheckIfTableIsEmpty = "select * from " + tableName;
				Statement statementToCheckEmpty = connection.createStatement();
				ResultSet resultSetForTable = statementToCheckEmpty
						.executeQuery(sqlToCheckIfTableIsEmpty);

				if (!resultSetForTable.next()) {
					String sqlToDropTable = "DROP TABLE IF EXISTS " + tableName;
					statementToCheckEmpty.execute(sqlToDropTable);

					/* return empty archive date */
					archiveDate = "";

				} else {

					/*
					 * otherwise, archive the table by changing its name with
					 * the current timestamp get the current date
					 */
					java.util.Date today = new java.util.Date();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddhhmm");
					archiveDate = formatter.format(today);

					String sqlToAlterTable = "ALTER TABLE " + tableName + " RENAME " + tableName
							+ "_arch_" + archiveDate;
					statement.execute(sqlToAlterTable);
					archiveString = archiveDate;
				}

			}
		} catch (SQLException e) {
			LOGGER.error("Error while archiving survey", e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				LOGGER.error("DataBank survey table creation error:" + e.toString(), e);
			}
		}
		return archiveString;
	}

	/**
	 * Drop data tables including the survey data table & subject set data
	 * tables; update surveys table Remove a survey -- should only be called for
	 * Development mode surveys.
	 * 
	 * @param survey
	 *            Survey which has to be removed.
	 * @return String HTML string of all the dropped tables.
	 */
	public String deleteSurvey(Survey survey) {
		String useResult = "";
		Connection conn = null;
		Statement stmt = null;
		Statement stmtM = null;

		try {

			/* connect to the database */
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			stmtM = conn.createStatement();

			/* pick up all the related data tables */
			String sqlM = "show tables";
			stmtM.execute(sqlM);
			ResultSet rsM = stmtM.getResultSet();
			while (rsM.next()) {
				String tableName = rsM.getString(1);
				if ((tableName.indexOf(survey.getId() + "_") != -1)
						&& (tableName.indexOf(MainTableExtension) != -1)) {

					/* drop this table */
					String sql = "DROP TABLE IF EXISTS " + tableName;
					stmt.execute(sql);
				}
			}
			useResult = this.clearSurveyUseData(survey);
			sqlM = "Update " + this.wiseTables.getSurveys()
					+ " set status='R', uploaded=uploaded, archive_date='no_archive' "
					+ "WHERE id ='" + survey.getId() + "'";
			stmtM.execute(sqlM);
			return "<p align=center>Survey " + survey.getId()
					+ " successfully dropped & old survey files archived.</p>" + useResult;
		} catch (SQLException e) {
			LOGGER.error("SURVEY - DROP Table error: " + e.toString(), e);
			return "<p align=center>ERROR deleting survey " + survey.getId() + ".</p>" + useResult
					+ "Please discuss with the WISE Administrator.</p>";
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (stmtM != null) {
					stmtM.close();
				}
			} catch (SQLException e) {
				LOGGER.error("SURVEY - DROP Table error: " + e.toString(), e);
			}
		}
	}

	/**
	 * Deletes survey references - those related data tables while survey is in
	 * production mode.
	 * 
	 * @param survey
	 *            Survey whose table references are to be removed.
	 * @return String HTML output saying the survey has been closed.
	 */
	public String archiveProdSurvey(Survey survey) {
		Connection conn = null;
		Statement stmt = null;
		try {
			/*
			 * change the survey mode from P to C in table surveys C - survey
			 * closed
			 */
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			String sql = "update " + this.wiseTables.getSurveys()
					+ " set status='C', uploaded=uploaded, archive_date='"
					+ System.currentTimeMillis() + "' " + "WHERE id ='" + survey.getId() + "'";
			stmt.execute(sql);

			/* remove the interview records from table - interview_assignment */
			sql = "DELETE FROM " + this.wiseTables.getInterviewAssignment() + " WHERE survey = '"
					+ survey.getId() + "' and pending=-1";
			stmt.execute(sql);

			return "<p align=center>Survey "
					+ survey.getId()
					+ " successfully closed archived. Discuss with WISE database Admin if you need access to old data.</p>";
		} catch (Exception e) {
			LOGGER.error("Error - Closing PRODUCTION SURVEY: " + e.toString(), e);
			return "<p align=center>ERROR Closing survey " + survey.getId() + ".</p>"
					+ "Please discuss with the WISE Administrator.</p>";

		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Error - Closing PRODUCTION SURVEY: " + e.toString(), e);
			}
		}
	}

	/**
	 * Delete associated "use" data for the survey -- should only be enabled in
	 * Development mode.
	 * 
	 * @param survey
	 *            Survey whose all the associated table data is to be cleared.
	 * @return String HTML response of the status of clearing the table data.
	 */
	public String clearSurveyUseData(Survey survey) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			String sql = "DELETE FROM " + this.wiseTables.getSurveyMessageUse()
					+ " WHERE survey = '" + survey.getId() + "'";
			stmt.execute(sql);
			sql = "DELETE FROM " + this.wiseTables.getConsentResponse() + " WHERE survey = '"
					+ survey.getId() + "'";
			stmt.execute(sql);
			sql = "DELETE FROM " + this.wiseTables.getSurveyUserState() + " WHERE survey = '"
					+ survey.getId() + "'";
			stmt.execute(sql);
			sql = "DELETE FROM " + this.wiseTables.getPageSubmit() + " WHERE survey = '"
					+ survey.getId() + "'";
			stmt.execute(sql);
			sql = "DELETE FROM " + this.wiseTables.getInterviewAssignment() + " WHERE survey = \""
					+ survey.getId() + "\"";
			stmt.execute(sql);
			stmt.close();
			conn.close();
			return "<p align=center>Associated use data for survey "
					+ survey.getId()
					+ " successfully cleared "
					+ "(tables survey_user_state, survey_message_use, page_submit, consent_response & for interviews).</p>";
		} catch (SQLException e) {
			LOGGER.error(e.toString(), e);
			return "<p align=center>ERROR clearing Associated use data for survey "
					+ survey.getId() + " from " + "(one or more of the tables "
					+ "survey_user_state, survey_message_use, page_submit, consent_response).";
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}

			} catch (SQLException e) {
				LOGGER.error("ERROR clearing Associated use data for survey" + e.toString(), e);
			}
		}
	}

	/**
	 * ITERATE over all study spaces and send email invitations to any subject
	 * due for a reminder or pending for initial invite.
	 * 
	 * @return String Outputs when the process of sending emails end.
	 */
	public String sendReminders() {
		MessageSequence msgSeq = null;
		String selectSql = "", outputStr = "";
		String surveyId;
		String msID;
		Connection conn = null;
		try {

			/*
			 * connect to the database Move users in the "started" state more
			 * than 6 hrs to be "interrupted", regardless of survey
			 */
			conn = this.getDBConnection();
			selectSql = "UPDATE " + this.wiseTables.getSurveyUserState()
					+ " SET state='interrupted', state_count=1, entry_time=entry_time "
					+ "WHERE state='started' AND entry_time <= date_sub(now(), interval 6 hour)";
			Statement outrStmt = conn.createStatement();
			outrStmt.executeUpdate(selectSql);
			outrStmt.close();

			// TODO: pending table needs separate handling -- doesn't belong in
			// loop below but how to get survey id otherwise

			/*
			 * Send initial invitations to users due for one according to the
			 * pending table
			 */
			outputStr += ("\nChecking for pending initial invitations");

			/*
			 * also Survey was not previously included in WHERE. Check: Could
			 * this obviate need for separate function??
			 */

			/* run the sql query to update the user group's states */
			this.invitePendingUsers();

			/*
			 * send initial invitation & reminders by going through each survey
			 * - message_seq pair currently in use
			 */
			String sql1 = "SELECT distinct survey, message_sequence FROM "
					+ this.wiseTables.getSurveyUserState() + " order by survey";
			Statement svyStmt = conn.createStatement();
			svyStmt.execute(sql1);
			ResultSet rsSurvey = svyStmt.getResultSet();
			while (rsSurvey.next()) {
				surveyId = rsSurvey.getString("survey");
				msID = rsSurvey.getString("message_sequence");
				msgSeq = this.studySpace.get_preface().getMessageSequence(msID);
				if (msgSeq == null) {
					continue;
				}

				outputStr += "\n\nStart checks for surveyId=" + surveyId + ", message sequence id="
						+ msID;

				/* 1. send the start reminders */
				outputStr += this.advanceReminders("start", msgSeq, surveyId, conn);

				/* 2. send the completion reminders */
				outputStr += this.advanceReminders("completion", msgSeq, surveyId, conn);
			}// end of while
			svyStmt.close();
			outputStr += ("\nEnd emailing at " + Calendar.getInstance().getTime().toString() + "\n");
		} catch (SQLException e) {
			outputStr += ("\nReminder generation ERROR! w/ select sql (" + selectSql + "): " + e
					.toString());
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		return outputStr;
	}

	/**
	 * Finds all the invitees who are due to get a reminder either start or
	 * completion and sends them the message.
	 * 
	 * @param reminderType
	 *            Weather it is a start or completion reminder
	 * @param msgSeq
	 *            Message sequence form which reminders are obtained and sent to
	 *            the users.
	 * @param surveyId
	 *            Survey whose emails are to be sent.
	 * @param conn
	 *            Data base connection.
	 * @return String Output string.
	 */
	private String advanceReminders(String reminderType, MessageSequence msgSeq, String surveyId,
			Connection conn) {
		Reminder reminderMessage;
		int remCount;
		String selectSql = "", updateSql = "", outputStr = "", entryState, lastState;
		MessageSender sender = new MessageSender(msgSeq); // sets
		// up
		// properly-authenticated
		// mail session
		if (reminderType.equals("start")) {
			reminderMessage = msgSeq.getStartReminder(0);
			entryState = "invited";
			lastState = "non_responder";
			remCount = msgSeq.totalStartReminders();
		} else {
			reminderMessage = msgSeq.getCompletionReminder(0);
			entryState = "interrupted";
			lastState = "incompleter";
			remCount = msgSeq.totalCompletionReminders();
		}
		if (reminderMessage == null) {
			return "No " + reminderType + " reminders\n";
		}
		int maxCount = 1; // max in 1st entry state is 1
		int entryTrigDays = reminderMessage.triggerDays;

		for (int i = 0; i < remCount; i++) {
			int n = i + 1;

			/*
			 * i represents 0-based index for current reminder; n represents the
			 * number that administrators see
			 */
			outputStr += "\nChecking for those needing a new " + reminderType + "_reminder " + n
					+ " from entry state " + entryState;
			selectSql = "SELECT id, AES_DECRYPT(email,\"" + this.getEmailEncryptionKey()
					+ "\") as email, salutation, firstname, lastname " + "FROM "
					+ this.wiseTables.getInvitee() + ", " + this.wiseTables.getSurveyUserState()
					+ " WHERE survey='" + surveyId + "' AND state='" + entryState + "' "
					+ " AND entry_time <= date_sub(now(), interval " + entryTrigDays + " day) "
					+ " AND state_count >= " + maxCount + " AND id=invitee AND message_sequence='"
					+ msgSeq.id + "'";
			updateSql = "UPDATE " + this.wiseTables.getSurveyUserState() + " SET state='"
					+ reminderType + "_reminder_" + n + "', state_count=1 WHERE survey='"
					+ surveyId + "' AND invitee=";
			outputStr += this.sendReminders(surveyId, sender, reminderMessage, selectSql,
					updateSql, conn);

			outputStr += ("\nChecking for those needing another " + reminderType + " reminder " + n);

			/* Select users NOT at max */
			selectSql = "SELECT id, AES_DECRYPT(email,\"" + this.getEmailEncryptionKey()
					+ "\") as email, salutation, firstname, lastname " + "FROM "
					+ this.wiseTables.getInvitee() + ", " + this.wiseTables.getSurveyUserState()
					+ " WHERE state='" + reminderType + "_reminder_" + n + "' AND survey='"
					+ surveyId + "'" + " AND entry_time <= date_sub(now(), interval "
					+ reminderMessage.triggerDays + " day)" + " AND state_count < "
					+ reminderMessage.maxCount + " AND id=invitee AND message_sequence='"
					+ msgSeq.id + "'";
			updateSql = "UPDATE " + this.wiseTables.getSurveyUserState()
					+ " SET state_count=state_count+1 " + "WHERE survey='" + surveyId
					+ "' AND invitee=";
			outputStr += this.sendReminders(surveyId, sender, reminderMessage, selectSql,
					updateSql, conn);
			entryState = reminderType + "_reminder_" + n;
			entryTrigDays = reminderMessage.triggerDays;
			if (n < remCount) // need to keep last for final tag-out, below
			{
				if (reminderType.equals("start")) {
					reminderMessage = msgSeq.getStartReminder(n);
				} else {
					reminderMessage = msgSeq.getCompletionReminder(n);
				}
			}
		}

		/* Move users at max of last reminder to to final state */
		selectSql = "UPDATE survey_user_state SET state='" + lastState + "', state_count=1 "
				+ "WHERE state='" + reminderType
				+ "_reminder_"
				+ remCount
				+ "' " // same as entryState
				+ "AND state_count = " + reminderMessage.maxCount + " "
				+ "AND entry_time <= date_sub(now(), interval " + reminderMessage.triggerDays
				+ " day) " + "AND survey='" + surveyId + "' AND message_sequence='" + msgSeq.id
				+ "'";

		/* (No message to send; run UPDATE on all at once) */
		try {
			Statement statement = conn.createStatement();
			statement.execute(selectSql);
			statement.close();
		} catch (SQLException e) {
			outputStr += ("\nadvanceReminder ERROR! w/ select sql (" + selectSql + "): " + e
					.toString());
		}
		return outputStr;
	}

	/**
	 * This function will read and update the pending table, sending messages
	 * that are due.
	 * 
	 * @return String Output string.
	 */
	private String invitePendingUsers() {

		String sql = "", outputStr = "";
		String selectSql = "SELECT id, AES_DECRYPT(email,'"
				+ this.studySpace.db.getEmailEncryptionKey()
				+ "') as email, salutation, firstname, lastname, survey, message_sequence FROM "
				+ this.wiseTables.getInvitee()
				+ ", "
				+ this.wiseTables.getPending()
				+ " "
				+ "WHERE DATE(send_time) <= DATE(now()) AND pending.completed = 'N' AND invitee.id = pending.invitee";
		Statement statement = null;
		Connection conn = null;

		try {
			conn = this.getDBConnection();
			statement = conn.createStatement();
			statement.execute(selectSql);
			ResultSet rs = statement.getResultSet();
			while (rs.next()) {
				String inviteeId = rs.getString("id");
				String email = rs.getString("email");
				String salutation = rs.getString("salutation");
				String lastname = rs.getString("lastname");
				String surveyId = rs.getString("survey");
				String msID = rs.getString("message_sequence");
				MessageSequence msgSeq = this.studySpace.get_preface().getMessageSequence(msID);
				MessageSender messageSender = new MessageSender(msgSeq);
				Message invMsg = msgSeq.getTypeMessage("invite");
				if (invMsg == null) {
					LOGGER.error("Failed to get the initial invitation", null);
					return "Failed";
				}

				outputStr += ("Sending invitation to invitee = " + inviteeId);
				Statement statement2 = conn.createStatement();
				String messageId = org.apache.commons.lang3.RandomStringUtils
						.randomAlphanumeric(22);

				sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES ('"
						+ messageId
						+ "',"
						+ inviteeId
						+ ",'"
						+ surveyId
						+ "', '"
						+ invMsg.id
						+ "')";
				statement2.execute(sql);
				String msgIndex = "";
				if (invMsg.hasLink) {
					msgIndex = messageId;
				}

				EmailMessage emailMessage = new EmailMessage(email, salutation, lastname);

				String emailResponse = messageSender.sendMessage(invMsg, msgIndex, emailMessage,
						this.studySpace.id, this, inviteeId, WISEApplication.getInstance()
								.getWiseProperties());
				if (emailResponse.equalsIgnoreCase("")) {
					outputStr += (" --> Email Sent");
					// TODO: I have Fixed insertion of message_sequence in the
					// reminder code, because it was not inserting any
					// message_sequence in the survey_user_state table.
					String updateSql = "INSERT INTO survey_user_state(invitee, survey, message_sequence, state, state_count) "
							+ "values ("
							+ inviteeId
							+ ", '"
							+ surveyId
							+ "', '"
							+ msID
							+ "','invited', 1) ON DUPLICATE KEY UPDATE state='invited', state_count=1";
					statement2.execute(updateSql);
					// update the pending table
					String sql3 = "update pending set completed='Y', completed_time = now() where invitee="
							+ inviteeId
							+ " and survey ='"
							+ surveyId
							+ "' and message_sequence ='"
							+ msID + "'";
					statement2.execute(sql3);
				} else {
					outputStr += (" --> ERROR SENDING EMAIL (" + emailResponse + ")");
					LOGGER.error("Error sending invitation email to invitee = " + inviteeId, null);
				}// if
			}// while
		} catch (SQLException e) {
			LOGGER.error("Pending initial invite ERROR:", e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return outputStr;
	}

	/**
	 * Finds all the invitees who are due to get a reminder either start or
	 * completion and sends them the message.
	 * 
	 * @param surveyId
	 *            Survey whose emails are to be sent.
	 * @param messageSender
	 *            Message sender object used for sending Emails.
	 * @param r
	 *            Message object which contains actual email to send.
	 * @param selQry
	 *            Query to select to the people who needs email to be sent.
	 * @param updQry
	 *            Query to update the information of the email sent into the
	 *            table
	 * @param conn
	 *            Data base connection.
	 * @return String Output string.
	 */
	private String sendReminders(String surveyId, MessageSender messageSender, Message r,
			String selQry, String updQry, Connection conn) {
		String sql = "", outputStr = "";
		try {
			Statement statement = conn.createStatement();
			statement.execute(selQry);
			ResultSet rs = statement.getResultSet();
			while (rs.next()) {
				String iid = rs.getString("id");
				String email = rs.getString("email");
				String salutation = rs.getString("salutation");
				String lastname = rs.getString("lastname");
				outputStr += ("\nSending reminder invitation to invitee = " + iid);
				Statement statement2 = conn.createStatement();
				String messageId = org.apache.commons.lang3.RandomStringUtils
						.randomAlphanumeric(22);
				sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES ('"
						+ messageId + "'," + iid + ",'" + surveyId + "', '" + r.id + "')";
				statement2.execute(sql);
				String msgIndex = "";
				if (r.hasLink) {
					msgIndex = messageId;
				}
				/*
				 * args: send_message(Message msg, String from_str, String
				 * message_useID, String toEmail, String salutation, String
				 * lastname, String ssid)
				 */
				EmailMessage emailMessage = new EmailMessage(email, salutation, lastname);
				String emailResponse = messageSender.sendMessage(r, msgIndex, emailMessage,
						this.studySpace.id, this, iid, WISEApplication.getInstance()
								.getWiseProperties());
				if (emailResponse.equalsIgnoreCase("")) {
					outputStr += (" --> Email Sent");
					statement2.execute(updQry + iid);
				} else {
					outputStr += (" --> ERROR SENDING EMAIL (" + emailResponse + ")");
					LOGGER.error("Error sending invitation email to invitee = " + iid, null);
				}
			}// while
		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.error("Reminder sending ERROR: " + e.toString(), null);
		}
		return outputStr;
	}

	/**
	 * Gets information about a question from the survey data table.
	 * 
	 * @param surveyId
	 *            Survey Id for which results are to be obtained.
	 * @param pgName
	 *            Page name which contains this question.
	 * @param itemName
	 *            Question name that needs results.
	 * @param whereclause
	 *            Selection of the invitees.
	 * @return Hashtable Results of the item.
	 */
	public Hashtable<String, Integer> getDataForItem(String surveyId, String pgName,
			String itemName, String whereclause) {
		Hashtable<String, Integer> h1 = new Hashtable<String, Integer>();

		/* connect to the database */
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			// count the total number of invitees for each distinct answer;
			// join to page_submit prevents counting nulls from people who never
			// submitted the item
			String sql = "select " + itemName + ", count(distinct s.invitee) from " + surveyId
					+ MainTableExtension + " as s, page_submit as p where "
					+ "p.invitee=s.invitee and p.survey='" + surveyId + "'" + " and p.page='"
					+ pgName + "'";
			if (!whereclause.equals("")) {
				sql += " and s." + whereclause;
			}
			sql += " group by " + itemName;

			// New query to fix bug

			String sqlInteger = "SELECT answer, sum(counter) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
					+ this.wiseTables.getMainDataInteger()
					+ " INNER JOIN "
					+ this.wiseTables.getPageSubmit()
					+ " ON "
					+ this.wiseTables.getMainDataInteger()
					+ ".inviteeId = "
					+ this.wiseTables.getPageSubmit()
					+ ".invitee and "
					+ this.wiseTables.getMainDataInteger()
					+ ".survey="
					+ this.wiseTables.getPageSubmit()
					+ ".survey where "
					+ this.wiseTables.getPageSubmit()
					+ ".page = '"
					+ pgName
					+ "' and "
					+ this.wiseTables.getMainDataInteger()
					+ ".questionId = '"
					+ itemName
					+ "' and "
					+ this.wiseTables.getMainDataInteger()
					+ ".survey = '"
					+ surveyId
					+ "'";

			if (!whereclause.equals("")) {
				sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
			}
			sqlInteger += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

			String sqlText = "SELECT answer, sum(counter) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
					+ this.wiseTables.getMainDataText()
					+ " INNER JOIN "
					+ this.wiseTables.getPageSubmit()
					+ " ON "
					+ this.wiseTables.getMainDataText()
					+ ".inviteeId = "
					+ this.wiseTables.getPageSubmit()
					+ ".invitee and "
					+ this.wiseTables.getMainDataText()
					+ ".survey="
					+ this.wiseTables.getPageSubmit()
					+ ".survey where "
					+ this.wiseTables.getPageSubmit()
					+ ".page = '"
					+ pgName
					+ "' and "
					+ this.wiseTables.getMainDataText()
					+ ".questionId = '"
					+ itemName
					+ "' and "
					+ this.wiseTables.getMainDataText() + ".survey = '" + surveyId + "'";

			if (!whereclause.equals("")) {
				sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
			}
			sqlText += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

			PreparedStatement stmtForInteger = conn.prepareStatement(sqlInteger);
			ResultSet rs = stmtForInteger.executeQuery();

			while (rs.next()) {

				/* if the answer is null */
				if (rs.getString(1) == null) {
					h1.put("null", new Integer(rs.getInt(2)));
				} else {
					h1.put(rs.getString(1), new Integer(rs.getInt(2)));
				}
			}
			rs.close();

			PreparedStatement stmtForText = conn.prepareStatement(sqlText);
			rs = stmtForText.executeQuery();

			while (rs.next()) {

				/* if the answer is null */
				if (rs.getString(1) == null) {
					h1.put("null", new Integer(rs.getInt(2)));
				} else {
					h1.put(rs.getString(1), new Integer(rs.getInt(2)));
				}
			}

			rs.close();
			stmt.close();
			conn.close();
		} catch (Exception e) {
			LOGGER.error("WISE - CLOSED QUESTION RENDER RESULTS EXCLUSIVE: " + e.toString(), e);
		}
		return h1;
	}

	/**
	 * Returns the maximum and minimum for an item in the survey data.
	 * 
	 * @param page
	 *            Page name which contains this item.
	 * @param itemName
	 *            Question whose max and min values are asked for.
	 * @param whereclause
	 *            The invitees from whom these values are to be selected.
	 * @return HashMap Result
	 */
	public HashMap<String, Float> getMinMaxForItem(Page page, String itemName, String whereclause) {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = null;
		HashMap<String, Float> retMap = new HashMap<String, Float>();
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			sql = "select " + "min(" + itemName + "), max(" + itemName + ") from "
					+ page.getSurvey().getId()
					+ "_data as s, page_submit as p where s.invitee=p.invitee and p.survey='"
					+ page.getSurvey().getId() + "' and p.page='" + page.getId() + "'";
			if (!whereclause.equalsIgnoreCase("")) {
				sql += " and s." + whereclause;
			}

			stmt.execute(sql);
			rs = stmt.getResultSet();
			if (rs.next()) {
				retMap.put("min", rs.getFloat(1));
				retMap.put("max", rs.getFloat(2));
			}
		} catch (SQLException ex) {
			LOGGER.error("SQL Query Error", ex);
			return retMap;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
			try {
				rs.close();
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
		return retMap;
	}

	/**
	 * Generates a histogram of a particular question for all the response set
	 * for that Item.
	 * 
	 * @param page
	 *            Page name which contains this item.
	 * @param itemName
	 *            Question whose max and min values are asked for.
	 * @param scaleStart
	 *            Start scale of histogram.
	 * @param binWidthFinal
	 *            Width of the histogram.
	 * @param whereclause
	 *            The invitees from whom these values are to be selected.
	 * @return HashMap Result of the histogram
	 */
	public HashMap<String, String> getHistogramForItem(Page page, String itemName,
			float scaleStart, float binWidthFinal, String whereclause) {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		String sql = null;
		HashMap<String, String> retMap = new HashMap<String, String>();
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();

			/* get bins on that question from database */
			sql = "select floor((" + itemName + "-" + scaleStart + ")/" + binWidthFinal
					+ "), count(*) from " + page.getSurvey().getId()
					+ "_data as s, page_submit as p where s.invitee=p.invitee and p.survey='"
					+ page.getSurvey().getId() + "' and p.page='" + page.getId() + "'";
			if (!whereclause.equalsIgnoreCase("")) {
				sql += " and s." + whereclause;
			}
			sql += " group by floor((" + itemName + "-" + scaleStart + ")/" + binWidthFinal + ")";
			stmt.execute(sql);
			rs = stmt.getResultSet();
			String name, count;
			while (rs.next()) {
				name = rs.getString(1);
				if (name == null) {
					name = "null";
				}
				count = rs.getString(2);
				if (count == null) {
					count = "null";
					retMap.put("unanswered", String.valueOf(1));
				}
				retMap.put(name, count);
			}

		} catch (SQLException ex) {
			LOGGER.error("SQL Query Error", ex);
			return retMap;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
			try {
				rs.close();
			} catch (SQLException e) {
				LOGGER.error(e);
			}
		}
		return retMap;
	}

	/**
	 * Updates the status of the survey system into data base.
	 * 
	 * @param surveyName
	 *            Name of the survey whose status is updated.
	 */
	public void updateSurveyHealthStatus(String surveyName) {
		long currentTimeMillis = System.currentTimeMillis();
		StringBuffer query = new StringBuffer(
				"insert into survey_health (survey_name, last_update_time) values ('");
		query.append(surveyName).append("',").append(currentTimeMillis).append(")");
		query.append(" on duplicate key update last_update_time=").append(currentTimeMillis)
				.append(";");
		Connection conn = null;
		Statement stmt = null;

		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			stmt.executeUpdate(query.toString());
		} catch (SQLException e) {
			LOGGER.error("Could not update survey_health table", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.equals(e);
				}
			}
		}

	}

	/**
	 * Returns the time when the status of the survey system has been updated in
	 * the database in milli seconds.
	 * 
	 * @param studyName
	 *            Name of the whose value is needed.
	 * @return long Time in milli seconds.
	 */
	public long lastSurveyHealthUpdateTime(String studyName) {

		long lastUpdateTime = 0;

		Connection conn = null;
		PreparedStatement stmt = null;
		String sqlQuery = "select * from survey_health where survey_name= ? ";
		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sqlQuery);
			stmt.setString(1, studyName);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				lastUpdateTime = rs.getLong(2);
			}

		} catch (SQLException e) {
			LOGGER.error("Could not update survey_health table", e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error(e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.equals(e);
				}
			}
		}
		return lastUpdateTime;

	}

	/**
	 * Returns the HTML format of the fields in invitees. This is used by admin
	 * page to add new invitee information
	 * 
	 * @return String HTML format of the invitee fields.
	 */
	public String displayAddInvitee() {
		StringBuffer strBuff = new StringBuffer();
		Connection conn = null;
		Statement stmt = null;
		try {

			/* connect to the database */
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			stmt.execute("describe invitee");
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {

				/*
				 * read col name from database, matching col value from input
				 * request
				 */
				String columnName = rs.getString("Field");
				if (columnName.equalsIgnoreCase("id")) {
					continue;
				}
				strBuff.append("<tr><td width=400 align=left>").append(columnName);

				/* check for required field values */
				if (columnName.equalsIgnoreCase("lastname")) {
					strBuff.append(" (required)");
				}
				strBuff.append(": <input type='text' name='").append(columnName).append("' ");
				if (columnName.equalsIgnoreCase("salutation")) {
					strBuff.append("maxlength=5 size=5 ");
				} else {
					strBuff.append("maxlength=64 size=40 ");
				}
				strBuff.append("></td></tr>");
			}

			/* display the submit button */
			strBuff.append("<tr><td align=center>")
					.append("<input type='image' alt='submit' src='admin_images/submit.gif' border=0>")
					.append("</td></tr>");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
		return strBuff.toString();
	}

	/**
	 * Returns the HTML format of the fields in invitees for a particular
	 * survey. This is used by admin page to add new invitee information
	 * 
	 * @param surveyId
	 *            Name of the survey whose invitee fields are to be displayed.
	 * @return String HTML format of the invitee fields.
	 */
	public String displayAddInvitee(String surveyId) {

		Survey survey = this.studySpace.getSurvey(surveyId);
		Map<String, Values> inviteeMap = new HashMap<String, Values>(survey.getInviteeMetadata()
				.getFieldMap());
		StringBuffer strBuff = new StringBuffer();

		for (INVITEE_FIELDS field : INVITEE_FIELDS.values()) {
			if (!field.isShouldDisplay()) {
				continue;
			}
			strBuff.append(this.getInviteeEntry(field.name(), inviteeMap.get(field.name())));
			inviteeMap.remove(field.name());
		}

		for (Map.Entry<String, Values> map : inviteeMap.entrySet()) {
			strBuff.append(this.getInviteeEntry(map.getKey(), map.getValue()));
		}

		/* display the submit button */
		strBuff.append("<tr><td align=center>")
				.append("<input type='image' alt='submit' src='admin_images/submit.gif' onmousedown='submit_inv();return true;' border=0>")
				.append("</td></tr>");
		return strBuff.toString();
	}

	/**
	 * Helper function to display invitee fields of a particular study space.
	 * 
	 * @param columnName
	 *            Column name which has to be displayed.
	 * @param value
	 *            Value in case the field is a drop down.
	 * @return String HTML format of the string.
	 */
	private String getInviteeEntry(String columnName, Values value) {
		StringBuffer strBuff = new StringBuffer();
		strBuff.append("<tr><td width=450 align=left>").append(value.label);

		/* not a drop down */
		if (value.values.size() == 0) {
			strBuff.append(": <input type='text' name='").append(columnName).append("' ");
			if (columnName.equalsIgnoreCase(INVITEE_FIELDS.salutation.name())) {
				strBuff.append("maxlength=5 size=5 ");
			} else {
				strBuff.append("maxlength=64 size=40 ");
			}
			strBuff.append("></td></tr>");
		} else {
			strBuff.append(": <select name='").append(columnName).append("'>");
			for (Map.Entry<String, String> valueNode : value.values.entrySet()) {
				strBuff.append("<option value='").append(valueNode.getKey()).append("'>");
				strBuff.append(valueNode.getValue() == null ? valueNode.getKey() : valueNode
						.getValue());
				strBuff.append("</option>");
			}
			strBuff.append("</select>");
		}
		return strBuff.toString();
	}

	/**
	 * Adds a new invitee and then displays the page.
	 * 
	 * @param requestParameters
	 *            parameters need for adding a new invitee into the tables.
	 * @return String HTML format of the further Page.
	 */
	public String addInviteeAndDisplayPage(Map<String, String> requestParameters) {
		return this.handleAddInviteeAndDisplayPage(requestParameters);
	}

	private String handleAddInviteeAndDisplayPage(Map<String, String> requestParameters) {
		String errStr = "", resStr = "";
		/* connect to the database */
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			String sql, sqlIns = "insert into invitee(", sqlVal = "values(";

			/* get the column names of the table of invitee */
			stmt.execute("describe invitee");
			ResultSet rs = stmt.getResultSet();

			boolean submit = (requestParameters.get("submit") != null);
			while (rs.next()) {

				/*
				 * read col name from database, matching col value from input
				 * request
				 */
				String columnName = rs.getString("Field");
				if (columnName.equalsIgnoreCase("id")) {
					continue;
				}
				String columnVal = requestParameters.get(columnName);
				if ((columnVal != null) && !columnVal.equalsIgnoreCase("null")
						&& (columnVal.indexOf("\'") != -1)) {
					columnVal = columnVal.replace("'", "\\'");
				}

				String columnType = rs.getString("Type");
				resStr += "<tr><td width=400 align=left>" + columnName;

				/* check for required field values */
				if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.lastname.name())
						|| (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.email.name()))) {
					resStr += " (required)";
					if (submit && Strings.isNullOrEmpty(columnVal)) {
						errStr += "<b>" + columnName + "</b> ";
					}
				}
				resStr += ": <input type='text' name='" + columnName + "' ";
				if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.salutation.name())) {
					resStr += "maxlength=5 size=5 ";
				} else {
					resStr += "maxlength=64 size=40 ";
				}
				if (submit) {

					/*
					 * tempValue=column_val; if(tempValue.indexOf("\'")!=-1){
					 * tempValue=tempValue.replace("'", "&#x27"); }
					 */

					resStr += "value='" + columnVal + "'"; // add submitted
					sqlIns += columnName + ",";

					if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.email.name())) {
						if (Strings.isNullOrEmpty(columnVal) || columnVal.equalsIgnoreCase("null")) {
							columnVal = WISEApplication.getInstance().getWiseProperties()
									.getAlertEmail();
						}
						sqlVal += "AES_ENCRYPT('" + columnVal + "','"
								+ this.getEmailEncryptionKey() + "'),";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.irb_id.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.salutation.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "Mr." : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.firstname.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.lastname.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.phone.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "0" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase("subj_type")) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "1" : columnVal)
								+ "\",";
					} else {
						/* code added by Vijay */
						String temp = "";
						if (columnType.toLowerCase().contains("int")) {
							temp = "\"" + (Strings.isNullOrEmpty(columnVal) ? "0" : columnVal)
									+ "\"";
						} else if (columnType.toLowerCase().contains("date")) {
							temp = "\""
									+ (Strings.isNullOrEmpty(columnVal) ? "2012-12-31" : columnVal)
									+ "\"";
						} else {
							temp = "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
									+ "\"";
						}
						sqlVal += "AES_ENCRYPT('" + temp + "','" + this.getEmailEncryptionKey()
								+ "'),";
						;

					}
				}

				resStr += "></td></tr>";
			}

			/*
			 * run the insertion if all the required fields have been filled in
			 * with values
			 */
			if (!errStr.equals("")) {
				resStr += "<tr><td align=center>Required fields " + errStr
						+ " not filled out </td></tr>";
			} else if (submit) {
				sql = sqlIns.substring(0, sqlIns.length() - 1) + ") "
						+ sqlVal.substring(0, sqlVal.length() - 1) + ")";
				LOGGER.info("The sql trying to execute is " + sql);
				stmt.execute(sql);
				resStr += "<tr><td align=center>New invitee " + requestParameters.get("last_name")
						+ " has been added</td></tr>";
			}

			/* display the submit button */
			resStr += "<tr><td align=center>" + "<input type='hidden' name='submit' value='true' >"
					+ "<input type='image' alt='submit' src='admin_images/submit.gif' border=0>"
					+ "</td></tr>";
		} catch (RuntimeException e) {
			LOGGER.error("Runtime exception while loading invitee", e);
		} catch (SQLException e) {
			LOGGER.error("WISE ADMIN - LOAD INVITEE: " + e.toString(), e);
			// Security feature fix Exception should not be displayed
			// resStr += "<p>Error: " + e.toString() + "</p>";
			resStr += "<p>Error: Problem while entering data into database</p>";
			return resStr;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
			}
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		return resStr;
	}

	/**
	 * Adds a new invitee and then returns the id.
	 * 
	 * @param requestParameters
	 *            parameters need for adding a new invitee into the tables.
	 * @return int Id of the new invitee added.
	 */
	public int addInviteeAndReturnUserId(Map<String, String> requestParameters) {
		return Integer.parseInt(this.handleAddInvitees(requestParameters));
	}

	/**
	 * Runs database to handle input and also print table for adding invitees.
	 * 
	 * @param requestParameters
	 *            parameters need for adding a new invitee into the tables.
	 * @param showNextPage
	 *            Should next page be displayed or not.
	 * @return String Returns the id of the new user added or
	 */
	private String handleAddInvitees(Map<String, String> requestParameters) {
		String errStr = "", resStr = "";
		int userId = 0;

		/* connect to the database */
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			String sql, sqlIns = "insert into invitee(", sqlVal = "values(";

			/* get the column names of the table of invitee */
			stmt.execute("describe invitee");
			ResultSet rs = stmt.getResultSet();

			boolean submit = (requestParameters.get("save") != null);
			while (rs.next()) {

				/*
				 * read col name from database, matching col value from input
				 * request
				 */
				String columnName = rs.getString("Field");
				if (columnName.equalsIgnoreCase("id")) {
					continue;
				}
				String columnVal = requestParameters.get(columnName);
				if ((columnVal != null) && !columnVal.equalsIgnoreCase("null")
						&& (columnVal.indexOf("\'") != -1)) {
					columnVal = columnVal.replace("'", "\\'");
				}

				String columnType = rs.getString("Type");
				resStr += "<tr><td width=400 align=left>" + columnName;

				/* check for required field values */
				if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.lastname.name())) {
					resStr += " (required)";
					if (submit && Strings.isNullOrEmpty(columnVal)) {
						errStr += "<b>" + columnName + "</b> ";
					}
				}
				resStr += ": <input type='text' name='" + columnName + "' ";
				if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.salutation.name())) {
					resStr += "maxlength=5 size=5 ";
				} else {
					resStr += "maxlength=64 size=40 ";
				}
				if (submit) {

					/*
					 * tempValue=column_val; if(tempValue.indexOf("\'")!=-1){
					 * tempValue=tempValue.replace("'", "&#x27"); }
					 */

					resStr += "value='" + columnVal + "'"; // add submitted
					sqlIns += columnName + ",";

					if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.email.name())) {
						if (Strings.isNullOrEmpty(columnVal) || columnVal.equalsIgnoreCase("null")) {
							columnVal = WISEApplication.getInstance().getWiseProperties()
									.getAlertEmail();
						}
						sqlVal += "AES_ENCRYPT('" + columnVal + "','"
								+ this.getEmailEncryptionKey() + "'),";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.irb_id.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.salutation.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "Mr." : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.firstname.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.lastname.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase(User.INVITEE_FIELDS.phone.name())) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "0" : columnVal)
								+ "\",";
					} else if (columnName.equalsIgnoreCase("subj_type")) {
						sqlVal += "\"" + (Strings.isNullOrEmpty(columnVal) ? "1" : columnVal)
								+ "\",";
					} else {
						/* code added by Vijay */
						String temp = "";
						if (columnType.toLowerCase().contains("int")) {
							temp = "\"" + (Strings.isNullOrEmpty(columnVal) ? "0" : columnVal)
									+ "\"";
						} else if (columnType.toLowerCase().contains("date")) {
							temp = "\""
									+ (Strings.isNullOrEmpty(columnVal) ? "2012-12-31" : columnVal)
									+ "\"";
						} else {
							temp = "\"" + (Strings.isNullOrEmpty(columnVal) ? "" : columnVal)
									+ "\"";
						}
						sqlVal += "AES_ENCRYPT('" + temp + "','" + this.getEmailEncryptionKey()
								+ "'),";
						;

					}
				}

				resStr += "></td></tr>";
			}

			/*
			 * run the insertion if all the required fields have been filled in
			 * with values
			 */
			if (!errStr.equals("")) {
				resStr += "<tr><td align=center>Required fields " + errStr
						+ " not filled out </td></tr>";
			} else if (submit) {
				sql = sqlIns.substring(0, sqlIns.length() - 1) + ") "
						+ sqlVal.substring(0, sqlVal.length() - 1) + ")";
				LOGGER.info("The sql trying to execute is " + sql);
				stmt.execute(sql);
				resStr += "<tr><td align=center>New invitee " + requestParameters.get("last_name")
						+ " has been added</td></tr>";
			}

			/* display the submit button */
			resStr += "<tr><td align=center>" + "<input type='hidden' name='submit' value='true' >"
					+ "<input type='image' alt='submit' src='admin_images/submit.gif' border=0>"
					+ "</td></tr>";
			stmt.execute("select last_insert_id()");
			rs = stmt.getResultSet();
			if (rs.next()) {
				userId = rs.getInt(1);
			}

		} catch (RuntimeException e) {
			LOGGER.error("Runtime exception while loading invitee", e);
		} catch (SQLException e) {
			LOGGER.error("WISE ADMIN - LOAD INVITEE: " + e.toString(), e);
			// Security feature fix Exception should not be displayed
			// resStr += "<p>Error: " + e.toString() + "</p>";
			resStr += "<p>Error: Problem while entering data into database</p>";
			return resStr;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
			}
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		return String.valueOf(userId);
	}

	/**
	 * Returns the ID of the current survey.
	 * 
	 * @return String Name of the current survey.
	 */
	public String getCurrentSurveyIdString() {

		Connection conn = null;
		PreparedStatement stmt = null;
		String surveyIdString = null;
		String sql = "select id from surveys where status in ('P', 'D') and internal_id in"
				+ "(select max(internal_id) from surveys group by id) order by uploaded DESC";

		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				surveyIdString = rs.getString(1);
			}
		} catch (SQLException ex) {

		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
			}
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
		return surveyIdString;
	}

	// TODO: (low) continue moving functionality from User and Study_Space to
	// Data_Bank

	/**
	 * If a new survey is uploaded and there is changes the invitee_fields, this
	 * function will make sure that database and survey fields remain in sync.
	 * This function ensures that it does not touches the default mandatory
	 * arguments in the invitee table, even if they are tampered in the survey
	 * XML.
	 * 
	 * @param inviteeMetadata
	 *            Object which contains the metadata of the invitees populated
	 *            from the survey table.
	 */
	public void syncInviteeTable(InviteeMetadata inviteeMetadata) {
		Connection conn = null;
		Statement stmt = null;
		Set<String> dbColumns = new HashSet<String>();
		Set<String> columnsToBeRemoved = new HashSet<String>();
		Set<String> columnsToBeAdded = new HashSet<String>();
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			stmt.execute("describe invitee");
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				dbColumns.add(rs.getString("Field"));
			}
			for (Map.Entry<String, Values> map : inviteeMetadata.getFieldMap().entrySet()) {
				String columnName = map.getKey();
				Values columnValue = map.getValue();
				if (!columnValue.userNode || dbColumns.contains(columnName)) {
					continue;
				}
				columnsToBeAdded.add(columnName);
			}
			Iterator<String> it = dbColumns.iterator();
			while (it.hasNext()) {
				String columnName = it.next();
				Values columnValue = inviteeMetadata.getFieldMap().get(columnName);

				/* present in the DB, but not in the Xml file. */
				if (columnValue == null) {
					if (INVITEE_FIELDS.contains(columnName)) {
						continue;
					}
					columnsToBeRemoved.add(columnName);
				}
			}

			it = columnsToBeAdded.iterator();
			while (it.hasNext()) {
				String columnName = it.next();
				StringBuffer strBuff = new StringBuffer();
				strBuff.append("alter table invitee add column ")
						.append(columnName)
						.append("")
						.append(inviteeMetadata.getFieldMap().get(columnName).type.substring(0,
								inviteeMetadata.getFieldMap().get(columnName).type.length() - 1));
				LOGGER.info("@@@@@@ Columns being added are : " + strBuff.toString());
				stmt.execute(strBuff.toString());
			}

			it = columnsToBeRemoved.iterator();
			while (it.hasNext()) {
				String columnName = it.next();
				StringBuffer strBuff = new StringBuffer();
				strBuff.append("alter table invitee drop column ").append(columnName);
				LOGGER.info("@@@@@@ Columns being removed are : " + strBuff.toString());
				stmt.execute(strBuff.toString());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * Loads file from the data base.
	 * 
	 * @param fileName
	 *            File name that is to be loaded from the database.
	 * @param studySpaceName
	 *            StudySpace from the file has to be loaded.
	 * @return InputStream The file read form the data base is sent as the
	 *         stream to the caller functions.
	 */
	public InputStream getFileFromDatabase(String fileName, String studySpaceName) {
		Connection conn = null;
		PreparedStatement pstmnt = null;
		InputStream is = null;

		try {
			conn = this.getDBConnection();
			String querySQL = "SELECT filecontents FROM " + studySpaceName
					+ ".wisefiles WHERE filename = '" + fileName + "'";
			pstmnt = conn.prepareStatement(querySQL);
			ResultSet rs = pstmnt.executeQuery();

			while (rs.next()) {
				is = rs.getBinaryStream(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.error("Error while retrieving file from database");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (is == null) {
			LOGGER.error("The requested file is not available:'" + fileName + "' for studySpace: '"
					+ studySpaceName + "'");
		}
		return is;
	}

	/**
	 * Loads file from the data base. These are the survey xml files.
	 * 
	 * @param fileName
	 *            File name that is to be loaded from the database.
	 * @param studySpaceName
	 *            StudySpace from the file has to be loaded.
	 * @return InputStream The file read form the data base is sent as the
	 *         stream to the caller functions.
	 */
	public InputStream getXmlFileFromDatabase(String fileName) {
		Connection connection = null;
		PreparedStatement prepStmt = null;
		InputStream inputStream = null;

		String studySpaceName = this.studySpace.studyName;

		if (Strings.isNullOrEmpty(studySpaceName)) {
			LOGGER.error("No study space name  provided");
			return null;
		}

		LOGGER.debug("Fetching file '" + fileName + "' in study space '" + studySpaceName + "'");

		try {
			connection = this.getDBConnection();
			String querySQL = "SELECT filecontents FROM " + studySpaceName
					+ ".xmlfiles WHERE filename='" + fileName + "'";
			prepStmt = connection.prepareStatement(querySQL);
			ResultSet resultSet = prepStmt.executeQuery();
			while (resultSet.next()) {
				inputStream = resultSet.getBinaryStream(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			LOGGER.error("Error while retrieving file from database");
		}

		if (inputStream == null) {
			LOGGER.error("No file with filename '" + fileName + "' found in the database");
		}

		return inputStream;
	}

	/**
	 * This Method is used to replace the patterns in the email body.
	 * 
	 * @param msg
	 *            Message whose content has to be matched and replaced.
	 * @param inviteeId
	 *            Invitee for whom the message is sent as email.
	 * @return String The modified Message.
	 */
	@SuppressWarnings("rawtypes")
	public String replacePattern(String msg, String inviteeId) {
		if (msg != null) {
			Connection connect = null;
			Statement stmt1 = null;
			Statement stmt2 = null;
			String fieldName = "";
			String actualValue = "";
			try {
				connect = this.getDBConnection();
				stmt1 = connect.createStatement();
				stmt2 = connect.createStatement();
				stmt1.execute("describe invitee");
				ResultSet rs1 = stmt1.getResultSet();
				rs1.last();
				int columnCount = rs1.getRow();

				rs1.beforeFirst();
				Map<String, String> mMap = new HashMap<String, String>();
				String myStatement = "SELECT";
				int tempCount = 1;
				while (rs1.next()) {
					fieldName = rs1.getString("Field");
					mMap.put(fieldName, "");

					if (fieldName.equalsIgnoreCase("id") || fieldName.equalsIgnoreCase("lastname")
							|| fieldName.equalsIgnoreCase("firstname")
							|| fieldName.equalsIgnoreCase("salutation")
							|| fieldName.equalsIgnoreCase("irb_id")
							|| fieldName.equalsIgnoreCase("phone")
							|| fieldName.equalsIgnoreCase("subj_type")) {
						if (tempCount == columnCount) {
							myStatement = myStatement + " " + fieldName;
						} else {
							myStatement = myStatement + " " + fieldName + ",";
						}
					} else {
						if (tempCount == columnCount) {
							myStatement = myStatement + " CAST(AES_DECRYPT(" + fieldName + ", '"
									+ this.getEmailEncryptionKey() + "') AS CHAR) as " + fieldName;
						} else {
							myStatement = myStatement + " CAST(AES_DECRYPT(" + fieldName + ", '"
									+ this.getEmailEncryptionKey() + "') AS CHAR) as " + fieldName
									+ ",";
						}
					}
					tempCount++;
				}
				myStatement += " FROM invitee WHERE id = " + inviteeId;
				stmt2.execute(myStatement);
				ResultSet rs2 = stmt2.getResultSet();
				Iterator iter = mMap.entrySet().iterator();

				rs2.next();
				while (iter.hasNext()) {
					Map.Entry mEntry = (Map.Entry) iter.next();
					fieldName = (String) mEntry.getKey();
					mEntry.setValue(rs2.getString(fieldName));
				}
				Pattern p;
				Matcher m;
				Iterator iter1 = mMap.entrySet().iterator();

				while (iter1.hasNext()) {
					Map.Entry mEntry = (Map.Entry) iter1.next();
					fieldName = (String) mEntry.getKey();
					if (fieldName.equalsIgnoreCase("id") || fieldName.equalsIgnoreCase("lastname")
							|| fieldName.equalsIgnoreCase("firstname")
							|| fieldName.equalsIgnoreCase("email")
							|| fieldName.equalsIgnoreCase("salutation")
							|| fieldName.equalsIgnoreCase("irb_id")
							|| fieldName.equalsIgnoreCase("phone")
							|| fieldName.equalsIgnoreCase("subj_type")) {
						continue;
					}
					actualValue = (String) mEntry.getValue();
					p = Pattern.compile("(@" + fieldName + "@)");
					m = p.matcher(msg);
					LOGGER.error("The ActualValue that is being replaced is " + actualValue);
					if (actualValue == null) {
						actualValue = "";
					}
					actualValue = actualValue.replaceAll("^\"|\"$", "");
					LOGGER.error("The ActualValue that is being replaced  after removing the quotes is "
							+ actualValue);
					// if (actualValue.equalsIgnoreCase(""))
					msg = m.replaceAll(actualValue);
				}
				connect.close();
			} catch (SQLException e) {
				e.printStackTrace();
				LOGGER.error("Error while Replacing the email message patterns");
			}
		}
		return msg;
	}

	/**
	 * Enters new record in survey_message_use table for the new URL.
	 * 
	 * @param messageId
	 *            Message id of the new record
	 * @param inviteeId
	 *            Invitee Id for whom the mail is sent.
	 * @param surveyID
	 *            Survey name for which this user is linked to.
	 * @return String New randomly generated number.
	 */
	public String recordMessageUse(String messageId, String inviteeId, String surveyID) {
		String messageUseId = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(22);
		String sql = "INSERT INTO survey_message_use(messageId,invitee, survey, message) VALUES (?, ?, ?, ?) ";
		String newid = messageUseId;
		Connection connect = null;
		PreparedStatement stmt = null;
		try {
			connect = this.getDBConnection();
			stmt = connect.prepareStatement(sql);
			stmt.setString(1, messageUseId);
			stmt.setInt(2, Integer.parseInt(inviteeId));
			stmt.setString(3, surveyID);
			stmt.setString(4, messageId);

			// check if the user has already existed in the survey data table
			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error recording new message using " + sql + ": " + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("Error recording new message using " + sql + ": " + e.toString(), null);
		} finally {
			try {
				if (connect != null) {
					connect.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return newid;
	}

	/**
	 * Updates the message id in survey_message_use table.
	 * 
	 * @param messageId
	 *            Message id of the new record
	 * @param inviteeId
	 *            Invitee Id for whom the mail is sent.
	 * @param surveyID
	 *            Survey name for which this user is linked to.
	 * @return String Returns success in case of correct update.
	 */
	public String updateMessageUse(String messageId, String inviteeId, String surveyID) {

		String msgUseSql = "UPDATE survey_message_use SET message= ?"
				+ " WHERE message = 'attempt' AND survey = ?" + " AND invitee = ? ";
		Connection connect = null;
		PreparedStatement stmt = null;
		try {
			connect = this.getDBConnection();
			stmt = connect.prepareStatement(msgUseSql);

			stmt.setString(1, messageId);
			stmt.setString(2, surveyID);
			stmt.setInt(3, Integer.parseInt(inviteeId));

			stmt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error updating new message using " + msgUseSql + ": " + e.toString(),
					null);
		} catch (NumberFormatException e) {
			LOGGER.error("Error recording new message using:" + e.toString(), null);
		} finally {
			try {
				if (connect != null) {
					connect.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return "success";
	}

	/**
	 * Record an entry into table survey_user_state.
	 * 
	 * @param state
	 *            State to be record.
	 * @param inviteeId
	 *            Invitee id .
	 * @param surveyID
	 *            Survey name.
	 * @param messageSeqId
	 *            Name of the message sequence.
	 * @return String Sucess is returned if true.
	 */
	public String recordSurveyState(String state, String inviteeId, String surveyID,
			String messageSeqId) {

		String sqlU = "INSERT INTO survey_user_state (invitee, state, survey, message_sequence) "
				+ "VALUES( ?, ?, ?, ? ) "
				+ "ON DUPLICATE KEY UPDATE state = ?, state_count=1, message_sequence=VALUES(message_sequence)";
		Connection connect = null;
		PreparedStatement stmt = null;

		try {
			connect = this.getDBConnection();
			/* connect to database */
			stmt = connect.prepareStatement(sqlU);
			stmt.setInt(1, Integer.parseInt(inviteeId));
			stmt.setString(2, state);
			stmt.setString(3, surveyID);
			stmt.setString(4, messageSeqId);
			stmt.setString(5, state);

			/* check if the user has already existed in the survey data table */
			stmt.executeUpdate();
			stmt.close();
		} catch (SQLException e) {
			LOGGER.error("Error recording new message using " + sqlU + ": " + e.toString(), null);
		} catch (NumberFormatException e) {
			LOGGER.error("Error recording new message using:" + e.toString(), null);
		} finally {
			try {
				if (connect != null) {
					connect.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return "success";
	}

	public String printInvitee(String surveyId) {
		String outputString = "";
		String sql = "SELECT i.id, firstname, lastname, salutation, irb_id, state, "
				+ "email FROM invitee as i, survey_user_state as s where i.id=s.invitee and survey= ?"
				+ " ORDER BY i.id";
		String sqlm = "select invitee from survey_user_state where state='declined' and invitee= ?";
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmtm = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, surveyId);
			ResultSet rs = stmt.executeQuery();
			outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
			outputString += "<tr>";
			outputString += "<th class=sfon></th>";
			outputString += "<th class=sfon>User ID</th>";
			outputString += "<th class=sfon>User Name</th>";
			outputString += "<th class=sfon>IRB ID</th>";
			outputString += "<th class=sfon>User state</th>";
			outputString += "<th class=sfon>User's Email Address</th></tr>";

			stmtm = conn.prepareStatement(sqlm);
			while (rs.next()) {
				stmtm.clearParameters();
				stmtm.setInt(1, rs.getInt(1));
				ResultSet rsm = stmtm.executeQuery();
				if (rsm.next()) {
					outputString += "<tr bgcolor='#E4E4E4'>";
				} else {
					outputString += "<tr>";
				}
				outputString += "<td><input type='checkbox' name='user' value='" + rs.getString(1)
						+ "'></td>";
				outputString += "<td>" + rs.getString(1) + "</td>";
				outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " "
						+ rs.getString(3) + "</td>";
				outputString += "<td>" + rs.getString(5) + "</td>";
				outputString += "<td>" + rs.getString(6) + "</td>";
				outputString += "<td>" + rs.getString(7) + "</td>";
				outputString += "</tr>";
			}
			rs.close();
			outputString += "</table>";
		} catch (SQLException e) {
			LOGGER.error("ADMIN Data Bank - PRINT INVITEE WITH STATE: " + e.toString(), e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (stmtm != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("ADMIN Data Bank - PRINT INVITEE WITH STATE: " + e.toString(), e);
			}
		}
		return outputString;

	}

	public String generateEmailMessage(String surveyId, MessageSequence msgSeq, Message msg,
			String msgType, String messageSeqId, String whereStr, boolean displayMessage) {

		String outputString = "";
		String messageUseId = "";
		MessageSender sender = new MessageSender(msgSeq);
		try {
			Connection conn = this.getDBConnection();
			Statement inviteeQuery = conn.createStatement();

			List<String> successIds = new ArrayList<String>();
			outputString += "Sending message '" + msg.subject + "' to:<p>";

			String inviteeSql = "SELECT id, firstname, lastname, salutation, AES_DECRYPT(email,'"
					+ this.getEmailEncryptionKey() + "') FROM invitee WHERE " + whereStr;
			LOGGER.info("The sql query run when selecting the invitees is " + inviteeSql);
			ResultSet rs = inviteeQuery.executeQuery(inviteeSql);

			/* send email message to each selected invitee */
			while (rs.next()) {
				String inviteeId = rs.getString(1);
				String firstname = rs.getString(2);
				String lastname = rs.getString(3);
				String salutation = rs.getString(4);
				String email = rs.getString(5);

				/*
				 * This is used when for anonymous user. We want to return the
				 * message id to the calling function from save_anno_user so
				 * that it can forward the survey request automatically.
				 */

				/* print out the user information */
				outputString += salutation + " " + firstname + " " + lastname
						+ " with email address &lt;" + email + "&gt; -&gt; ";

				messageUseId = this.recordMessageUse("attempt", inviteeId, surveyId);

				EmailMessage emailMessage = new EmailMessage(email, salutation, lastname);
				String msgResult = sender.sendMessage(msg, messageUseId, emailMessage,
						this.studySpace.id, this, inviteeId, WISEApplication.getInstance()
								.getWiseProperties());

				if (msgResult.equalsIgnoreCase("")) {
					outputString += "message sent.<br>";
					successIds.add(inviteeId);
					this.updateMessageUse(msg.id, inviteeId, surveyId);
				} else {
					this.updateMessageUse("err: " + msgResult, inviteeId, surveyId);
				}

				if (msgType.equalsIgnoreCase("invite")) {
					String state = msgResult.equalsIgnoreCase("") ? "invited" : "email_error";
					this.recordSurveyState(state, inviteeId, surveyId, messageSeqId);
				}

			}
			if (successIds.size() > 0) {
				String successLst = "(";
				for (int i = 0; i < (successIds.size() - 1); i++) {
					successLst += successIds.get(i) + ",";
				}
				successLst += successIds.get(successIds.size() - 1) + ")";
				outputString += successLst + "<br><br>";
			}
			conn.close();

			if (!displayMessage) {
				outputString = messageUseId;
			}
		} catch (SQLException e) {
			LOGGER.info("ADMIN INFO - SEND MESSAGES: " + e.toString(), e);
		}
		return outputString;
	}

	public String viewOpenResults(String question, Survey survey, String page, String whereClause,
			String unanswered) {
		StringBuilder out = new StringBuilder();
		try {

			/* open database connection */
			// TODO: Change to prepared Statement.
			Connection conn = this.getDBConnection();
			Statement stmt = conn.createStatement();

			if (page != null) {

				/*
				 * get all the answers from data table regarding to this
				 * question
				 */

				String sql = "select invitee, firstname, lastname, status, " + question + " from "
						+ survey.getId() + "_data, invitee where ";
				sql += "id=invitee and (status not in (";

				for (int k = 0; k < survey.getPages().length; k++) {
					if (!page.equalsIgnoreCase(survey.getPages()[k].getId())) {
						sql += "'" + survey.getPages()[k].getId() + "', ";
					} else {
						break;
					}
				}
				sql += "'" + page + "') or status is null) and " + question + " is not null and "
						+ question + " !=''";
				if (!whereClause.equalsIgnoreCase("")) {
					sql += " and " + whereClause;
				}

				// New query to fix bug

				String sqlInteger = "SELECT answer FROM " + this.wiseTables.getMainDataInteger()
						+ " INNER JOIN " + this.wiseTables.getPageSubmit() + " ON "
						+ this.wiseTables.getMainDataInteger() + ".inviteeId = "
						+ this.wiseTables.getPageSubmit() + ".invitee and "
						+ this.wiseTables.getMainDataInteger() + ".survey="
						+ this.wiseTables.getPageSubmit() + ".survey where "
						+ this.wiseTables.getPageSubmit() + ".page = '" + page + "' and "
						+ this.wiseTables.getMainDataInteger() + ".questionId = '" + question
						+ "' and " + this.wiseTables.getMainDataInteger() + ".survey = '"
						+ survey.getId() + "'";

				if (!whereClause.equalsIgnoreCase("")) {
					sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereClause;
				}

				sqlInteger += " Group by answer;";

				String sqlText = "SELECT answer FROM " + this.wiseTables.getMainDataText()
						+ " INNER JOIN " + this.wiseTables.getPageSubmit() + " ON "
						+ this.wiseTables.getMainDataText() + ".inviteeId = "
						+ this.wiseTables.getPageSubmit() + ".invitee and "
						+ this.wiseTables.getMainDataText() + ".survey="
						+ this.wiseTables.getPageSubmit() + ".survey where "
						+ this.wiseTables.getPageSubmit() + ".page = '" + page + "' and "
						+ this.wiseTables.getMainDataText() + ".questionId = '" + question
						+ "' and " + this.wiseTables.getMainDataText() + ".survey = '"
						+ survey.getId() + "'";

				if (!whereClause.equalsIgnoreCase("")) {
					sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereClause;
				}

				sqlText += " Group by answer;";

				PreparedStatement stmtForInteger = conn.prepareStatement(sqlInteger);
				ResultSet rs = stmtForInteger.executeQuery();

				String text;
				while (rs.next()) {
					text = rs.getString("answer");
					if ((text == null) || text.equalsIgnoreCase("")) {
						text = "null";
					}
					out.append("<tr>");
					out.append("<td align=left>" + text + "</td>");
					out.append("</tr>");
				}

				PreparedStatement stmtForText = conn.prepareStatement(sqlText);
				rs = stmtForText.executeQuery();

				while (rs.next()) {
					text = rs.getString("answer");
					if ((text == null) || text.equalsIgnoreCase("")) {
						text = "null";
					}
					out.append("<tr>");
					out.append("<td align=left>" + text + "</td>");
					out.append("</tr>");
				}

			} // end of if

			/* display unanswered question number */
			if ((unanswered != null) && !unanswered.equalsIgnoreCase("")) {
				out.append("<tr><td align=left>Number of unanswered:" + unanswered + "</td></tr>");
				out.append("</table></center><br><br>");
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("WISE - VIEW OPEN RESULT: " + e.toString(), null);
		}
		return out.toString();
	}

	public Hashtable<String, String> getIrbGroups() {
		Hashtable<String, String> irbGroups = new Hashtable<String, String>();

		/* select the different IRBs from the invitees tables. */
		String sqle = "select distinct(irb_id) from invitee order by irb_id";

		/* select all the invitees belonging to a particular IRB */
		String sql1 = "select id from invitee where irb_id IS NULL";
		String sql2 = "select id from invitee where irb_id = ?";

		Connection conn = null;
		PreparedStatement statement = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		try {
			conn = this.getDBConnection();
			statement = conn.prepareStatement(sqle);
			stmt1 = conn.prepareStatement(sql1);
			stmt2 = conn.prepareStatement(sql2);
			ResultSet rse = statement.executeQuery();
			while (rse.next()) {
				String irbId = rse.getString("irb_id");
				ResultSet rs;
				if (irbId == null) {
					rs = stmt1.executeQuery();
				} else {
					stmt2.clearParameters();
					stmt2.setString(1, irbId);
					rs = stmt2.executeQuery();
				}
				String irbGroupsId = " ";
				while (rs.next()) {
					irbGroupsId += rs.getString(1) + " ";
				}
				irbGroups.put(irbId, irbGroupsId);
			}
		} catch (NullPointerException e) {
			LOGGER.error("ADMIN INFO - GET IRB GROUPS: " + e.toString(), e);
		} catch (SQLException e) {
			LOGGER.error("ADMIN INFO - GET IRB GROUPS: " + e.toString(), e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt1 != null) {
					stmt1.close();
				}
				if (stmt2 != null) {
					stmt2.close();
				}
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				LOGGER.error("ADMIN INFO - GET IRB GROUPS: " + e.toString(), e);
			}
		}
		return irbGroups;
	}

	public String printAuditLogs() {
		String outputString = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		String sql = "select invitee, concat(firstname,' ',lastname) as name, AES_DECRYPT(patient_name,'"
				+ this.getEmailEncryptionKey()
				+ "')as ptname,ipAddress ,actions,updated_time from audit_logs";
		try {

			/* connect to the database */
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			outputString += "<tr><td class=sfon align=center>ID</td>"
					+ "<td class=sfon align=center>User Name</td>"
					+ "<td class=sfon align=center>Patient Name</td>"
					+ "<td class=sfon align=center>IP Address</td>"
					+ "<td class=sfon align=center>Action</td>"
					+ "<td class=sfon align=center>TimeStamp</td></tr>";

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				outputString += "<tr><td align=center>" + rs.getString("invitee") + "</td>";
				outputString += "<td align=center>" + rs.getString("name") + "</td>";
				outputString += "<td align=center>" + rs.getString("ptname") + "</td>";
				outputString += "<td align=center>" + rs.getString("ipAddress") + "</td>";
				outputString += "<td align=center>" + rs.getString("actions") + "</td>";
				outputString += "<td align=center>" + rs.getString("updated_time") + "</td></tr>";
			}
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("ADMIN INFO - PRINT AUDIT LOGS: " + e.toString(), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("check why prepared statement creation failed", e);
			}
		}
		return outputString;

	}

	public String printUserState(String state, String surveyId) {
		String outputString = "";
		try {
			/* connect to the database */
			Connection conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			String sql = "";

			if (state.equalsIgnoreCase("not_invited")) {

				outputString += "<tr><td class=sfon align=center>ID</td>"
						+ "<td class=sfon align=center>Name</td>"
						+ "<td class=sfon align=center>Email Address</td></tr>";

				sql = "select id, firstname, lastname, AES_DECRYPT(email,'"
						+ this.getEmailEncryptionKey()
						+ "') as email from invitee where id not in (select invitee from "
						+ "survey_user_state where survey='" + surveyId + "')";
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				while (rs.next()) {
					outputString += "<tr><td align=center>" + rs.getString("id") + "</td>";
					outputString += "<td align=center>" + rs.getString("firstname") + " "
							+ rs.getString("lastname") + "</td>";
					outputString += "<td align=center>" + rs.getString("email") + "</td></tr>";
				}
			} else if (state.equalsIgnoreCase("all")) {

				/* all users who have been invited */
				outputString += "<tr><td class=sfon align=center>ID</td>"
						+ "<td class=sfon align=center>Name</td>"
						+ "</td><td class=sfon align=center>State</td>"
						+ "<td class=sfon align=center>Email</td>";
				sql = "select i.id, i.firstname, i.lastname, AES_DECRYPT(i.email, '"
						+ this.getEmailEncryptionKey() + "') as email, u.state as state "
						+ "from invitee as i, survey_user_state as u "
						+ "where i.id=u.invitee and u.survey='" + surveyId + "' order by i.id";
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				// String user_id = "";
				while (rs.next()) {
					outputString += "<tr><td align=center>" + rs.getString("id") + "</td>";
					outputString += "<td align=center>" + rs.getString("firstname") + " "
							+ rs.getString("lastname") + "</td>";
					outputString += "<td align=center>" + rs.getString("state") + "</td>";
					outputString += "<td align=center>" + rs.getString("email") + "</td></tr>";
				}

				/* all users who have not been invited */
				sql = "select id, firstname, lastname, AES_DECRYPT(email,'"
						+ this.getEmailEncryptionKey()
						+ "') as email from invitee where id not in (select invitee "
						+ "from survey_user_state where survey='" + surveyId + "')";
				stmt.execute(sql);
				rs = stmt.getResultSet();
				while (rs.next()) {
					outputString += "<tr><td align=center>" + rs.getString("id") + "</td>";
					outputString += "<td align=center>" + rs.getString("firstname") + " "
							+ rs.getString("lastname") + "</td>";
					outputString += "<td align=center>" + "Not Invited" + "</td>";
					outputString += "<td align=center>" + rs.getString("email") + "</td></tr>";
				}
			} else {
				outputString += "<tr><td class=sfon align=center>ID</td>"
						+ "<td class=sfon align=center>Name</td>"
						+ "</td><td class=sfon align=center>State</td>"
						+ "<td class=sfon align=center>Entry Time</td>"
						+ "<td class=sfon align=center>Email</td>"
						+ "<td class=sfon align=center>Messages (Sent Time)";
				sql = "select i.id, firstname, lastname, AES_DECRYPT(email, '"
						+ this.getEmailEncryptionKey()
						+ "') as email, state, entry_time, message, sent_date "
						+ "from invitee as i, survey_message_use as m, survey_user_state as u "
						+ "where i.id = m.invitee and i.id=u.invitee and m.survey=u.survey and u.survey='"
						+ surveyId + "' " + "and state like '" + state + "%' order by i.id";

				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				String userId = "", lastUserId = "";
				while (rs.next()) {
					userId = rs.getString("id");
					if (!userId.equalsIgnoreCase(lastUserId)) {
						lastUserId = userId;

						/* print out the new row */
						outputString += "</td></tr><tr><td align=center>" + userId + "</td>";
						outputString += "<td align=center>" + rs.getString("firstname") + " "
								+ rs.getString("lastname") + "</td>";
						outputString += "<td align=center>" + rs.getString("state") + "</td>";
						outputString += "<td align=center>" + rs.getString("entry_time") + "</td>";
						outputString += "<td align=center>" + rs.getString("email") + "</td>";
						outputString += "<td align=center>" + rs.getString("message") + " "
								+ rs.getString("sent_date");
					} else {

						/* append other messages under the same invitee ID */
						outputString += "<br>" + rs.getString("message") + " "
								+ rs.getString("sent_date");
					}
				}
				outputString += "</td></tr>";
			}

			stmt.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("ADMIN INFO - PRINT USER STATE: " + e.toString(), e);
		}
		return outputString;

	}

	public String renderInitialInviteTable(String surveyId, boolean isReminder) {
		String outputString = "";
		MessageSequence[] msgSeqs = this.studySpace.preface.getMessageSequences(surveyId);
		if (msgSeqs.length == 0) {
			return "No message sequences found in Preface file for selected Survey.";
		}
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			for (int i = 0; i < msgSeqs.length; i++) {
				MessageSequence msgSeq = msgSeqs[i];
				String irbName = msgSeq.irbId;
				if (Strings.isNullOrEmpty(irbName)) {
					irbName = "= ''";
				} else {
					irbName = "= '" + irbName + "'";
				}
				Message msg = msgSeq.inviteMsg;
				outputString += "<form name=form1 method=post action='initial_invite_send.jsp'>\n"
						+ "<input type='hidden' name='seq' value='"
						+ msgSeq.id
						+ "'>\n"
						+ "<input type='hidden' name='reminder' value='"
						+ String.valueOf(isReminder)
						+ "'>\n"
						+ "<input type='hidden' name='svy' value='"
						+ surveyId
						+ "'>\n"
						+ "Start Message Sequence <B>"
						+ msgSeq.id
						+ "</b> (designated for IRB "
						+ irbName
						+ ")<BR>\n"
						+ "...using Initial Message: "
						+ "<a href='print_msg_body.jsp?seqID="
						+ msgSeq.id
						+ "&msgID=invite' target='_blank'>"
						+ msg.subject
						+ "</a><br>\n"
						+ "<p align=center><input type='image' alt='Click to send email. This button is equivalent to the one at bottom.' "
						+ "src='admin_images/send.gif'></p>"
						+ "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
				try {

					/* select the invitees without any states */
					String sql = this.buildInitialInviteQuery(surveyId, msgSeq.id, irbName,
							isReminder);
					stmt.execute(sql);
					ResultSet rs = stmt.getResultSet();
					outputString += "<tr>";
					outputString += "<th class=sfon></th>";
					outputString += "<th class=sfon>User ID</th>";
					outputString += "<th class=sfon>User Name</th>";
					outputString += "<th class=sfon>IRB</th>";
					outputString += "<th class=sfon>User's Email Address</th></tr>";

					while (rs.next()) {
						outputString += "<tr>";
						outputString += "<td><input type='checkbox' name='user' value='"
								+ rs.getString(1) + "'></td>";
						outputString += "<td>" + rs.getString(1) + "</td>";
						outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " "
								+ rs.getString(3) + "</td>";
						outputString += "<td>" + rs.getString(5) + "</td>";
						outputString += "<td>" + rs.getString(6) + "</td>";
						outputString += "</tr>";
					}
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(
							"ADMIN Data Bank error - render_initial_invite_table: " + e.toString(),
							e);
				}
				outputString += "</table><p align='center'>"
						+ "<input type='image' alt='Click to send email. This button is the same as one above.' src='admin_images/send.gif'>"
						+ "</p></form>";
			} // for
		} catch (SQLException e) {
			LOGGER.error(
					"ADMIN Data Bank connection error - renderInitialInviteTable: " + e.toString(),
					e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error(
						"ADMIN Data Bank connection error - renderInitialInviteTable: "
								+ e.toString(), e);
			}
		}
		return outputString;

	}

	/**
	 * Builds the query that is used to get information about invitees for
	 * sending emails.
	 * 
	 * @param surveyId
	 *            Survey Id for which all invitees have to listed.
	 * @param msgSeq
	 *            To classify the invitee groups based on the message sequence.
	 * @param irbName
	 *            Irb name whom the invitees are linked.
	 * @param isReminder
	 *            Includes all the invitees who have not complete the survey
	 *            incase if it true else it includes only the invitees who have
	 *            not started received any mail presviously.
	 * @return String The composed SQL query
	 */
	private String buildInitialInviteQuery(String surveyId, String msgSeq, String irbName,
			boolean isReminder) {
		StringBuffer strBuff = new StringBuffer();
		if (isReminder) {
			strBuff.append("SELECT I.id, I.firstname, I.lastname, I.salutation, I.irb_id, AES_DECRYPT(I.email,'"
					+ this.getEmailEncryptionKey()
					+ "') FROM invitee as I, survey_user_state as S WHERE I.irb_id "
					+ irbName
					+ " AND I.id not in (select invitee from survey_user_state where survey='"
					+ surveyId
					+ "' AND state like 'completed') AND I.id=S.invitee AND S.message_sequence='"
					+ msgSeq + "' ORDER BY id");
		} else {
			strBuff.append("SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email,'"
					+ this.getEmailEncryptionKey()
					+ "') FROM invitee WHERE irb_id "
					+ irbName
					+ " AND id not in (select invitee from survey_user_state where survey='"
					+ surveyId + "')" + "ORDER BY id");
		}
		return strBuff.toString();
	}

	public String getUserCountsInStates(String surveyId) {
		String outputString = "";
		// Hashtable states_counts = new Hashtable();
		int nNotInvited = 0, nInvited = 0, nDeclined = 0, nStarted = 0, nStartReminded = 0;
		int nNotResponded = 0, nInterrupted = 0, nCompleteReminded = 0, nNotCompleted = 0, nCompleted = 0;
		int nAll = 0;

		Connection conn = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;
		String sql1 = "select count(distinct id) as uninvited from invitee where id not in "
				+ "(select invitee from survey_user_state where survey=?)";
		String sql2 = "select count(distinct invitee) as counts, state from survey_user_state where survey=?"
				+ " group by state order by state";
		try {

			/* connect to the database */
			conn = this.getDBConnection();
			stmt1 = conn.prepareStatement(sql1);
			stmt2 = conn.prepareStatement(sql2);
			outputString += "<table border=0>";

			stmt1.setString(1, surveyId);
			ResultSet rs1 = stmt1.executeQuery();
			while (rs1.next()) {
				nNotInvited = rs1.getInt("uninvited");
			}
			nAll += nNotInvited;

			stmt2.setString(1, surveyId);
			ResultSet rs2 = stmt2.executeQuery();
			while (rs2.next()) {
				if (rs2.getString("state").equalsIgnoreCase("invited")) {
					nInvited = rs2.getInt("counts");
					nAll += nInvited;
				}
				if (rs2.getString("state").equalsIgnoreCase("declined")) {
					nDeclined = rs2.getInt("counts");
					nAll += nDeclined;
				}
				if (rs2.getString("state").equalsIgnoreCase("started")) {
					nStarted = rs2.getInt("counts");
					nAll += nStarted;
				}
				if (rs2.getString("state").equalsIgnoreCase("interrupted")) {
					nInterrupted = rs2.getInt("counts");
					nAll += nInterrupted;
				}
				if (rs2.getString("state").indexOf("start_reminder") != -1) {
					nStartReminded += rs2.getInt("counts");
					nAll += nStartReminded;
				}
				if (rs2.getString("state").equalsIgnoreCase("non_responder")) {
					nNotResponded = rs2.getInt("counts");
					nAll += nNotResponded;
				}
				if (rs2.getString("state").indexOf("completion_reminder") != -1) {
					nCompleteReminded += rs2.getInt("counts");
					nAll += nCompleteReminded;
				}
				if (rs2.getString("state").equalsIgnoreCase("incompleter")) {
					nNotCompleted = rs2.getInt("counts");
					nAll += nNotCompleted;
				}
				if (rs2.getString("state").equalsIgnoreCase("completed")) {
					nCompleted = rs2.getInt("counts");
					nAll += nCompleted;
				}
			}

			outputString += "<tr><td><p class=\"status\">All</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=all'>" + nAll + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Not Invited</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=not_invited'>" + nNotInvited + "</td></tr>";

			outputString += "<tr><td><p class=\"status-category\"><u>Not Started</u></p></td><td></td></tr>";
			outputString += "<tr><td><p class=\"status\">Invited</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=invited'>" + nInvited + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Reminder Sent</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=start_reminder'>" + nStartReminded + "</a></td></tr>";

			outputString += "<tr><td><p class=\"status-category\"><u>Incomplete</u></p></td><td/></tr>";
			outputString += "<tr><td><p class=\"status\">Currently Taking</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=started'>" + nStarted + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Interrupted</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=interrupted'>" + nInterrupted + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Reminder Sent</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=completion_reminder'>" + nCompleteReminded + "</a></td></tr>";

			outputString += "<tr><td><p class=\"status-category\"><u>End States</u></p></td><td/></tr>";
			outputString += "<tr><td><p class=\"status\">Completed</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=completed'>" + nCompleted + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Incompleter</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=incompleter'>" + nNotCompleted + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Nonresponder</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=non_responder'>" + nNotResponded + "</a></td></tr>";
			outputString += "<tr><td><p class=\"status\">Declined</p></td><td align=center><a href='show_people.jsp?s="
					+ surveyId + "&st=declined'>" + nDeclined + "</a></td></tr>";

			outputString += "</table>";
			rs1.close();
			rs2.close();
		} catch (SQLException e) {
			LOGGER.error("ADMIN APPLICATION - GET USER COUNTS IN STATES: " + e.toString(), e);
		} finally {
			try {
				if (stmt1 != null) {
					stmt1.close();
				}
				if (stmt2 != null) {
					stmt2.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("check why prepared statement creation failed", e);
			}
		}
		return outputString;
	}

	public String renderInviteTable(String surveyId) {
		String outputString = "";
		MessageSequence[] msgSeqs = this.studySpace.preface.getMessageSequences(surveyId);
		if (msgSeqs.length == 0) {
			return "No message sequences found in Preface file for selected Survey.";
		}
		String sql = "SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email, '"
				+ this.getEmailEncryptionKey() + "') FROM invitee WHERE irb_id = ?"
				+ " ORDER BY id";
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			for (int i = 0; i < msgSeqs.length; i++) {
				MessageSequence msgSeq = msgSeqs[i];
				String irbName = msgSeq.irbId;

				Message msg = msgSeq.inviteMsg;
				outputString += "<form name=form1 method=post action='invite_send.jsp'>\n"
						+ "<input type='hidden' name='seq' value='"
						+ msgSeq.id
						+ "'>\n"
						+ // repeat form so we can use same hidden field names
							// on each
						"<input type='hidden' name='svy' value='" + surveyId + "'>\n"
						+ "Using Message Sequence <B>" + msgSeq.id + "</b> (designated for IRB "
						+ irbName + ")<BR>\n" + "...SEND Message: <BR>"
						+ "<input type='radio' name='message' value='invite'>\n"
						+ "<a href='print_msg_body.jsp?seqID=" + msgSeq.id
						+ "&msgID=invite' target='_blank'>" + msg.subject + "</a><br>\n";
				for (int j = 0; j < msgSeq.totalOtherMessages(); j++) {
					msg = msgSeq.getTypeMessage("" + j);
					outputString += "<input type='radio' name='message' value='" + j + "'>\n"
							+ "<a href='print_msg_body.jsp?seqID=" + msgSeq.id + "&msgID=" + j
							+ "' target='_blank'>" + msg.subject + "</a><br>\n";
				}
				outputString += "<p align=center><input type='image' alt='Click to send email. This button is equivalent to the one at bottom.' "
						+ "src='admin_images/send.gif'></p>"
						+ "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
				try {

					/* select the invitees without any states */
					stmt.clearParameters();
					stmt.setString(1, irbName);
					ResultSet rs = stmt.executeQuery();
					outputString += "<tr>";
					outputString += "<th class=sfon></th>";
					outputString += "<th class=sfon>User ID</th>";
					outputString += "<th class=sfon>User Name</th>";
					outputString += "<th class=sfon>IRB</th>";
					outputString += "<th class=sfon>User's Email Address</th></tr>";

					while (rs.next()) {
						outputString += "<tr>";
						outputString += "<td><input type='checkbox' name='user' value='"
								+ rs.getString(1) + "'></td>";
						outputString += "<td>" + rs.getString(1) + "</td>";
						outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " "
								+ rs.getString(3) + "</td>";
						outputString += "<td>" + rs.getString(5) + "</td>";
						outputString += "<td>" + rs.getString(6) + "</td>";
						outputString += "</tr>";
					}
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(
							"ADMIN Data Bank error - render_initial_invite_table: " + e.toString(),
							e);
				}
				outputString += "</table><p align='center'>"
						+ "<input type='image' alt='Click to send email. This button is the same as one above.' src='admin_images/send.gif'>"
						+ "</p></form>";
			} // for
			conn.close();
		} catch (SQLException e) {
			LOGGER.error(
					"ADMIN Data Bank DB connection error - renderInitialInviteTable: "
							+ e.toString(), e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error(
						"ADMIN Data Bank connection error - renderInitialInviteTable: "
								+ e.toString(), e);
			}
		}
		return outputString;
	}

	public String printInvite() {
		String outputString = "";
		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmtm = null;
		String sql = "SELECT id, firstname, lastname, salutation, email FROM invitee ORDER BY id";

		String sqlm = "select distinct(invitee) from consent_response where invitee not in "
				+ "(select invitee from consent_response where answer='Y') and invitee= ?";

		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			stmtm = conn.prepareStatement(sqlm);

			ResultSet rs = stmt.executeQuery();
			outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
			outputString += "<tr>";
			outputString += "<th class=sfon></th>";
			outputString += "<th class=sfon>User ID</th>";
			outputString += "<th class=sfon>User Name</th>";
			outputString += "<th class=sfon>User's Email Address</th></tr>";

			while (rs.next()) {
				stmtm.clearParameters();
				stmtm.setInt(1, Integer.parseInt(rs.getString(1)));
				stmtm.execute(sqlm);
				ResultSet rsm = stmtm.getResultSet();
				if (rsm.next()) {
					outputString += "<tr bgcolor='#E4E4E4'>";
				} else {
					outputString += "<tr>";
				}
				outputString += "<td><input type='checkbox' name='user' value='" + rs.getString(1)
						+ "'></td>";
				outputString += "<td>" + rs.getString(1) + "</td>";
				outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " "
						+ rs.getString(3) + "</td>";
				outputString += "<td>" + rs.getString(5) + "</td>";
				outputString += "</tr>";
			}
			rs.close();
			outputString += "</table>";
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("ADMIN APPLICATION - PRINT INVITE: " + e.toString(), e);
		} catch (NumberFormatException e) {
			LOGGER.error("ADMIN APPLICATION - PRINT INVITE: " + e.toString(), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (stmtm != null) {
					stmtm.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("check why prepared statement creation failed", e);
			}
		}
		return outputString;
	}

	public String buildCsvString(String surveyName) {
		String surveyId = surveyName.substring(0, surveyName.indexOf("."));
		LOGGER.info("Building results string for survey '" + surveyId + "'");
		Table<Integer, String, String> table = HashBasedTable.create();

		for (int inviteeId : this.getInviteesWithData(surveyId)) {
			Map<String, String> inviteeAnswers = this.getMainDataForInvitee(surveyId, inviteeId, 0);
			for (Entry<String, String> entry : inviteeAnswers.entrySet()) {
				table.put(inviteeId, entry.getKey(), entry.getValue());
			}
		}
		return table.toString();
	}

	public Map<String, String> getMainDataForInvitee(String surveyId, int inviteeId,
			int questionLevel) {
		String sqlForText = "SELECT questionId,answer FROM (SELECT * FROM "
				+ this.wiseTables.getMainDataText() + " WHERE level=" + questionLevel
				+ " AND inviteeId=" + inviteeId + " AND survey='" + surveyId
				+ "' ORDER BY id DESC) AS x GROUP BY questionId";
		String sqlForInteger = "SELECT questionId,answer FROM (SELECT * FROM "
				+ this.wiseTables.getMainDataInteger() + " WHERE level=" + questionLevel
				+ " AND inviteeId=" + inviteeId + " AND survey='" + surveyId
				+ "' ORDER BY id DESC) AS x GROUP BY questionId";

		LOGGER.debug("SQL:" + sqlForText);
		LOGGER.debug("SQL:" + sqlForInteger);

		Map<String, String> answerMap = new HashMap<>();
		try {
			Connection connection = this.getDBConnection();
			PreparedStatement stmtForText = connection.prepareStatement(sqlForText);

			ResultSet rs = stmtForText.executeQuery();

			while (rs.next()) {
				String questionId = rs.getString(1);
				String answerId = rs.getString(2);
				answerMap.put(questionId, answerId);
			}

			PreparedStatement stmtForInteger = connection.prepareStatement(sqlForInteger);

			rs = stmtForInteger.executeQuery();

			while (rs.next()) {
				String questionId = rs.getString(1);
				String answerId = rs.getString(2);
				answerMap.put(questionId, answerId);
			}

		} catch (SQLException e) {
			LOGGER.error("Exception while getting invitee data: id '" + inviteeId + "'", e);
		}
		LOGGER.debug(answerMap);
		return answerMap;
	}

	/**
	 * 
	 * @return list of invitees for whom data is available
	 */
	public List<Integer> getInviteesWithData(String surveyName) {
		String sql = "SELECT DISTINCT inviteeId FROM " + this.studySpace.studyName + ".data_text"
				+ " INNER JOIN data_integer USING (inviteeId) WHERE data_text.survey='"
				+ surveyName + "' AND " + "data_integer.survey='" + surveyName + "'";
		List<Integer> inviteeList = new ArrayList<>();
		try {
			PreparedStatement stmt = this.getDBConnection().prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				inviteeList.add(rs.getInt(1));
			}

		} catch (SQLException e) {
			LOGGER.error("Could not get invitees with answers");
		}
		return inviteeList;
	}

	public String buildCsvStringOld(String filename) {

		/* get the data table name */
		String tname = filename.substring(0, filename.indexOf("."));
		Connection conn = null;
		Statement stmt = null;

		try {

			/* get database connection */
			conn = this.getDBConnection();
			stmt = conn.createStatement();
			String sql = "";
			sql = "describe " + tname;
			// log_error(sql);
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();
			String sqlm = "select ";
			String outputStr = "";
			String[] fieldName = new String[1000];
			String[] delimitor = new String[1000];
			int i = 0;
			while (rs.next()) {

				fieldName[i] = rs.getString("Field");
				outputStr += "\"" + fieldName[i] + "\",";
				sqlm += fieldName[i] + ",";
				if ((rs.getString("Type").indexOf("int") != -1)
						|| (rs.getString("Type").indexOf("decimal") != -1)) {
					delimitor[i] = "";
				} else {
					delimitor[i] = "\"";
				}
				i++;
			}

			outputStr = outputStr.substring(0, outputStr.length() - 1) + "\n";
			sqlm = sqlm.substring(0, sqlm.length() - 1) + " from " + tname;
			// log_error(sqlm);
			stmt.execute(sqlm);
			rs = stmt.getResultSet();

			while (rs.next()) {
				for (int j = 0; j < i; j++) {
					String field_value = rs.getString(fieldName[j]);
					if ((field_value == null) || field_value.equalsIgnoreCase("null")) {
						field_value = "";
					}
					if (field_value.indexOf("\"") != -1) {
						field_value = field_value.replaceAll("\"", "\"\"");
						LOGGER.info(field_value);
					}
					// if(field_value.equalsIgnoreCase(""))
					// delimitor[j] = "";
					outputStr += delimitor[j] + field_value + delimitor[j] + ",";
				}
				outputStr = outputStr.substring(0, outputStr.length() - 1) + "\n";
			}

			return outputStr;
		} catch (SQLException e) {
			LOGGER.error("ADMIN INFO - CREATE CSV FILE: " + e.toString(), e);
			LOGGER.error("Database Error while download invitee list ", e);
			return null;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("SQL connection closing failed", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("SQL connection closing failed", e);
				}
			}
		}

	}

	public String printInitialInviteeEditable(String surveyId) {
		String outputString = "";
		Connection conn = null;
		PreparedStatement stmt = null;

		/* select the invitees without any states */
		String sql = "SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email, '"
				+ this.getEmailEncryptionKey()
				+ "') FROM invitee WHERE id not in (select invitee from survey_user_state where survey= ?"
				+ ") ORDER BY id";
		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, surveyId);
			ResultSet rs = stmt.executeQuery();
			outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
			outputString += "<tr>";
			// outputString += "<th class=sfon></th>";
			outputString += "<th class=sfon>User ID</th>";
			outputString += "<th class=sfon>First name</th>";
			outputString += "<th class=sfon>Last name</th>";
			outputString += "<th class=sfon>IRB</th>";
			outputString += "<th class=sfon>User's Email Address</th>";
			outputString += "<th class=sfon>Action</th></tr>";

			while (rs.next()) {
				outputString += "<tr>";
				// outputString +=
				// "<td><input type='checkbox' name='user' value='"+rs.getString(1)+"'></td>";
				outputString += "<td>" + rs.getString(1) + "</td>";
				outputString += "<td><input type='text' name='fname" + rs.getString(1)
						+ "' value='" + rs.getString(2) + "'/></td>";
				outputString += "<td><input type='text' name='lname" + rs.getString(1)
						+ "' value='" + rs.getString(3) + "'/></td>";
				outputString += "<td><input type='text' name='irb" + rs.getString(1) + "' value='"
						+ rs.getString(5) + "'/></td>";
				outputString += "<td><input type='text' name='email" + rs.getString(1)
						+ "' value='" + rs.getString(6) + "'/></td>";
				outputString += "<td><a href='javascript:update_inv(" + rs.getString(1)
						+ ");'> Update </a><br>" + "<a href='javascript:delete_inv("
						+ rs.getString(1) + ");'> Delete </a>" + "</td>";
				outputString += "</tr>";
			}
			rs.close();
			outputString += "</table>";
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("ADMIN Data Bank - PRINT INITIAL INVITEE EDITABLE: " + e.toString(), e);
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("ADMIN Data Bank - PRINT INITIAL INVITEE EDITABLE: " + e.toString(), e);
			}
		}
		return outputString;

	}

	public boolean updateInvitees(String delFlag, String updateID, Map<String, String[]> request) {
		PreparedStatement stmt = null;
		Connection conn = null;
		try {

			if (SanityCheck.sanityCheck(updateID) || SanityCheck.sanityCheck(delFlag)) {
				return false;
			}
			conn = this.getDBConnection();
			if ((delFlag != null) && delFlag.equals("true") && (updateID != null)) {
				stmt = conn.prepareStatement("delete from invitee where id = " + updateID);
			} else if (updateID != null) {
				stmt = conn
						.prepareStatement("update invitee set firstname=?, lastname=?, irb_id=?, email="
								+ "AES_ENCRYPT(?,'"
								+ this.getEmailEncryptionKey()
								+ "')"
								+ " where id=?");
				String irbid = request.get("irb" + updateID)[0];
				String firstName = request.get("fname" + updateID)[0];
				String lastName = request.get("lname" + updateID)[0];
				String emailId = request.get("email" + updateID)[0];

				if (SanityCheck.sanityCheck(emailId) || SanityCheck.sanityCheck(firstName)
						|| SanityCheck.sanityCheck(lastName) || SanityCheck.sanityCheck(irbid)) {
					return false;
				}
				if (irbid.equals("") || irbid.equalsIgnoreCase("null")) {
					irbid = null;
				}
				stmt.setString(1, firstName);
				stmt.setString(2, lastName);
				stmt.setString(3, irbid);
				stmt.setString(4, emailId);
				stmt.setString(5, updateID);
			}
			if (stmt != null) {
				stmt.execute();
			}

		} catch (SQLException e) {
			LOGGER.error("Deleting/Updating the invitee failed.", e);
			LOGGER.error("ADMIN INFO - UPDATE INVITEE: " + e.toString(), e);
			return false;
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("check why prepared statement creation failed", e);
			}

		}
		return true;

	}

	public String printInterviewer() {
		String outputString = "";
		String sql = "SELECT id, firstname, lastname, salutation, email FROM interviewer ORDER BY id";
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			ResultSet rs = stmt.executeQuery();
			outputString += "<table class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
			outputString += "<tr>";
			outputString += "<th class=sfon></th>";
			outputString += "<th class=sfon>Interviewer ID</th>";
			outputString += "<th class=sfon>Interviewer Name</th>";
			outputString += "<th class=sfon>Interviewer's Email Address</th>";
			outputString += "<th class=sfon>Go to WATI</th></tr>";

			while (rs.next()) {
				outputString += "<tr>";
				outputString += "<td><input type='radio' name='interviewer' value='"
						+ rs.getString(1) + "'></td>";
				outputString += "<td align=center>" + rs.getString(1) + "</td>";
				outputString += "<td align=center>" + rs.getString(4) + " " + rs.getString(2) + " "
						+ rs.getString(3) + "</td>";
				outputString += "<td>" + rs.getString(5) + "</td>";
				outputString += "<td align=center><a href='goto_wati.jsp?interview_id="
						+ rs.getString(1)
						+ "'><img src='admin_images/go_view.gif' border=0></a></td>";
				outputString += "</tr>";
			}
			rs.close();
			outputString += "</table>";
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("ADMIN INFO - PRINT INTERVIEWER LIST:" + e.toString(), e);
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("check why prepared statement creation failed", e);
			}
		}
		return outputString;

	}

	public void getNonrespondersIncompters(String[] spId, String sId) {
		Connection conn = null;
		PreparedStatement stmt1 = null;
		PreparedStatement stmt2 = null;

		String sql1 = "select distinct(s.invitee) from survey_message_use as s, invitee as i where s.survey='"
				+ sId
				+ "' and s.invitee=i.id "
				+ "and s.invitee not in (select invitee from consent_response where answer='N') "
				+ "and not exists (select u.invitee from "
				+ sId
				+ "_data as u where u.invitee=s.invitee) "
				+ "group by s.invitee order by s.invitee";

		String sql2 = "select distinct(invitee) from "
				+ sId
				+ "_data as s, invitee as i where s.invitee=i.id and status IS NOT NULL order by invitee";

		try {
			String nonresponderId = " ";
			String incompleterId = " ";

			/* connect to the database */
			conn = this.getDBConnection();
			stmt1 = conn.prepareStatement(sql1);
			stmt2 = conn.prepareStatement(sql2);

			/* get the non-responders user ID list */
			ResultSet rs1 = stmt1.executeQuery();
			while (rs1.next()) {
				nonresponderId += rs1.getString(1) + " ";
			}

			/* get the incompleters user ID list */
			ResultSet rs2 = stmt2.executeQuery();
			while (rs2.next()) {
				incompleterId += rs2.getString(1) + " ";
			}
			spId[0] = nonresponderId;
			spId[1] = incompleterId;

		} catch (SQLException e) {
			LOGGER.error("ADMIN APPLICATION - GET NONRESPONDERS INCOMPLETERS: " + e.toString(), e);
		} finally {
			try {
				if (stmt1 != null) {
					stmt1.close();
				}
				if (stmt2 != null) {
					stmt2.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("check why prepared statement creation failed", e);
			}
		}

	}

	public void registerCompletionInDB(String user, String surveyID) {
		PreparedStatement stmt = null;
		Connection conn = null;
		try {
			String sql = "update survey_user_state set state='completed', state_count=1, entry_time=now()"
					+ " where invitee= ? AND survey= ?";
			conn = this.getDBConnection();
			stmt = conn.prepareStatement(sql);
			int userId = Integer.parseInt(user);
			stmt.setInt(1, userId);
			stmt.setString(2, surveyID);
			stmt.executeUpdate();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public void changeDevToProd(String internalId) {
		try {

			/* open database connection */
			Connection conn = this.getDBConnection();
			String sql = "SELECT id, filename, title FROM surveys WHERE internal_id = ?";

			PreparedStatement stmt1 = conn.prepareStatement(sql);
			stmt1.setInt(1, Integer.parseInt(internalId));
			ResultSet rs = stmt1.executeQuery();
			rs.next();
			String sId = rs.getString(1);
			String fileName = rs.getString(2);
			String title = rs.getString(3);

			sql = "INSERT INTO surveys (id, filename, title, status) ";
			sql += "VALUES ('" + sId + "','" + fileName + "',\"" + title + "\", 'P')";
			sql = "INSERT INTO surveys (id, filename, title, status) " + "VALUES(?, ?, ?, ?)";

			PreparedStatement stmt2 = conn.prepareStatement(sql);
			stmt2.setString(1, sId);
			stmt2.setString(2, fileName);
			stmt2.setString(3, title);
			stmt2.setString(4, "P");

			stmt2.executeUpdate();

			stmt1.close();
			stmt2.close();
			conn.close();
		} catch (NumberFormatException e) {
			LOGGER.error("Wise Admin - Dev to Prod Error: " + e.toString(), e);
			return;
		} catch (SQLException e) {
			LOGGER.error("Wise Admin - Dev to Prod Error: " + e.toString(), e);
			return;
		}
	}

	public void saveFileToDatabase(MultipartRequest multi, String filename, String tableName,
			String studySpaceName) {
		Connection conn = null;
		PreparedStatement psmnt = null;
		FileInputStream fis = null;
		try {
			/* open database connection */
			conn = this.getDBConnection();

			File f = multi.getFile("file");
			psmnt = conn.prepareStatement("DELETE FROM " + studySpaceName + "." + tableName
					+ " where filename =" + "'" + filename + "'");
			psmnt.executeUpdate();
			psmnt = conn.prepareStatement("INSERT INTO " + studySpaceName + "." + tableName
					+ "(filename,filecontents,upload_date)" + "VALUES (?,?,?)");
			psmnt.setString(1, filename);
			fis = new FileInputStream(f);
			psmnt.setBinaryStream(2, fis, (int) (f.length()));
			java.util.Date currentDate = new java.util.Date();
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDateString = sdf.format(currentDate);
			psmnt.setString(3, currentDateString);
			psmnt.executeUpdate();
			psmnt.close();

		} catch (SQLException e) {
			LOGGER.error("Could not save the file to the database", e);
		} catch (FileNotFoundException e) {
			LOGGER.error("Could not find the file to save", e);
		} finally {
			try {
				psmnt.close();
				conn.close();
			} catch (SQLException e) {
				LOGGER.error("Could not close connection", e);
			}

		}

	}

	public String processSurveyFile(Document doc) {

		StringBuilder out = new StringBuilder();

		NodeList nodeList;
		Node n, nodeOne;
		NamedNodeMap nnm;

		String id, title;
		String sql;
		String returnVal;

		try {

			Connection con = this.getDBConnection();
			Statement stmt = con.createStatement();

			/* parsing the survey node */
			nodeList = doc.getElementsByTagName("Survey");
			n = nodeList.item(0);
			nnm = n.getAttributes();

			/* get the survey attributes */
			id = nnm.getNamedItem("ID").getNodeValue();
			title = nnm.getNamedItem("Title").getNodeValue();
			nodeOne = nnm.getNamedItem("Version");
			if (nodeOne != null) {
				title = title + " (v" + nodeOne.getNodeValue() + ")";
			}

			/* get the latest survey's internal ID from the table of surveys */
			sql = "select max(internal_id) from surveys where id = '" + id + "'";
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();
			rs.next();
			String maxId = rs.getString(1);

			/* initiate the survey status as "N" */
			String status = "N";

			/* display processing information */
			out.append("<table border=0><tr><td align=center>Processing a SURVEY (ID = " + id
					+ ")</td></tr>");

			/* get the latest survey's status */
			if (maxId != null) {
				sql = "select status from surveys where internal_id = " + maxId;
				stmt.execute(sql);
				rs = stmt.getResultSet();
				rs.next();
				status = (rs.getString(1)).toUpperCase();
			}

			/*
			 * If the survey status is in Developing or Production mode NOTE
			 * this just sets up survey info in surveys table; actual read of
			 * survey is handled by the Surveyor application.
			 */
			if (status.equalsIgnoreCase("D") || status.equalsIgnoreCase("P")) {

				/* display the processing situation about the status */
				out.append("<tr><td align=center>Existing survey is in " + status
						+ " mode. </td></tr>");

				/* insert a new survey record */
				sql = "INSERT INTO surveys (id, title, status, archive_date) VALUES ('" + id
						+ "',\"" + title + "\", '" + status + "', 'current')";
				stmt.execute(sql);

				/* get the new inserted internal ID */
				sql = "SELECT max(internal_id) from surveys";
				stmt.execute(sql);
				rs = stmt.getResultSet();
				rs.next();
				String newId = rs.getString(1);

				/* use the newly created internal ID to name the file */
				String fileName = "file" + newId + ".xml";

				/* update the file name and uploading time in the table */
				sql = "UPDATE surveys SET filename = '" + fileName
						+ "', uploaded = now() WHERE internal_id = " + newId;
				stmt.execute(sql);

				/* display the processing information about the file name */
				out.append("<tr><td align=center>New version becomes the one with internal ID = "
						+ id + "</td></tr>");
				out.append("</table>");
				returnVal = fileName;
			} else if (status.equalsIgnoreCase("N") || status.equalsIgnoreCase("R")
					|| status.equalsIgnoreCase("C")) {

				/*
				 * If the survey status is in Removed or Closed mode. Or there
				 * is no such survey (keep the default status as N) the survey
				 * will be treated as a brand new survey with the default
				 * Developing status
				 */
				out.append("<tr><td align=center>This is a NEW Survey.  Adding a new survey into DEVELOPMENT mode...</td></tr>");

				/* insert the new survey record */
				sql = "INSERT INTO surveys (id, title, status, archive_date) VALUES ('" + id
						+ "',\"" + title + "\",'D','current')";
				stmt.execute(sql);

				/* get the newly created internal ID */
				sql = "SELECT max(internal_id) from surveys";
				stmt.execute(sql);
				rs = stmt.getResultSet();
				rs.next();
				String newId = rs.getString(1);
				String filename = "file" + newId + ".xml";

				/* update the file name and uploading time */
				sql = "UPDATE surveys SET filename = '" + filename
						+ "', uploaded = now() WHERE internal_id = " + newId;
				stmt.execute(sql);
				out.append("<tr><td align=center>New version becomes the one with internal ID = "
						+ id + "</td></tr>");
				out.append("</table>");
				returnVal = filename;
				rs.close();
			} else {
				out.append("<tr><td align=center>ERROR!  Unknown STATUS!</td></tr>");
				out.append("<tr><td align=center>status:" + status + "</td></tr>");
				out.append("</table>");
				returnVal = "NONE";
			}

		} catch (SQLException e) {
			LOGGER.error("WISE ADMIN - PROCESS SURVEY FILE:" + e.toString(), e);
			returnVal = "ERROR";
		}
		return returnVal;

	}

	public void processInviteesCsvFile(File f) {

		StringBuilder out = new StringBuilder();
		// TODO: Currently, ID column should be deleted from the csv file to
		// Handle
		// Adding Invitees. In future, we want to make sure, that if ID column
		// exists in
		// the csv file then it should be automatically handled up update if
		// exists

		/* Storing the fields that are not encoded into the HashSet. */
		HashSet<String> nonEncodedFieldSet = new HashSet<String>();
		nonEncodedFieldSet.add("firstname");
		nonEncodedFieldSet.add("lastname");
		nonEncodedFieldSet.add("salutation");
		nonEncodedFieldSet.add("phone");
		nonEncodedFieldSet.add("irb_id");

		HashSet<Integer> nonEncodedFieldPositions = new HashSet<Integer>();

		String[] colVal = new String[1000];
		BufferedReader br = null;

		try {

			Connection con = this.getDBConnection();
			Statement stmt = con.createStatement();

			String sql = "insert into invitee(";
			FileReader fr = new FileReader(f);
			br = new BufferedReader(fr);
			String line = new String();

			int colNumb = 0, lineCount = 0;
			while (!Strings.isNullOrEmpty(line = br.readLine())) {
				line = line.trim();
				if (line.length() != 0) {
					lineCount++;
					ArrayList<String> columns = new ArrayList<String>(
							Arrays.asList(line.split(",")));

					/*
					 * first row indicates the number of columns in the invitees
					 * csv.
					 */
					if (lineCount == 1) {
						colNumb = columns.size();
					} else {
						if (columns.size() < colNumb) {
							while ((colNumb - columns.size()) != 0) {
								columns.add("");
							}
						}
					}

					/* assign the column values */
					for (int i = 0, j = 0; i < columns.size(); i++, j++) {
						colVal[j] = columns.get(i);

						/*
						 * mark as the null string if the phrase is an empty
						 * string
						 */
						if ((columns.size() == 0) || columns.get(i).equals("")) {
							colVal[j] = "NULL";
						} else if ((columns.get(i).charAt(0) == '\"')
								&& (columns.get(i).charAt(columns.get(i).length() - 1) != '\"')) {
							/*
							 * parse the phrase with the comma inside (has the
							 * double-quotation mark) this string is just part
							 * of the entire string, so append with the next one
							 */
							do {
								i++;
								colVal[j] += "," + columns.get(i);
							} while ((i < columns.size())
									&& (columns.get(i).charAt(columns.get(i).length() - 1) != '\"'));

							/*
							 * remove the double-quotation mark at the beginning
							 * and end of the string
							 */
							colVal[j] = colVal[j].substring(1, colVal[j].length() - 1);
						} else if ((columns.get(i).charAt(0) == '\"')
								&& (columns.get(i).charAt(columns.get(i).length() - 1) == '\"')) {

							/*
							 * there could be double-quotation mark(s) (doubled
							 * by csv format) inside this string keep only one
							 * double-quotation mark(s)
							 */
							if (columns.get(i).indexOf("\"\"") != -1) {
								colVal[j] = colVal[j].replaceAll("\"\"", "\"");
							}
						}

						/*
						 * keep only one double-quotation mark(s) if there is
						 * any inside the string
						 */
						if (columns.get(i).indexOf("\"\"") != -1) {
							colVal[j] = colVal[j].replaceAll("\"\"", "\"");
						}

						/* compose the sql query with the column values */
						if ((lineCount == 1) || colVal[j].equalsIgnoreCase("null")) {
							if (nonEncodedFieldSet.contains(colVal[j].toLowerCase())) {
								nonEncodedFieldPositions.add(j);
							}
							sql += colVal[j] + ",";
						} else {
							if (!nonEncodedFieldPositions.contains(j)) {
								colVal[j] = "AES_ENCRYPT('" + colVal[j] + "','"
										+ this.getEmailEncryptionKey() + "')";
								sql += colVal[j] + ",";
							} else {
								sql += "\"" + colVal[j] + "\",";
							}
						}
					}
				}

				/* compose the sql query */
				if (lineCount == 1) {
					sql = sql.substring(0, sql.length() - 1) + ") values (";
				} else {
					sql = sql.substring(0, sql.length() - 1) + "),(";
				}
			}

			/* delete the last "," and "(" */
			sql = sql.substring(0, sql.length() - 2);
			LOGGER.info("The Sql Executed is" + sql);

			/* insert into the database */
			stmt.execute(sql);
			out.append("The data has been successfully uploaded and input into database");
		} catch (FileNotFoundException err) {

			/* catch possible file not found errors from FileReader() */
			LOGGER.error("CVS parsing: FileNotFoundException error!");
			err.printStackTrace();
		} catch (IOException err) {
			/* catch possible io errors from readLine() */
			LOGGER.error("CVS parsing: IOException error!");
			err.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void archiveOldAndCreateNewDataTable(Survey survey, String surveyID) {
		String sql = "DELETE FROM interview_assignment WHERE survey = ?";

		StringBuilder out = new StringBuilder();
		try {

			/* connect to the database */
			Connection conn = this.getDBConnection();
			PreparedStatement stmt = conn.prepareStatement(sql);

			/* create data table - archive old data - copy old data */
			out.append("<tr><td align=center>Creating new data table.<td></tr>");
			this.setupSurvey(survey);

			/* remove the interview records from table - interview_assignment */
			out.append("<tr><td align=center>Deleting data from tables "
					+ "of interview_assignment and interview_session.</td><tr>");
			stmt.setString(1, surveyID);
			stmt.executeUpdate();

			out.append("</table>");
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("WISE - SURVEY LOADER: " + e.toString(), null);
			out.append("<tr><td align=center>survey loader Error: " + e.toString() + "</td></tr>");
		}
	}

	public String getNewId() {
		String id = null;
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = this.getDBConnection();
			String sql = "SELECT MAX(id) from interviewer";
			statement = conn.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				id = Integer.toString(rs.getInt(1) + 1);
			}
		} catch (SQLException e) {
			LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
			LOGGER.error("SQL Error getting new ID", e);
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
		}
		return id;
	}

	public String addInterviewer(Interviewer interviewer) {
		Connection conn = null;
		PreparedStatement statement = null;
		PreparedStatement statement1 = null;
		ResultSet rs = null;
		String sql = null;
		String returnId = null;

		try {
			conn = this.getDBConnection();

			sql = "insert into interviewer(username, firstname, lastname, salutation, email)"
					+ " values(?,?,?,?,?)";
			statement = conn.prepareStatement(sql);

			statement.setString(1, interviewer.getUserName());
			statement.setString(2, interviewer.getFirstName());
			statement.setString(3, interviewer.getLastName());
			statement.setString(4, interviewer.getSalutation());
			statement.setString(5, interviewer.getEmail());

			statement.executeUpdate();

			/*
			 * Now get the ID of the last inserted value, this needs the method
			 * to be synchronized.
			 */
			sql = "SELECT LAST_INSERT_ID() from interviewer";
			statement1 = conn.prepareStatement(sql);

			rs = statement1.executeQuery();
			if ((rs != null) && rs.next()) {
				returnId = rs.getString(1);
			}
		} catch (SQLException e) {
			LOGGER.error("Add interviewer ID:" + e.toString(), e);
			LOGGER.error("SQL Error adding new ID", e);
			return null;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (statement1 != null) {
				try {
					statement1.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
		}
		return returnId;
	}

	public boolean removeInterviewer(String interviewers) {
		Connection conn = null;
		PreparedStatement statement = null;
		PreparedStatement statement1 = null;
		String sql = null;

		try {
			conn = this.getDBConnection();

			sql = "delete from interviewer where id in " + interviewers;
			statement = conn.prepareStatement(sql);

			boolean result = statement.execute(sql);
			return result;

		} catch (SQLException e) {
			LOGGER.error("SQL Error removing interviewer", e);
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (statement1 != null) {
				try {
					statement1.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
		}
	}

	public String saveProfile(Interviewer interviewer) {

		Connection conn = null;
		PreparedStatement statement = null;
		String sql = null;

		try {
			conn = this.getDBConnection();

			sql = "UPDATE interviewer SET username=" + "? , firstname=" + "? , lastname="
					+ "? , salutation=" + "? , email=" + "? WHERE id = ?";

			statement = conn.prepareStatement(sql);
			statement.setString(1, interviewer.getUserName());
			statement.setString(2, interviewer.getFirstName());
			statement.setString(3, interviewer.getLastName());
			statement.setString(4, interviewer.getSalutation());
			statement.setString(5, interviewer.getEmail());
			statement.setInt(6, Integer.valueOf(interviewer.getId()));

			statement.executeUpdate();

		} catch (NumberFormatException e) {
			LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
			LOGGER.error("SQL Error updating new ID", e);
			return null;
		} catch (SQLException e) {
			LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
			LOGGER.error("SQL Error updating new ID", e);
			return null;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
		}
		return interviewer.getId();

	}

	public Interviewer getInterviewer(String interviewId) {
		Interviewer interviewer = null;
		Connection conn = null;
		PreparedStatement statement = null;
		String sql;

		try {
			conn = this.getDBConnection();

			sql = "select id, username, firstname, lastname, salutation, email, submittime from interviewer where id="
					+ "?";
			statement = conn.prepareStatement(sql);

			statement.setInt(1, Integer.valueOf(interviewId));
			ResultSet rs = statement.executeQuery();

			if (rs.wasNull()) {
				return null;
			}
			if (rs.next()) {
				String id = rs.getString("id");
				String username = rs.getString("username");
				String firstName = rs.getString("firstname");
				String lastName = rs.getString("lastname");
				String salutation = rs.getString("salutation");
				String email = rs.getString("email");
				String loginTime = rs.getString("submittime");
				interviewer = new Interviewer(this.studySpace, id, username, email, firstName,
						lastName, salutation, loginTime);
			}

		} catch (NumberFormatException e) {
			LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
			LOGGER.error("SQL Error updating new ID", e);
			return null;
		} catch (SQLException e) {
			LOGGER.error("GET NEW INTERVIEWER ID:" + e.toString(), e);
			LOGGER.error("SQL Error getting new ID", e);
			return null;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error("SQL Statement failure", e);
				}
			}
		}
		return interviewer;
	}

	public Interviewer verifyInterviewer(String interviewId, String interviewUsername) {
		boolean getResult = false;
		Interviewer interviewer = null;
		try {

			/* connect to the database */
			Connection conn = this.getDBConnection();
			Statement statement = conn.createStatement();
			Statement statement_1 = conn.createStatement();

			/* check if the record exists in the table of interviewer */
			String sql = "select firstname, lastname, salutation, email, submittime from interviewer where id='"
					+ interviewId + "' and username='" + interviewUsername + "'";
			statement.execute(sql);
			ResultSet rs = statement.getResultSet();

			/* if the interviewer exists in the current database */
			if (rs.next()) {

				/* update the login time */
				String sql_1 = "update interviewer set submittime=now() where id='" + interviewId
						+ "'";
				statement_1.execute(sql_1);

				/* assign the attributes */
				sql_1 = "select firstname, lastname, salutation, email, submittime from interviewer where id='"
						+ interviewId + "'";
				statement_1.execute(sql_1);
				ResultSet rs_1 = statement_1.getResultSet();
				if (rs_1.next()) {
					String firstName = rs.getString("firstname");
					String lastName = rs.getString("lastname");
					String salutation = rs.getString("salutation");
					String email = rs.getString("email");
					String loginTime = rs.getString("submittime");
					getResult = true;
					interviewer = new Interviewer(this.studySpace, interviewId, interviewUsername,
							email, firstName, lastName, salutation, loginTime);
				}
				rs_1.close();
				statement_1.close();
			}
			rs.close();
			statement.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("INTERVIEWER - VERIFY INTERVIEWER:" + e.toString(), null);
			getResult = false;
		}
		return interviewer;
	}

	public String createSurveyMessage(String inviteeId, String surveyId) {
		String surveyMsgId = null;
		try {

			/* connect to the database */
			Connection conn = this.getDBConnection();
			Statement statement = conn.createStatement();
			String messageId = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(22);

			/* insert an interview record */
			String sql = "INSERT INTO survey_message_use (invitee, survey, message, sent_date) "
					+ " values ('" + messageId + "','" + inviteeId + "','" + surveyId
					+ "','interview', now())";
			statement.execute(sql);
			surveyMsgId = messageId;

			statement.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("INTERVIEW - CREATE SURVEY MESSAGE:" + e.toString(), null);
		}
		return surveyMsgId;
	}

	public void beginInterviewSession(String userSession) {

		/*
		 * the interview_assign_id is a foreign key reference to the interviewer
		 * assignment id which value has been assigned in the
		 * Begin_Interview.jsp
		 */
		try {

			/* connect to the database */
			Connection conn = this.getDBConnection();
			Statement statement = conn.createStatement();

			/* insert a session record */
			String sql = "INSERT INTO interview_session (session_id, assign_id) VALUES ('"
					+ userSession + "','" + userSession + "')";
			statement.execute(sql);
			statement.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("INTERVIEW - BEGIN SESSION:" + e.toString(), null);
		}
	}

	public void saveInterviewSesssion(String interviewAssignId) {
		try {

			/* connect to the database */
			Connection conn = this.getDBConnection();
			Statement statement = conn.createStatement();
			String sql = "UPDATE interview_assignment SET close_date = now(), pending=0 WHERE id = "
					+ interviewAssignId;
			statement.execute(sql);
			statement.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("INTERVIEW - SET DONE:" + e.toString(), null);
		}
	}

	public int getPageDoneNumb(Survey survey, Page page, String whereClause) {
		if (page.getAllFieldNames().length > 0) {
			int doneNumb = 0;
			try {

				/* connect to the database */
				Connection conn = this.getDBConnection();
				Statement stmt = conn.createStatement();

				/* count the total number of users who have done this page */
				String sql = "select count(*) from " + survey.getId()
						+ "_data where status not in(";
				for (int k = 0; k < survey.getPages().length; k++) {
					if (!page.getId().equalsIgnoreCase(survey.getPages()[k].getId())) {
						sql += "'" + survey.getPages()[k].getId() + "', ";
					} else {
						break;
					}
				}
				sql += "'" + page.getId() + "') or status is null";
				if (!whereClause.equalsIgnoreCase("")) {
					sql += " and " + whereClause;
				}
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				if (rs.next()) {
					doneNumb = rs.getInt(1);
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				LOGGER.error("WISE - GET PAGE DONE NUMBER: " + e.toString(), null);
			}
			return doneNumb;
		} else {
			return 0;
		}
	}

	public String renderQuestionBlockResults(Page pg, QuestionBlockforSubjectSet qb, DataBank db,
			String whereclause, Hashtable data) {
		int levels = Integer.valueOf(qb.responseSet.levels).intValue();
		int startValue = Integer.valueOf(qb.responseSet.startvalue).intValue();

		String s = qb.renderQBResultHeader();

		/* display each of the subjectSetLabels on the left side of the block */
		for (int i = 0; i < qb.subjectSetLabels.length; i++) {
			s += "<tr>";
			int tnull = 0;
			int t = 0;
			float avg = 0;
			Hashtable<String, String> h1 = new Hashtable<String, String>();

			/* get the user's conducted data from the hashtable */
			String subjAns = (String) data.get(qb.stemFieldNames[i].toUpperCase());

			String t1, t2;
			try {

				/* connect to the database */
				Connection conn = this.getDBConnection();
				Statement stmt = conn.createStatement();

				/* if the question block doesn't have the subject set ref */
				String sql = "";

				/* get the user's data from the table of subject set */
				String userId = (String) data.get("invitee");
				if ((userId != null) && !userId.equalsIgnoreCase("")) {
					sql = "select "
							+ qb.name
							+ " from "
							+ pg.getSurvey().getId()
							+ "_"
							+ qb.getSubjectSetName()
							+ "_data"
							+ " where subject="
							+ qb.stemFieldNames[i]
									.substring((qb.stemFieldNames[i].lastIndexOf("_") + 1))
							+ " and invitee=" + userId;
					stmt.execute(sql);
					ResultSet rs = stmt.getResultSet();
					if (rs.next()) {
						subjAns = rs.getString(1);
					}
				}

				/*
				 * get values from the subject data table count total number of
				 * the users who have the same answer level
				 */
				sql = "select " + qb.name + ", count(*) from " + pg.getSurvey().getId() + "_"
						+ qb.getSubjectSetName() + "_data as s, page_submit as p";
				sql += " where s.invitee=p.invitee and p.survey='" + pg.getSurvey().getId() + "'";
				sql += " and p.page='" + pg.getId() + "'";
				sql += " and s.subject="
						+ qb.stemFieldNames[i]
								.substring((qb.stemFieldNames[i].lastIndexOf("_") + 1));
				if (!whereclause.equalsIgnoreCase("")) {
					sql += " and s." + whereclause;
				}
				sql += " group by " + qb.name;
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				h1.clear();
				String s1, s2;

				while (rs.next()) {
					if (rs.getString(1) == null) {
						tnull = tnull + rs.getInt(2);
					} else {
						s1 = rs.getString(1);
						s2 = rs.getString(2);
						h1.put(s1, s2);
						t = t + rs.getInt(2);
					}
				}
				rs.close();

				if (subjAns == null) {
					subjAns = "null";
				}

				/* if the question block doesn't have the subject set ref */
				if (!qb.hasSubjectSetRef) {

					/*
					 * get values from the subject data table calculate the
					 * average answer level
					 */
					sql = "select round(avg(" + qb.stemFieldNames[i] + "),1) from "
							+ pg.getSurvey().getId() + "_data as s, page_submit as p"
							+ " where s.invitee=p.invitee and p.page='" + pg.getId()
							+ "' and p.survey='" + pg.getSurvey().getId() + "'";
					if (!whereclause.equalsIgnoreCase("")) {
						sql += " and s." + whereclause;
					}
				} else {

					/*
					 * if the question block has the subject set ref get values
					 * from the subject data table calculate the average answer
					 * level
					 */
					sql = "select round(avg(" + qb.name + "),1) from " + pg.getSurvey().getId()
							+ "_" + qb.getSubjectSetName() + "_data as s, page_submit as p";
					sql += " where s.invitee=p.invitee and p.survey='" + pg.getSurvey().getId()
							+ "'";
					sql += " and p.page='" + pg.getId() + "'";
					sql += " and s.subject="
							+ qb.stemFieldNames[i]
									.substring((qb.stemFieldNames[i].lastIndexOf("_") + 1));
					if (!whereclause.equalsIgnoreCase("")) {
						sql += " and s." + whereclause;
					}
				}
				stmt.execute(sql);
				rs = stmt.getResultSet();
				if (rs.next()) {
					avg = rs.getFloat(1);
				}
				rs.close();

				stmt.close();
				conn.close();
			} catch (SQLException e) {
				LOGGER.error("WISE - QUESTION BLOCK RENDER RESULTS: " + e.toString(), e);
				return "";
			}

			/* display the statistic results */
			String s1;

			/* if classified level is required for the question block */
			if (levels == 0) {
				s += "<td bgcolor=#FFCC99>";
				s += qb.subjectSetLabels[i] + "<p>";
				s += "<div align='right'>";
				s += "<font size='-2'><b><font color=green>mean: </font></b>" + avg;
				if (tnull > 0) {
					s += "&nbsp;<b><font color=green>unanswered: </font></b>";

					/*
					 * if the user's answer is null, highlight the answer note
					 * that if the call came from admin page, this value is
					 * always highlighted because the user's data is always to
					 * be null
					 */
					if (subjAns.equalsIgnoreCase("null")) {
						s += "<span style=\"background-color: '#FFFF77'\">" + tnull + "</span>";
					} else {
						s += tnull;
					}
				}

				s += "</font></div>";
				s += "</td>";

				for (int j = 0; j < qb.responseSet.responses.size(); j++) {
					t2 = String.valueOf(j + startValue);
					if (j < qb.responseSet.responses.size()) {
						t1 = qb.responseSet.responses.get(j);
					}
					int num1 = 0;
					int p = 0;
					int p1 = 0;
					float af = 0;
					float bf = 0;
					float cf = 0;
					String ps, ps1;
					s1 = h1.get(t2);
					if (s1 == null) {
						ps = "0";
						ps1 = "0";
					} else {
						num1 = Integer.parseInt(s1);
						af = (float) num1 / (float) t;
						bf = af * 50;
						cf = af * 100;
						p = Math.round(bf);
						p1 = Math.round(cf);
						ps = String.valueOf(p);
						ps1 = String.valueOf(p1);
					}

					/*
					 * if the user's answer belongs to this answer level,
					 * highlight the image
					 */
					if (subjAns.equalsIgnoreCase(t2)) {
						s += "<td bgcolor='#FFFF77'>";
					} else {
						s += "<td>";
					}
					s += "<center>";
					s += "<img src='" + "imgs/vertical/bar_" + ps + ".gif' ";
					s += "width='10' height='50'>";
					s += "<br><font size='-2'>" + ps1 + "</font>";
					s += "</center>";
					s += "</td>";
				}
			} else {

				/* if classified level is required for the question block */
				s += "<td bgcolor=#FFCC99>";
				s += qb.subjectSetLabels[i] + "<p>";
				s += "<div align='right'>";
				s += "<font size='-2'><b><font color=green>mean: </font></b>" + avg;

				if (tnull > 0) {
					s += "&nbsp;<b><font color=green>unanswered: </font></b>";

					/*
					 * if the user's answer is null, highlight the answer note
					 * that if the call came from admin page, this value is
					 * always highlighted because the user's data is always to
					 * be null
					 */
					if (subjAns.equalsIgnoreCase("null")) {
						s += "<span style=\"background-color: '#FFFF77'\">" + tnull + "</span>";
					} else {
						s += tnull;
					}
				}

				s += "</font></div>";
				s += "</td>";
				// int step = Math.round((levels - 1)
				// / (responseSet.responses.size() - 1));
				for (int j = 0; j < levels; j++) {

					// t2 = String.valueOf(j);
					t2 = String.valueOf(j + startValue);
					if (j < qb.responseSet.responses.size()) {
						t1 = qb.responseSet.responses.get(j);
					}
					int num1 = 0;
					int p = 0;
					int p1 = 0;
					float af = 0;
					float bf = 0;
					float cf = 0;
					String ps, ps1;
					s1 = h1.get(t2);
					if (s1 == null) {
						ps = "0";
						ps1 = "0";
					} else {
						num1 = Integer.parseInt(s1);
						af = (float) num1 / (float) t;
						bf = af * 50;
						cf = af * 100;
						p = Math.round(bf);
						p1 = Math.round(cf);
						ps = String.valueOf(p);
						ps1 = String.valueOf(p1);
					}

					/*
					 * if the user's answer belongs to this answer level,
					 * highlight the image
					 */
					if (subjAns.equalsIgnoreCase(t2)) {
						s += "<td bgcolor='#FFFF77'>";
					} else {
						s += "<td>";
					}
					s += "<center>";
					s += "<img src='" + "imgs/vertical/bar_" + ps + ".gif' ";
					s += "width='10' height='50'>";
					s += "<br><font size='-2'>" + ps1 + "</font>";
					s += "</center>";
					s += "</td>";
				}
			}
		}

		s += "</table></center>";
		return s;
	}

	public String renderResultsForQuestionBlock(Page pg, QuestionBlock questionBlock, DataBank db,
			String whereclause, Hashtable data) {
		int levels = Integer.valueOf(questionBlock.responseSet.levels).intValue();
		int startValue = Integer.valueOf(questionBlock.responseSet.startvalue).intValue();

		/* display the ID of the question */
		String s = "<center><table width=100%><tr><td align=right>";
		s += "<span class='itemID'>" + questionBlock.name + "</span></td></tr></table><br>";

		/* display the question block */
		s += "<table cellspacing='0' cellpadding='1' bgcolor=#FFFFF5 width=600 border='1'>";
		s += "<tr><td bgcolor=#BA5D5D rowspan=2 width='60%'>";
		s += "<table><tr><td width='95%'>";

		/* display the instruction if it has */
		if (!questionBlock.instructions.equalsIgnoreCase("NONE")) {
			s += "<b>" + questionBlock.instructions + "</b>";
		} else {
			s += "&nbsp;";
		}

		s += "</td><td width='5%'>&nbsp;</td></tr></table></td>";
		String t1, t2;

		/* display the level based on the size of the question block */
		if (levels == 0) {
			s += "<td colspan=" + questionBlock.responseSet.responses.size() + " width='40%'>";
			s += "<table bgcolor=#FFCC99 width=100% cellpadding='1' border='0'>";

			for (int j = 0; j < questionBlock.responseSet.responses.size(); j++) {
				t2 = String.valueOf(j + startValue);
				t1 = questionBlock.responseSet.responses.get(j);
				s += "<tr>";

				if (j == 0) {
					s += "<td align=left>";
				} else if ((j + 1) == questionBlock.responseSet.responses.size()) {
					s += "<td align=right>";
				} else {
					s += "<td align=center>";
				}
				s += t2 + ". " + t1 + "</td>";
				s += "</tr>";
			}
			s += "</table>";
			s += "</td>";
			s += "</tr>";
			int width = 40 / questionBlock.responseSet.responses.size();
			for (int j = 0; j < questionBlock.responseSet.responses.size(); j++) {
				t2 = String.valueOf(j + startValue);
				s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>" + t2
						+ "</center></b></td>";
			}
		} else {

			/* display the classified level */
			s += "<td colspan=" + levels + " width='40%'>";
			s += "<table bgcolor=#FFCC99 cellpadding='0' border='0' width='100%'>";

			/* calculate the step between levels */
			int step = Math.round((levels - 1) / (questionBlock.responseSet.responses.size() - 1));

			for (int j = 1, i = 0, l = startValue; j <= levels; j++, l++) {
				int det = (j - 1) % step;
				if (det == 0) {
					s += "<tr>";
					if (j == 1) {
						s += "<td align='left'>";
					} else if (j == levels) {
						s += "<td align='right'>";
					} else {
						s += "<td align='center'>";
					}
					s += l + ". " + questionBlock.responseSet.responses.get(i);
					s += "</td></tr>";
					i++;
				}
			}
			s += "</table>";
			s += "</td>";
			s += "</tr>";

			int width = 40 / levels;
			for (int j = 0; j < levels; j++) {
				t2 = String.valueOf(j + startValue);
				s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>" + t2
						+ "</center></b></td>";
			}
		}
		s += "</tr>";

		/* display each of the stems on the left side of the block */
		for (int i = 0; i < questionBlock.stems.size(); i++) {
			s += "<tr>";
			int tnull = 0;
			int t = 0;
			float avg = 0;
			Hashtable<String, String> h1 = new Hashtable<String, String>();

			/* get the user's conducted data from the hashtable */
			String subjAns = h1.get(questionBlock.stemFieldNames.get(i).toUpperCase());

			try {

				/* connect to the database */
				Connection conn = this.getDBConnection();
				Statement stmt = conn.createStatement();

				/* if the question block doesn't have the subject set ref */
				String sql = "";
				String sqlInteger = "", sqlText = "";
				if (!questionBlock.hasSubjectSetRef) {

					/*
					 * get values from the survey data table count total number
					 * of the users who have the same answer level
					 */
					sql = "select " + questionBlock.stemFieldNames.get(i)
							+ ", count(distinct s.invitee) from " + pg.getSurvey().getId()
							+ "_data as s, page_submit as p where ";
					sql += "p.invitee=s.invitee and p.survey='" + pg.getSurvey().getId() + "'";
					sql += " and p.page='" + pg.getId() + "'";
					if (!whereclause.equalsIgnoreCase("")) {
						sql += " and s." + whereclause;
					}
					sql += " group by " + questionBlock.stemFieldNames.get(i);

					// New query to fix bug

					sqlInteger = "SELECT answer, sum(counter) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataInteger()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataInteger()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page = '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlInteger += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

					sqlText = "SELECT answer, sum(counter) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataText()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataText()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataText()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page = '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlText += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

				} else {

					/*
					 * if the question block has the subject set ref get the
					 * user's conducted data from the table of subject set
					 */
					String user_id = (String) data.get("invitee");
					if ((user_id != null) && !user_id.equalsIgnoreCase("")) {
						sql = "select "
								+ questionBlock.name
								+ " from "
								+ pg.getSurvey().getId()
								+ "_"
								+ questionBlock.getSubjectSetName()
								+ "_data"
								+ " where subject="
								+ questionBlock.stemFieldNames.get(i).substring(
										(questionBlock.stemFieldNames.get(i).lastIndexOf("_") + 1))
								+ " and invitee=" + user_id;
						stmt.execute(sql);
						ResultSet rs = stmt.getResultSet();
						if (rs.next()) {
							subjAns = rs.getString(1);
						}
					}

					/*
					 * get values from the subject data table count total number
					 * of the users who have the same answer level
					 */
					sql = "select " + questionBlock.name + ", count(*) from "
							+ pg.getSurvey().getId() + "_" + questionBlock.getSubjectSetName()
							+ "_data as s, page_submit as p";
					sql += " where s.invitee=p.invitee and p.survey='" + pg.getSurvey().getId()
							+ "'";
					sql += " and p.page='" + pg.getId() + "'";
					sql += " and s.subject="
							+ questionBlock.stemFieldNames.get(i).substring(
									(questionBlock.stemFieldNames.get(i).lastIndexOf("_") + 1));
					if (!whereclause.equalsIgnoreCase("")) {
						sql += " and s." + whereclause;
					}
					sql += " group by " + questionBlock.name;

					// New query to fix bug

					sqlInteger = "SELECT answer, sum(counter) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataInteger()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataInteger()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page = '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".questionId = "
							+ questionBlock.name
							+ " and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey = '" + pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlInteger += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

					sqlText = "SELECT answer, sum(counter) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataText()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataText()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataText()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page = '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlText += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

				}

				PreparedStatement stmtForInteger = conn.prepareStatement(sqlInteger);
				ResultSet rs = stmtForInteger.executeQuery();

				h1.clear();
				String s1, s2;

				while (rs.next()) {
					if (rs.getString(1) == null) {
						tnull = tnull + rs.getInt(2);
					} else {
						s1 = rs.getString(1);
						s2 = rs.getString(2);
						h1.put(s1, s2);
						t = t + rs.getInt(2);
					}
				}
				rs.close();

				PreparedStatement stmtForText = conn.prepareStatement(sqlText);
				rs = stmtForText.executeQuery();

				while (rs.next()) {
					if (rs.getString(1) == null) {
						tnull = tnull + rs.getInt(2);
					} else {
						s1 = rs.getString(1);
						s2 = rs.getString(2);
						h1.put(s1, s2);
						t = t + rs.getInt(2);
					}
				}
				rs.close();

				if (subjAns == null) {
					subjAns = "null";
				}

				/* if the question block doesn't have the subject set ref */
				if (!questionBlock.hasSubjectSetRef) {

					/*
					 * get values from the survey data table calculate the
					 * average answer level
					 */
					sql = "select round(avg(" + questionBlock.stemFieldNames.get(i) + "),1) from "
							+ pg.getSurvey().getId() + "_data as s, page_submit as p"
							+ " where s.invitee=p.invitee and p.page='" + pg.getId()
							+ "' and p.survey='" + pg.getSurvey().getId() + "'";
					if (!whereclause.equalsIgnoreCase("")) {
						sql += " and s." + whereclause;
					}

					// New query to fix bug

					sqlInteger = "SELECT round(avg(answer),1) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataInteger()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataInteger()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page =  '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlInteger += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

					sqlText = "SELECT round(avg(answer),1) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataText()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataText()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataText()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page =  '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlText += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

				}

				/* if the question block has the subject set ref */
				else {

					/*
					 * get values from the subject data table calculate the
					 * average answer level
					 */
					sql = "select round(avg(" + questionBlock.name + "),1) from "
							+ pg.getSurvey().getId() + "_" + questionBlock.getSubjectSetName()
							+ "_data as s, page_submit as p";
					sql += " where s.invitee=p.invitee and p.survey='" + pg.getSurvey().getId()
							+ "'";
					sql += " and p.page='" + pg.getId() + "'";
					sql += " and s.subject="
							+ questionBlock.stemFieldNames.get(i).substring(
									(questionBlock.stemFieldNames.get(i).lastIndexOf("_") + 1));
					if (!whereclause.equalsIgnoreCase("")) {
						sql += " and s." + whereclause;
					}

					// New query to fix bug

					sqlInteger = "SELECT round(avg(answer),1) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataInteger()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataInteger()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page =  '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataInteger()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlInteger += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

					sqlText = "SELECT round(avg(answer),1) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
							+ this.wiseTables.getMainDataText()
							+ " INNER JOIN "
							+ this.wiseTables.getPageSubmit()
							+ " ON "
							+ this.wiseTables.getMainDataText()
							+ ".inviteeId = "
							+ this.wiseTables.getPageSubmit()
							+ ".invitee and "
							+ this.wiseTables.getMainDataText()
							+ ".survey="
							+ this.wiseTables.getPageSubmit()
							+ ".survey where "
							+ this.wiseTables.getPageSubmit()
							+ ".page =  '"
							+ pg.getId()
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".questionId = '"
							+ questionBlock.stemFieldNames.get(i)
							+ "' and "
							+ this.wiseTables.getMainDataText()
							+ ".survey = '"
							+ pg.getSurvey().getId() + "'";

					if (!whereclause.equals("")) {
						sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
					}
					sqlText += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

				}

				stmtForInteger = conn.prepareStatement(sqlInteger);
				rs = stmtForInteger.executeQuery();

				if (rs.next()) {
					avg = rs.getFloat(1);
				}
				rs.close();

				stmtForText = conn.prepareStatement(sqlText);
				rs = stmtForText.executeQuery();

				if (rs.next()) {
					avg = rs.getFloat(1);
				}

				stmt.close();
				conn.close();
			} catch (SQLException e) {
				LOGGER.error("WISE - QUESTION BLOCK RENDER RESULTS: " + e.toString(), e);
				return "";
			} catch (NullPointerException e) {
				LOGGER.error("WISE - QUESTION BLOCK RENDER RESULTS: " + e.toString(), e);
				return "";
			}

			/* display the statistic results */
			String s1;

			/* if classified level is required for the question block */
			if (levels == 0) {
				s += "<td bgcolor=#FFCC99>";
				s += questionBlock.stems.get(i).stemValue + "<p>";
				s += "<div align='right'>";
				s += "mean: </b>" + avg;

				if (tnull > 0) {
					s += "&nbsp;<b>unanswered:</b>";

					/*
					 * if the user's answer is null, highlight the answer note
					 * that if the call came from admin page, this value is
					 * always highlighted because the user's data is always to
					 * be null
					 */
					if (subjAns.equalsIgnoreCase("null")) {
						s += "<span style=\"background-color: '#FFFF77'\">" + tnull + "</span>";
					} else {
						s += tnull;
					}
				}

				s += "</div>";
				s += "</td>";

				for (int j = 0; j < questionBlock.responseSet.responses.size(); j++) {
					t2 = String.valueOf(j + startValue);
					if (j < questionBlock.responseSet.responses.size()) {
						t1 = questionBlock.responseSet.responses.get(j);
					}
					int num1 = 0;
					int p = 0;
					int p1 = 0;
					float af = 0;
					float bf = 0;
					float cf = 0;
					String ps, ps1;
					s1 = h1.get(t2);
					if (s1 == null) {
						ps = "0";
						ps1 = "0";
					} else {
						num1 = Integer.parseInt(s1);
						af = (float) num1 / (float) t;
						bf = af * 50;
						cf = af * 100;
						p = Math.round(bf);
						p1 = Math.round(cf);
						ps = String.valueOf(p);
						ps1 = String.valueOf(p1);
					}

					/*
					 * if the user's answer belongs to this answer level,
					 * highlight the image
					 */
					if (subjAns.equalsIgnoreCase(t2)) {
						s += "<td bgcolor='#FFFF77'>";
					} else {
						s += "<td>";
					}
					s += "<center>";
					s += "<img src='" + "imgs/vertical/bar_" + ps + ".gif' ";
					s += "width='10' height='50'>";
					s += "<br>" + ps1;
					s += "</center>";
					s += "</td>";
				}
			}
			/* if classified level is required for the question block */
			else {
				s += "<td bgcolor=#FFCC99>";
				s += questionBlock.stems.get(i).stemValue + "<p>";
				s += "<div align='right'>";
				s += "mean: </b>" + avg;

				if (tnull > 0) {
					s += "&nbsp;<b>unanswered: </b>";

					/*
					 * if the user's answer is null, highlight the answer note
					 * that if the call came from admin page, this value is
					 * always highlighted because the user's data is always to
					 * be null
					 */
					if (subjAns.equalsIgnoreCase("null")) {
						s += "<span style=\"background-color: '#FFFF77'\">" + tnull + "</span>";
					} else {
						s += tnull;
					}
				}

				s += "</div>";
				s += "</td>";
				// int step = Math.round((levels - 1)
				// / (responseSet.responses.size() - 1));
				for (int j = 0; j < levels; j++) {

					// t2 = String.valueOf(j);
					t2 = String.valueOf(j + startValue);
					if (j < questionBlock.responseSet.responses.size()) {
						t1 = questionBlock.responseSet.responses.get(j);
					}
					int num1 = 0;
					int p = 0;
					int p1 = 0;
					float af = 0;
					float bf = 0;
					float cf = 0;
					String ps, ps1;
					s1 = h1.get(t2);
					if (s1 == null) {
						ps = "0";
						ps1 = "0";
					} else {
						num1 = Integer.parseInt(s1);
						af = (float) num1 / (float) t;
						bf = af * 50;
						cf = af * 100;
						p = Math.round(bf);
						p1 = Math.round(cf);
						ps = String.valueOf(p);
						ps1 = String.valueOf(p1);
					}

					/*
					 * if the User's answer belongs to this answer level,
					 * highlight the image
					 */
					if (subjAns.equalsIgnoreCase(t2)) {
						s += "<td bgcolor='#FFFF77'>";
					} else {
						s += "<td>";
					}
					s += "<center>";
					s += "<img src='" + "imgs/vertical/bar_" + ps + ".gif' ";
					s += "width='10' height='50'>";
					s += "<br>" + ps1;
					s += "</center>";
					s += "</td>";
				}
			}
		}

		s += "</table></center>";
		return s;

	}

	public float getAvgForQuestion(Page page, String questionName, String whereclause) {
		float avg = 0;
		try {

			/* connect to the database */
			Connection conn = this.getDBConnection();
			Statement stmt = conn.createStatement();

			/* get the average answer of the question from data table */
			String sql = "select round(avg(" + questionName + "),1) from "
					+ page.getSurvey().getId() + "_data as s where s.invitee in "
					+ "(select distinct(invitee) from page_submit where page='" + page.getId()
					+ "' and survey='" + page.getSurvey().getId() + "')";
			if (!whereclause.equalsIgnoreCase("")) {
				sql += " and s." + whereclause;
			}

			// New query to fix bug

			String sqlInteger = "SELECT round(avg(answer),1) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
					+ this.wiseTables.getMainDataInteger()
					+ " INNER JOIN "
					+ this.wiseTables.getPageSubmit()
					+ " ON "
					+ this.wiseTables.getMainDataInteger()
					+ ".inviteeId = "
					+ this.wiseTables.getPageSubmit()
					+ ".invitee and "
					+ this.wiseTables.getMainDataInteger()
					+ ".survey="
					+ this.wiseTables.getPageSubmit()
					+ ".survey where "
					+ this.wiseTables.getPageSubmit()
					+ ".page = '"
					+ page.getId()
					+ "' and "
					+ this.wiseTables.getMainDataInteger()
					+ ".questionId = '"
					+ questionName
					+ "' and "
					+ this.wiseTables.getMainDataInteger()
					+ ".survey = '"
					+ page.getSurvey().getId() + "'";

			if (!whereclause.equals("")) {
				sqlInteger += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
			}
			sqlInteger += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

			String sqlText = "SELECT round(avg(answer),1) FROM (SELECT answer, count(distinct inviteeId) AS counter FROM "
					+ this.wiseTables.getMainDataText()
					+ " INNER JOIN "
					+ this.wiseTables.getPageSubmit()
					+ " ON "
					+ this.wiseTables.getMainDataText()
					+ ".inviteeId = "
					+ this.wiseTables.getPageSubmit()
					+ ".invitee and "
					+ this.wiseTables.getMainDataText()
					+ ".survey="
					+ this.wiseTables.getPageSubmit()
					+ ".survey where "
					+ this.wiseTables.getPageSubmit()
					+ ".page = '"
					+ page.getId()
					+ "' and "
					+ this.wiseTables.getMainDataText()
					+ ".questionId = '"
					+ questionName
					+ "' and "
					+ this.wiseTables.getMainDataText()
					+ ".survey = '"
					+ page.getSurvey().getId() + "'";

			if (!whereclause.equals("")) {
				sqlText += " and " + this.wiseTables.getPageSubmit() + "." + whereclause;
			}
			sqlText += " GROUP BY inviteeId ORDER BY id DESC) AS X GROUP BY answer;";

			PreparedStatement stmtForInteger = conn.prepareStatement(sqlInteger);
			ResultSet rs = stmtForInteger.executeQuery();

			if (rs.next()) {
				avg = rs.getFloat(1);
			}
			rs.close();

			PreparedStatement stmtForText = conn.prepareStatement(sqlText);
			rs = stmtForText.executeQuery();

			if (rs.next()) {
				avg = rs.getFloat(1);
			}
			rs.close();

			stmt.close();
			conn.close();
		} catch (SQLException e) {
			LOGGER.error("WISE - QUESTION GET AVG: " + e.toString(), e);
		}
		return avg;
	}

	public String renderResultsMultiselect(Hashtable data, Page pg, ClosedQuestion closedQuestion,
			DataBank db, String whereclause) {

		String s = "";
		int num = 0;
		String t2, t3, t4;
		Hashtable<String, String> h1 = new Hashtable<String, String>();
		int tnull = 0;

		for (int j = 0; j < closedQuestion.responseSet.responses.size(); j++) {
			num = j + 1;
			t2 = String.valueOf(num);
			t3 = closedQuestion.name + "_" + t2;
			// get the User's answer
			String subjAns = data == null ? null : (String) data.get(t3.toUpperCase());

			/* if the call came from admin page, the data will be null */
			if (subjAns == null) {
				subjAns = "null";
			}

			try {

				/* connect to the database */
				Connection conn = this.getDBConnection();
				Statement stmt = conn.createStatement();

				/*
				 * count the total number of invitees who has the same level of
				 * answer
				 */
				String sql = "select " + t3 + ", count(distinct s.invitee) from "
						+ pg.getSurvey().getId() + "_data as s, page_submit as p where ";
				sql += "p.invitee=s.invitee and p.survey='" + pg.getSurvey().getId() + "'";
				sql += " and p.page='" + pg.getId() + "'";
				if (!whereclause.equalsIgnoreCase("")) {
					sql += " and s." + whereclause;
				}
				sql += " group by " + t3;
				stmt.execute(sql);
				ResultSet rs = stmt.getResultSet();
				h1.clear();
				String s2;
				while (rs.next()) {
					if (rs.getString(1) != null) {
						// s1 = rs.getString(1);
						s2 = rs.getString(2);

						/*
						 * put the level of answer and its invitee number into
						 * the hashtable
						 */
						h1.put(t3, s2);
					} else {
						tnull = tnull + rs.getInt(2);
					}
				}
				rs.close();
				stmt.close();
				conn.close();
			} catch (SQLException e) {
				LOGGER.error("WISE - CLOSED QUESTION RENDER RESULTS MULTISELECT: " + e.toString(),
						e);
				return "";
			}

			s += "<tr><td width='2%'>&nbsp;</td><td width='4%'>&nbsp;</td>";

			t4 = h1.get(t3);

			float p = 0;
			float pt = 0;
			if (t4 != null) {
				Integer it4 = new Integer(t4);
				p = (float) it4.intValue() / (float) pg.getPagedoneNumb(whereclause);
			}

			p = p * 50;
			pt = p * 2;
			int p1 = Math.round(p);
			int p1t = Math.round(pt);
			String ps = String.valueOf(p1);
			String pst = String.valueOf(p1t);

			/*
			 * if the user's answer belongs to this answer level, highlight the
			 * answer
			 */
			if (subjAns.equalsIgnoreCase(t2)) {
				s += "<td bgcolor='#FFFF77' width='3%'>";
			} else {
				s += "<td width='3%'>";
			}
			s += "<div align='right'><font size='-2'>" + pst + "% </font></div></td>";

			/*
			 * if the user's answer belongs to this answer level, highlight the
			 * image
			 */
			if (subjAns.equalsIgnoreCase(t2)) {
				s += "<td bgcolor='#FFFF77' width='6%'>";
			} else {
				s += "<td width='6%'>";
			}
			s += "<img src='" + SurveyorApplication.getInstance().getSharedFileUrl()
					+ "imgs/horizontal/bar_" + ps + ".gif' ";
			s += "width='50' height='10'></td>";
			s += "<td>&nbsp;&nbsp;" + closedQuestion.responseSet.responses.get(j) + "</td></tr>";
		}
		return s;

	}

	public void checkDbHealth() {
		HealthStatus hStatus = HealthStatus.getInstance();
		Connection dbConnection = null;
		try {
			dbConnection = this.getDBConnection();
		} catch (SQLException e) {
			this.LOGGER.error(e);
			hStatus.updateDb(false, Calendar.getInstance().getTime());
			return;
		} finally {
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		hStatus.updateDb(true, Calendar.getInstance().getTime());
	}

	public List<SurveyInformation> getCurrentSurveys() {
		List<SurveyInformation> currentSurveysList = new ArrayList<>();
		try {

			Statement stmt2 = this.getDBConnection().createStatement();
			String sql2 = "select internal_id, id, filename, title, status, uploaded from surveys where status in ('P', 'D') and internal_id in"
					+ "(select max(internal_id) from surveys group by id) order by uploaded DESC";
			boolean dbtype = stmt2.execute(sql2);
			ResultSet rs2 = stmt2.getResultSet();

			while (rs2.next()) {
				SurveyInformation surveyInformation = new SurveyInformation();
				surveyInformation.internalId = rs2.getString(1);
				surveyInformation.id = rs2.getString(2);
				surveyInformation.filename = rs2.getString(3);
				surveyInformation.title = rs2.getString(4);
				surveyInformation.status = rs2.getString(5);
				surveyInformation.uploaded = rs2.getString(6);
				if (surveyInformation.status.equalsIgnoreCase("D")) {
					surveyInformation.surveyMode = "Development";
				}
				if (surveyInformation.status.equalsIgnoreCase("P")) {
					surveyInformation.surveyMode = "Production";
				}

				surveyInformation.anonymousInviteUrl = Message.buildInviteUrl(
						this.studySpace.appUrlRoot, null, this.studySpace.id, surveyInformation.id);
				currentSurveysList.add(surveyInformation);
			}
		} catch (SQLException e) {
			LOGGER.error("Could not fetch current surveys from the database", e);
		}
		return currentSurveysList;
	}

	public WebResponseMessage modifyInviteeTable(String editType, String colName, String colValue,
			String colDef, String colOName) {

		WebResponseMessageType responseType = WebResponseMessageType.SUCCESS;
		String responseMessage = "";

		// declare variables
		String sqlm = null;
		try {
			Connection con = this.getDBConnection();
			java.sql.Statement stmtm = con.createStatement();

			// if it is to add a new column, the default value should be
			// entered in single quotes if it is only string
			if (editType.equalsIgnoreCase("add")) {
				if (!(Strings.isNullOrEmpty(colName) || Strings.isNullOrEmpty(colValue))) {
					sqlm = "alter table invitee add " + colName + " " + colValue;
					if ((colDef != null) && colDef.equalsIgnoreCase("null")) {
						sqlm += " default NULL";
					} else if (colDef != null) {
						sqlm += " NOT NULL default " + colDef;
					}
				} else {
					responseType = WebResponseMessageType.FAILURE;
					responseMessage = "<p>Please note that you have to fill up the column name/value to add it</p>";
				}
			}
			// if it is to delete a column - display the warning message
			// before proceed
			else if (editType.equalsIgnoreCase("delete")) {
				sqlm = "alter table invitee drop " + colOName;
			}
			// if it is to update the column
			else if (editType.equalsIgnoreCase("update")) {
				if ((colName != null) && !colName.equalsIgnoreCase("") && (colValue != null)
						&& !colValue.equalsIgnoreCase("")) {
					sqlm = "alter table invitee change " + colOName + " " + colName + " "
							+ colValue;
					if ((colDef != null) && colDef.equalsIgnoreCase("null")) {
						sqlm += " default NULL";
					} else if (colDef != null) {
						sqlm += " NOT NULL default " + colDef;
					}
				} else {
					responseType = WebResponseMessageType.FAILURE;
					responseMessage = "<p>You have to fill up the column name/value to update it</p>";
				}
			}

			// run the query to update/add/delete the column
			boolean dbtypem;
			if (sqlm != null) {

				dbtypem = stmtm.execute(sqlm);

				responseType = WebResponseMessageType.SUCCESS;
				responseMessage = "<p>The invitee table has been successfully modified</p>";
			}
		} catch (SQLException e) {
			LOGGER.error("Could not update/add/delete invitee column", e);
			responseType = WebResponseMessageType.ERROR;
		}
		return new WebResponseMessage(responseType, responseMessage);
	}

	public WebResponseMessage describeInviteeTable() {

		StringBuilder responseBuilder = new StringBuilder();
		WebResponseMessageType responseType = WebResponseMessageType.SUCCESS;
		try {
			// connect to the database
			Connection conn = this.getDBConnection();

			java.sql.Statement stmt = conn.createStatement();
			// get the column names in the invitee table
			String sql = "describe invitee";
			boolean dbtype = stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				String column_name = rs.getString("Field");
				String column_type = rs.getString("Type");
				String column_default = rs.getString("Default");
				String column_key = rs.getString("Key");

				responseBuilder.append("<tr><td>" + column_name + "</td>");
				responseBuilder.append("<td>" + column_type + "</td>");
				responseBuilder.append("<td align=center>" + column_default + "</td>");
			}
		} catch (SQLException e) {

			this.LOGGER.error("WISE ADMIN - CHANGE INVITEE", e);
			responseType = WebResponseMessageType.ERROR;

		}

		return new WebResponseMessage(responseType, responseBuilder.toString());
	}

	public String printAdminResults(String surveyId) {
		String sql = "SELECT id, firstname, lastname, salutation, AES_DECRYPT(email,'"
				+ this.getEmailEncryptionKey() + "') FROM " + this.wiseTables.getInvitee()
				+ " WHERE id=?";

		List<Integer> inviteesWithAnswers = this.getInviteesWithData(surveyId);
		StringBuilder out = new StringBuilder();

		out.append("<tr>");
		out.append("<td class=sfon>&nbsp;</td>");
		out.append("<td class=sfon align=center>User ID</td>");
		out.append("<td class=sfon align=center>User Name</td>");
		out.append("<td class=sfon align=center>User's Email Address</td></tr>");
		try {

			Connection conn = this.getDBConnection();

			for (int inviteeId : inviteesWithAnswers) {

				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setInt(1, inviteeId);
				LOGGER.debug("SQL:" + sql);
				// get the survey responders' info
				ResultSet rs = stmt.executeQuery();

				if (rs.next()) {
					out.append("<tr>");
					out.append("<td align=center><input type='checkbox' name='user' value='"
							+ rs.getString(1)
							+ "' onClick='javascript: remove_check_allusers()'></td>");
					out.append("<td align=center>" + rs.getString(1) + "</td>");
					out.append("<td align=center>" + rs.getString(4) + " " + rs.getString(2) + " "
							+ rs.getString(3) + "</td>");
					out.append("<td align=center>" + rs.getString(5) + "</td>");
				} else {
					LOGGER.error("Could not find attributes for the invitee '" + inviteeId + "'");
				}
				rs.close();
				stmt.close();
			}
			out.append("</tr>");

			conn.close();

		} catch (SQLException e) {
			LOGGER.error("WISE ADMIN - VIEW RESULT:" + e.toString(), e);
		}

		return out.toString();

	}

	public String listInterviewer() {
		StringBuilder out = new StringBuilder();
		try {
			// open database connection
			Connection conn = this.getDBConnection();
			Statement statement = conn.createStatement();

			String sql = "select id, username, firstname, lastname, salutation, email from interviewer";

			boolean results = statement.execute(sql);
			ResultSet rs = statement.getResultSet();

			String id, user_name, first_name, last_name, salutation, email;

			while (rs.next()) {
				id = rs.getString("id");
				user_name = rs.getString("username");
				first_name = rs.getString("firstname");
				last_name = rs.getString("lastname");
				salutation = rs.getString("salutation");
				email = rs.getString("email");
				out.append("<tr>");
				out.append("<td align=center><input type='checkbox' name='interviewer'");
				out.append("value='" + id + "'></td>");
				out.append("<td align=center>" + user_name + "</td>");
				out.append("<td align=center>" + salutation + "</td>");
				out.append("<td align=center>" + first_name + "</td>");
				out.append("<td align=center>" + last_name + "</td>");
				out.append("<td>" + email + "</td>");
				out.append("<td align=center><a href='goto_wati.jsp?interview_id=" + id + "'><img");
				out.append("src='admin_images/go_view.gif' border='0'></a></td>");
				out.append("</tr>");
			}
			rs.close();
			statement.close();
			conn.close();
		} catch (Exception e) {
			LOGGER.error("LIST INTERVIEWER:" + e.toString(), e);
		}
		return out.toString();
	}

	public String reassignWati(String[] inviteePending, String[] inviteeReassign,
			String interviewerId, String surveyId, Map<String, String[]> webParameters) {

		StringBuilder out = new StringBuilder();

		try {
			Connection conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			Statement stmta = conn.createStatement();
			if (inviteePending != null) {
				// update the pending status
				for (int i = 0; i < inviteePending.length; i++) {
					String pend_attr = "openpend_" + inviteePending[i];
					String open_pend = webParameters.get(pend_attr)[0];
					if (open_pend.equalsIgnoreCase("yes")) {
						String sql = "update interview_assignment set pending=1 where invitee="
								+ inviteePending[i];
						sql += " and interviewer=" + interviewerId + " and survey='" + surveyId
								+ "'";
						boolean dbtype = stmt.execute(sql);
						ResultSet rs = stmt.getResultSet();
						out.append("The new assignment has activiated the pending status");
					}
				}
			}
			if (inviteeReassign != null) {
				// insert the new assignment
				for (int j = 0; j < inviteeReassign.length; j++) {
					String invitee_id = inviteeReassign[j];
					String sql = "insert into interview_assignment(interviewer, invitee, survey, assign_date, pending) values('"
							+ interviewerId
							+ "','"
							+ invitee_id
							+ "','"
							+ surveyId
							+ "', now(), 1)";
					boolean dbtype = stmt.execute(sql);
					ResultSet rs = stmt.getResultSet();

					out.append("The new reassignment has been created.<br>");
					String reassign_attr = "reassignment_" + invitee_id;
					String reassign_id[] = webParameters.get(reassign_attr);

					if (reassign_id != null) {
						// make the reassignment for the current invitee
						sql = "update interview_assignment set pending=-1 where id in (";
						// String sql =
						// "delete from interview_assignment where id in(";
						for (int i = 0; i < reassign_id.length; i++) {
							sql += reassign_id[i];
							if (i < (reassign_id.length - 1)) {
								sql += ", ";
							}
						}
						sql += ")";
						dbtype = stmt.execute(sql);
						rs = stmt.getResultSet();
						out.append("And the reassignments have been updated.");
					} // end if
				} // end for
			} // end if
			stmt.close();
			conn.close();
		} catch (Exception e) {
			out.append("error message:" + e.toString());
			LOGGER.error("REASSIGN WATI:" + e.toString(), e);
		}

		return out.toString();
	}

	public String removeProfile(String[] interviewer) {
		StringBuilder output = new StringBuilder();
		try {
			Connection conn = this.getDBConnection();
			Statement statement = conn.createStatement();
			String sql = "delete from interviewer where id in (";
			for (int i = 0; i < interviewer.length; i++) {
				sql += interviewer[i];
				if (i < (interviewer.length - 1)) {
					sql += ", ";
				} else {
					sql += ")";
				}
			}
			boolean results = statement.execute(sql);
			output.append("The removing of interviewer(s) is done.");
		} catch (Exception e) {
			LOGGER.error("REMOVE INTERVIEWERS:" + e.toString(), e);
			output.append("Error of removing:" + e.toString());
		}

		return output.toString();
	}

	public String saveWati(String whereStr, String interviewerId, String surveyId) {
		boolean new_assign = false;
		boolean pend_assign = false;

		StringBuilder out = new StringBuilder();
		try {
			Connection conn = this.getDBConnection();
			Statement stmt = conn.createStatement();
			Statement stmta = conn.createStatement();
			Statement stmtb = conn.createStatement();
			String sql = "SELECT id, firstname, lastname FROM invitee where " + whereStr;
			boolean dbtype = stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();
			while (rs.next()) {
				String invitee_id = rs.getString("id");
				String invitee_firstname = rs.getString("firstname");
				String invitee_lastname = rs.getString("lastname");
				// check the previous assignment
				String sqla = "select assign_date, pending from interview_assignment where interviewer='"
						+ interviewerId
						+ "' and invitee='"
						+ invitee_id
						+ "' and survey='"
						+ surveyId + "'";
				// Study_Util.email_alert("sqla:"+sqla);
				boolean dbtypea = stmta.execute(sqla);
				ResultSet rsa = stmta.getResultSet();
				// if the new assignment had been done before, ignore it
				if (rsa.next()) {
					String pend_val = rsa.getString("pending");
					// print out the already assigned info - a duplicate
					out.append("<table class=tth width=400 border=1 cellpadding=1 cellspacing=1 bgcolor=#FFFFE1>");
					out.append("<tr bgcolor=#CC6666><td align=center colspan=3><font color=white>Redundant Assignment</font></td></tr>");
					out.append("<tr><td class=sfon align=center>ID</td>");
					out.append("<td align=center>" + interviewerId + "</td>");
					out.append("<td rowspan=5>");

					if (pend_val.equalsIgnoreCase("1")) {
						out.append("This interviewer had already been assigned to the invitee <b>"
								+ invitee_firstname + " " + invitee_lastname
								+ "</b>. New assignment is <b>ignored</b>.</td></tr>");
					}
					/*
					 * //the option has already been checked in tool.jsp else
					 * if(pend_val.equalsIgnoreCase("0")) { out.append(
					 * "This interviewer had been assigned to the invitee in the past <b>"
					 * +invitee_firstname+" "+invitee_lastname+
					 * "</b> and already finished this interview. New assignment is <b>ignored</b>.</td></tr>"
					 * ); }
					 */
					else if (pend_val.equalsIgnoreCase("-1")) {
						pend_assign = true;
						out.append("This interviewer had been assigned to the invitee <b>"
								+ invitee_firstname + " " + invitee_lastname
								+ "</b> in the past and put in pending status now.");
						out.append("Do you want to continue(activiate the pending)? <br>");
						out.append("<input type='radio' name='openpend_" + invitee_id
								+ "' value='yes' checked> YES ");
						out.append("<input type='radio' name='openpend_" + invitee_id
								+ "' value='no'> NO<br>");
						out.append("<input type='hidden' name='inviteepend' value='" + invitee_id
								+ "'></td></tr>");
					}

					out.append("<tr><td class=sfon align=center>Interviewer</td>");
					// out.append("<td align=center>"+inv.first_name+" "+inv.last_name+"</td></tr>");
					out.append("<tr><td class=sfon align=center>Invitee</td>");
					out.append("<td align=center>" + invitee_firstname + " " + invitee_lastname
							+ "</td></tr>");
					out.append("<tr><td class=sfon align=center>Assigned Date</td>");
					out.append("<td align=center>" + rsa.getString("assign_date") + "</td></tr>");
					out.append("</table>");
					out.append("&nbsp;&nbsp;&nbsp;&nbsp;");
				} else {
					out.append("<table class=tth width=400 border=1 cellpadding=1 cellspacing=1 bgcolor=#FFFFE1>");

					// list those interviewers with the same
					// assignment(invitee&survey) before for resignment
					String sqlb = "select id, interviewer, assign_date from interview_assignment where "
							+ "invitee='"
							+ invitee_id
							+ "' and survey='"
							+ surveyId
							+ "' and pending <> -1 and interviewer <>" + interviewerId;
					boolean dbtypeb = stmta.execute(sqlb);
					ResultSet rsb = stmta.getResultSet();
					Interviewer[] pre_inv = new Interviewer[100];
					String[] pre_id = new String[100];
					String[] pre_date = new String[100];
					int i = 0;

					while (rsb.next()) {
						new_assign = true;
						InterviewManager.getInstance().getInterviewer(this.studySpace,
								rsb.getString("interviewer"));
						// pre_inv[i].get_interviewer(rsb.getString("interviewer"));
						pre_id[i] = rsb.getString("id");
						pre_date[i] = rsb.getString("assign_date");
						i++;
					}

					if (new_assign) {
						out.append("<tr bgcolor=#3399CC>");
						out.append("<td align=center colspan=4><font color=white>Duplicate Assignment</font></td>");
						out.append("</tr><tr>");
						out.append("<td align=left colspan=4>");
						out.append("The assignment are also assigned to the following other interviewers.");
						out.append("In order to continue the new assignment, they have to be set to be reassigned (set to be pending status).");
						out.append("Click the reassign button to cotinue OR click the back button to cancle.");
						out.append("<input type='hidden' name='inviteereassign' value='"
								+ invitee_id + "'></td>");
						out.append("</tr><tr>");
						// out.append("<td class=sfon align=center>&nbsp;</td>");
						out.append("<td class=sfon align=center>ID</td>");
						out.append("<td class=sfon align=center>Interviewer</td>");
						out.append("<td class=sfon align=center>Invitee</td>");
						out.append("<td class=sfon align=center>Assigned Date</td></tr>");

						for (int k = 0; k < i; k++) {

							out.append("<tr>");
							out.append("<td align=center><input type='hidden'");
							out.append("name='reassignment_" + invitee_id + "' value='" + pre_id[k]
									+ "> " + pre_inv[k].getId() + "</td>");
							out.append("<td align=center>" + pre_inv[k].getFirstName() + " "
									+ pre_inv[k].getLastName() + "</td>");
							out.append("<td align=center>" + invitee_firstname + " "
									+ invitee_lastname + "</td>");
							out.append("<td align=center>" + pre_date[k] + "</td>");
							out.append("</tr>");
						} // end for

					} // end of if 2
					else {
						// no duplication, no assigned peers, then make the
						// assignment
						String sqlc = "insert into interview_assignment(interviewer, invitee, survey, assign_date, pending) values('"
								+ interviewerId
								+ "','"
								+ invitee_id
								+ "','"
								+ surveyId
								+ "', now(), 1)";
						boolean dbtypec = stmta.execute(sqlc);
						ResultSet rsc = stmta.getResultSet();

						out.append("<tr bgcolor=#996600>");
						out.append("<td align=center colspan=4><font color=white>Interviewer <b><!-- inv.first_name%> -->");
						out.append("<!-- inv.last_name%></b> has been assigned invitee <b><%=invitee_firstname%> -->");
						out.append("<%=invitee_lastname%></b>.</font></td>");
						out.append("</tr>");
					} // end of else

					out.append("</table>&nbsp;&nbsp;&nbsp;&nbsp;");
				} // end of else
			} // end of while
		} catch (Exception e) {
			out.append("error message:" + e.toString());
			LOGGER.error("SAVE WATI - SAVE ASSIGNMENTS:" + e.toString(), e);
		}
		if (new_assign || pend_assign) {
			out.append("</td>");
			out.append("</tr>");
			out.append("<tr>");
			out.append("<td align=center><input type='image' alt='submit'");
			out.append("src='admin_images/reassign.gif'>");
		}
		return out.toString();
	}

	@Override
	public String getStudySpace() {
		return this.studySpace.studyName;
	}

	public String getEmailEncryptionKey() {
		return this.emailEncryptionKey;
	}

	@Override
	public WiseTables getWiseTables() {
		return this.wiseTables;
	}

	public String getDbdata() {
		return this.dbdata;
	}

	public String getDbuser() {
		return this.dbuser;
	}

	@Override
	public SQLTemplateUtil getSqlTemplateUtil() {
		return new SQLTemplateUtil(AdminApplication.getInstance().getSQLTemplateConfiguration());
	}

}