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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.databank.DataBank;

/**
 * This class is interface between admin and the database.
 */
public class AdminDataBank {

    public static final Logger LOGGER = Logger.getLogger(AdminDataBank.class);
    private final DataBank db;

    /**
     * Constructor setting up data storage for a admin session.
     * 
     * @param ss
     *            Study space to which the data bank class is linked to.
     */
    public AdminDataBank(DataBank db) {
        this.db = db;
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
        String outputString = "";
        String sql = "SELECT i.id, firstname, lastname, salutation, irb_id, state, "
                + "email FROM invitee as i, survey_user_state as s where i.id=s.invitee and survey= ?"
                + " ORDER BY i.id";
        String sqlm = "select invitee from survey_user_state where state='declined' and invitee= ?";
        Connection conn = null;
        PreparedStatement stmt = null;
        PreparedStatement stmtm = null;

        try {
            conn = this.db.getDBConnection();
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
                outputString += "<td><input type='checkbox' name='user' value='" + rs.getString(1) + "'></td>";
                outputString += "<td>" + rs.getString(1) + "</td>";
                outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " " + rs.getString(3) + "</td>";
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

    /**
     * Returns invitees belonging to different IRB Ids..
     * 
     * @return Hashtable hash table of keys as irb id and value as all the
     *         invitees belonging to this irb id.
     */
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
            conn = this.db.getDBConnection();
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
    public String renderInitialInviteTable(String surveyId, boolean isReminder, StudySpace ss) {
        String outputString = "";
        MessageSequence[] msgSeqs = ss.preface.getMessageSequences(surveyId);
        if (msgSeqs.length == 0) {
            return "No message sequences found in Preface file for selected Survey.";
        }
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = this.db.getDBConnection();
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
                    String sql = this.buildInitialInviteQuery(surveyId, msgSeq.id, irbName, isReminder);
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
                        outputString += "<td><input type='checkbox' name='user' value='" + rs.getString(1) + "'></td>";
                        outputString += "<td>" + rs.getString(1) + "</td>";
                        outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " " + rs.getString(3)
                                + "</td>";
                        outputString += "<td>" + rs.getString(5) + "</td>";
                        outputString += "<td>" + rs.getString(6) + "</td>";
                        outputString += "</tr>";
                    }
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.error("ADMIN Data Bank error - render_initial_invite_table: " + e.toString(), e);
                }
                outputString += "</table><p align='center'>"
                        +
                        // TODO: resolve file path references between admin and
                        // survey applications
                        "<input type='image' alt='Click to send email. This button is the same as one above.' src='admin_images/send.gif'>"
                        + "</p></form>";
            } // for
        } catch (SQLException e) {
            LOGGER.error("ADMIN Data Bank connection error - renderInitialInviteTable: " + e.toString(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                LOGGER.error("ADMIN Data Bank connection error - renderInitialInviteTable: " + e.toString(), e);
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
    private String buildInitialInviteQuery(String surveyId, String msgSeq, String irbName, boolean isReminder) {
        StringBuffer strBuff = new StringBuffer();
        if (isReminder) {
            strBuff.append("SELECT I.id, I.firstname, I.lastname, I.salutation, I.irb_id, AES_DECRYPT(I.email,'"
                    + this.db.getEmailEncryptionKey() + "') FROM invitee as I, survey_user_state as S WHERE I.irb_id "
                    + irbName + " AND I.id not in (select invitee from survey_user_state where survey='" + surveyId
                    + "' AND state like 'completed') AND I.id=S.invitee AND S.message_sequence='" + msgSeq
                    + "' ORDER BY id");
        } else {
            strBuff.append("SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email,'"
                    + this.db.getEmailEncryptionKey() + "') FROM invitee WHERE irb_id " + irbName
                    + " AND id not in (select invitee from survey_user_state where survey='" + surveyId + "')"
                    + "ORDER BY id");
        }
        return strBuff.toString();
    }

    /**
     * print table of all sendable invitees, all invitees, by message sequence
     * (& therefore irb ID)
     * 
     * @param surveyId
     *            Survey Id for which all invitees have to listed.
     * @param StudySpace
     *            StudySpace whose list of invitees have to be displayed.
     * @return String HTML table format of all the invitees under the given
     *         survey id.
     */
    public String renderInviteTable(String surveyId, StudySpace ss) {
        String outputString = "";
        MessageSequence[] msgSeqs = ss.preface.getMessageSequences(surveyId);
        if (msgSeqs.length == 0) {
            return "No message sequences found in Preface file for selected Survey.";
        }
        String sql = "SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email, '"
                + this.db.getEmailEncryptionKey() + "') FROM invitee WHERE irb_id = ?" + " ORDER BY id";
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.db.getDBConnection();
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
                        "<input type='hidden' name='svy' value='" + surveyId + "'>\n" + "Using Message Sequence <B>"
                        + msgSeq.id + "</b> (designated for IRB " + irbName + ")<BR>\n" + "...SEND Message: <BR>"
                        + "<input type='radio' name='message' value='invite'>\n" + "<a href='print_msg_body.jsp?seqID="
                        + msgSeq.id + "&msgID=invite' target='_blank'>" + msg.subject + "</a><br>\n";
                for (int j = 0; j < msgSeq.totalOtherMessages(); j++) {
                    msg = msgSeq.getTypeMessage("" + j);
                    outputString += "<input type='radio' name='message' value='" + j + "'>\n"
                            + "<a href='print_msg_body.jsp?seqID=" + msgSeq.id + "&msgID=" + j + "' target='_blank'>"
                            + msg.subject + "</a><br>\n";
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
                        outputString += "<td><input type='checkbox' name='user' value='" + rs.getString(1) + "'></td>";
                        outputString += "<td>" + rs.getString(1) + "</td>";
                        outputString += "<td>" + rs.getString(4) + " " + rs.getString(2) + " " + rs.getString(3)
                                + "</td>";
                        outputString += "<td>" + rs.getString(5) + "</td>";
                        outputString += "<td>" + rs.getString(6) + "</td>";
                        outputString += "</tr>";
                    }
                    rs.close();
                } catch (SQLException e) {
                    LOGGER.error("ADMIN Data Bank error - render_initial_invite_table: " + e.toString(), e);
                }
                outputString += "</table><p align='center'>"
                        +
                        // TODO: resolve file path references between admin and
                        // survey applications
                        "<input type='image' alt='Click to send email. This button is the same as one above.' src='admin_images/send.gif'>"
                        + "</p></form>";
            } // for
            conn.close();
        } catch (SQLException e) {
            LOGGER.error("ADMIN Data Bank DB connection error - renderInitialInviteTable: " + e.toString(), e);
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException e) {
                LOGGER.error("ADMIN Data Bank connection error - renderInitialInviteTable: " + e.toString(), e);
            }
        }
        return outputString;
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
        String outputString = "";
        Connection conn = null;
        PreparedStatement stmt = null;

        /* select the invitees without any states */
        String sql = "SELECT id, firstname, lastname, salutation, irb_id, AES_DECRYPT(email, '"
                + this.db.getEmailEncryptionKey()
                + "') FROM invitee WHERE id not in (select invitee from survey_user_state where survey= ?"
                + ") ORDER BY id";
        try {
            conn = this.db.getDBConnection();
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
                outputString += "<td><input type='text' name='fname" + rs.getString(1) + "' value='" + rs.getString(2)
                        + "'/></td>";
                outputString += "<td><input type='text' name='lname" + rs.getString(1) + "' value='" + rs.getString(3)
                        + "'/></td>";
                outputString += "<td><input type='text' name='irb" + rs.getString(1) + "' value='" + rs.getString(5)
                        + "'/></td>";
                outputString += "<td><input type='text' name='email" + rs.getString(1) + "' value='" + rs.getString(6)
                        + "'/></td>";
                outputString += "<td><a href='javascript:update_inv(" + rs.getString(1) + ");'> Update </a><br>"
                        + "<a href='javascript:delete_inv(" + rs.getString(1) + ");'> Delete </a>" + "</td>";
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

}
