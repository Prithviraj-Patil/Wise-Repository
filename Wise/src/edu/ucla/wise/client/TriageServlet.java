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

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants.STATES;

/**
 * TriageServlet is used to direct the user after browser check to appropriate
 * next step or page.
 * 
 */
@WebServlet("/survey/start")
public class TriageServlet extends AbstractUserSessionServlet {
    public static final Logger LOGGER = Logger.getLogger(TriageServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Replaces the current page with the new page.
     * 
     * @param newPage
     *            Url of the new page
     * @return String html to replace the current page.
     */
    public String pageReplaceHtml(String newPage) {
        return "<html><head><script LANGUAGE='javascript'>top.location.replace('" + newPage + "');"
                + "</script></head><body></body></html>";
    }

    @Override
    public String serviceMethod(User user, HttpSession session) {
        StringBuilder response = new StringBuilder();

        String interviewBegin = (String) session.getAttribute("INTERVIEW");
        String mainUrl = "";

        /* check if user already completed the survey */
        if (user.completedSurvey()) {
            LOGGER.debug("User: '" + user.getId() + "' has completed the survey");
            if (user.getMyDataBank().getUserState().equalsIgnoreCase(STATES.incompleter.name())) {
                user.getMyDataBank().setUserState(STATES.started.name());
            }
            if (interviewBegin != null) {

                /*
                 * then IS an interview, always direct interviewer to the survey
                 * page. This previously *just* recorded the current page in the
                 * db; not sure why if interviewing and done
                 */
                mainUrl = SurveyorApplication.getInstance().getServletUrl() + "setup_survey";
            } else {

                /*
                 * not an interview forward to another application's URL, if
                 * specified in survey xml file.
                 */
                Survey currentSurvey = user.getCurrentSurvey();
                if (!Strings.isNullOrEmpty(currentSurvey.getForwardUrl())) {

                    mainUrl = currentSurvey.getForwardUrl();

                    /*
                     * if an educational module ID is specified in the survey
                     * xml, then add it to the URL
                     */
                    if (!Strings.isNullOrEmpty(currentSurvey.getEduModule())) {
                        mainUrl += "/" + currentSurvey.getStudySpace().dirName + "/survey?t="
                                + WISEApplication.encode(currentSurvey.getEduModule()) + "&r="
                                + WISEApplication.encode(user.getId());
                    } else {

                        /*
                         * otherwise the link will be the URL plus the user ID
                         * Added Study Space ID and Survey ID, was sending just
                         * the UserID earlier
                         */
                        mainUrl = mainUrl + "?s=" + WISEApplication.encode(user.getId()) + "&si="
                                + currentSurvey.getId() + "&ss="
                                + WISEApplication.encode(currentSurvey.getStudySpace().id);
                    }
                } else if (currentSurvey.getMinCompleters() == -1) {

                    /*
                     * if the min completers is not set in survey xml, then
                     * direct to Thank You page
                     */
                    mainUrl = SurveyorApplication.getInstance().getSharedFileUrl() + "thank_you";
                } else if (currentSurvey.getMinCompleters() != -1) {

                    /*
                     * this link may come from the invitation email for results
                     * review or user reclicked the old invitation link check if
                     * the number of completers has reached the minimum number
                     * set in survey xml, then redirect the user to the review
                     * result page
                     */
                    if (user.checkCompletionNumber() < currentSurvey.getMinCompleters()) {
                        mainUrl = SurveyorApplication.getInstance().getSharedFileUrl() + "thank_you" + "?review=false";
                    } else {
                        mainUrl = SurveyorApplication.getInstance().getServletUrl() + "view_results";
                    }
                }
            }
        } else if (user.startedSurvey()) {

            /*
             * for either user or interviewer, redirect to start the current
             * page.
             */
            mainUrl = SurveyorApplication.getInstance().getServletUrl() + "setup_survey";
        } else {

            mainUrl = SurveyorApplication.getInstance().getServletUrl() + "welcome";
        }

        LOGGER.info("User: '" + user.getId() + "' will be forwarded to " + mainUrl);
        /* output javascript to forward */
        response.append(this.pageReplaceHtml(mainUrl));

        return response.toString();
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}