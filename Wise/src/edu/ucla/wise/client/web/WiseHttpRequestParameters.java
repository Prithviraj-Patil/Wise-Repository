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
package edu.ucla.wise.client.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.commons.SanityCheck;

/**
 * Use this class to get parameters from HTTP requests.
 */
public class WiseHttpRequestParameters {

    /**
     * HttpRequest to be wrapped by this method.
     */
    private final HttpServletRequest request;

    /**
     * Constructor to create a wrapper around request.
     * 
     * @param request
     */
    public WiseHttpRequestParameters(HttpServletRequest request) {
        this.request = request;
    }

    public String getAlphaNumericParameterValue(String parameter) {
        String value = SanityCheck.onlyAlphaNumeric(this.request.getParameter(parameter));
        return value;
    }

    public String getNonSanitizedStringParameter(String parameter) {
        return this.request.getParameter(parameter);
    }

    public String getEncodedStudySpaceId() {
        return this.getAlphaNumericParameterValue("t");
    }

    public String getEncodedMessageId() {
        return this.getAlphaNumericParameterValue("msg");
    }

    public String getEncodedSurveyId() {
        return this.getAlphaNumericParameterValue("s");
    }

    public AdminUserSession getAdminUserSessionFromHttpSession() {
        return (AdminUserSession) this.getSession(true).getAttribute("ADMIN_USER_SESSION");
    }

    public HttpSession getSession(boolean createNew) {
        return this.request.getSession(createNew);
    }
}
