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

import java.io.IOException;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import edu.ucla.wise.email.Emailer;
import edu.ucla.wise.emailscheduler.EmailScheduler;
import edu.ucla.wise.initializer.ProductionConfiguration;
import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.initializer.WiseConfiguration;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * Class to represent common elements for a given installation of the wise
 * surveyor or admin java application.
 */
public class WISEApplication {

    private static WISEApplication wiseApplication;

    public static WISEApplication getInstance() {
        return wiseApplication;
    }

    public static void initialize(String contextPath, String rootFolderPath, WiseProperties properties)
            throws IOException {
        if (wiseApplication == null) {
            wiseApplication = new WISEApplication(contextPath, rootFolderPath, properties);
        }
    }

    private static Logger LOGGER = Logger.getLogger(WISEApplication.class);

    private final WiseProperties wiseProperties;
    private final Emailer emailer;

    private final WiseConfiguration wiseConfiguration;

    private final AdminApplication adminApplication;
    private final SurveyorApplication surveyorApplication;

    public WISEApplication(String contextPath, String rootFolderPath, WiseProperties properties) throws IOException {

        // first initialize the study space parameters provider
        this.wiseConfiguration = new ProductionConfiguration(properties);
        this.initializeStudySpaceParametersProvider(this.wiseConfiguration);

        this.wiseProperties = properties;
        this.emailer = new Emailer(properties);
        this.initializeAdminApplication(contextPath, rootFolderPath, properties);
        this.initializeSurveyApplication(contextPath, rootFolderPath, properties);

        this.adminApplication = AdminApplication.getInstance();
        this.surveyorApplication = SurveyorApplication.getInstance();

        /* set up Study_Space class -- pre-reads from sharedProps */
        StudySpaceMap.setupStudies(properties);

        this.startEmailSendingThreads(properties, this.wiseConfiguration);
    }

    private void startEmailSendingThreads(WiseProperties properties, WiseConfiguration configuration) {
        if (configuration.getConfigType() == WiseConfiguration.CONFIG_TYPE.PRODUCTION) {
            LOGGER.info("Staring Email Scheduler");
            EmailScheduler.intialize(properties);
            EmailScheduler.getInstance().startEmailSendingThreads();
            LOGGER.info("Email Scheduler is alive");
        } else {
            LOGGER.info("Skipping email scheduler in dev mode");
        }
    }

    public void reloadStudySpaceParametersProvider() {
        StudySpaceParametersProvider.reload();
    }

    private void initializeStudySpaceParametersProvider(WiseConfiguration config) {
        StudySpaceParametersProvider.initialize(config);
    }

    private void initializeAdminApplication(String contextPath, String rootFolderPath, WiseProperties properties)
            throws IOException {
        AdminApplication.initialize(contextPath, rootFolderPath, properties);
    }

    private void initializeSurveyApplication(String contextPath, String rootFolderPath, WiseProperties properties)
            throws IOException {
        SurveyorApplication.initialize(contextPath, rootFolderPath, properties);
    }

    /**
     * decoding - convert character-formatted ID to be the digit-formatted *
     * TOGGLE NAME OF THIS FUNCTION to move to production mode
     * 
     * @param charId
     *            The String to decode.
     * @return String The decoded input.
     */
    public static String decode(String charId) {
        String result = new String();
        int sum = 0;
        for (int i = charId.length() - 1; i >= 0; i--) {
            char c = charId.charAt(i);
            int remainder = c - 65;
            sum = (sum * 26) + remainder;
        }

        sum = sum - 97654;
        int remain = sum % 31;
        if (remain == 0) {
            sum = sum / 31;
            result = Integer.toString(sum);
        } else {
            result = "invalid";
        }
        return result;
    }

    public WiseProperties getWiseProperties() {
        return this.wiseProperties;
    }

    /**
     * encoding - convert digit-formatted ID to be the character-formatted
     * TOGGLE NAME OF THIS FUNCTION to move to production mode
     * 
     * @param charId
     *            The String to encode.
     * @return String The encoded input.
     */
    public static String encode(String userId) {
        int baseNumb = (Integer.parseInt(userId) * 31) + 97654;
        String s1 = Integer.toString(baseNumb);
        String s2 = Integer.toString(26);
        BigInteger b1 = new BigInteger(s1);
        BigInteger b2 = new BigInteger(s2);

        int counter = 0;
        String charId = new String();
        while (counter < 5) {
            BigInteger[] bs = b1.divideAndRemainder(b2);
            b1 = bs[0];
            int encodeValue = bs[1].intValue() + 65;
            charId = charId + (new Character((char) encodeValue).toString());
            counter++;
        }
        return charId;
    }

    public Emailer getEmailer() {
        return this.emailer;
    }

}
