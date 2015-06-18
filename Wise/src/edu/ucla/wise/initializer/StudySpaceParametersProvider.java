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
package edu.ucla.wise.initializer;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * StudySpaceParametersProvider class is used to talk to WiseStudySpaceWizard
 * and get all the parameters related to study paces in the system.
 */
public class StudySpaceParametersProvider {

    public static final Logger LOGGER = Logger.getLogger(StudySpaceParametersProvider.class);

    private static StudySpaceParametersProvider studySpaceParametersProvider;

    private final Map<String, StudySpaceParameters> studySpaceParameters;

    private final WiseConfiguration configuration;

    /**
     * Singleton constructor to ensure only one object of
     * StudySpaceParametersProvider is created.
     */
    private StudySpaceParametersProvider(WiseConfiguration config) {

        this.studySpaceParameters = config.getStudySpaceParameters();
        this.configuration = config;

        LOGGER.info("Found " + this.studySpaceParameters.size() + " Study Spaces");
        LOGGER.info("Spaces are " + this.studySpaceParameters.toString());
    }

    /**
     * Checks if the studySpaceParametersProvider object is already created or
     * not. If not it creates a new instance of studySpaceParametersProvider.
     * 
     * @return true
     */
    public static boolean initialize(WiseConfiguration config) {
        if (studySpaceParametersProvider == null) {
            studySpaceParametersProvider = new StudySpaceParametersProvider(config);
        } else {
            LOGGER.info("studySpaceParametersProvider already initialized");
        }
        return true;
    }

    public static boolean reload() {
        if (studySpaceParametersProvider != null) {
            studySpaceParametersProvider = new StudySpaceParametersProvider(StudySpaceParametersProvider.getInstance()
                    .getConfiguration());
            return true;
        }
        return false;
    }

    /**
     * Destroys studySpaceParametersProvider and returns true.
     * 
     * @return true
     */
    public static boolean destroy() {
        studySpaceParametersProvider = null;
        return true;
    }

    /**
     * Checks if StudySpaceParametersProvider is null, if it is new
     * StudySpaceParametersProvider is created.
     * 
     * @return StudySpaceParametersProvider returns the initialized parameter.
     */
    public static StudySpaceParametersProvider getInstance() {
        return studySpaceParametersProvider;
    }

    /**
     * Returns all the parameters related to given studySpace.
     * 
     * @param studySpaceName
     *            Name of the studySpace whose parameters are needed.
     * @return StudySpaceParameters returns the studySpace parameters.
     */
    public StudySpaceParameters getStudySpaceParameters(String studySpaceName) {
        LOGGER.info("Requesting parameters for " + studySpaceName);
        if (this.studySpaceParameters.get(studySpaceName) == null) {
            LOGGER.info("Study space parameters not found");
            LOGGER.info("Current study space parameters are " + this.getStudySpaceParametersMap().toString());
        }
        LOGGER.info("The desired Study Space is :" + this.studySpaceParameters.get(studySpaceName).toString());
        return this.studySpaceParameters.get(studySpaceName);
    }

    /**
     * Returns all the parameters related to all studySpaces in System.
     * 
     * 
     * @return StudySpaceParameters returns the studySpace parameters map which
     *         contains parameters related to all studySpaces.
     */
    public Map<String, StudySpaceParameters> getStudySpaceParametersMap() {
        return this.studySpaceParameters;
    }

    public WiseConfiguration getConfiguration() {
        return this.configuration;
    }

}
