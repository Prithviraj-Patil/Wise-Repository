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
package edu.ucla.wise.client.interview;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.StudySpace;

/**
 * This class represents functionality around Interviewer. For. ex.
 * Add/Modify/Get/Delete an interviewer. This is a singleton class.
 */
public class InterviewManager {

    private static InterviewManager interviewManager = null;
    public static final Logger LOGGER = Logger.getLogger(InterviewManager.class);

    private InterviewManager() {
    }

    /**
     * Checks if the InterviewManager class is instantiated, if yes returns the
     * it else creates a new object and returns it.
     * 
     * @return a singleton instance of {@link InterviewManager}
     */
    public synchronized static InterviewManager getInstance() {
        if (interviewManager == null) {
            interviewManager = new InterviewManager();
        }
        return interviewManager;
    }

    /**
     * This function get Maximum ID that can be assigned to new
     * {@link Interviewer}.
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @return id string maximum ID in the database.
     */
    public synchronized String getNewId(StudySpace studySpace) {
        return studySpace.getNewId();
    }

    /**
     * Add a new interviewer by creating a new record in the interviewer table.
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @param interviewer
     *            Interview to be added to the table
     * @return id of the newly added interviewer
     */
    public synchronized String addInterviewer(StudySpace studySpace, Interviewer interviewer) {
        return studySpace.addInterviewer(interviewer);
    }

    public synchronized boolean removeInterviewer(StudySpace studySpace, String interviewers) {
        return studySpace.removeInterviewer(interviewers);
    }
    
    /**
     * Update the profile of the interviewer
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @param interviewer
     *            Interview to be added to the table
     * 
     * @return id of the updated interviewer
     */
    public String saveProfile(StudySpace studySpace, Interviewer interviewer) {

        return studySpace.saveProfile(interviewer);
    }

    /**
     * Search by interviewer ID to assign the attributes
     * 
     * @param studySpace
     *            Study space to which interviewer is related to.
     * @param interviewId
     *            Interviewer ID to assign parameters.
     * 
     * @return Interviewer object
     */
    public Interviewer getInterviewer(StudySpace studySpace, String interviewId) {

        return studySpace.getInterviewer(interviewId);

    }
}
