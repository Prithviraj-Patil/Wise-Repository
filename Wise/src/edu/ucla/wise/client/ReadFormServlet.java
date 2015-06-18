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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ReadFormServlet is used to update the results of the user taking the survey
 * and redirecting them to a correct page based on the action.
 * 
 */
@WebServlet("/survey/readform")
public class ReadFormServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReadFormServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Creates a Html page with the input address as the new page.
     * 
     * @param newPage
     *            Url of the new page.
     * @return String Html of the new page.
     */
    public String pageReplaceHtml(String newPage) {
        return "<html>" + "<head><script LANGUAGE='javascript'>" + "top.location.replace('" + newPage + "');"
                + "</script></head>" + "<body></body>" + "</html>";
    }

    /**
     * Updates the answers of users and redirect the survey to next page
     * correctly.
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

        /* prepare to write */
        PrintWriter out;
        res.setContentType("text/html");
        out = res.getWriter();

        HttpSession session = req.getSession(true);
        // Surveyor_Application s = (Surveyor_Application) session
        // .getAttribute("SurveyorInst");
        if (session.isNew()) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "/error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        /* get the user from session */
        User theUser = (User) session.getAttribute("USER");
        if (theUser == null) {
            out.println("<p>Error: Can't find the user info.</p>");
            return;
        }

        /*
         * get all the fields values from the form and save them in the hash
         * table
         */
        HashMap<String, Object> params = new HashMap<String, Object>();
        String n, v;
        Enumeration e = req.getParameterNames();

        /* To check the sanity of the inputs */
        ArrayList<String> inputs = new ArrayList<String>();

        while (e.hasMoreElements()) {
            n = (String) e.nextElement();
            v = req.getParameter(n);
            inputs.add(v);
            params.put(n, v);
        }
        String path = req.getContextPath();
        if (SanityCheck.sanityCheck(inputs)) {
            res.sendRedirect(path + "/admin/error_pages/sanity_error.html");
            return;
        }

        String action = req.getParameter("action");
        if ((action == null) || (action.equals(""))) {

            /* if no action value is specified, fill in default */
            action = "NEXT";
        }

        String newPage = "";

        /* User jumping to page selected from progress bar */
        if (action.equalsIgnoreCase("linkpage")) {

            /*
             * the next page will be the page clicked by the user or the
             * interviewer
             */
            theUser.readAndAdvancePage(params, false);
            String linkPageId = req.getParameter("nextPage");
            theUser.setPage(linkPageId);
            newPage = "view_form?p=" + theUser.getCurrentPage().getId();
            out.println("<html>");
            out.println("<head></head>");
            out.println("<body ONLOAD=\"self.location = '" + newPage + "';\"></body>");
            out.println("</html>");
        } else if (action.equalsIgnoreCase("INTERRUPT")) {

            /* Detect interrupt states; forward to appropos page */
            theUser.readAndAdvancePage(params, false);
            theUser.setInterrupt();
            session.invalidate();
            newPage = SurveyorApplication.getInstance().getSharedFileUrl() + "interrupt" + WiseConstants.HTML_EXTENSION;
            out.println(this.pageReplaceHtml(newPage));
            return;
        } else if (action.equalsIgnoreCase("TIMEOUT")) {

            /* if it is an timeout event, then show the timeout info */
            theUser.readAndAdvancePage(params, false);
            theUser.setInterrupt();
            session.invalidate();
            newPage = SurveyorApplication.getInstance().getSharedFileUrl() + "timeout" + WiseConstants.HTML_EXTENSION;
            out.println(this.pageReplaceHtml(newPage));
            return;
        } else if (action.equalsIgnoreCase("ABORT")) {

            /*
             * if it is an abort event (entire window was closed), then record
             * event; nothing to show
             */
            theUser.readAndAdvancePage(params, false);
            theUser.setInterrupt();

            /*
             * should force user object to be dropped & connections to be
             * cleaned up
             */
            session.invalidate();
            return;
        } else {

            /*
             * either done or continuing; go ahead and advance page give user
             * submitted http params to record & process
             */
            theUser.readAndAdvancePage(params, true);
        }

        if (theUser.completedSurvey()) {

            /* check if it is an interview process */
            Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
            if (inv != null) {

                /* record interview info in the database */
                inv.setDone();

                /* remove the current user info */
                session.removeAttribute("USER");

                /* redirect to the show overview page */
                newPage = SurveyorApplication.getInstance().getSharedFileUrl() + "interview/Show_Assignment.jsp";
            } else {

                /*
                 * redirect the user to the forwarding URL specified in survey
                 * xml file
                 */
                if ((theUser.getCurrentSurvey().getForwardUrl() != null)
                        && !theUser.getCurrentSurvey().getForwardUrl().equalsIgnoreCase("")) {

                    // for example:
                    // forward_url="http://localhost:8080/ca/servlet/begin?t="
                    newPage = theUser.getCurrentSurvey().getForwardUrl();
                    // if the EDU ID (study space ID) is specified in survey
                    // xml,
                    // then add it to the URL
                    if ((theUser.getCurrentSurvey().getEduModule() != null)
                            && !theUser.getCurrentSurvey().getEduModule().equalsIgnoreCase("")) {
                        // new_page = new_page +
                        // "/"+theUser.getCurrentSurvey().study_space.dir_name+"/servlet/begin?t="

                        newPage = newPage + "/" + theUser.getCurrentSurvey().getStudySpace().dirName + "/survey?t="
                                + WISEApplication.encode(theUser.getCurrentSurvey().getEduModule()) + "&r="
                                + WISEApplication.encode(theUser.getId());

                    } else {
                        /* otherwise the link will be the URL plus the user ID */
                        newPage = newPage + "?s=" + WISEApplication.encode(theUser.getId()) + "&si="
                                + theUser.getCurrentSurvey().getId() + "&ss="
                                + WISEApplication.encode(theUser.getCurrentSurvey().getStudySpace().id);
                        LOGGER.info(newPage + ReadFormServlet.class.getName());
                    }
                } else {

                    /* Setting the User state to completed. */
                    theUser.setComplete();

                    // -1 is default if no results are going to be reviewed.
                    if (theUser.getCurrentSurvey().getMinCompleters() == -1) {
                        newPage = SurveyorApplication.getInstance().getSharedFileUrl() + "thank_you";
                    } else {
                        /*
                         * go to results review, send the view result email only
                         * once when it reaches the min number of completers
                         */
                        int currentNumbCompleters = theUser.checkCompletionNumber();
                        String review = "false";

                        if (currentNumbCompleters >= theUser.getCurrentSurvey().getMinCompleters()) {
                            review = "view_results";
                        }

                        /*
                         * redirect to the thank you html with the review link
                         * for the current user and future completers
                         */
                        newPage = SurveyorApplication.getInstance().getSharedFileUrl() + "/thank_you?review=" + review;
                    }
                }

            } // end of else (not interview)
            out.println(this.pageReplaceHtml(newPage));
        } else {

            /*
             * continue to the next page form the link to the next page
             */
            newPage = "view_form?p=" + theUser.getCurrentPage().getId();

            out.println("<html>");
            out.println("<head></head>");
            out.println("<body ONLOAD=\"self.location = '" + newPage + "';\"></body>");
            out.println("</html>");
        }
        out.close();
    }
}