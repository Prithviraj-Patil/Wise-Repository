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

import java.util.Date;

import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.commons.WiseConstants.SURVEY_STATUS;
import edu.ucla.wise.commons.databank.DataBank;

/**
 * HealthStatus is a class used to update the status of email database and
 * survey on wise admin page.
 * 
 */
public class HealthStatus {

    private static HealthStatus healthStatus = null;

    private HealthStatus() {
    }

    public static synchronized HealthStatus getInstance() {
        if (healthStatus == null) {
            healthStatus = new HealthStatus();
        }
        return healthStatus;
    }

    public boolean dbIsAlive;
    public Date dbLastUpdatedTime;
    public boolean smtpIsAlive;
    public Date smtpLastUpdatedTime;

    /**
     * Returns status of DB.
     * 
     * @returns dbIsAlive status of DB if it is alive or not.
     */
    public boolean isDbIsAlive() {
        return this.dbIsAlive;
    }

    /**
     * Sets status of DB.
     * 
     * @param dbIsAlive
     *            DB status to set.
     */
    public void setDbIsAlive(boolean dbIsAlive) {
        this.dbIsAlive = dbIsAlive;
    }

    /**
     * Returns last updated time of DB.
     * 
     * @returns dbLastUpdatedTime last updated time of DB.
     */
    public Date getDbLastUpdatedTime() {
        return this.dbLastUpdatedTime;
    }

    /**
     * Sets last updated time of DB.
     * 
     * @param dbLastUpdatedTime
     *            last updated time of DB.
     */
    public void setDbLastUpdatedTime(Date dbLastUpdatedTime) {
        this.dbLastUpdatedTime = dbLastUpdatedTime;
    }

    /**
     * Returns status of smtp.
     * 
     * @returns smtpIsAlive status of smtp.
     */
    public boolean isSmtpIsAlive() {
        return this.smtpIsAlive;
    }

    /**
     * Sets status of smtp.
     * 
     * @param smtpIsAlive
     *            status of smtp to set.
     */
    public void setSmtpIsAlive(boolean smtpIsAlive) {
        this.smtpIsAlive = smtpIsAlive;
    }

    /**
     * Returns last updated time of smtp.
     * 
     * @returns smtpLastUpdatedTime last updated time of smtp.
     */
    public Date getSmtpLastUpdatedTime() {
        return this.smtpLastUpdatedTime;
    }

    /**
     * Sets last updated time of smtp.
     * 
     * @param smtpLastUpdatedTime
     *            last updated time of smtp.
     */
    public void setSmtpLastUpdatedTime(Date smtpLastUpdatedTime) {
        this.smtpLastUpdatedTime = smtpLastUpdatedTime;
    }

    /**
     * Returns the status of the survey based on the last updated time of the
     * survey.
     * 
     * @param String
     *            studyName Study space for which status has to be found.
     * @param DataBank
     *            db Databank to get the last updated time of the survey.
     * 
     * @return SURVEY_STATUS status of the survey.
     */
    public SURVEY_STATUS isSurveyAlive(String studyName, DataBank db) {
        long lastUpdateTime = db.lastSurveyHealthUpdateTime(studyName), currentTimeMillis = System.currentTimeMillis();

        /* if the difference is more than 10min */
        if (lastUpdateTime == 0) {
            return SURVEY_STATUS.NOT_AVAIL;
        }
        return (currentTimeMillis - lastUpdateTime) < WiseConstants.surveyCheckInterval ? SURVEY_STATUS.OK
                : SURVEY_STATUS.FAIL;
    }

    /**
     * Sets the parameters with respect to smtp
     * 
     * @param health
     *            Boolean value if the smtp is live or dead
     * @param updateTime
     *            last updated time when the smtp status is checked.
     */
    public void updateSmtp(boolean health, Date updateTime) {
        this.smtpIsAlive = health;
        this.smtpLastUpdatedTime = updateTime;
    }

    /**
     * Sets the parameters with respect to DB
     * 
     * @param health
     *            Boolean value if the DB is live or dead
     * @param updateTime
     *            last updated time when the DB status is checked.
     */
    public void updateDb(boolean health, Date updateTime) {
        this.dbIsAlive = health;
        this.dbLastUpdatedTime = updateTime;
    }

}
