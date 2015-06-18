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
import edu.ucla.wise.commons.databank.UserDBConnection;

/**
 * RepeatingItemSetIOServlet will handle retrieving survey page values sent
 * through AJAX calls currently implemented only for the repeating item set.
 * 
 */
@WebServlet("/survey/repeating_item_io")
public class RepeatingItemSetIOServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Gets all the data that is there in the repeating item table and prepares
     * a Json object.
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
        res.setContentType("application/json");
        out = res.getWriter();
        HttpSession session = req.getSession(true);

        /*
         * if session is new, then it must have expired since begin; show the
         * session expired info
         */
        if (session.isNew()) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        /* get the user from session */
        User theUser = (User) session.getAttribute("USER");
        if ((theUser == null) || (theUser.getId() == null)) {

            /* latter signals an improperly-initialized User */
            out.println("<p>Error: Can't find the user info.</p>");
            return;
        }

        /* get database connection */
        UserDBConnection userDbConnection = theUser.getMyDataBank();

        /* get the table name from request */
        String repeatTableName = req.getParameter("repeat_table_name");

        /* get the table values as a string */
        String repeatTableValues = userDbConnection.getAllDataForRepeatingSet(repeatTableName);
        out.println(repeatTableValues);
    }
}
