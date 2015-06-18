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

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.admin.healthmon.HealthMonitoringManager;
import edu.ucla.wise.commons.WiseConstants;

/**
 * LogonpServlet is a class which is called when user tries to log into wise
 * admin system, it also sets up AdminInfo in the admin user's session in case
 * the user has logged in with valid credentials.
 * 
 * @author Douglas Bell
 * @version 1.0
 */

@WebServlet("/admin/logonp")
public class LogonpServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LogonpServlet.class);

    /**
     * Checks if the user has entered proper credentials and also verifies if he
     * is blocked and initializes AdminInfo object or redirects to error page
     * accordingly.
     * 
     * @param request
     *            HTTP Request.
     * @param response
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html");
        String path = request.getContextPath();

        String userName = request.getParameter("username");
        String password = request.getParameter("password");

        if (Strings.isNullOrEmpty(userName)) {
            response.getWriter().println("Please provide a valid username");
            return;
        }

        if (Strings.isNullOrEmpty(password)) {
            response.getWriter().println("Please provide a valid password");
            return;
        }

        userName = userName.toLowerCase();

        // HttpSession session = req.getSession(true);
        /* security features changes */
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        session = request.getSession(true);
        /* end of security features changes */

        try {
            /* initialize AdminInfo instance and store in session */
            AdminUserSession adminUserSession = new AdminUserSession(userName, password);
            HealthMonitoringManager.monitor(adminUserSession);
            if (this.isUserBlocked(userName)) {
                LOGGER.error("User is blocked due to too many login attempts:" + userName);
                response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
            } else {
                session.setAttribute("ADMIN_USER_SESSION", adminUserSession);

                /* send HTTP request to create study space and admin user */
                response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/tool.jsp");
                LOGGER.info("Admin login Success!");
            }
        } catch (IllegalArgumentException e) {
            /* updating number of attempts made to login. */
            int numberOfAttempts = 0;
            if (AdminUserSession.getLoginAttemptNumbers().containsKey(userName)) {
                numberOfAttempts = AdminUserSession.getLoginAttemptNumbers().get(userName);
            }
            numberOfAttempts++;
            AdminUserSession.getLoginAttemptNumbers().put(userName, numberOfAttempts);

            /* checking if the user is blocked */
            if (this.isUserBlocked(userName)) {
                LOGGER.error("User is blocked due to too many login attempts:" + userName);
                response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
            } else {
                LOGGER.error("Incorrect input: Username or password was entered wrong.");
                response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
            }
        }
    }

    /**
     * Returns a boolean value after verifying if user has exceeded 5 failed
     * login attempts within last 30 mins
     * 
     * @param username
     *            Username who is trying to login.
     * @return If the user trying to login is blocked or not.
     */
    private boolean isUserBlocked(String username) {

        boolean userIsBlocked = false;
        if (AdminUserSession.getLoginAttemptNumbers().containsKey(username)) {
            int numberOfAttempts = AdminUserSession.getLoginAttemptNumbers().get(username);
            if (numberOfAttempts > 5) {
                if ((System.currentTimeMillis() - AdminUserSession.getLastlogintime().get(username)) < (30 * 60 * 1000)) {
                    userIsBlocked = true;
                }
            }
        }

        /* updating the last login time */
        AdminUserSession.getLastlogintime().put(username, System.currentTimeMillis());
        return userIsBlocked;
    }
}
