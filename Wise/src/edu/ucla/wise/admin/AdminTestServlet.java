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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.WISEApplication;

/**
 * AdminTestServlet class directs the user coming from email URL or interviewers
 * to appropriate next step or page.
 * 
 */
public class AdminTestServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(AdminTestServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Tests mailing part of the code.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        /* prepare for writing */
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        HttpSession session = req.getSession(true);
        session.getServletContext();

        String fromStr = "";
        try {

            /* Define message */
            MimeMessage message = new MimeMessage(WISEApplication.getInstance().getEmailer().getMailSession());

            if (req.getParameter("froma") != null) {
                fromStr += "<" + req.getParameter("froma") + ">";
            } else {
                fromStr += "<merg@mednet.ucla.edu>";
            }

            if (req.getParameter("from") != null) {
                fromStr = req.getParameter("from") + fromStr;
            }

            InternetAddress ia = new InternetAddress(fromStr);
            message.setFrom(ia);

            if (req.getParameter("repa") != null) {
                fromStr = "<" + req.getParameter("repa") + ">";
                InternetAddress ib = new InternetAddress(fromStr);
                message.setReplyTo(new InternetAddress[] { ib });
            }
            if (req.getParameter("senda") != null) {
                fromStr = "<" + req.getParameter("senda") + ">";
                InternetAddress ib = new InternetAddress(fromStr);
                message.setSender(ib);
            }

            message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress("<merg@mednet.ucla.edu>"));
            message.setSubject("This is a test");
            message.setText("this is a test body");

            /* Send message */
            Transport.send(message);
        } catch (AddressException e) {
            LOGGER.error("Error in AdminTest:", e);
        } catch (MessagingException e) {
            String initError = e.toString();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            initError += sw.toString();
            LOGGER.error("Error in begin_test:" + initError, e);
        }

        out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
                + "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
                + "<body text=#000000 bgColor=#ffffcc><center><table>"

                // + "<tr><td>Successful test. StudySpace id [t]= " +
                // id2 + "</td></tr>"
                + "<tr><td>Root URL= "
                + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                + "</td></tr>"
                + "<tr><td>XML path = "
                + WISEApplication.getInstance().getWiseProperties().getXmlRootPath()
                + "</td></tr>"

                // + "<tr><td>SS file path = " + thesharedFile +
                // "</td></tr>"
                + "<tr><td>Image path = "
                + AdminApplication.getInstance().getImageRootPath()
                + "</td></tr>"
                + "<tr><td>DB backup path = "
                + AdminApplication.getInstance().getDbBackupPath()
                + "</td></tr>"
                + "<tr><td>Context Path= "
                + AdminApplication.ApplicationName
                + "</td></tr>"
                + "<tr><td>Servlet Path= "
                + AdminApplication.servletUrl
                + "</td></tr>"

                // + "<tr><td>message id= " + msgid_encode +
                // "</td></tr>"
                + "<tr><td>Default email_from= "
                + WISEApplication.getInstance().getWiseProperties().getEmailFrom()
                + "</td></tr>"
                + "<tr><td>constructed fromstr= "
                + fromStr
                + "</td></tr>"
                + "</table></center></body></html>");
    }
}
