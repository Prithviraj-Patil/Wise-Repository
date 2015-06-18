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

/**
 * This class contains the constants used in the application.
 */
public class WiseConstants {
    public static final String ADMIN_APP = "admin";
    public static final String SURVEY_APP = "survey";

    /* This is used by remote servlet loader to initiate the monitoring process. */
    public static final String SURVEY_HEALTH_LOADER = "survey_health";

    public enum STATES {
        started, completed, incompleter, non_responder, interrupted, start_reminder_1, start_reminder_2, start_reminder_3, completion_reminder_1, completion_reminder_2, completion_reminder_3,
    }

    public enum SURVEY_STATUS {
        OK, FAIL, NOT_AVAIL
    }

    public static final long surveyCheckInterval = 10 * 60 * 1000; // 10 mins
    public static final long surveyUpdateInterval = 5 * 60 * 1000; // 5 mins
    public static final long dbSmtpCheckInterval = 3 * 60 * 1000; // 3 mins

    public static final String NEW_INVITEE_JSP_PAGE = "new_invitee.jsp";

    public static final String HTML_EXTENSION = ".htm";
    public static final String NEWLINE = "\n";
    public static final String COMMA = ",";
    public static final Object NULL = "NULL";

}