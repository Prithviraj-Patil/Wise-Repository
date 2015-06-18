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
package edu.ucla.wise.client.interview;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Interviewer;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * InterviewLoginServlet is a class which is called when interviewer tries to
 * directly log in wise system, it also sets up Interviewer object in the
 * session, in case the interviewer has logged in with valid credentials.
 */
@WebServlet("/survey/interview_login")
public class InterviewLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1000L;

    /**
     * Checks if the interviewer has entered proper credentials and initializes
     * Interviewer object or redirects to error page accordingly.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * 
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");

        /* get the interviewer login info from the login form */
        String interviewerName = req.getParameter("interviewername");
        String interviewerId = req.getParameter("interviewerid");
        String studyId = req.getParameter("studyid");
        String path = req.getContextPath() + "/" + WiseConstants.ADMIN_APP;

        if (SanityCheck.sanityCheck(interviewerName) || SanityCheck.sanityCheck(interviewerId)
                || SanityCheck.sanityCheck(studyId)) {
            res.sendRedirect(path + "/sanity_error.html");
            return;
        }

        interviewerName = SanityCheck.onlyAlphaNumeric(interviewerName);
        interviewerId = SanityCheck.onlyAlphaNumeric(interviewerId);
        studyId = SanityCheck.onlyAlphaNumeric(studyId);

        if ((interviewerName == null) || interviewerName.isEmpty() || (interviewerId == null)
                || interviewerId.isEmpty() || (studyId == null) || studyId.isEmpty()) {
            res.sendRedirect(path + "/admin/parameters_error.html");
            return;
        }

        HttpSession session = req.getSession(true);

        /* get the study space and create the interviewer object */
        StudySpace theStudy = StudySpaceMap.getInstance().get(studyId);
        Interviewer inv = Interviewer.verifyInterviewer(theStudy, interviewerId, interviewerName);
        String url;

        /* check the interviewer's verification and assign the attributes */
        if (inv != null) {
            session.setAttribute("INTERVIEWER", inv);
            url = SurveyorApplication.getInstance().getSharedFileUrl() + "interview/Show_Assignment.jsp";
        } else {
            url = theStudy.appUrlRoot + theStudy.dirName + "/interview/error" + WiseConstants.HTML_EXTENSION;
        }
        res.sendRedirect(url);
    }
}
