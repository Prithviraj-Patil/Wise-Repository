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
package edu.ucla.wise.admin.web;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import freemarker.template.TemplateException;

/**
 * Checks if there is an active and validated session before proceeding.
 */
public abstract class AdminSessionServlet extends HttpServlet {

    public enum FunctionType {

        GET, POST

    }

    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 1L;

    @Override
    public final void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.validateSession(request, response, FunctionType.GET);
        } catch (TemplateException e) {
            this.getLogger().error(e);
        } catch (IOException e) {
            this.getLogger().error(e);
        }

    }

    @Override
    public final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            this.validateSession(request, response, FunctionType.POST);
        } catch (TemplateException e) {
            this.getLogger().error(e);
        } catch (IOException e) {
            this.getLogger().error(e);
        }

    }

    private void validateSession(HttpServletRequest request, HttpServletResponse response, FunctionType fType)
            throws IOException, TemplateException {
        String path = request.getContextPath();
        HttpSession session = request.getSession(true);
        // if the session is expired, go back to the logon page
        if (session.isNew()) {
            response.sendRedirect(path + "/index.html");
            return;
        }
        // get the admin info object from session
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
        // if the session is invalid, display the error
        if (adminUserSession == null) {
            request.setAttribute("error", "AdminUserSession is null");
            response.sendRedirect(path + "/error_pages/error.jsp?error=" + "NoAdminUserSession");
            return;
        }

        switch (fType) {
        case GET:
            this.getMethod(request, response, adminUserSession);
            break;
        case POST:
            this.postMethod(request, response, adminUserSession);
            break;
        }
    }

    public abstract void getMethod(HttpServletRequest request, HttpServletResponse response,
            AdminUserSession adminUserSession) throws IOException, TemplateException;

    public abstract void postMethod(HttpServletRequest request, HttpServletResponse response,
            AdminUserSession adminUserSession) throws IOException;

    public abstract Logger getLogger();

}
