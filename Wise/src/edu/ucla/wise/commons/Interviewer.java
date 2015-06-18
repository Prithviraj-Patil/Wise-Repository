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

import org.apache.log4j.Logger;

/**
 * This class represents an interviewer object
 */
public class Interviewer {
	public static final Logger LOGGER = Logger.getLogger(Interviewer.class);
	/** Instance Variables */
	private final StudySpace studySpace;

	public String id;
	public String userName;
	public String email;
	public String firstName;
	public String lastName;
	public String salutation;
	public String loginTime;

	public String interviewSessionId;
	public String interviewAssignId;

	public Interviewer(StudySpace studySpace, String id, String userName, String email,
			String firstName, String lastName, String salutation, String loginTime) {
		this.studySpace = studySpace;
		this.id = id;
		this.userName = userName;
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.salutation = salutation;
		this.loginTime = loginTime;

	}

	/**
	 * Getter method
	 * 
	 * @return StudySpace
	 */
	public StudySpace getStudySpace() {
		return this.studySpace;
	}

	/**
	 * Getter method
	 * 
	 * @return String id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Getter method
	 * 
	 * @return String userName
	 */
	public String getUserName() {
		return this.userName;
	}

	/**
	 * Getter method
	 * 
	 * @return String email
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Getter method
	 * 
	 * @return String firstName
	 */
	public String getFirstName() {
		return this.firstName;
	}

	/**
	 * Getter method
	 * 
	 * @return String lastName
	 */
	public String getLastName() {
		return this.lastName;
	}

	/**
	 * Getter method
	 * 
	 * @return String salutation
	 */
	public String getSalutation() {
		return this.salutation;
	}

	/**
	 * Getter method
	 * 
	 * @return String loginTime
	 */
	public String getLoginTime() {
		return this.loginTime;
	}

	/**
	 * check the interviewer's verification when logging in and assign the
	 * attributes.
	 * 
	 * @param interviewId
	 *            Id of the interviewer who is trying to login to the system.
	 * @param interviewUsername
	 *            User name of the interviewer.
	 * @return boolean If the user is logging in with valid credentials or not.
	 */
	public static Interviewer verifyInterviewer(StudySpace studySpace, String interviewId,
			String interviewUsername) {
		return studySpace.verifyInterviewer(interviewId, interviewUsername);
	}

	/**
	 * Creates an interview survey message in the table of survey_message_use
	 * before starting the interview.
	 * 
	 * @param inviteeId
	 *            Invitee ID for whom the message has to be created.
	 * @param surveyId
	 *            Survey ID to whom the invitee is linked to.
	 * @return String message ID is returned which is used for making the URL
	 *         for the invitee to access the system
	 */
	public String createSurveyMessage(String inviteeId, String surveyId) {
		return this.studySpace.createSurveyMessage(inviteeId, surveyId);
	}

	/**
	 * create an interview session in the table of interview_session when
	 * starting the interview.
	 * 
	 * @param userSession
	 *            Session ID whose value is put into the data base for this
	 *            interviewer.
	 */
	public void beginSession(String userSession) {
		/*
		 * the interview_session_id is a foreign key reference to the user's
		 * survey session id
		 */
		this.interviewSessionId = userSession;

		this.studySpace.beginInterviewSession(userSession);
	}

	/**
	 * save the interview session info in the table of interview_assignment
	 * before ending the session
	 */
	public void setDone() {
		this.studySpace.saveInterviewSession(this.interviewAssignId);
	}

}
