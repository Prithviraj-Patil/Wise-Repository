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
package edu.ucla.wise.commons;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.oreilly.servlet.MultipartRequest;

import edu.ucla.wise.admin.view.SurveyInformation;
import edu.ucla.wise.commons.databank.DataBank;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;
import edu.ucla.wise.web.WebResponseMessage;

/**
 * Study space is the core of WISE system -- represents the core abstractions
 * for individual survey projects.
 */
public class StudySpace {

    public static final Logger LOGGER = Logger.getLogger(StudySpace.class);
    public static String font = "<font face='Verdana, Arial, Helvetica, sans-serif' size='-1'>";

    /** INSTANCE VARIABLES */
    public HashMap<String, Survey> surveys;
    public Preface preface;

    public String id; // the study_space's number, which can be encoded
    public String studyName;
    public String title;

    // DIRECTORIES AND PATHS
    public String serverUrl;
    public String dirName;
    private String prefacePath;
    private String application;
    public String appUrlRoot, servletUrlRoot;
    public String sharedFileUrlRoot, styleUrl, imageUrl;
    public String emailSendingTime;

    public DataBank db; // one DB per SS

    /**
     * Constructor to create study space and initialize the surveys & messages
     * hashtables
     * 
     * @param studyName
     *            Name of the study space that has to be initialized.
     */
    public StudySpace(String studyName, StudySpaceParameters parameters) {
        if ((studyName == null) || studyName.equals("")) {// will still return
            // an uninitialized
            // instance
            return;
        }
        this.studyName = studyName;

        this.db = new DataBank(this, parameters); // one DB per SS

        /* Construct instance variables for this particular study space */
        this.id = parameters.getId();
        this.title = parameters.getProjectTitle();
        /*
         * SET UP all of the paths that will apply for this Study Space,
         * regardless of the app instantiating it
         */
        this.serverUrl = parameters.getServerUrl();
        String dirInProps = parameters.getFolderName();
        if (dirInProps == null) {
            this.dirName = studyName; // default
        } else {
            this.dirName = dirInProps;
        }
        this.application = parameters.getServerApplication();
        this.emailSendingTime = parameters.getEmailSendingTime();
        this.appUrlRoot = this.serverUrl + "/" + this.application + "/";
        this.servletUrlRoot = this.serverUrl + "/" + this.application + "/";
        this.sharedFileUrlRoot = this.appUrlRoot + parameters.getSharedFiles_linkName() + "/";
        /*
         * project-specific styles and images need to be in shared area so they
         * can be uploaded by admin server
         */
        this.styleUrl = this.sharedFileUrlRoot + "style/" + this.dirName + "/";
        this.imageUrl = this.sharedFileUrlRoot + "images/" + this.dirName + "/";

        /* create & initialize the Preface */
        this.prefacePath = "WISE/" + this.dirName + "/preface.xml";
        this.loadPreface();

        /* create the message sender */
        this.surveys = new HashMap<>();
        this.db.readSurveys();
    }

    /**
     * Deconstructor to destroy the surveys and messages hashtables
     */
    public void destroy() {
        this.surveys = null;
    }

    /**
     * Returns the DataBank class that is linked to this studySpace.
     * 
     * @return DataBank DataBank instance.
     */
    public DataBank getDB() {
        return this.db;
    }

    /**
     * Load or Reload a survey from file, return survey ID or null if
     * unsuccessful
     * 
     * @param filename
     *            File from which the survey has to be loaded. *
     * @return String The survey Id that has been loaded.
     */
    public String loadSurvey(String filename) {
        String sid = null;
        Survey s;
        try {
            // String file_loc = SurveyorApplication.xmlLoc
            // + System.getProperty("file.separator") + dirName
            // + System.getProperty("file.separator") + filename;
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setCoalescing(true);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);

            /*
             * Document xml_doc = factory.newDocumentBuilder().parse(
             * CommonUtils.loadResource(file_loc));
             */

            LOGGER.info("Fetching survey file " + filename + " from database for " + this.studyName);
            InputStream surveyFileInputStream = this.db.getXmlFileFromDatabase(filename);

            if (surveyFileInputStream == null) {
                throw new FileNotFoundException();
            }

            Document xmlDoc = factory.newDocumentBuilder().parse(surveyFileInputStream);

            s = new Survey(xmlDoc, this);
            if (s != null) {
                sid = s.getId();
                this.surveys.put(sid, s);
            }

        } catch (DOMException e) {
            LOGGER.error("WISE - SURVEY parse error: " + e.toString() + "\n" + this.id + "\n" + this.toString(), null);

        } catch (FileNotFoundException e) {
            LOGGER.error("Study Space " + this.dirName + " failed to parse survey " + filename + ". Error: " + e, e);
        } catch (SAXException e) {
            LOGGER.error("Study Space " + this.dirName + " failed to parse survey " + filename + ". Error: " + e, e);
        } catch (ParserConfigurationException e) {
            LOGGER.error("Study Space " + this.dirName + " failed to parse survey " + filename + ". Error: " + e, e);
        } catch (IOException e) {
            LOGGER.error("Study Space " + this.dirName + " failed to parse survey " + filename + ". Error: " + e, e);
        }
        return sid;
    }

    /**
     * Drops survey related to surveyId from the study space.
     * 
     * @param surveyId
     *            Id of the survey to be dropped.
     */
    public void dropSurvey(String surveyId) {
        this.surveys.remove(surveyId);
    }

    /**
     * Loads the preface file
     * 
     * @return boolean True if the preface load happens correctly else false.
     */
    public boolean loadPreface() {

        // TODO: check admin; call when new preface uploaded
        this.preface = new Preface(this, "preface.xml");
        if (this.preface == null) {
            return false;
        }
        this.preface.setHrefs(this.servletUrlRoot, this.imageUrl);
        return true;
    }

    /**
     * Gets a preface
     * 
     * @return Preface preface object related to this study space is returned.
     */
    public Preface get_preface() {
        if (this.preface == null) {// should happen only if there's been some
            // major problem
            if (!this.loadPreface()) {
                LOGGER.info("Study Space " + this.dirName + " failed to load its preface file ");
                return null;
            }
        }
        return this.preface;
    }

    /**
     * Returns the User object linked with the message ID.
     * 
     * @param msgId
     *            Message Id from the URL whose user object is to be
     *            initialized. *
     * @return User Newly created User Object from the provided message ID.
     */
    public User getUser(String msgId) {
        return this.db.makeUserFromMsgID(msgId);
    }

    /**
     * Method that calls the private send method to send emails to users.
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
     * @param displayMessage
     *            This is used to send the message back as output for the case
     *            of anonymous users
     * @return String output message or message use ID for the invitee to whom
     *         email is sent.
     */
    public String sendInviteReturnDisplayMessage(String msg_type, String message_seq_id, String survey_id,
            String whereStr, boolean isReminder) {
        return this.sendMessages(msg_type, message_seq_id, survey_id, whereStr, isReminder, true);
    }

    /**
     * Method that calls the private send method to send emails to users.
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
     * @param displayMessage
     *            This is used to send the message back as output for the case
     *            of anonymous users
     * @return String output message or message use ID for the invitee to whom
     *         email is sent.
     */
    public String sendInviteReturnMsgSeqId(String msg_type, String message_seq_id, String survey_id, String whereStr,
            boolean isReminder) {
        return this.sendMessages(msg_type, message_seq_id, survey_id, whereStr, isReminder, false);
    }

    /**
     * private method which Prepares the message for email depending on the
     * message sequence then sends it to the invitee.
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
     * @param displayMessage
     *            This is used to send the message back as output for the case
     *            of anonymous users
     * @return String output message or message use ID for the invitee to whom
     *         email is sent.
     */
    private String sendMessages(String msgType, String msgSeqId, String surveyId, String whereStr, boolean isReminder,
            boolean displayMessage) {
        /* look up the correct message sequence in preface */
        MessageSequence msgSeq = this.preface.getMessageSequence(msgSeqId);
        if (msgSeq == null) {
            LOGGER.info("ADMIN INFO - SEND MESSAGES: Can't get the requested  message sequence " + msgSeqId
                    + AdminApplication.class.getSimpleName());
            return null;
        }
        Message msg = msgSeq.getTypeMessage(msgType); // passes thru an integer
        // for 'other' messages
        if (msg == null) {
            LOGGER.info("ADMIN INFO - SEND MESSAGES: Can't get the message from hash");
            return null;
        }

        /*
         * If the call comes from UI, we return outputString, if the call comes
         * from the anno user trying to take the survey we return messageSeqid
         * to the caller.
         */
        return this.db.generateEmailMessage(surveyId, msgSeq, msg, msgType, msgSeqId, whereStr, displayMessage);
    }

    /**
     * Prints a specific study space
     */
    @Override
    public String toString() {
        String s = "STUDY SPACE<br>";
        s += "ID: " + this.id + "<br>";
        s += "Location: " + this.dirName + "<br>";
        s += "Study Name: " + this.studyName + "<br>";
        // s += "DB Password: "+dbpwd+"<p>";

        /* print surveys */
        s += "<hr>SURVEYS<BR>";

        for (Entry<String, Survey> surveyEntry : this.surveys.entrySet()) {
            s += surveyEntry.getValue().toString();
        }

        s += "<hr>PREFACE<BR>";
        s += this.preface.toString();
        return s;
    }

    public String printInviteeWithState(String surveyId) {

        return this.db.printInvitee(surveyId);

    }

    public Survey getSurvey(String surveyId) {
        return this.surveys.get(surveyId);
    }

    public String viewOpenResults(String question, Survey survey, String page, String whereClause, String unanswered) {
        return this.db.viewOpenResults(question, survey, page, whereClause, unanswered);
    }

    public Hashtable<String, String> getIrbGroups() {

        return this.db.getIrbGroups();
    }

    public String printAuditLogs() {
        return this.db.printAuditLogs();
    }

    public String printUserState(String state, String surveyId) {
        return this.db.printUserState(state, surveyId);
    }

    public String renderInitialInviteTable(String surveyId, boolean isReminder) {
        return this.db.renderInitialInviteTable(surveyId, isReminder);
    }

    public String getUserCountsInStates(String surveyId) {
        return this.db.getUserCountsInStates(surveyId);
    }

    public String renderInviteTable(String surveyId) {
        return this.db.renderInviteTable(surveyId);
    }

    public String printInvite() {
        return this.db.printInvite();
    }

    public String buildCsvString(String filename) {
        return this.db.buildCsvString(filename);
    }

    public String printInitialInviteeEditable(String surveyId) {
        return this.db.printInitialInviteeEditable(surveyId);
    }

    public boolean updateInvitees(String delFlag, String updateID, Map<String, String[]> parameters) {
        return this.db.updateInvitees(delFlag, updateID, parameters);
    }

    public String printInterviewer() {
        return this.db.printInterviewer();
    }

    public void getNonrespondersIncompleters(String[] spId, String sId) {
        this.db.getNonrespondersIncompters(spId, sId);
    }

    public void registerCompletionInDB(String user, String surveyId) {
        this.db.registerCompletionInDB(user, surveyId);

    }

    public void changeDevToProd(String internalId) {
        this.db.changeDevToProd(internalId);
    }

    public void saveFileToDatabase(MultipartRequest multi, String filename, String tableName, String studySpaceName) {
        this.db.saveFileToDatabase(multi, filename, tableName, studySpaceName);
    }

    public String processSurveyFile(Document doc) {
        return this.db.processSurveyFile(doc);
    }

    public void processInviteesCsvFile(File f) {
        this.db.processInviteesCsvFile(f);
    }

    public void archiveOldAndCreateNewDataTable(Survey survey, String surveyID) {
        this.db.archiveOldAndCreateNewDataTable(survey, surveyID);

    }

    public String getNewId() {
        return this.db.getNewId();
    }

    public String addInterviewer(Interviewer interviewer) {

        return this.db.addInterviewer(interviewer);

    }
    
    public boolean removeInterviewer(String interviewers) {

        return this.db.removeInterviewer(interviewers);

    }

    public String saveProfile(Interviewer interviewer) {
        return this.db.saveProfile(interviewer);
    }

    public Interviewer getInterviewer(String interviewId) {
        return this.db.getInterviewer(interviewId);
    }

    public Interviewer verifyInterviewer(String interviewId, String interviewUsername) {
        return this.db.verifyInterviewer(interviewId, interviewUsername);
    }

    public String createSurveyMessage(String inviteeId, String surveyId) {
        return this.db.createSurveyMessage(inviteeId, surveyId);
    }

    public void beginInterviewSession(String userSession) {
        this.db.beginInterviewSession(userSession);
    }

    public void saveInterviewSession(String interviewAssignId) {
        this.db.saveInterviewSesssion(interviewAssignId);
    }

    public void checkDbHealth() {
        this.db.checkDbHealth();
    }

    public List<SurveyInformation> getCurrentSurveys() {

        return this.db.getCurrentSurveys();
    }

    public WebResponseMessage modifyInviteeTable(String editType, String colName, String colValue, String colDef,
            String colOName) {
        return this.db.modifyInviteeTable(editType, colName, colValue, colDef, colOName);

    }

    public WebResponseMessage describeInviteeTable() {

        return this.db.describeInviteeTable();
    }

    public String printAdminResults(String surveyId) {
        return this.db.printAdminResults(surveyId);
    }

    public String listInterviewer() {
        return this.db.listInterviewer();
    }

    public String reassignWati(String[] inviteePending, String[] inviteeReassign, String interviewerId,
            String surveyId, Map<String, String[]> webParameters) {
        return this.db.reassignWati(inviteePending, inviteeReassign, interviewerId, surveyId, webParameters);
    }

    public String removeProfile(String[] interviewer) {
        return this.db.removeProfile(interviewer);
    }

    public String saveWati(String whereStr, String interviewerId, String surveyId) {
        return this.db.saveWati(whereStr, interviewerId, surveyId);
    }
}
