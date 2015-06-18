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
package edu.ucla.wise.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ViewFormServlet displays a single survey page as a form to be filled out.
 * 
 */
@WebServlet("/survey/view_form")
public class ViewFormServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Sets up the session for the user accessing the survey and also redirects
     * the survey to correct page for returning users.
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

        HttpSession session = req.getSession(true);

        // Surveyor_Application s =
        // (Surveyor_Application)session.getAttribute("SurveyorInst");

        /* if session is new, then show the session expired info */
        if (session.isNew()) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        /* get the user from session */
        User theUser = (User) session.getAttribute("USER");
        if (theUser == null) {
            out.println("<p>Error: Can't find the user info.</p>");
            return;
        }

        /* check if it is an interview process */
        Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
        if (inv != null) {

            /* get the current page */
            String pageid = req.getParameter("p");

            /* set the current page */
            theUser.setCurrentPage(theUser.getCurrentSurvey().getPage(pageid));
        }
        out.println(SetupSurveyServlet.getHtml(theUser.getId(), this.getPageHTML(theUser),
                this.getProgressDivContent(theUser, session)));
    }

    /**
     * Returns the elements to go inside the progress div
     * 
     * @param theUser
     *            The user whose survey page is being displayed.
     * @param session
     *            Session under which survey is being displayed.
     * @return String html version of the progress bar
     */
    public String getProgressDivContent(User theUser, HttpSession session) {
        StringBuffer progressBar = new StringBuffer("");
        Hashtable<String, String> completedPages = theUser.getCompletedPages();

        /* get the interviewer if it is on the interview status */
        Interviewer intv = (Interviewer) session.getAttribute("INTERVIEWER");

        /* Interviewer can always browse any pages */
        if (intv != null) {
            theUser.getCurrentSurvey().setAllowGoback(true);
        }
        if (theUser.getCurrentSurvey().isAllowGoback()) {
            progressBar.append(theUser.getCurrentSurvey().printProgress(theUser.getCurrentPage()));
        } else {
            progressBar.append(theUser.getCurrentSurvey().printProgress(theUser.getCurrentPage(), completedPages));
        }

        return progressBar.toString();
    }

    /**
     * Returns the html version of current page.
     * 
     * @param theUser
     *            The user whose survey page is being displayed.
     * @return String html version of current page.
     */
    public String getPageHTML(User theUser) {
        StringBuffer pageHtml = new StringBuffer("");

        /* get the output string for the current page */
        String pOutput = theUser.getCurrentPage().renderPage(theUser);

        if ((pOutput != null) && !pOutput.equalsIgnoreCase("")) {
            pageHtml.append("<script type='text/javascript' language='JavaScript1.1' src='"
                    + SurveyorApplication.getInstance().getSharedFileUrl() + "/js/survey.js'></script>");
            pageHtml.append("<script type='text/javascript' src='"
                    + SurveyorApplication.getInstance().getSharedFileUrl() + "../js/jquery-1.7.1.min.js'></script>"
                    + "<script type='text/javascript' language='javascript' SRC='"
                    + SurveyorApplication.getInstance().getSharedFileUrl()
                    + "/js/survey_form_values_handler.js'></script>");
            pageHtml.append(pOutput);
        } else {

            /*
             * redirect to the next page by outputting hidden field values and
             * running JS submit()
             */
            pageHtml.append("<form name='mainform' method='post' action='readform'>");
            pageHtml.append("<input type='hidden' name='action' value=''>");
            if ((theUser.getCurrentSurvey().isLastPage(theUser.getCurrentPage().getId()))
                    || (theUser.getCurrentPage().isFinalPage())) {
                pageHtml.append("<input type='hidden' name='nextPage' value='DONE'>");
            } else {

                /*
                 * if the id of the next page is not set in the survey xml file
                 * (with value=NONE), then get its id from the page hash table
                 * in the survey class.
                 */
                if (theUser.getCurrentPage().getNextPage().equalsIgnoreCase("NONE")) {
                    pageHtml.append("<input type='hidden' name='nextPage' value='"
                            + theUser.getCurrentSurvey().nextPage(theUser.getCurrentPage().getId()).getId() + "'>");
                } else {

                    /* otherwise, assign the page id directly to the form */
                    pageHtml.append("<input type='hidden' name='nextPage' value='"
                            + theUser.getCurrentPage().getNextPage() + "'>");
                }
            }
            pageHtml.append("</form>");
            pageHtml.append("<script LANGUAGE='JavaScript1.1'>document.mainform.submit();</script>");
        }
        return pageHtml.toString();
    }
}