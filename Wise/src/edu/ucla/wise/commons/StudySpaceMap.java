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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.databank.DataBank;
import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/**
 * Singleton class to hold all current study spaces.
 * 
 * @author pdessai
 * 
 */
public final class StudySpaceMap implements Map<String, StudySpace> {

    private static final Logger LOGGER = Logger.getLogger(StudySpaceMap.class);

    private static StudySpaceMap studySpaceMap;

    private final Map<String, StudySpace> studySpaces = new HashMap<>();

    private final Map<String, String> studySpaceNames = new HashMap<>();

    private StudySpaceMap() {

    }

    public static void initialize() {
        if (studySpaceMap == null) {
            studySpaceMap = new StudySpaceMap();
        }
    }

    public static StudySpaceMap getInstance() {
        return studySpaceMap;
    }

    public static StudySpaceMap reload() {
        studySpaceMap = new StudySpaceMap();
        return studySpaceMap;
    }

    @Override
    public void clear() {
        this.studySpaces.clear();

    }

    @Override
    public boolean containsKey(Object arg0) {
        return this.studySpaces.containsKey(arg0);
    }

    @Override
    public boolean containsValue(Object arg0) {
        return this.studySpaces.containsValue(arg0);
    }

    @Override
    public Set<java.util.Map.Entry<String, StudySpace>> entrySet() {
        return this.studySpaces.entrySet();
    }

    @Override
    public StudySpace get(Object studyId) {
        StudySpace ss = this.studySpaces.get(studyId);
        if (ss == null) {
            String sName = this.studySpaceNames.get(studyId);
            if (sName != null) {
                ss = new StudySpace(sName, StudySpaceParametersProvider.getInstance().getStudySpaceParameters(sName));

                /* put Study_Space in ALL_SPACES */
                this.studySpaces.put(ss.id, ss);
            }

        }
        return ss;
    }

    /**
     * Returns all the study spaces in the system. This is used while sending
     * emails.
     * 
     * 
     * @return StudySpace Array of studySpaces in the system.
     */
    public StudySpace[] getAll() {
        int nSpaces = this.studySpaces.size();
        LOGGER.info("There are " + nSpaces + " Study Spaces");
        if (nSpaces < 1) {
            loadStudySpaces();
            nSpaces = this.studySpaces.size();
            LOGGER.info("Loaded " + nSpaces + " study spaces");
        }
        StudySpace[] result = new StudySpace[nSpaces];
        int i = 0;
        for (Entry<String, StudySpace> entry : this.studySpaces.entrySet()) {
            result[i++] = entry.getValue();
        }
        return result;
    }

    /**
     * Sets all the study spaces in the WISE system.
     */
    public static void setupStudies(WiseProperties properties) {
        initialize();
        DataBank.SetupDB(properties);

        /*
         * Just read the names of all unique Studies and save the name:ID pairs
         * in a hash for quicker lookup later note when called by a reload, does
         * not drop already-parsed studies but does reread props file to enable
         * load of new studies TODO (low): consider a private "stub" class to
         * hold all values from props file without parsing XML file
         */
        Map<String, StudySpaceParameters> allSpaceParams = StudySpaceParametersProvider.getInstance()
                .getStudySpaceParametersMap();
        Iterator<String> allSpaceParamsItr = allSpaceParams.keySet().iterator();

        while (allSpaceParamsItr.hasNext()) {
            String spaceName = allSpaceParamsItr.next();
            StudySpaceMap.getInstance().putIndex(allSpaceParams.get(spaceName).getId(), spaceName);
        }
        LOGGER.info("study space setup complete");
    }

    /**
     * Load all the StudySpace objects applicable for the given instance of the
     * application.
     * 
     * @return String Message if the load is successful or not.
     */
    public static String loadStudySpaces() {
        String spaceName = "";
        String resultstr = "";
        try {
            Map<String, StudySpaceParameters> allSpaceParams = StudySpaceParametersProvider.getInstance()
                    .getStudySpaceParametersMap();
            LOGGER.info("There are " + allSpaceParams.size() + " StudySpaceParameters objects");
            Iterator<String> allSpaceNameItr = allSpaceParams.keySet().iterator();

            while (allSpaceNameItr.hasNext()) {

                spaceName = allSpaceNameItr.next();
                String studySvr = allSpaceParams.get(spaceName).getServerUrl();
                String studyApp = allSpaceParams.get(spaceName).getServerApplication();

                LOGGER.info("Study space: '" + spaceName + "'");
                LOGGER.info("Study server: '" + studySvr + "'");
                LOGGER.info("Study app: '" + studyApp + "'");

                if (!Strings.isNullOrEmpty(spaceName)) {

                    /* create new StudySpace */
                    StudySpace ss = new StudySpace(spaceName, allSpaceParams.get(spaceName));

                    /* put StudySpace in ALL_SPACES */
                    StudySpaceMap.getInstance().put(ss.id, ss);
                    resultstr += "Loaded Study Space: " + ss.id + " for user " + ss.db.getDbuser() + " <BR>\n";
                }

            }
        } catch (ClassCastException e) {
            LOGGER.error("Load Study Spaces Error for  name " + spaceName, e);
        } catch (NullPointerException e) {
            LOGGER.error("Load Study Spaces Error for  name " + spaceName, e);
        }
        return resultstr;
    }

    @Override
    public boolean isEmpty() {
        return this.studySpaces.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        return this.studySpaces.keySet();
    }

    @Override
    public StudySpace put(String arg0, StudySpace arg1) {
        return this.studySpaces.put(arg0, arg1);
    }

    public void putIndex(String ssName, String ssId) {
        this.studySpaceNames.put(ssName, ssId);
    }

    @Override
    public void putAll(Map<? extends String, ? extends StudySpace> arg0) {
        this.studySpaces.putAll(arg0);

    }

    @Override
    public StudySpace remove(Object arg0) {
        return this.studySpaces.remove(arg0);
    }

    @Override
    public int size() {
        return this.studySpaces.size();
    }

    @Override
    public Collection<StudySpace> values() {
        return this.studySpaces.values();
    }

}
