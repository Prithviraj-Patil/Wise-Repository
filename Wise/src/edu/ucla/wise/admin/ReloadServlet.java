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
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.initializer.WiseProperties;

/**
 * ReloadServlet class is used to load a new survey and set up its Data tables.
 * (Called via URL request from load.jsp in the admin application)
 * 
 */
@WebServlet("/admin/reload")
public class ReloadServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(ReloadServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Reloads the new survey and sets up its data tables.
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
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        HttpSession session = req.getSession(true);
        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(req);

        /* check if the session is still valid */
        AdminUserSession adminInfo = parameters.getAdminUserSessionFromHttpSession();
        if (adminInfo == null) {
            out.println("Wise Admin - Reload Error: Can't get your Admin Info");
            return;
        }
        WiseProperties properties = new WiseProperties("wise.properties", "WISE");
        String initErr = AdminApplication.forceInit(req.getContextPath(), req.getServletContext().getRealPath("/"),
                properties);
        out.println("<HTML><HEAD><TITLE>WISE Admin Reloader</TITLE>"
                + "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
                + "<body text=#000000 bgColor=#ffffcc><center><table>");
        if (initErr != null) {
            out.println("<tr><td>Sorry, the WISE Administration application failed to initialize. "
                    + "Please contact the system administrator with the following information." + "<P>" + initErr
                    + "</td></tr>");
            LOGGER.error("WISE Admin Init Error: " + initErr, null);
        } else {
            out.println("<tr><td align=center>WISE Admin Application Reload succeeded.</td></tr></table>");
        }
        out.println("</table></center></body></html>");
    }
}
