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
package edu.ucla.wise.admin.console;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.client.interview.InterviewManager;
import edu.ucla.wise.commons.Interviewer;
import freemarker.template.TemplateException;

public class SaveProfile extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(SaveProfile.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession();

        // get the path
        String path = request.getContextPath();
        String newId = null;

        Interviewer[] inv = (Interviewer[]) session.getAttribute("INTERVIEWER");
        if (inv == null) {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }

        if (inv != null) {
            if (session.getAttribute("EditType") != null) {

                String userName = request.getParameter("username_" + inv[0].getId());
                String firstName = request.getParameter("firstname_" + inv[0].getId()).toLowerCase();
                String lastName = request.getParameter("lastname_" + inv[0].getId()).toLowerCase();
                String salutation = request.getParameter("salutation_" + inv[0].getId());
                String email = request.getParameter("email_" + inv[0].getId());

                inv[0] = new Interviewer(adminUserSession.getMyStudySpace(), newId, userName, email, firstName,
                        lastName, salutation, "" + System.currentTimeMillis());
                newId = InterviewManager.getInstance().addInterviewer(adminUserSession.getMyStudySpace(), inv[0]);

                // remove the session attributes
                session.removeAttribute("EditType");

            } else {
                for (int i = 0; i < inv.length; i++) {
                    String userName = request.getParameter("username_" + inv[0].getId());
                    String firstName = request.getParameter("firstname_" + inv[0].getId()).toLowerCase();
                    String lastName = request.getParameter("lastname_" + inv[0].getId()).toLowerCase();
                    String salutation = request.getParameter("salutation_" + inv[0].getId());
                    String email = request.getParameter("email_" + inv[0].getId());

                    inv[i] = new Interviewer(adminUserSession.getMyStudySpace(), newId, userName, email, firstName,
                            lastName, salutation, "" + System.currentTimeMillis());

                    // record the changes of profile
                    newId = InterviewManager.getInstance().saveProfile(adminUserSession.getMyStudySpace(), inv[i]);

                }
            }
        } else {
            out.println("Error - can not get the interviewer object.");
        }
        if (newId != null) {
            out.println("Record Added/Updated successfully!");
        } else {
            out.println("Record Add/Update failed!</td>");
        }

    }

    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException {
        // do nothing
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

}
