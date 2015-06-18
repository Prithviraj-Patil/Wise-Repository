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
import freemarker.template.TemplateException;

public class SaveWati extends AdminSessionServlet {

    private static final Logger LOGGER = Logger.getLogger(SaveWati.class);
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();
        // get the path
        String path = request.getContextPath();

        String surveyId = request.getParameter("survey");
        String interviewerId = request.getParameter("interviewer");
        if ((interviewerId == null) || interviewerId.equals("")) {
            out.write("<p>");
            out.write("Error: You must select one interviewer");
            out.write("</p>");
            return;
        }
        session.setAttribute("SURVEY_ID", surveyId);
        session.setAttribute("INTERVIEWER_ID", interviewerId);

        String url = null;

        String whereStr = request.getParameter("whereclause");
        if ((whereStr == null) || whereStr.equals("")) {
            String allUser = request.getParameter("alluser");
            if ((allUser == null) || allUser.equals("")) {
                String nonResp = request.getParameter("nonresp");
                if ((nonResp == null) || nonResp.equals("")) {
                    String user[] = request.getParameterValues("user");
                    whereStr = "id in (";
                    for (int i = 0; i < user.length; i++) {
                        whereStr += user[i] + ",";
                    }
                    whereStr = whereStr.substring(0, whereStr.lastIndexOf(',')) + ")";
                } else {
                    whereStr = "id not in (select distinct invitee from survey_subject)";
                }
            } else {
                whereStr = "id in (select distinct id from invitee)";
            }
        }

        String output = adminUserSession.getMyStudySpace().saveWati(whereStr, interviewerId, surveyId);

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
