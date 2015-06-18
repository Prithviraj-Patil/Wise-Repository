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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ConsentRecordServlet is a class which leads user to the survey if he accepted
 * the consent or lead him to the page to ask for decline reason if he declined
 * the consent.
 * 
 */

@WebServlet("/survey/consent_record")
public class ConsentRecordServlet extends HttpServlet {
    static final long serialVersionUID = 1000L;

    /**
     * Redirects the page to survey or decline consent reason.
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

        // Surveyor_Application s = (Surveyor_Application) session
        // .getAttribute("SurveyorInst");

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

        /* get user's consent decision */
        String answer = req.getParameter("answer");

        String url = "";
        // accepted the consent
        if (answer.equalsIgnoreCase("yes")) {
            theUser.consent();

            /* forward to setup_survey servlet */
            url = "setup_survey";
        } else if (answer.equalsIgnoreCase("no_consent")) {

            /*
             * Accepted the consent forward to setup_survey servlet, which
             * handles all other state changes.
             */
            url = "setup_survey";
        } else {

            /* declined the consent */
            theUser.decline();

            /* forward to decline servlet */
            url = SurveyorApplication.getInstance().getSharedFileUrl() + "decline" + WiseConstants.HTML_EXTENSION;
        }
        res.sendRedirect(url);
        out.close();
    }

}
