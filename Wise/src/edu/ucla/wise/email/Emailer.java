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
package edu.ucla.wise.email;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;

public class Emailer {

    private final Session mailSession;

    public Emailer(EmailProperties properties) {
        this.mailSession = this.getMailSession(properties);
    }

    /* return the default session if null */
    public Session getMailSession(EmailProperties properties) {
        String smtpHost = properties.getEmailHost();
        String smtpAuthUser = properties.getEmailUsername();
        String smtpAuthPassword = properties.getEmailPassword();
        String smtpAuthPort = properties.getEmailAuthenticationPort();
        boolean tempsslEmail = properties.isUseSSL();

        /* Set the host smtp address */
        if (tempsslEmail) {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpAuthPort);
            props.put("mail.smtp.user", smtpAuthUser);
            props.put("mail.smtp.password", smtpAuthPassword);
            props.setProperty("mail.smtp.socketFactory.port", smtpAuthPort);

            // defaults
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.setProperty("mail.smtp.socketFactory.fallback", "false");
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            props.setProperty("mail.smtp.timeout", "10000");
            Authenticator auth = new VarAuthenticator(smtpAuthUser, smtpAuthPassword);

            /* create the message session */
            return Session.getInstance(props, auth);
        } else {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.setProperty("mail.smtp.connectiontimeout", "10000");
            props.setProperty("mail.smtp.timeout", "10000");
            return Session.getInstance(props);
        }

    }

    public Session getMailSession() {
        return this.mailSession;
    }

    private static class VarAuthenticator extends Authenticator {
        String userName = null;
        String password = null;

        public VarAuthenticator(String uName, String pword) {
            super();
            this.userName = uName;
            this.password = pword;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.userName, this.password);
        }
    }
}
