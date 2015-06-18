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
package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.WiseConstants;

/**
 * SurveyResultServlet is a class used when user tries to check the responses of
 * people who have taken the survey.
 * 
 */
@WebServlet("/admin/survey_result")
public class SurveyResultServlet extends HttpServlet {
    private static final long serialVersionUID = 1000L;

    /**
     * Checks the validity of the sessions and redirects, so that results of the
     * question in the first page can be viewed.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        /* prepare for writing */
        PrintWriter out;
        res.setContentType("text/html");
        out = res.getWriter();

        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);
        /* get the survey ID from request and the admin info object from session */
        String surveyId = parameters.getEncodedSurveyId();

        AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();

        /* if the session is invalid, display the error */
        if ((adminUserSession == null) || (surveyId == null)) {
            out.println("Wise Admin - Survey Result Error: Can't get the Admin Info");
            return;
        }

        /*
         * get the selected users and the where clause of the query to select
         * users
         */
        String whereclauseV = req.getParameter("whereclause");
        String alluserV = req.getParameter("alluser");
        String userV[] = req.getParameterValues("user");

        /* initiate the user ID list */
        String userList = "";

        /* put each user ID into the list, seperated by comma */
        if (userV != null) {
            int lastI = userV.length - 1;
            for (int i = 0; i < lastI; i++) {
                userList += userV[i] + ",";
            }
            userList += userV[lastI];
        }

        /*
         * compose the forwarding URL to review the survey data conducted by the
         * selected users
         */
        res.sendRedirect(adminUserSession.getStudyServerPath() + WiseConstants.SURVEY_APP + "/admin_view_results?SID="
                + adminUserSession.getStudyId() + "&a=FIRSTPAGE&s=" + surveyId + "&whereclause=" + whereclauseV
                + "&alluser=" + alluserV + "&user=" + userList);
        out.close();
    }
}
