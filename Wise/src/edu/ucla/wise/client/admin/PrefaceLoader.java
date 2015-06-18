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
package edu.ucla.wise.client.admin;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;

/**
 * PrefaceLoader is a class, which includes both welcome page and consent form
 * (optional) (continue running the URL request from the admin - load.jsp)
 */
@WebServlet("/survey/admin_preface_loader")
public class PrefaceLoader extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Checks if the user has entered proper credentials and also verifies if he
     * is blocked and initializes AdminInfo object or redirects to error page
     * accordingly.
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

        out.println("<table border=0>");

        /* get the survey name and study ID */
        String studyId = req.getParameter("SID");
        if (studyId == null) {
            out.println("<tr><td align=center>PREFACE LOADER ERROR: can't get the preface name or study id from URL</td></tr></table>");
            return;
        }

        /* get the study space */
        StudySpace studySpace = StudySpaceMap.getInstance().get(studyId);
        if (studySpace == null) {
            out.println("<tr><td align=center>SURVEY LOADER ERROR: can't create study space</td></tr></table>");
            return;
        }

        /* get the preface */
        if (studySpace.loadPreface()) {
            out.println("<tr><td align=center>The preface has been successfully loaded for the study space.<td></tr>");
        } else {
            out.println("<tr><td align=center>Failed to load the preface for the study space.<td></tr>");
        }
        out.println("</table>");
        return;
    }

}
