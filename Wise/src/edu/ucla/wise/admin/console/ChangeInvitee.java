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
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.client.web.TemplateUtils;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.web.WebResponseMessage;
import freemarker.template.TemplateException;

@WebServlet("/admin/change_invitee.jsp")
public class ChangeInvitee extends AdminSessionServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChangeInvitee.class);

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {

        PrintWriter out = response.getWriter();

        String path = request.getContextPath();
        // get the edit type - add, delete, update etc from the request
        String editType = request.getParameter("cedit");
        // Security features Changes
        if (SanityCheck.sanityCheck(editType)) {
            response.sendRedirect("/admin/error_pages/sanity_error.html");
            return;
        }
        editType = SanityCheck.onlyAlphaNumeric(editType);

        String colOName, colName, colValue, colDef;
        String column_name, column_type, column_default, column_key;

        // get the new column name, data type, default value and old column
        // name
        if (editType != null) {
            colName = request.getParameter("cname");
            colValue = request.getParameter("ctype");
            colDef = request.getParameter("cdefault");
            colOName = request.getParameter("coname");
            if (SanityCheck.sanityCheck(colName) || SanityCheck.sanityCheck(colValue)
                    || SanityCheck.sanityCheck(colDef) || SanityCheck.sanityCheck(colOName)) {
                response.sendRedirect(path + "/admin/error_pages/sanity_error.html");
                return;
            }
            colName = SanityCheck.onlyAlphaNumericandSpecial(colName);
            colValue = SanityCheck.onlyAlphaNumericandSpecial(colValue);
            colDef = SanityCheck.onlyAlphaNumericandSpecial(colDef);
            colOName = SanityCheck.onlyAlphaNumericandSpecial(colOName);
            // end of security features changes

            WebResponseMessage crudWebResponseMessage = adminUserSession.getMyStudySpace().modifyInviteeTable(editType,
                    colName, colValue, colDef, colOName);

            WebResponseMessage describeInviteeResponse = adminUserSession.getMyStudySpace().describeInviteeTable();

            if (crudWebResponseMessage.isSuccess() && describeInviteeResponse.isSuccess()) {
                Map<String, Object> parametersForChangeInviteePage = new HashMap<>();
                parametersForChangeInviteePage.put("crudMessage", crudWebResponseMessage.getResponse());
                parametersForChangeInviteePage.put("inviteeTableDescription", describeInviteeResponse.getResponse());
                String html = TemplateUtils.getHtmlFromTemplateForAdmin(parametersForChangeInviteePage,
                        "changeInviteeTemplate.ftl");
                out.write(html);
            } else {
                out.write("Error in page, please contact the developers");
            }
        }
    }

    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession) {
        // do nothing

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
