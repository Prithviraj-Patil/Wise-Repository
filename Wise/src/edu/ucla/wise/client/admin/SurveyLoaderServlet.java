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
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.databank.DataBank;
import edu.ucla.wise.initializer.StudySpaceParametersProvider;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;

/*
 Load a new survey and set up its Data tables. 
 (Called via URL request from load.jsp in the admin application)
 */

/**
 * SurveyLoaderServlet class is used to load a new survey and set up its Data
 * tables and also archives old tables. (Called via URL request from load.jsp in
 * the admin application)
 */
@WebServlet("/survey/admin_survey_loader")
public class SurveyLoaderServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(SurveyLoaderServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Archives the old survey and sets up new survey.
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

        out.println("<table border=0>");

        /* get the survey name and study ID */
        String surveyName = req.getParameter("SurveyName");
        String studyId = req.getParameter("SID");
        if ((surveyName == null) || (studyId == null)) {
            out.println("<tr><td align=center>SURVEY LOADER ERROR: can't "
                    + "get the survey name or study id from URL</td></tr></table>");
            return;
        }

        out.println("<tr><td align=center>SURVEY Name:" + surveyName + " STUDY ID: " + studyId + "</td></tr>");

        /* get the study space */
        StudySpace studySpace = StudySpaceMap.getInstance().get(studyId);
        if (studySpace == null) {
            out.println("<tr><td align=center>SURVEY LOADER ERROR: " + "can't create study space</td></tr></table>");
            return;
        }

        /* get the survey */
        String surveyID = studySpace.loadSurvey(surveyName);
        Survey survey = studySpace.getSurvey(surveyID);

        StudySpaceParameters params = StudySpaceParametersProvider.getInstance().getStudySpaceParameters(
                studySpace.studyName);

        DataBank db = new DataBank(studySpace, params);

        studySpace.archiveOldAndCreateNewDataTable(survey, surveyID);
        return;
    }
}
