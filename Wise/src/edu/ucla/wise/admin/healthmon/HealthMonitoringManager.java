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
package edu.ucla.wise.admin.healthmon;

import java.util.Calendar;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.email.EmailProperties;

/**
 * HealthMonitoringManager class is used to monitor the status of
 * email/database/survey server. It is a thread that runs continuously to
 * monitor the status
 * 
 */
public class HealthMonitoringManager implements Runnable {

    public static final Logger LOGGER = Logger.getLogger(HealthMonitoringManager.class);
    AdminUserSession adminUserSession;
    private static HealthMonitoringManager hMon = null;

    private HealthMonitoringManager(AdminUserSession adminUserSession) {
        this.adminUserSession = adminUserSession;
    }

    /**
     * This function will start monitoring for database and smtp if it has
     * already been not started. If the monitoring has already started then this
     * function will just return;
     * 
     * @param survey
     */
    public static synchronized void monitor(AdminUserSession adminUserSession) {
        if (hMon == null) {
            hMon = new HealthMonitoringManager(adminUserSession);
            Thread t = new Thread(hMon);
            t.start();
        }
    }

    @Override
    public void run() {
        while (true) {
            this.checkDbHealth();
            this.checkSmtpHealth();
            try {
                Thread.sleep(WiseConstants.dbSmtpCheckInterval);
            } catch (InterruptedException e) {
                this.LOGGER.error("Could not get session variable! Please retry!", e);
            }
        }
    }

    /**
     * This function will check smtp server status by connecting to the email
     * client and will update the status in the Wise admin page.
     */
    private void checkSmtpHealth() {
        HealthStatus hStatus = HealthStatus.getInstance();
        Session session = WISEApplication.getInstance().getEmailer().getMailSession();
        if (session == null) {
            this.LOGGER.error("Could not get session variable! Please retry!");
            hStatus.updateSmtp(false, Calendar.getInstance().getTime());
            return;
        }
        Transport tr = null;
        try {
            tr = session.getTransport("smtp");
        } catch (NoSuchProviderException e) {
            this.LOGGER.error(e);
            hStatus.updateSmtp(false, Calendar.getInstance().getTime());
            return;
        }
        if (tr == null) {
            this.LOGGER.error("Could not get transport object");
            hStatus.updateSmtp(false, Calendar.getInstance().getTime());
            return;
        }

        EmailProperties properties = WISEApplication.getInstance().getWiseProperties();

        String emailHost = properties.getEmailHost();
        String user = properties.getEmailUsername();
        String pass = properties.getEmailPassword();

        try {
            tr.connect(emailHost, user, pass);
        } catch (MessagingException e) {
            this.LOGGER.error("Could not connect!");
            hStatus.updateSmtp(false, Calendar.getInstance().getTime());
            return;
        }
        try {
            tr.close();
        } catch (MessagingException e) {
            this.LOGGER.info("Transport connected successully " + "however closing failed but thats fine", e);
        }
        hStatus.updateSmtp(true, Calendar.getInstance().getTime());
    }

    /**
     * This function will check the database status by getting the connection
     * and will update the status in the Wise admin page.
     */
    private void checkDbHealth() {
        this.adminUserSession.getMyStudySpace().checkDbHealth();
    }
}