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

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * DeclineNOWServlet class is used if user declined the invitation from the
 * email link, then forward him to the decline reason page.
 * 
 */

@WebServlet("/survey/declineNOW")
public class DeclineNOWServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DeclineNOWServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Forwards the page to decline page after checking all the necessary
     * parameter.
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

        /* get the ecoded study space ID */
        String spaceIdEncode = req.getParameter("t");

        /* get the email message ID */
        String msgIdEncode = req.getParameter("m");

        /*
         * if can't get sufficient information, then the email URL maybe broken
         * into lines
         */
        if ((msgIdEncode == null) || msgIdEncode.equalsIgnoreCase("") || (spaceIdEncode == null)
                || spaceIdEncode.equalsIgnoreCase("")) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "link_error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        /* decode the message ID & study space ID */
        String spaceId = WISEApplication.decode(spaceIdEncode);
        String msgId = WISEApplication.decode(msgIdEncode);

        /* initiate the study space ID and put it into the session */
        StudySpace theStudy = StudySpaceMap.getInstance().get(spaceId);
        User theUser = theStudy == null ? null : theStudy.getUser(msgId);

        /* if the user can't be created, send error info */
        if (theUser == null) {
            out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>");
            out.println("<LINK href='" + SurveyorApplication.getInstance().getSharedFileUrl()
                    + "style.css' type=text/css rel=stylesheet>");
            out.println("<body><center><table>");
            // out.println("<body text=#000000 bgColor=#ffffcc><center><table>");
            out.println("<tr><td>Error: Can't get the user information.</td></tr>");
            out.println("</table></center></body></html>");
            LOGGER.error("WISE BEGIN - Error: Can't create the user.", null);
            return;
        }

        /* put the user into the session */
        session.setAttribute("USER", theUser);

        /* record this visit */
        theUser.recordDeclineHit(msgId, spaceId);

        /* mark user as declining */
        theUser.decline();

        /* forward to ask for reason of declining */
        String url = WISEApplication.getInstance().getWiseProperties() + "/WISE/" + WiseConstants.SURVEY_APP
                + "/decline" + WiseConstants.HTML_EXTENSION;
        res.sendRedirect(url);
        out.close();
    }
}
