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
package edu.ucla.wise.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;

/**
 * Dev2ProdServlet class is used for converting the survey system from
 * Development to production mode. Once converted from Development mode to
 * Production mode you cannot come back.
 * 
 */
@WebServlet("/admin/dev2prod")
public class Dev2ProdServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(Dev2ProdServlet.class);
    private static final long serialVersionUID = 1L;

    /**
     * Converts the survey system from Development to Production mode.
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

        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);

        /* prepare to write */
        PrintWriter out;
        res.setContentType("text/html");
        out = res.getWriter();

        /* get the server path */
        String path = req.getContextPath();
        out.println("<html><head>");
        out.println("<link rel='stylesheet' href='" + path + "/style.css' type='text/css'>");
        out.println("<title>WISE CHANGE SURVEY MODE</title>");
        out.println("</head><body text=#333333 bgcolor=#FFFFCC>");
        out.println("<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>");
        out.println("<tr><td>");
        HttpSession session = parameters.getSession(true);
        if (session.isNew()) {
            out.println("<h2>Your session has timed out.</h2><p>");
            out.println("<h3>Please return to the <a href='../'>admin logon page</a> and try again.</h3>");
            out.println("</td></tr></table></center></body></html>");
            out.close();
            return;
        }
        AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();
        String internalId = parameters.getEncodedStudySpaceId();

        /* if session does not exists */
        if ((adminUserSession == null) || (internalId == null)) {
            out.println("Wise Admin - Dev to Prod Error: Can't get the Admin Info");
            return;
        }

        out.println("Changing status from DEVELOPMENT to PRODUCTION...<br>");
        adminUserSession.getMyStudySpace().changeDevToProd(internalId);
        out.println("<p><a href='../tool.jsp'>Return to Administration Tools</a>");
        out.println("</td></tr></table></center></body></html>");
        out.close();
    }

}
