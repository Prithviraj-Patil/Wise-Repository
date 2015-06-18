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

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents our email prompts
 */
public class Message {

    public static final Logger LOGGER = Logger.getLogger(Message.class);

    /** Email Image File Names */
    private static final String HEADER_IMAGE_FILENAME = "email_header_img.jpg";
    private static final String FOOTER_IMAGE_FILENAME_1 = "email_bottom_img1.jpg";
    private static final String FOOTER_IMAGE_FILENAME_2 = "email_bottom_img2.jpg";
    private static final String WISE_SHARED = "WiseShared";

    private static final String HTML_OPEN = "<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8'></head>"
            + "<body bgcolor=#FFFFFF text=#000000><center>";
    private static final String htmlClose = "</center></body></html>";

    /** Instance Variables */
    public String id, subject;
    public String mainBody, htmlBody, htmlHeader, htmlTail, htmlSignature;
    private boolean htmlFormat = false;
    public boolean hasLink = false, hasDLink = false;
    public String msgRef = null;
    private String servletPath = null;

    // TODO: should ideally be customizable for survey/irb; should use xlst

    /**
     * Constructor for Message
     * 
     * @param n
     *            XML DOM for the message from the preface file.
     */
    public Message(Node n) {
        try {
            /*
             * parse out the reminder attributes: ID, subject, format, trigger
             * days and max count
             */
            this.id = "";
            this.subject = "";

            /* ID, subject, trigger days and maximum count are required */
            this.id = n.getAttributes().getNamedItem("ID").getNodeValue();
            this.subject = n.getAttributes().getNamedItem("Subject").getNodeValue();

            /* email format is optional */
            Node node = n.getAttributes().getNamedItem("Format");
            if (node != null) {
                this.htmlFormat = node.getNodeValue().equalsIgnoreCase("html");
            }

            /* read out the contents of the email message */
            NodeList nodeP = n.getChildNodes();
            boolean hasRef = false;
            this.mainBody = "";
            this.htmlHeader = "";
            this.htmlBody = "";
            this.htmlTail = "";
            this.htmlSignature = "";

            for (int j = 0; j < nodeP.getLength(); j++) {
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("Message_Ref") && !hasRef) {

                    /* check prevents a 2nd ref (that's invalid) */
                    this.msgRef = nodeP.item(j).getAttributes().getNamedItem("ID").getNodeValue();
                    hasRef = true;
                    break;
                } else {
                    if (nodeP.item(j).getNodeName().equalsIgnoreCase("p")) {
                        this.mainBody += nodeP.item(j).getFirstChild().getNodeValue() + "\n\n";
                        this.htmlBody += "<p>" + nodeP.item(j).getFirstChild().getNodeValue() + "</p>\n";
                    }
                    if (nodeP.item(j).getNodeName().equalsIgnoreCase("s")) {
                        if (this.htmlFormat) {
                            this.htmlSignature += "<p>" + nodeP.item(j).getFirstChild().getNodeValue() + "</p>\n";
                        } else {
                            this.mainBody += nodeP.item(j).getFirstChild().getNodeValue() + "\n\n";
                            this.htmlBody += "<p>" + nodeP.item(j).getFirstChild().getNodeValue() + "</p>\n";
                        }
                    }

                    /* mark the URL link */
                    if (nodeP.item(j).getNodeName().equalsIgnoreCase("link")) {
                        this.hasLink = true;
                        this.mainBody = this.mainBody + "URL LINK\n\n";
                        this.htmlBody += "<p align=center><font color='blue'>[<u>URL Link to Start the Survey</u>]</font></p>\n";
                    }

                    /* mark the decline URL link */
                    if (nodeP.item(j).getNodeName().equalsIgnoreCase("declineLink")) {
                        this.hasDLink = true;
                        this.mainBody = this.mainBody + "DECLINE LINK\n\n";
                        LOGGER.info("########The Body of the email is: " + this.mainBody);
                        this.htmlBody += "<p align=center><font color='blue'>[<u>URL Link to Decline the Survey</u>]</font></p>\n";
                    }
                }
            }
        } catch (DOMException e) {
            LOGGER.error(
                    "WISE - TYPE MESSAGE: ID = " + this.id + "; Subject = " + this.subject + " --> " + e.toString(), e);
            return;
        }
    }

    /**
     * Resolve message references. Do this after construct-time so that order of
     * messages in file won't matter.
     * 
     * @param myPreface
     *            preface object on which the resolve is performed.
     */
    public void resolveRef(Preface myPreface) {
        if (this.msgRef != null) {
            Message refdMsg = myPreface.getMessage(this.msgRef);
            if (refdMsg.msgRef == null) {
                this.mainBody = refdMsg.mainBody;
                this.hasLink = refdMsg.hasLink;
                this.hasDLink = refdMsg.hasDLink;
                this.htmlTail = refdMsg.htmlTail;
                this.htmlHeader = refdMsg.htmlHeader;
            } else {
                LOGGER.error("MESSAGE: ID = " + this.id + "; Subject = " + this.subject
                        + " refernces a message that itself has a message ref. Double-indirection not supported. ",
                        null);
            }
        }
    }

    /**
     * Sets the HTML header and footer of the email.
     * 
     * @param servletPath
     *            Servlet path to be set.
     * @param imgRootPath
     *            image root path for Email
     */
    public void setHrefs(String servletPath, String imgRootPath) {
        this.servletPath = servletPath;
        if (this.htmlFormat) {
            /* compose the html header and tail */
            this.htmlHeader = "<table width=510 border=0 cellpadding=0 cellspacing=0>"
                    + "<tr><td rowspan=5 width=1 bgcolor='#996600'></td>"
                    + "<td width=500 height=1 bgcolor='#996600'></td>"
                    + "<td rowspan=5 width=1 bgcolor='#996600'></td></tr>"
                    + "<tr><td height=120 align=center><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl() + "/" + WISE_SHARED
                    + "/image?img=" + HEADER_IMAGE_FILENAME + "'></td></tr>" + "<tr><td>"
                    + "<table width=100% border=0 cellpadding=0 cellspacing=0>" + "<tr><td width=20>&nbsp;</td>"
                    + "<td width=460><font size=1 face='Verdana'>\n\n";

            /* NOTE: signature included in the tail */
            this.htmlTail = "</font></td><td width=20>&nbsp;</td>" + "</tr></table></td></tr>" + "<tr><td>"
                    + "<table width=100% border=0 cellpadding=0 cellspacing=0>"
                    + "<tr><td rowspan=2 width=25>&nbsp;</td>" + "<td height=80 width=370><font size=1 face='Verdana'>"
                    + this.htmlSignature + "</font></td>"
                    + "<td rowspan=2 height=110 width=105 align=left valign=bottom><img src=\""
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl() + "/" + WISE_SHARED
                    + "/image?img=" + FOOTER_IMAGE_FILENAME_2 + "\"></td></tr>"
                    + "<tr><td height=30 width=370 align=center valign=bottom><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl() + "/" + WISE_SHARED
                    + "/image?img=" + FOOTER_IMAGE_FILENAME_1 + "'></td></tr>" + "</table></td></tr>"
                    + "<tr><td width=500 height=1 bgcolor='#996600'></td></tr></table>\n\n";
        }
    }

    /**
     * Composes the body of email in text format.
     * 
     * @param salutation
     *            Salutation to address the invitee to whom email has to be
     *            sent.
     * @param lastname
     *            Last name of the invitee.
     * @param ssid
     *            Studyspace id for generation of the URL.
     * @param msgIndex
     *            Id to identify the invitee.
     * @return String Body of the email in text format.
     */
    public String composeTextBody(String salutation, String lastname, String ssid, String msgIndex) {
        String textBody = null;

        /* compose the text body */
        textBody = "Dear " + salutation + " " + lastname + ":\n\n" + this.mainBody;

        if (this.hasLink) {
            String reminderLink = this.servletPath + "survey?msg=" + msgIndex + "&t=" + WISEApplication.encode(ssid);

            String declineLink = this.servletPath + "survey/declineNOW?m=" + msgIndex + "&t="
                    + WISEApplication.encode(ssid);

            textBody = textBody.replaceAll("URL LINK", reminderLink + "\n");
            if (this.hasDLink) {
                textBody = textBody.replaceAll("DECLINE LINK", declineLink + "\n");
            }
        }
        return textBody;
    }

    /**
     * The URL for anonymous user to use.
     * 
     * @param servletPath
     *            Root path of the URL being generated.
     * @param msgIndex
     *            Index to identify a particular user.
     * @param studySpaceId
     *            Study space id for which link has to be generated.
     * @param surveyId
     *            Survey in the study space for which URL is generated.
     * @return String URL for accessing the survey by anonymous users.
     */
    public static String buildInviteUrl(String servletPath, String msgIndex, String studySpaceId, String surveyId) {

        /*
         * t = xxxx -> study space m = yyyy -> survey_user_message_space s =
         * zzzz -> survey id for anonymous user we do not have survey message
         * user and it is not possible to get survey from study space while
         * knowing the survey for annonymous users because study space can have
         * multiple surveys.
         */
        if (msgIndex == null) {
            return servletPath + "survey?t=" + WISEApplication.encode(studySpaceId) + "&s="
                    + CommonUtils.base64Encode(surveyId);
        }
        return servletPath + "survey?msg=" + msgIndex + "&t=" + WISEApplication.encode(surveyId);
    }

    /**
     * Composes the body of email in text format.
     * 
     * @param salutation
     *            Salutation to address the invitee to whom email has to be
     *            sent.
     * @param lastname
     *            Last name of the invitee.
     * @param ssid
     *            Studyspace id for generation of the URL.
     * @param msgIndex
     *            Id to identify the invitee.
     * @return String Body of the email in text format.
     */
    public String composeHtmlBody(String salutation, String lastname, String ssid, String msgIndex) {

        if (!this.htmlFormat) {

            /*
             * null return is the external signal the message doesn't have an
             * HTML version
             */
            return null;
        }

        /*
         * this overrides the iVar TODO: FIX so that we can actually use it here
         * and for overview display
         */

        String htmlBody = null;

        /* add the html header & the top of the body to the html body */
        htmlBody = HTML_OPEN + this.htmlHeader;
        htmlBody += "<p><b>Dear " + salutation + " " + lastname + ":</b></p>" + this.mainBody;
        htmlBody = htmlBody.replaceAll("\n", "<br>");
        if (this.hasLink) {
            String reminderLink = this.servletPath + "survey?msg=" + msgIndex + "&t=" + WISEApplication.encode(ssid);
            String declineLink = this.servletPath + "declineNOW?m=" + msgIndex + "&t=" + WISEApplication.encode(ssid);
            htmlBody = htmlBody.replaceAll("URL LINK", "<p align=center><a href='" + reminderLink + "'>" + reminderLink
                    + "</a></p>");
            if (this.hasDLink) {
                htmlBody = htmlBody.replaceAll("DECLINE LINK", "<p align=center><a href='" + declineLink + "'>"
                        + declineLink + "</a></p>");
            }
        }

        /* append the bottom part of body for the html email */
        htmlBody += this.htmlTail + htmlClose;
        return htmlBody;
    }

    /**
     * Renders html table rows to complete sample display page (used by Admin)
     * 
     * @return String HTML format of the mail.
     */
    public String renderSampleAsHtmlRows() {
        String outputString = "<tr><td class=sfon>Subject: </td>";
        outputString += "<td>" + this.subject + "</td></tr>";
        outputString += "<tr><td colspan=2>";

        /* add the the bottom imag & signature to the html body */
        if (this.htmlFormat) {
            outputString += this.htmlHeader;
            outputString += "<p>Dear [Salutation] [Name]:</p>";
            outputString += this.htmlBody;
            outputString += this.htmlTail; // note: includes signature
        } else {
            outputString += "<table width=100% border=0 cellpadding=0 cellspacing=0>";
            outputString += "<tr><td>&nbsp;</td></tr>";
            outputString += "<tr><td colspan=2><font size=1 face='Verdana'><p>Dear [Salutation] [Name],</p>\n";
            outputString += this.mainBody + "<p>&nbsp;</p></font></td></tr></table>\n\n";
        }
        outputString += "</td></tr>";
        return outputString;
    }

    @Override
    public String toString() {
        return "<P><B>Message</b> ID: " + this.id + "<br>\n" + "References: " + this.msgRef + "<br>\n" + "Subject: "
                + this.subject + "<br>\n" + "Body: " + this.mainBody + "</p>\n";
    }
}
