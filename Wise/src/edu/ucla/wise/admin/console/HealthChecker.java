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
package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.commons.WISEApplication;
import freemarker.template.TemplateException;

@WebServlet("/admin/healthChecker")
public class HealthChecker extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(HealthChecker.class);
    private static final long serialVersionUID = 1L;

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {
        PrintWriter out = response.getWriter();
        StringBuilder output = new StringBuilder();
        try {
            String message = request.getParameter("message");
            String subject = request.getParameter("subject");
            String email = request.getParameter("email");
            if (Strings.isNullOrEmpty(email)) {
                output.append("Please provide and email address.");

            } else {
                String fromAddress = email;
                String[] recipients = { email };

                Session session = WISEApplication.getInstance().getEmailer().getMailSession();

                /* create a message */
                javax.mail.Message msg = new MimeMessage(session);

                /* set the from and to address */
                InternetAddress addressFrom = new InternetAddress(fromAddress);
                msg.setFrom(addressFrom);

                InternetAddress[] addressTo = new InternetAddress[recipients.length];
                for (int i = 0; i < recipients.length; i++) {
                    addressTo[i] = new InternetAddress(recipients[i]);
                }
                msg.setRecipients(javax.mail.Message.RecipientType.TO, addressTo);

                /* Setting the Subject and Content Type */
                msg.setSubject(subject);
                msg.setContent(message, "text/plain");
                Transport.send(msg);

            }
        } catch (MessagingException e) {
            LOGGER.error("Mail system failure", e);
            output.append("Error is: ").append(e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("Health checker failure", e);
            output.append("Error is: ").append(e.getMessage());
        }
        out.write(output.toString());

    }

    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException {

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
