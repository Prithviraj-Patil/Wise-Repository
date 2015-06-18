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

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.WiseConstants;

/**
 * PrintSurveyServlet is a class used when user tries to print the survey page
 * by page as in the survey XML form wise admin system.
 */
@WebServlet("/admin/admin_print_survey")
public class PrintSurveyServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    Logger log = Logger.getLogger(PrintSurveyServlet.class);

    /**
     * Sets the session parameters for the next page to be printed and also
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
        String a = req.getParameter("a");

        if (SanityCheck.sanityCheck(a)) {
            String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;
            res.sendRedirect(path + "/sanity_error.html");
            return;
        }
        a = SanityCheck.onlyAlphaNumeric(a);

        /* check if it is the first link */
        if ((a != null) && a.equalsIgnoreCase("FIRSTPAGE")) {

            /* get the study id */
            String studyId = req.getParameter("SID");

            /* get the survey id */
            String surveyId = req.getParameter("s");

            String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;

            if (SanityCheck.sanityCheck(studyId) || SanityCheck.sanityCheck(surveyId)) {
                res.sendRedirect(path + "/sanity_error.html");
                return;
            }

            studyId = SanityCheck.onlyAlphaNumeric(studyId);
            surveyId = SanityCheck.onlyAlphaNumeric(surveyId);

            if ((studyId == null) || studyId.isEmpty() || (surveyId == null) || surveyId.isEmpty()) {
                res.sendRedirect(path + "/parameters_error.html");
                return;
            }

            // LOGGER.error("The SID from the admin print_survey page is "+study_id);

            StudySpace ss = StudySpaceMap.getInstance().get(studyId);

            /* set the study space in the session */
            session.setAttribute("STUDYSPACE", ss);

            /* get the current survey */
            if (ss == null) {
                out.println("<p>ADMIN VIEW FORM Error: can't get details"
                        + " of study space with the given study space ID</p>");
                return;
            }
            Survey sy = ss.getSurvey(surveyId);

            /* set the survey in the session */
            session.setAttribute("SURVEY", sy);

            /* set the first page id */
            String pageId = sy.getPages()[0].getId();

            /* set the page id in the session as the current page id */
            session.setAttribute("PAGEID", pageId);

            /* call itself to display the page */
            res.sendRedirect("admin_print_survey?s=" + surveyId);
        } else {

            /* get the survey from the session */
            Survey survey = (Survey) session.getAttribute("SURVEY");

            /* get the page id from the session */
            String pageId = (String) session.getAttribute("PAGEID");

            /* get the study space from the session */
            StudySpace studySpace = (StudySpace) session.getAttribute("STUDYSPACE");
            if ((survey == null) || (studySpace == null) || (pageId == null)) {
                out.println("<p>ADMIN VIEW FORM Error: can't get the study space/survey/page info.</p>");
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
            out.println("<html><head>");
            out.println("<title>" + survey.getTitle().toUpperCase() + "</title>");
            out.println("<link rel='stylesheet' href='" + studySpace.styleUrl + "print.css' type='text/css'>");
            out.println("</head>");
            out.println("<body text='#000000' bgcolor='#FFFFFF'>");
            out.println(pg.printSurveyPage());
            out.println("</body></html>");
        }
        out.close();

    }
}
