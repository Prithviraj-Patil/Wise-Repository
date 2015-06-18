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
package edu.ucla.wise.client.healthmon;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WiseConstants;

/**
 * This class is a single thread per survey application which runs and reports
 * the health of the survey server in the database. The goal is to make the
 * admin application to read "survey_health" in database and display the health
 * of survey application.
 */
public class SurveyHealth implements Runnable {

    public StudySpace studySpace;
    public static Set<String> monitorStudies = new HashSet<String>();
    private static Logger LOGGER = Logger.getLogger(SurveyHealth.class);

    private SurveyHealth(StudySpace study) {
        this.studySpace = study;
    }

    /**
     * This function will start monitoring for "this" survey if it has already
     * been not started. If the monitoring has already started then this
     * function will just return;
     * 
     * @param survey
     */
    public static synchronized void monitor(StudySpace study) {
        if (!monitorStudies.contains(study.studyName)) {
            monitorStudies.add(study.studyName);
            Thread t = new Thread(new SurveyHealth(study));
            t.start();
        }
    }

    @Override
    public synchronized void run() {
        while (true) {
            this.studySpace.db.updateSurveyHealthStatus(this.studySpace.studyName);
            try {
                Thread.sleep(WiseConstants.surveyUpdateInterval);
            } catch (InterruptedException e) {
                LOGGER.error("WISE ADMIN - SURVEY HEALTH:" + e.toString(), e);
            }
        }
    }
}