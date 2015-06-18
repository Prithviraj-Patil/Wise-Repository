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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.commons.MessageSequence;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.WiseConstants;

/**
 * SaveAnnoUserServlet is used to give anonymous users access to the survey.
 * User has to enter all the needed details to take up the survey, once done
 * user will be redirected to the survey welcome page.
 * 
 */
@WebServlet("/survey/save_anno_user")
public class AnonymousUserSaverServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AnonymousUserSaverServlet.class);

    /**
     * Creates a new invitee with all the entered details and then forwards the
     * user to welcome page to take up the survey.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter pw = response.getWriter();
        /* get the ecoded study space ID */
        String spaceidEncode = request.getParameter("t");

        /* Sanity check of the input variables */
        String path = request.getContextPath() + "/" + WiseConstants.ADMIN_APP;
        spaceidEncode = SanityCheck.onlyAlphaNumeric(spaceidEncode);

        if (Strings.isNullOrEmpty(spaceidEncode)) {
            response.sendRedirect(path + "/parameters_error.html");
            return;
        }

        /* decode study space ID */
        String spaceidDecode = WISEApplication.decode(spaceidEncode);
        StudySpace theStudy = StudySpaceMap.getInstance().get(spaceidDecode);

        LOGGER.debug("Trying to add a new anonymous user to space:'" + spaceidDecode + "'");
        /* adding new user */
        Map<String, String> parametersMap = new HashMap<String, String>();

        Enumeration<String> parametersNames = request.getParameterNames();

        ArrayList<String> inputs = new ArrayList<String>();
        while (parametersNames.hasMoreElements()) {
            String parameterName = parametersNames.nextElement();
            String[] parameterValues = request.getParameterValues(parameterName);
            inputs.add(parameterValues[0]);
            parametersMap.put(parameterName, parameterValues[0]);
        }

        /* Sanity of input parameters. */
        if (SanityCheck.sanityCheck(inputs)) {
            response.sendRedirect(path + "/sanity_error.html");
            return;
        }

        if (Strings.isNullOrEmpty(parametersMap.get("lastname"))) {
            pw.write("<html><body>The 'Last Name' field cannot be left blank</body><html>");
            pw.close();
            return;
        }

        int userId = theStudy.db.addInviteeAndReturnUserId(parametersMap);

        /*
         * Sending the New User initial invite Get the Message Sequence
         * associated with invite.
         */
        String surveyIdString = theStudy.db.getCurrentSurveyIdString();
        MessageSequence[] msgSeqs = theStudy.preface.getMessageSequences(surveyIdString);
        if (msgSeqs.length == 0) {
            pw.println("No message sequences found in Preface file for selected Survey");
        }
        String msgUseId = theStudy.sendInviteReturnMsgSeqId("invite", msgSeqs[0].id, surveyIdString,
                " invitee.id in ( " + userId + " )", false);
        request.setAttribute("msg", msgUseId);
        StringBuffer destination = new StringBuffer();
        destination.append("/WISE/survey").append("?msg=").append(msgUseId)
                .append("&t=" + WISEApplication.encode(theStudy.id)).append("&n=true");
        response.sendRedirect(destination.toString());
    }
}
