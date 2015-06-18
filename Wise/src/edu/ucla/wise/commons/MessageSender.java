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

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.databank.DataBank;
import edu.ucla.wise.email.EmailMessage;
import edu.ucla.wise.email.EmailProperties;

/**
 * This class encapsulates some specific methods to send messages from Message
 * Sequences.
 */
public class MessageSender {
    public static final Logger LOGGER = Logger.getLogger(MessageSender.class);
    /** Instance Variables */
    private final String fromStr, replyStr;

    /**
     * Constructor : gets the email session from WISE Application and other
     * instance variables from MessageSequence.
     * 
     * @param msgSeq
     *            Message sequence for which sender has to be created.
     */
    public MessageSender(MessageSequence msgSeq) {
        this.fromStr = msgSeq.getFromString();
        this.replyStr = msgSeq.getReplyString();
    }

    /**
     * looks up, compose, and send email message
     * 
     * @param msg
     *            Message object to be sent to the User.
     * @param messageUseID
     *            The id used for generating URL which is unique to the user.
     * @param toUser
     *            User to whom the message has to be sent.
     * @param db
     *            Data bank object used from talking to DB.
     * @return String Empty string if successful.
     */
    public String sendMessage(Message msg, String messageUseID, User toUser, DataBank db,
            EmailProperties emailProperties) {
        String salutation = toUser.getSalutation();
        String lastname = toUser.getLastName();
        String email = toUser.getEmail();
        String inviteeID = toUser.getId();

        EmailMessage emailMessage = new EmailMessage(email, salutation, lastname);

        /* these are all pretty fixed relationships */
        String ssid = toUser.getCurrentSurvey().getStudySpace().id;
        return this.sendMessage(msg, messageUseID, emailMessage, ssid, db, inviteeID, emailProperties);
    }

    /**
     * Sends an email to the User.
     * 
     * @param msg
     *            Message object to be sent to the User.
     * @param messageUseID
     *            The id used for generating URL which is unique to the user.
     * @param toEmail
     *            Email id to which message has to be sent.
     * @param salutation
     *            Salutation to address the invitee to whom email has to be
     *            sent.
     * @param lastname
     *            Last name of the invitee.
     * @param ssid
     *            Studyspace id for generation of the URL.
     * @param db
     *            Data bank object used from talking to DB.
     * @param inviteeId
     *            Id of the user to whom email has to be sent
     * @return String Empty string if successful.
     */
    public String sendMessage(Message msg, String messageUseID, EmailMessage emailMessage, String ssid, DataBank db,
            String inviteeId, EmailProperties emailProperties) {
        String outputString = "uncaught exception";
        String message = null;
        try {
            Session session = WISEApplication.getInstance().getEmailer().getMailSession();
            /* create message object */
            MimeMessage mMessage = new MimeMessage(session);

            /* send message to each of the users */
            InternetAddress tmpAddr = new InternetAddress(this.fromStr);
            mMessage.setFrom(tmpAddr);
            if (this.replyStr != null) {
                tmpAddr = new InternetAddress(this.replyStr);
                mMessage.setReplyTo(new InternetAddress[] { tmpAddr });
            }
            java.util.Date today = new java.util.Date();
            mMessage.setSentDate(today);
            mMessage.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(emailMessage.getToEmail()));
            mMessage.setSubject(msg.subject);

            /* check if message produces an html body; null indicates no */
            message = msg.composeHtmlBody(emailMessage.getSalutation(), emailMessage.getLastname(), ssid, messageUseID);
            message = db.replacePattern(message, inviteeId);

            /* if message is null go ahead and prepare a text body */
            if (message == null) {
                message = msg.composeTextBody(emailMessage.getSalutation(), emailMessage.getLastname(), ssid,
                        messageUseID);
                message = db.replacePattern(message, inviteeId);
                mMessage.setText(message);
            } else {

                /*
                 * create an "Alternative" Multipart message to send both html &
                 * text email
                 */
                Multipart mp = new MimeMultipart("alternative");

                /* add text body part */
                BodyPart bpText = new MimeBodyPart();
                bpText.setDataHandler(new DataHandler(msg.composeTextBody(emailMessage.getSalutation(),
                        emailMessage.getLastname(), ssid, messageUseID), "text/plain"));
                mp.addBodyPart(bpText);

                /* add html body part */
                BodyPart bpHtml = new MimeBodyPart();
                bpHtml.setDataHandler(new DataHandler(message, "text/html"));
                mp.addBodyPart(bpHtml);

                /* set the message body */
                mMessage.setContent(mp);
            }

            // System.out.println(message);
            /* send message and return the result */
            outputString = mailingProcess(mMessage, session, this.fromStr, this.replyStr, emailMessage, emailProperties);

        } catch (MessagingException e) {
            LOGGER.error(
                    "\r\nWISE - MESSAGE SENDER on email message: " + message + ".\r\n Full error: " + e.toString(), e);
        } catch (Exception e) {
            LOGGER.error(
                    "\r\nWISE - MESSAGE SENDER on email message: " + message + ".\r\n Full error: " + e.toString(), e);
        }
        return outputString;
    }

    /**
     * This method tests sending messages.
     * 
     * @param msgText
     *            Message test that has to be sent.
     * @return String If the mail sending is successful or not.
     */
    public String sendTest(String msgText, String fromEmail, String toEmail, EmailMessage emailMessage,
            EmailProperties emailProperties) {
        String outputString = "";
        try {
            Session session = WISEApplication.getInstance().getEmailer().getMailSession();
            /* create message object */
            MimeMessage message = new MimeMessage(session);

            /* send message to each of the users */
            message.setFrom(new InternetAddress(fromEmail));
            java.util.Date today = new java.util.Date();
            message.setSentDate(today);
            message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("This is a test");
            message.setText(msgText);

            /* send message and analyze the mailing failure */
            String msgResult = mailingProcess(message, session, this.fromStr, this.replyStr, emailMessage,
                    emailProperties);
            if (msgResult.equalsIgnoreCase("")) {
                outputString = "D";
            } else {
                outputString = msgResult;
            }

        } catch (MessagingException e) {
            LOGGER.error("WISE EMAIL - MESSAGE SENDER - SEND REMINDER: " + e.toString(), null);
        }

        return outputString;
    }

    /**
     * Sends the actual email to the user.
     * 
     * @param message
     *            Mime message which contains the details for sending email.
     * @return String Empty if successful.
     * @throws MessagingException
     * @throws Exception
     */
    public static String mailingProcess(MimeMessage message, Session session, String fromEmail, String replyEmail,
            EmailMessage emailMessage, EmailProperties emailProperties) throws MessagingException {
        StringBuilder mailingResult = new StringBuilder();
        if (message == null) {
            return "msg is null";
        }
        try {
            if (session == null) {
                throw new IllegalStateException("Session is null!");
            }
            Transport transport = session.getTransport("smtp");
            if (transport == null) {
                throw new IllegalStateException("Transport is null!");
            }

            message.saveChanges(); // don't forget this
            if (message.getAllRecipients() == null) {
                throw new IllegalStateException("Get all recipients is null");
            }

            if (emailProperties.isUseSSL()) {
                transport.connect(emailProperties.getEmailHost(), emailProperties.getEmailUsername(),
                        emailProperties.getEmailPassword());
                Transport.send(message, message.getAllRecipients());
            } else {
                Transport.send(message, message.getAllRecipients());
            }
            transport.close();

        } catch (AuthenticationFailedException e) {
            LOGGER.error("Message_Sender - Authentication failed. From string:" + fromEmail + "| Reply: " + replyEmail
                    + ". \n");
            LOGGER.error(
                    "emailProperties: " + emailProperties.getEmailHost() + "|" + emailProperties.getEmailUsername()
                            + "|" + emailProperties.getEmailPassword(), e);
            mailingResult.append("Authentication process failed");
        } catch (SendFailedException e) {
            LOGGER.error("Message_Sender - Invalid email address. " + e.toString(), e);
            mailingResult.append("Email address is invalid.");
        } catch (MethodNotSupportedException e) {
            LOGGER.error("Message_Sender - Unsupported message type. " + e.toString(), e);
            mailingResult.append("Message is not supported.");
        }
        return mailingResult.toString();
    }

}
