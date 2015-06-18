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
package edu.ucla.wise.persistence.data;

public class WiseTables {

    private static final String SURVEY_USER_PAGE_STATUS_TABLE = "survey_user_page";
    private static final String MAIN_DATA_TEXT_TABLE = "data_text";
    private static final String MAIN_DATA_INTEGER_TABLE = "data_integer";
    private static final String INVITEE_TABLE = "invitee";
    private static final String DATA_RPT_INS_TO_QUES_ID_TABLE = "data_rpt_ins_id_to_ques_id";
    private static final String DATA_REPEAT_SET_INSTANCE_TABLE = "data_repeat_set_instance";
    private static final String SURVEYS_TABLE = "surveys";
    private static final String SURVEY_MESSAGE_USE_TABLE = "survey_message_use";
    private static final String INTERVIEW_ASSIGNMENT_TABLE = "interview_assignment";
    private static final String CONSENT_RESPONSE_TABLE = "consent_response";
    private static final String SURVEY_USER_STATE_TABLE = "survey_user_state";
    private static final String PAGE_SUBMIT_TABLE = "page_submit";
    private static final String PENDING_TABLE = "pending";

    private final String schemaName;

    public WiseTables(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSurveyUserPageStatus() {
        return this.getSchemaPrefix() + SURVEY_USER_PAGE_STATUS_TABLE;
    }

    public String getMainDataText() {
        return this.getSchemaPrefix() + MAIN_DATA_TEXT_TABLE;
    }

    public String getMainDataInteger() {
        return this.getSchemaPrefix() + MAIN_DATA_INTEGER_TABLE;
    }

    public String getInvitee() {
        return this.getSchemaPrefix() + INVITEE_TABLE;
    }

    public String getDataRepeatInstanceToQuestionId() {
        return this.getSchemaPrefix() + DATA_RPT_INS_TO_QUES_ID_TABLE;
    }

    public String getDataRepeatSetToInstance() {
        return this.getSchemaPrefix() + DATA_REPEAT_SET_INSTANCE_TABLE;
    }

    private String getSchemaPrefix() {
        return this.schemaName + ".";
    }

    public String getSurveys() {
        return this.getSchemaPrefix() + SURVEYS_TABLE;
    }

    public String getSurveyMessageUse() {
        return this.getSchemaPrefix() + SURVEY_MESSAGE_USE_TABLE;
    }

    public String getInterviewAssignment() {
        return this.getSchemaPrefix() + INTERVIEW_ASSIGNMENT_TABLE;
    }

    public String getConsentResponse() {
        return this.getSchemaPrefix() + CONSENT_RESPONSE_TABLE;
    }

    public String getSurveyUserState() {
        return this.getSchemaPrefix() + SURVEY_USER_STATE_TABLE;
    }

    public String getPageSubmit() {
        return this.getSchemaPrefix() + PAGE_SUBMIT_TABLE;
    }

    public String getPending() {
        return this.getSchemaPrefix() + PENDING_TABLE;
    }
}
