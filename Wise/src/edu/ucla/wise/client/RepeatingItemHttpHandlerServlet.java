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

import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.databank.UserDBConnection;

/**
 * RepeatingItemHttpHandlerServlet will handle deleting a repeating item from
 * the database.
 * 
 * Sample request expected
 * ?request_type=DELETE&table_name=repeat_set_project&instance_name
 * =hi&invitee_id=31
 * 
 */
@WebServlet("/survey/repeating_set_control")
public class RepeatingItemHttpHandlerServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String DELETE_REQUEST = "DELETE";
    public static final String REQUEST_TYPE = "request_type";
    public static final String TABLE_NAME = "table_name";
    public static final String INSTANCE_NAME = "instance_name";
    public static final String INVITEE_ID = "invitee_id";

    public static final String SUCCESS = "success";
    public static final String FAILURE = "failure";

    /**
     * Deletes a repeating item set from repeating item table.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text");
        PrintWriter out = res.getWriter();

        User user = BrowserRequestChecker.getUserFromSession(req, res, out);
        if (user == null) {
            out.close();
            return;
        }

        String requestType = req.getParameter(REQUEST_TYPE);
        if (DELETE_REQUEST.equalsIgnoreCase(requestType)) {

            String inviteeId = req.getParameter(INVITEE_ID);
            String tableName = req.getParameter(TABLE_NAME);
            String instanceName = req.getParameter(INSTANCE_NAME);

            /* get database connection */
            UserDBConnection userDbConnection = user.getMyDataBank();
            if (userDbConnection.deleteRowFromTable(tableName, instanceName)) {
                out.println(SUCCESS);
            } else {
                out.println(FAILURE);
            }

        } else {
            out.println("Please specify a request type");
            return;
        }
        out.close();
    }
}
