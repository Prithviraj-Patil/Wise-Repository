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
import edu.ucla.wise.commons.WiseConstants;

/* 
 Handle the interviewer's log out
 */

/**
 * InterviewLogoutServlet is a class which is called when interviewer tries to
 * log out.
 */
@WebServlet("/survey/interview_logout")
public class InterviewLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 100L;

    /**
     * Logs out the interviewer and removes Interviewer object from the session
     * and redirects login page accordingly.
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

        String url = null;
        HttpSession session = req.getSession(true);

        // Surveyor_Application s = (Surveyor_Application) session
        // .getAttribute("SurveyorInst");

        if (session.isNew()) {
            url = "interview/expired.htm";
        } else if (session != null) {
            Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");

            /* get the URL of the forwarding page */
            url = inv.getStudySpace().appUrlRoot + WiseConstants.SURVEY_APP + "/interview/expired.htm";

            /* remove the interviewer from the session */
            session.removeAttribute("INTERVIEWER");

            /* end the session */
            session.invalidate();
        }
        res.sendRedirect(url);
    }

}
