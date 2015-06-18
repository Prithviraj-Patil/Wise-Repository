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
package edu.ucla.wise.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.AdminDataBank;
import edu.ucla.wise.commons.CommonUtils;
import edu.ucla.wise.commons.Message;
import edu.ucla.wise.commons.MessageSequence;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.commons.databank.DataBank;
import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

public class AdminUserSession {
	private static final Logger LOGGER = Logger.getLogger(AdminUserSession.class);

	private static final Map<String, String> LOGGED_IN = new ConcurrentHashMap<String, String>();
	private static final ConcurrentHashMap<String, Integer> loginAttemptNumbers = new ConcurrentHashMap<String, Integer>();
	private static final ConcurrentHashMap<String, Long> lastLoginTime = new ConcurrentHashMap<String, Long>();

	private final String studyName;
	private final String studyId;
	private final StudySpace myStudySpace;
	private final String studyTitle;
	private final String studyXmlPath;
	private final String studyImagePath;

	private final AdminDataBank adminDataBank;

	/**
	 * Constructor to create an Admin user session.
	 * 
	 * @param username
	 *            username to login into admin application.
	 * @param passwordGiven
	 *            password to login.
	 * @throws IllegalArgumentException
	 */
	public AdminUserSession(String username, String passwordGiven) throws IllegalArgumentException {
		StudySpaceParameters params = StudySpaceParametersProvider.getInstance()
				.getStudySpaceParameters(username);
		if (params == null) {
			LOGGER.info("params object is null");
			throw new IllegalArgumentException();
		}

		String dbPwd = params.getDatabasePassword();
		boolean pwValid = passwordGiven.equalsIgnoreCase(dbPwd);
		if (!pwValid) {
			throw new IllegalArgumentException("Password is not valid");
		}

		this.studyName = username;
		this.studyId = params.getId();
		this.myStudySpace = StudySpaceMap.getInstance().get(this.getStudyId());
		this.studyTitle = this.getMyStudySpace().title;

		/* assign other attributes */
		this.studyXmlPath = WISEApplication.getInstance().getWiseProperties().getXmlRootPath()
				+ System.getProperty("file.separator") + this.getStudyName()
				+ System.getProperty("file.separator");
		this.studyImagePath = AdminApplication.getInstance().getImageRootPath()
				+ System.getProperty("file.separator") + this.getStudyName()
				+ System.getProperty("file.separator");

		/* assign the AdminDataBank class */
		this.adminDataBank = new AdminDataBank(this.getMyStudySpace().db);

		/* record Admin user login */
		LOGGED_IN.put(this.getStudyName(), this.getStudyId());
		LOGGER.info("Study name and study id inserted in loggedIn");

	}

	@Override
	protected void finalize() throws Throwable {
		try {
			LOGGED_IN.remove(this.getStudyName());
		} catch (NullPointerException e) {
			LOGGER.error(
					"Exception deleting Admin user " + this.getStudyName() + ": " + e.toString(), e);
		} finally {
			super.finalize();
		}
	}

	/**
	 * Returns all the admins that are logged into the system.
	 * 
	 * @return String HTML format of all the admins logged in.
	 */
	public static String listAdminsOnNow() {
		String adminlist = "";
		for (String admin : LOGGED_IN.keySet()) {
			adminlist += "<P>" + admin + "</P>";
		}
		return adminlist;
	}

	/**
	 * Ask local copy of the StudySpace to parse out the preface file.
	 * 
	 * @return boolean True if the preface load happens correctly else false
	 */
	public boolean parseMessageFile() {
		return this.getMyStudySpace().loadPreface();
	}

	/**
	 * print the email message body retrieve using sequence, message type --
	 * guaranteed
	 * 
	 * @param seqId
	 *            Message sequence Id from which the text has to be printed.
	 * @param msgType
	 *            Type of the message that is to be printed from the message
	 *            sequence obtained
	 * @return String HTML format of the message body.
	 */
	public String renderMessageBody(String seqId, String msgType) {
		String outputString = "";
		outputString += "<table width=510 class=tth border=1 cellpadding=2 cellspacing=0 bgcolor=#FFFFF5>";
		outputString += "<tr><td width=50 class=sfon>From: </td>";

		/* get the message sequence from the hash */
		MessageSequence msgSeq = this.getMyStudySpace().preface.getMessageSequence(seqId);
		if (msgSeq == null) {
			LOGGER.info("ADMIN INFO - PRINT MESSAGE BODY: Can't get the message sequence for requested Sequence, Message Type");
			return null;
		}

		String emailFrom = msgSeq.fromString;
		emailFrom = emailFrom.replaceAll("<", "&lt;");
		emailFrom = emailFrom.replaceAll(">", "&gt;");
		outputString += "<td>\"" + emailFrom + "\"";
		outputString += "</td></tr>";

		/* get the message from the message sequence hash */
		Message m = msgSeq.getTypeMessage(msgType);
		if (m == null) {
			LOGGER.info("ADMIN INFO - PRINT MESSAGE BODY: Can't get the message from sequence hash");
			return null;
		}

		outputString += m.renderSampleAsHtmlRows() + "</table>";
		return outputString;
	}

	/**
	 * Prints invitees with state - excluding the initial invitees.
	 * 
	 * @param surveyId
	 *            Survey for which the invitees and their states are to be
	 *            lsited.
	 * @return String HTML format of the invitees and their states.
	 */
	public String printInviteeWithState(String surveyId) {
		return this.myStudySpace.printInviteeWithState(surveyId);
	}

	/**
	 * Returns invitees belonging to different IRB Ids..
	 * 
	 * @return Hashtable hash table of keys as irb id and value as all the
	 *         invitees belonging to this irb id.
	 */
	public Hashtable<String, String> getIrbGroups() {
		return this.myStudySpace.getIrbGroups();
	}

	/**
	 * print table of initial invites, eligible invitees for a survey, by
	 * message sequence (& therefore irb ID)
	 * 
	 * @param surveyId
	 *            Survey Id whose invitees are to be listed.
	 * @param isReminder
	 *            Is the message a reminder or not.
	 * @return String HTML format of the invitees tables separated based on the
	 *         message sequence type
	 */
	public String renderInitialInviteTable(String surveyId, boolean isReminder) {
		return this.getMyStudySpace().renderInitialInviteTable(surveyId, isReminder);
	}

	/**
	 * print table of all sendable invitees, all invitees, by message sequence
	 * (& therefore irb ID)
	 * 
	 * @param surveyId
	 *            Survey Id for which all invitees have to listed.
	 * @return String HTML table format of all the invitees under the given
	 *         survey id.
	 */
	public String renderInviteTable(String surveyId) {
		return this.myStudySpace.renderInviteTable(surveyId);
	}

	/**
	 * Prints initial invitees in a table format for editing -- called by
	 * load_invitee.jsp
	 * 
	 * @param surveyId
	 *            Survey Id for which all invitees have to listed.
	 * @return String HTML editable table format of all the invitees under the
	 *         given survey id.
	 */
	public String printInitialInviteeEditable(String surveyId) {
		return this.myStudySpace.printInitialInviteeEditable(surveyId);
	}

	/**
	 * This method is called from load_invitee.jsp and is used to update/delete
	 * any of the invitees information
	 * 
	 * @param request
	 *            Http request that contains all the necessary parameters to
	 *            update an invitee in the data base
	 */
	public boolean updateInvitees(HttpServletRequest request) {
		String delFlag = request.getParameter("delflag");
		String updateID = request.getParameter("changeID");

		return this.getMyStudySpace().updateInvitees(delFlag, updateID, request.getParameterMap());
	}

	/**
	 * Prints interviewer list for a studySpace
	 * 
	 * @return HTML table format of all the interviewers in the study space.
	 */
	public String printInterviewer() {
		return this.myStudySpace.printInterviewer();
	}

	/**
	 * Prepares the message for email depending on the message sequence and then
	 * sends it to the invitee.
	 * 
	 * @param msgType
	 *            Type of the message that has to be sent as email (The message
	 *            type can be invite/interrupt/done/review/others)
	 * @param messageSeqId
	 *            The ID of the message sequence which as to be emailed. This Id
	 *            is the one given in the preface.xml
	 * @param surveyId
	 *            The survey ID for which this message sequence is linked to and
	 *            this should be same as the value in preface.xml
	 * @param whereStr
	 *            The sql whereStr which is used to get the details of the
	 *            person to whom the email has to be sent from the invitee
	 *            table.
	 * @param isReminder
	 *            If the message is a reminder or not.
	 * @return String output message or message use ID for the invitee to whom
	 *         email is sent.
	 */
	public String sendMessages(String msgType, String messageSeqId, String surveyId,
			String whereStr, boolean isReminder) {
		return this.getMyStudySpace().sendInviteReturnDisplayMessage(msgType, messageSeqId,
				surveyId, whereStr, isReminder);
	}

	// TODO: move the data base access to seperate class.
	/**
	 * Gets the non-responders and incompleters of the survey from the tables.
	 * 
	 * @param spId
	 *            Array which contains the invitees who are non responders and
	 *            incompleters and is sent as output
	 * @param sId
	 *            Survey Id for which all non responders and incompleters
	 *            invitees have to listed.
	 */
	public void getNonrespondersIncompleters(String[] spId, String sId) {
		this.getMyStudySpace().getNonrespondersIncompleters(spId, sId);
	}

	/**
	 * Creates a CSV files.
	 * 
	 * @param filename
	 *            File name from which the table name is obtained.
	 * @return true If the CSV file is created & written successfully, otherwise
	 *         it returns false.
	 */
	public String buildCsvString(String filename) {
		return this.myStudySpace.buildCsvString(filename);
	}

	/**
	 * Returns a string form of the file read from the file system.
	 * 
	 * @param filePath
	 *            Path where to find the files.
	 * @param fileName
	 *            Name of the file to be read.
	 * @return String String form of the given filename
	 */
	public String buildSql(String filePath, String fileName) {

		LOGGER.info("The file name and filePath are :" + filePath + " " + fileName);
		InputStream fileInputStream = CommonUtils.loadResource(filePath + fileName);
		StringBuffer strBuff = new StringBuffer();
		int ch;

		if (fileInputStream != null) {
			try {
				while ((ch = fileInputStream.read()) != -1) {
					strBuff.append(Character.valueOf((char) ch));
				}
			} catch (IOException e) {
				LOGGER.error("I/O error occured", e);
				return strBuff.toString();
			} finally {
				if (fileInputStream != null) {
					try {
						fileInputStream.close();
					} catch (IOException e) {
						LOGGER.error("I/O error occured", e);
					}
				}
			}
		}
		return strBuff.toString();
	}

	/**
	 * Print invite list of users for a studySpace.
	 * 
	 * @return String HTML format of all the users in the system.
	 */
	public String printInvite() {
		return this.getMyStudySpace().printInvite();
	}

	/**
	 * Returns HTML showing counts of users in each state.
	 * 
	 * @param surveyId
	 *            Survey Id for which all non responders and incompleters
	 *            invitees have to listed.
	 * @return String Returns HTML format of the count of invitees in various
	 *         states.
	 */
	public String getUserCountsInStates(String surveyId) {
		return this.getMyStudySpace().getUserCountsInStates(surveyId);
	}

	/**
	 * Prints the user groups identified by their states.
	 * 
	 * @param state
	 *            State of the invitees who are supposed to be returned.
	 * @param surveyId
	 *            Survey Id for which invitees have to be listed.
	 * @return String HTML format of the invitees who belong to a state given.
	 */
	public String printUserState(String state, String surveyId) {
		return this.getMyStudySpace().printUserState(state, surveyId);
	}

	/**
	 * Prints the Audit logs of the users
	 * 
	 * @return String HTML format of the audit logs
	 */
	public String printAuditLogs() {
		return this.getMyStudySpace().printAuditLogs();
	}

	/**
	 * Forms a remote URL
	 * 
	 * @param fileType
	 *            File type name.
	 * @param studyName
	 *            Study space name.
	 * @return String URL formed
	 */
	public String makeRemoteURL(String fileType, String surveyName) {
		String urlStr = this.getMyStudySpace().servletUrlRoot + WiseConstants.SURVEY_APP + "/"
				+ "admin_" + fileType + "_loader" + "?SID=" + this.getStudyId() + "&SurveyName="
				+ surveyName;
		return urlStr;
	}

	/**
	 * Loads the remote URL.
	 * 
	 * @param fileType
	 *            File name to form URL.
	 * @param studyName
	 *            Study space name.
	 * @return String
	 */
	public String loadRemote(String fileType, String studyName) {
		String urlStr = this.makeRemoteURL(fileType, studyName);
		LOGGER.info("The Url accessed while uploading the data is " + urlStr);
		String uploadResult = "";
		// URL url = null;
		BufferedReader in = null;
		String currentLine = null;

		try {
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate,
						String paramString) throws CertificateException {
				}

				@Override
				public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate,
						String paramString) throws CertificateException {
				}
			};

			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[] { tm }, null);
			URLConnection conn = new URL(urlStr).openConnection();
			/*
			 * conn.setSSLSocketFactory(ctx.getSocketFactory());
			 * conn.setHostnameVerifier(new HostnameVerifier() {
			 * 
			 * @Override public boolean verify(String paramString, SSLSession
			 * paramSSLSession) { return true; } });
			 */
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while ((currentLine = in.readLine()) != null) {
				// System.out.println(input);
				uploadResult += currentLine;
			}
			in.close();
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Reader failed to read due to ", e);
			LOGGER.error("Wise error: Remote " + fileType + " load error after" + uploadResult
					+ ": " + e.toString(), e);
		} catch (KeyManagementException e) {
			LOGGER.error("Reader failed to read due to ", e);
			LOGGER.error("Wise error: Remote " + fileType + " load error after" + uploadResult
					+ ": " + e.toString(), e);
		} catch (MalformedURLException e) {
			LOGGER.error("Reader failed to read due to ", e);
			LOGGER.error("Wise error: Remote " + fileType + " load error after" + uploadResult
					+ ": " + e.toString(), e);
		} catch (IOException e) {
			LOGGER.error("Reader failed to read due to ", e);
			LOGGER.error("Wise error: Remote " + fileType + " load error after" + uploadResult
					+ ": " + e.toString(), e);
		} catch (Exception e) {
			LOGGER.error(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// That's okie!
					LOGGER.error("Reader Stream close failure ", e);
				}
			}
		}
		return uploadResult;
	}

	/**
	 * Return the complete URL to the servlet root directory for the application
	 * administering the survey
	 * 
	 * @return String Root servlet URL.
	 */
	public String getStudyServerPath() {
		return this.getMyStudySpace().servletUrlRoot;
	}

	// TODO: READ the databank at call time rather than relying on JSP tool to
	// know proper status
	/**
	 * Clears, drops, or archives survey data depending on survey's status. D -
	 * Clear submitted data from surveys in Development mode R - Remove entire
	 * survey in Development mode P - clean up and archive the data of surveys
	 * in production mode
	 * 
	 * @param surveyId
	 *            Survey name whose status has to be changed.
	 * @param surveyStatus
	 *            Status of the survey to be changed to.
	 * @return String
	 */
	public String clearSurvey(String surveyId, String surveyStatus) {
		if ((surveyId == null) || (this.getStudyId() == null) || (surveyStatus == null)) {
			return "<p align=center>SURVEY clear ERROR: can't get the survey id/status or study id </p>";
		}
		DataBank db = this.getMyStudySpace().db;
		Survey survey = this.getMyStudySpace().getSurvey(surveyId);
		if (surveyStatus.equalsIgnoreCase("D")) {
			return "This feature is undergoing changes. Please contact the developers.";
		} else if (surveyStatus.equalsIgnoreCase("R")) {
			return db.deleteSurvey(survey);
		} else if (surveyStatus.equalsIgnoreCase("P")) {
			return db.archiveProdSurvey(survey);
		}
		return "Unrecognized Survey Status/Type";
	}

	/**
	 * Returns number of login attempt.
	 * 
	 * @return the loginAttemptNumbers
	 */
	public static ConcurrentHashMap<String, Integer> getLoginAttemptNumbers() {
		return loginAttemptNumbers;
	}

	/**
	 * Returns the last logged in time.
	 * 
	 * @return the lastlogintime
	 */
	public static ConcurrentHashMap<String, Long> getLastlogintime() {
		return lastLoginTime;
	}

	public String getStudyXmlPath() {
		return this.studyXmlPath;
	}

	public String getStudyTitle() {
		return this.studyTitle;
	}

	public String getStudyImagePath() {
		return this.studyImagePath;
	}

	public AdminDataBank getAdminDataBank() {
		return this.adminDataBank;
	}

	public StudySpace getMyStudySpace() {
		return this.myStudySpace;
	}

	public String getStudyName() {
		return this.studyName;
	}

	public String getStudyId() {
		return this.studyId;
	}
}
