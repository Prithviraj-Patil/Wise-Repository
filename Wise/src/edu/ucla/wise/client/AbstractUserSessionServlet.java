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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WiseConstants;

public abstract class AbstractUserSessionServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        /* prepare for writing */
        PrintWriter out;
        res.setContentType("text/html");
        out = res.getWriter();

        HttpSession session = req.getSession(true);

        if (session.isNew()) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        User theUser = (User) session.getAttribute("USER");

        /* if the user can't be created, send error info */
        if (theUser == null) {
            out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
                    + "<LINK href='"
                    + SurveyorApplication.getInstance().getSharedFileUrl()
                    + "style.css' type=text/css rel=stylesheet>"
                    + "<body><center><table>"
                    // + "<body text=#000000 bgColor=#ffffcc><center><table>"
                    + "<tr><td>Error: WISE can't seem to store your identity in the browser. You may have disabled cookies.</td></tr>"
                    + "</table></center></body></html>");
            this.getLogger().error("WISE BEGIN - Error: Can't get the user from session");
            return;
        }

        out.println(this.serviceMethod(theUser, session));
    }

    public abstract Logger getLogger();

    public abstract String serviceMethod(User user, HttpSession session);

}
