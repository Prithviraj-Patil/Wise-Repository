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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.initializer.WiseProperties;

public class StudySpaceFetcher {

    private static final Logger LOGGER = Logger.getLogger(StudySpaceFetcher.class);

    public static List<StudySpace> getStudySpaces(String appName, WiseProperties properties) {

        LOGGER.info("Fetching study spaces for application " + appName);

        ArrayList<StudySpace> startConfigList = new ArrayList<StudySpace>();

        // start the email sending procedure
        java.util.Date today = new java.util.Date();

        LOGGER.info("Launching Email Manager on " + today.toString() + " for studies assigned to " + appName
                + " on this server.");

        StudySpace[] allSpaces;

        try {
            allSpaces = StudySpaceMap.getInstance().getAll();

            LOGGER.info("Found " + allSpaces.length + " study spaces");

            for (StudySpace studySpace : allSpaces) {

                startConfigList.add(studySpace);
            }

        } catch (RuntimeException e) {
            LOGGER.error(" --> Emailer err - Can't get study_spaces: ", e);
        }

        return startConfigList;
    }
}
