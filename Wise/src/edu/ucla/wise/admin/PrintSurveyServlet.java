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
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.WiseConstants;

/**
 * PrintSurveyServlet is a class used when user tries to print the survey form
 * wise admin system.
 * 
 */

@WebServlet("/admin/print_survey")
public class PrintSurveyServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PrintSurveyServlet.class);

    /**
     * Checks the validity of the sessions and the parameters and redirects, so
     * that first page can be printable
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) {
        LOGGER.info("Admin print survey called");
        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);
        try {

            /* prepare for writing */
            PrintWriter out;
            res.setContentType("text/html");
            out = res.getWriter();
            HttpSession session = req.getSession(true);

            String surveyId = parameters.getEncodedSurveyId();
            String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;
            if ((surveyId == null) || surveyId.isEmpty()) {
                res.sendRedirect(path + "/admin/parameters_error.html");
                return;
            }

            AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();

            /* check if the session is still valid */
            if (adminUserSession == null) {
                out.println("Wise Admin - Print Survey Error: Can't get the Admin Info");
                return;
            }

            /* Changing the URL pattern */
            String newUrl = adminUserSession.getStudyServerPath() + "/" + WiseConstants.ADMIN_APP + "/"
                    + "admin_print_survey?SID=" + adminUserSession.getStudyId() + "&a=FIRSTPAGE&s=" + surveyId;
            LOGGER.error("The URL built is: " + newUrl);
            res.sendRedirect(newUrl);
            out.close();
        } catch (IOException e) {
            LOGGER.error("IO Exception  while printing survey", e);
        }
    }
}
