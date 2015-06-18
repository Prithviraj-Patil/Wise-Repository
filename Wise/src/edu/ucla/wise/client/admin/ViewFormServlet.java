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
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;

/**
 * ViewFormServlet class is used to view the survey (without data filled in)
 * page by page through wise admin system.
 */
@WebServlet("/admin/admin_view_form")
public class ViewFormServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Sets the session parameters for the next page to be viewed and also
     * prints the current page.
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

        HttpSession session = req.getSession(true);

        // Surveyor_Application sa = (Surveyor_Application) session
        // .getAttribute("SurveyorInst");

        String studyId, surveyId;

        /* check if it is the first link */
        String a = req.getParameter("a");
        a = SanityCheck.onlyAlphaNumeric(a);

        /* create session info from the first URL link */
        if ((a != null) && a.equalsIgnoreCase("FIRSTPAGE")) {
            /* get the study id */
            studyId = req.getParameter("SID");

            /* get the survey id */
            surveyId = req.getParameter("s");
            studyId = SanityCheck.onlyAlphaNumeric(studyId);
            surveyId = SanityCheck.onlyAlphaNumeric(surveyId);

            /* get the current study space */
            StudySpace ss = StudySpaceMap.getInstance().get(studyId);
            if (ss == null) {
                out.println("<p>ADMIN VIEW FORM Error: can't get the study space ID " + studyId + ".</p>");
                return;
            }

            /* set the study space in the session */
            session.setAttribute("STUDYSPACE", ss);

            /* get the current survey */
            Survey sy = ss.getSurvey(surveyId);
            if (sy == null) {
                out.println("<p>ADMIN VIEW FORM Error: can't get the survey ID " + surveyId + ".</p>");
                return;
            }

            /* set the survey in the session */
            session.setAttribute("SURVEY", sy);

            /* set the first page id */
            String pageId = sy.getPages()[0].getId();

            /* set the page id in the session as the current page id */
            session.setAttribute("PAGEID", pageId);

            /* call itself to display the page */
            res.sendRedirect("admin_view_form");
        } else {

            /* get the survey from the session */
            Survey survey = (Survey) session.getAttribute("SURVEY");

            /* get the page id from the session */
            String pageId = (String) session.getAttribute("PAGEID");

            /* get the study space from the session */
            StudySpace study_space = (StudySpace) session.getAttribute("STUDYSPACE");
            if ((survey == null) || (study_space == null) || (pageId == null)) {
                out.println("<p>ADMIN VIEW FORM Error: Your session has expired; please go back and resume from the admin page.</p>");
                return;
            }

            /* get the current page */
            Page pg = survey.getPage(pageId);

            /* update the page id to be the next page */
            session.removeAttribute("PAGEID");
            if (!survey.isLastPage(pageId)) {
                session.setAttribute("PAGEID", survey.nextPage(pageId).getId());
            }

            /* display the current page */
            String adminPage = pg.renderAdminPage(study_space);
            if ((adminPage.contains("#SHAREDFILEURL#") == true) || (adminPage.contains("#SHAREDIMAGEURL#") == true)) {

                // admin_page.replaceAll("#SHAREDFILEURL#", sa.shared_file_url);
                adminPage.replaceAll("#SHAREDFILEURL#", SurveyorApplication.getInstance().getSharedFileUrl());
                // admin_page.replaceAll("#SHAREDIMAGEURL#",
                // sa.shared_image_url);
                adminPage.replaceAll("#SHAREDIMAGEURL#", SurveyorApplication.getInstance().getSharedImageUrl());
            }

            // out.println(pg.render_admin_page(study_space));
            out.println(adminPage);
        }
        out.close();
    }

}
