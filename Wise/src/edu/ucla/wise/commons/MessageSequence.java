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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a message sequence and its properties.
 */
public class MessageSequence {
    public static final Logger LOGGER = Logger.getLogger(MessageSequence.class);

    /** Instance Variables */
    public Preface myPref;
    public String id, surveyId, irbId;
    public String fromString = "";
    private String fromEmail = null;
    private String replyString = null;

    /* this holds both address and name components, unlike "from" */

    // public Hashtable message_hash;
    public Message inviteMsg;
    public Message interruptMsg;
    public Message doneMsg;
    public Message reviewMsg;
    ArrayList<Message> startReminders, completionReminders, otherMsgs;

    /**
     * Constructor for Message Sequences
     * 
     * @param sourceNode
     *            XML DOM message sequence node.
     * @param prefaceParam
     *            Preface object to which this sequence is related to.
     */
    public MessageSequence(Node sourceNode, Preface prefaceParam) {
        try {
            String errTemp = "";
            this.startReminders = new ArrayList<Message>();
            this.completionReminders = new ArrayList<Message>();
            this.otherMsgs = new ArrayList<Message>();
            this.myPref = prefaceParam;

            /*
             * parse out the message sequence attributes: ID, survey ID and IRB
             * ID if has
             */
            this.id = "";
            this.surveyId = "";
            this.irbId = "";

            /* ID & survey ID are required -- don't need to check for nulls */
            this.id = sourceNode.getAttributes().getNamedItem("ID").getNodeValue();
            this.surveyId = sourceNode.getAttributes().getNamedItem("Survey_ID").getNodeValue();

            /* IRB ID is optional */
            Node attrNode = sourceNode.getAttributes().getNamedItem("IRB_ID");
            if (attrNode != null) {
                this.irbId = attrNode.getNodeValue();
            }

            /* From String is optional */
            attrNode = sourceNode.getAttributes().getNamedItem("From_String");
            if (attrNode != null) {
                this.fromString = attrNode.getNodeValue();
                this.fromString = this.fromString.replaceAll(",", "");
            }

            attrNode = sourceNode.getAttributes().getNamedItem("From_Email");
            if (attrNode != null) {
                this.fromEmail = attrNode.getNodeValue(); // TODO: validate
                // presence of @
            }
            if (this.fromEmail == null) {
                this.fromEmail = WISEApplication.getInstance().getWiseProperties().getEmailFrom(); // always
                // assign
                // default
                // email
                // here
            }
            attrNode = sourceNode.getAttributes().getNamedItem("Reply_Email");
            if (attrNode != null) {
                this.replyString = attrNode.getNodeValue(); // TODO: validate
                // presence of @
                attrNode = sourceNode.getAttributes().getNamedItem("Reply_String");
                if (attrNode != null) {
                    this.replyString = attrNode.getNodeValue().replaceAll(",", "") + " <" + this.replyString + ">";
                }
            }

            NodeList msgNodeList = sourceNode.getChildNodes();
            for (int i = 0; i < msgNodeList.getLength(); i++) {

                /* create the messages for each stage in the message sequence */
                Node childNode = msgNodeList.item(i);
                String nodeName = childNode.getNodeName();
                Message newMsg = null;
                try {
                    if (nodeName.equalsIgnoreCase("Initial_Invitation")) {
                        newMsg = new Message(childNode);
                        this.inviteMsg = newMsg;
                    } else if (nodeName.equalsIgnoreCase("Interrupt")) {
                        newMsg = new Message(childNode);
                        this.interruptMsg = newMsg;
                    } else if (nodeName.equalsIgnoreCase("Done")) {
                        newMsg = new Message(childNode);
                        this.doneMsg = newMsg;
                    } else if (nodeName.equalsIgnoreCase("Review")) {
                        newMsg = new Message(childNode);
                        this.reviewMsg = newMsg;
                    } else if (nodeName.equalsIgnoreCase("Start_Reminder")) {

                        /* create the reminder class */
                        newMsg = new Reminder(childNode);
                        this.startReminders.add(newMsg);
                    } else if (nodeName.equalsIgnoreCase("Completion_Reminder")) {
                        newMsg = new Reminder(childNode);
                        this.completionReminders.add(newMsg);
                    } else if (nodeName.equalsIgnoreCase("Message")) {
                        newMsg = new Message(childNode);
                        this.otherMsgs.add(newMsg);
                    }
                } catch (RuntimeException e) {
                    LOGGER.error(
                            "Msg SEQ Choke at Parsing message" + nodeName + ". After:" + i + "\n" + errTemp
                                    + e.toString(), e);
                }

                /* save the message here and in preface's master index */
                try {
                    if (newMsg != null) {
                        this.myPref.addMessage(newMsg, this);
                    }
                } catch (RuntimeException e) {
                    LOGGER.error("Msg SEQ Choke at Adding " + nodeName + ". After:\n" + errTemp + e.toString(), e);
                }
            }
        } catch (DOMException e) {
            LOGGER.error(
                    "WISE - MESSAGE SEQUENCE: ID = " + this.id + "; Survey ID = " + this.surveyId + " --> "
                            + e.toString(), null);
            return;
        }
    }

    /**
     * Returns the from email address.
     * 
     * @return String Return from email id if the instance variables
     *         (fromString/fromEmail) are null default from address is sent.
     */
    public String getFromString() {

        /* should actually be initialized to this; just checking */
        if (this.fromEmail == null) {
            return WISEApplication.getInstance().getWiseProperties().getEmailFrom();
        }
        if (this.fromString == null) {
            return this.fromEmail;
        }

        /* brackets already added for now */
        return this.fromString + " <" + this.fromEmail + ">";
    }

    /**
     * Returns the body of the message.
     * 
     * @return String Body of the message.
     */
    public String getReplyString() {

        /* returns null if not specified */
        return this.replyString;
    }

    /**
     * Returns just the id part of the email for example if id is xxxx@yyyy.zzz
     * it returns xxxx.
     * 
     * @return String The id part of the email.
     */
    public String emailID() {
        if (this.fromEmail == null) {
            return null;
        }
        int atindx = this.fromEmail.indexOf('@');
        if (atindx > 0) {
            return this.fromEmail.substring(0, atindx);
        } else {
            return this.fromEmail;
        }
    }

    /**
     * Returns the requested message type from the sequence.
     * 
     * @param messageType
     *            The type of the message that is to be returned from the
     *            message sequence. Types can be invite/interrupt/done/review
     * 
     * @return Message The message of the requested type is returned.
     */
    public Message getTypeMessage(String messageType) {

        /* use integer to get one of the other messages */
        if (messageType.equalsIgnoreCase("invite")) {
            return this.inviteMsg;
        } else if (messageType.equalsIgnoreCase("interrupt")) {
            return this.interruptMsg;
        } else if (messageType.equalsIgnoreCase("done")) {
            return this.doneMsg;
        } else if (messageType.equalsIgnoreCase("review")) {
            return this.reviewMsg;
        } else {
            int index = Integer.parseInt(messageType);
            return this.otherMsgs.get(index);
        }
    }

    /**
     * Returns the indexed start reminder message.
     * 
     * @param index
     *            Index of the message in the message sequence.
     * @return Reminder returns the required reminder.
     */
    public Reminder getStartReminder(int index) {
        return (Reminder) this.startReminders.get(index);
    }

    /**
     * Returns the indexed Completion reminder message.
     * 
     * @param index
     *            Index of the message in the message sequence.
     * @return Reminder returns the required reminder.
     */
    public Reminder getCompletionReminder(int index) {
        return (Reminder) this.completionReminders.get(index);
    }

    /**
     * Returns the total number of start reminder messages.
     * 
     * @return int total number of start reminders.
     */
    public int totalStartReminders() {
        return this.startReminders.size();
    }

    /**
     * Returns the total number of completion reminder messages.
     * 
     * @return int total number of completion reminders.
     */
    public int totalCompletionReminders() {
        return this.completionReminders.size();
    }

    /**
     * Returns the total number of other messages.
     * 
     * @return int total number of other messages.
     */
    public int totalOtherMessages() {
        return this.otherMsgs.size();
    }

    /**
     * Returns the information about this message sequence in form of string.
     * 
     * @return String String format of the message sequence.
     */
    @Override
    public String toString() {
        String resp = "<b>Message Sequence: " + this.id + "</b> for survey ID(s): " + this.surveyId
                + "<br>Messages<br>";
        resp += this.inviteMsg.toString();
        resp += this.interruptMsg.toString();
        resp += this.doneMsg.toString();
        resp += "Start reminders n=" + this.startReminders.size() + "; Completion reminders n="
                + this.completionReminders.size() + "<br>";
        return resp;
    }
}
