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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * BeginServlet directs the user coming from email URL or interviewers to
 * appropriate next step or page.
 */
@WebServlet("/survey")
public class BeginServlet extends HttpServlet {
    static final long serialVersionUID = 1000;
    private static final Logger LOGGER = Logger.getLogger(BeginServlet.class);

    private static final String STUDY_SPACE = "STUDYSPACE";
    private static final String USER = "USER";

    /**
     * Checks call the passed parameters and initializes the survey for the
     * user. Also checks number of users currently accessing the system and if
     * it more then 100000 new users are not allowed to access the survey.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        res.setContentType("text/html");

        HttpSession session = this.createNewSessionForAnonymousUser(req);

        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);

        String spaceIdEncode = parameters.getEncodedStudySpaceId();
        String msgId = parameters.getEncodedMessageId();
        String surveyIdEncode = parameters.getEncodedSurveyId();

        String sharedFileUrl = SurveyorApplication.getInstance().getSharedFileUrl();
        String servletUrl = SurveyorApplication.getInstance().getServletUrl();

        if (Strings.isNullOrEmpty(spaceIdEncode)) {
            res.sendRedirect(sharedFileUrl + "incorrectUrl" + WiseConstants.HTML_EXTENSION);
            return;
        }

        /*
         * This is a general email address without any specific invitee
         * information. Hence, ask the user to enter his details so that invitee
         * shall be created.
         */
        if (Strings.isNullOrEmpty(msgId)) {
            LOGGER.debug("The msgId:'" + msgId
                    + "' is used by anonymous invitees. Redirecting to new_invitee.jsp page.");
            StringBuffer destination = new StringBuffer();
            destination.append("/WISE/survey/").append(WiseConstants.NEW_INVITEE_JSP_PAGE);
            if (Strings.isNullOrEmpty(surveyIdEncode)) {
                res.sendRedirect(sharedFileUrl + "link_error" + WiseConstants.HTML_EXTENSION);
                return;
            }
            res.sendRedirect(destination.toString() + "?s=" + surveyIdEncode + "&t=" + spaceIdEncode);
            return;
        }

        /* decode study space ID */
        String spaceId = WISEApplication.decode(spaceIdEncode);

        /* initiate the study space ID and put it into the session */
        session.removeAttribute(STUDY_SPACE);
        StudySpace theStudy = StudySpaceMap.getInstance().get(spaceId);
        session.setAttribute(STUDY_SPACE, theStudy);

        if (theStudy == null) {
            res.sendRedirect(sharedFileUrl + "link_error" + WiseConstants.HTML_EXTENSION);
            return;
        }

        /* get the user ID */
        User theUser = (User) session.getAttribute(USER);

        /* create a new User if none is already found in the session */
        if (theUser == null) {
            theUser = theStudy.getUser(msgId);
        }

        /* if the user can't be retrieved or created, send error info */
        if ((theUser == null) || (theUser.getId() == null)) {
            req.getRequestDispatcher("./survey/error_jsps/UserNotIdentified.jsp?studySpaceName=" + theStudy.studyName)
                    .forward(req, res);
            LOGGER.error("The user cannot be determined for message id " + msgId, null);
            return;
        }

        /* put the user into the session */
        session.setAttribute(USER, theUser);
        LOGGER.info("The user has been identified: '" + theUser.getId() + "'");

        String mainUrl = sharedFileUrl + "browser_check" + WiseConstants.HTML_EXTENSION + "?w=" + servletUrl + "start";

        res.sendRedirect(mainUrl);
    }

    private HttpSession createNewSessionForAnonymousUser(HttpServletRequest request) {
        HttpSession session = null;
        String newSession = request.getParameter("n");
        /*
         * code for checking if the user is from anonymous survey page or
         * directly from the mail
         */
        if (!Strings.isNullOrEmpty(newSession)) {
            if (newSession.equalsIgnoreCase("true")) {
                LOGGER.info("New session created for anonymous user");
                session = request.getSession();
                session.invalidate();
                session = request.getSession(true);
            }
        } else {
            session = request.getSession(true);
        }
        return session;
    }
}
