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
package edu.ucla.wise.emailscheduler;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.databank.DataBank;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * This email thread will spawn action of sending reminders.
 */

/**
 * This singleton class is used to spawn the email thread, which is used to send
 * reminders.
 */
public class EmailScheduler {
    public static final String APPLICATION_NAME = "/WISE";

    public static final int DEFAULT_EMAIL_START_TIME = 2;

    private static ScheduledExecutorService executor;

    static Logger LOG = Logger.getLogger(EmailScheduler.class);

    private final WiseProperties properties;

    private static EmailScheduler emailScheduler;

    private final Map<Long, String> emailSchedulerStatusMap = new ConcurrentHashMap<>();

    public EmailScheduler(final WiseProperties properties) {
        this.properties = properties;
    }

    /**
     * Fetches all the study spaces in the system and checks for the time when
     * reminders are to be sent and then runs the method which sends email.
     * 
     */
    public void startEmailSendingThreads() {

        List<StudySpace> studySpaceList = StudySpaceFetcher.getStudySpaces(APPLICATION_NAME, this.properties);
        LOG.info("Found " + studySpaceList.size() + " study spaces");
        executor = Executors.newSingleThreadScheduledExecutor();

        for (final StudySpace studySpace : studySpaceList) {
            long emailStartHour = DEFAULT_EMAIL_START_TIME;
            try {
                emailStartHour = Long.parseLong(studySpace.emailSendingTime);

                /* -1 will indicate that the emails should not be sent */
                if (emailStartHour == -1) {

                    /* proceed to the next study space */
                    continue;
                }

                if ((emailStartHour < 0) && (emailStartHour > 23)) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                LOG.error("Error in start time for " + studySpace.studyName + ", defaulting to time "
                        + DEFAULT_EMAIL_START_TIME, e);
                emailStartHour = DEFAULT_EMAIL_START_TIME;
            }

            long initialWaitPeriodInMillis = calculateInitialWaitPeriodInMillis(emailStartHour);

            LOG.info("Emailer for " + studySpace.id + " will start in "
                    + WISE_TimeUtils.convertMillisToHumanReadableForm(initialWaitPeriodInMillis));

            this.emailSchedulerStatusMap.put(System.currentTimeMillis(), "Emailer for " + studySpace.id
                    + " will start in " + WISE_TimeUtils.convertMillisToHumanReadableForm(initialWaitPeriodInMillis));

            executor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    LOG.info("Mail sender for " + studySpace.id + " has started");
                    DataBank db = studySpace.db;
                    LOG.info("Study_Space " + studySpace.studyName + " CONNECTING to database: " + db.getDbdata());
                    String completionString = db.sendReminders();
                    EmailScheduler.getInstance().getEmailSchedulerStatusMap()
                            .put(System.currentTimeMillis(), completionString);
                    LOG.info(completionString);
                    LOG.info("Mail sender for " + studySpace.id + " has finished");
                }

            }, initialWaitPeriodInMillis, WISE_TimeUtils.MILLISECONDS_IN_A_DAY, TimeUnit.MILLISECONDS);
        }
    }

    public Map<Long, String> getEmailSchedulerStatusMap() {
        return this.emailSchedulerStatusMap;
    }

    /**
     * Calculates the initial schedule time from hours to milli seconds.
     * 
     * @param emailStartTime
     *            Start time in hours.
     * 
     * @return emailStartTime in milli seconds.
     */
    public static long calculateInitialWaitPeriodInMillis(final long emailStartTime) {
        Calendar currentTime = Calendar.getInstance();

        Calendar calendarMidnight = Calendar.getInstance();
        calendarMidnight.set(Calendar.HOUR_OF_DAY, 0);
        calendarMidnight.set(Calendar.MINUTE, 0);
        calendarMidnight.set(Calendar.SECOND, 0);
        calendarMidnight.set(Calendar.MILLISECOND, 0);

        long millisAtMidnight = calendarMidnight.getTimeInMillis();

        LOG.info("Milliseconds at midnight: " + millisAtMidnight);

        long emailStartTimeMillis = emailStartTime * WISE_TimeUtils.MILLISECONDS_IN_AN_HOUR;

        LOG.info("Email startTime in millis " + emailStartTimeMillis);
        long currentTimeMillis = currentTime.getTimeInMillis();
        LOG.info("Current time in milliseconds: " + currentTimeMillis);

        /* 2. add todays milliseconds to it */
        if ((emailStartTimeMillis + millisAtMidnight) > currentTimeMillis) {
            return (emailStartTimeMillis + millisAtMidnight) - currentTimeMillis;
        } else {
            return WISE_TimeUtils.MILLISECONDS_IN_A_DAY - (currentTimeMillis - emailStartTimeMillis - millisAtMidnight);
        }
    }

    public boolean sendEmailsInStudySpace(final StudySpace ss) {
        return true;
    }

    /**
     * Destroys the email scheduler, called when WISE application is
     * uninitialized.
     */
    public static void destroyScheduler() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    public static void intialize(WiseProperties properties) {
        if (emailScheduler == null) {
            emailScheduler = new EmailScheduler(properties);
        }
    }

    public static EmailScheduler getInstance() {
        return emailScheduler;
    }
}