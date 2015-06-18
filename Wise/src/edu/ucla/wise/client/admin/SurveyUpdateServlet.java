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
package edu.ucla.wise.client.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;

/**
 * SurveyUpdateServlet is a class which is used to update the local survey info
 * Called by the Admin tool's drop_survey.jsp page *assuming* no error
 * SurveyStatus param to indicate change requested.
 */
@WebServlet("/admin/admin_survey_update")
public class SurveyUpdateServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(SurveyUpdateServlet.class);
    static final long serialVersionUID = 1000L;

    /**
     * Updates the survey info.
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
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        /*
         * get the survey ID, status and study ID survey status: D - delete
         * submitted data from surveys in developing mode R - remove the surveys
         * in developing mode P - clean up and archive the data of surveys in
         * production mode
         */
        String surveyId = req.getParameter("SurveyID");
        String surveyStatus = req.getParameter("SurveyStatus");
        String studyId = req.getParameter("SID");
        if ((surveyId == null) || (studyId == null) || (surveyStatus == null)) {
            out.println("<tr><td align=center>SURVEY UPDATE ERROR: "
                    + "can't get the survey id/status or study id from URL</td></tr></table>");
            return;
        }

        /* get the study space */
        StudySpace studySpace = StudySpaceMap.getInstance().get(studyId);
        if (studySpace == null) {
            out.println("<tr><td align=center>SURVEY UPDATE ERROR: "
                    + "can't find the requested study space</td></tr></table>");
            return;
        }

        try {
            if (surveyStatus.equalsIgnoreCase("R") || surveyStatus.equalsIgnoreCase("P")) {
                studySpace.dropSurvey(surveyId);
                out.println("Dropped survey " + surveyId);
            } else {
                out.println("Registered update of survey " + surveyId);
            }
        } catch (NullPointerException e) {
            LOGGER.error("WISE - DROP SURVEY DATA: " + e.toString(), null);
            out.println("<tr><td align=center>Survey Update Error: " + e.toString() + "</td></tr>");
        }
        return;
    }
}
