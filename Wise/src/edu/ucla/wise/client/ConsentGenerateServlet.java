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

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.ConsentForm;
import edu.ucla.wise.commons.IRBSet;
import edu.ucla.wise.commons.Preface;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ConsentGenerateServlet is a class is used to generate the consent form.
 * 
 */
@WebServlet("/survey/consent_generate")
public class ConsentGenerateServlet extends HttpServlet {
    static final long serialVersionUID = 1000L;
    private static final Logger LOGGER = Logger.getLogger(ConsentGenerateServlet.class);

    /**
     * Generates the consent form for user to enter the consent.
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
        res.setContentType("text/html");
        out = res.getWriter();

        HttpSession session = req.getSession(true);

        // Surveyor_Application s = (Surveyor_Application) session
        // .getAttribute("SurveyorInst");

        /* if session is new, then show the session expired info */
        if (session.isNew()) {
            res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
                    + WiseConstants.HTML_EXTENSION);
            return;
        }

        /* get the user object */
        User theUser = (User) session.getAttribute("USER");
        StudySpace studySpace = (StudySpace) session.getAttribute("STUDYSPACE");
        if ((theUser == null) || (studySpace == null) || (theUser.getId() == null)) {
            out.println("<p>Error: Can't find the user & study space info.</p>");
            return;
        }

        /* get the preface */
        Preface pf = studySpace.get_preface();
        if (pf != null) {
            if ((theUser.getIrbId() == null) || (theUser.getCurrentSurvey().getId() == null)) {
                out.println("<p>Error: the user's IRB/Survey ID should not be null</p>");
                return;
            }

            // String logo = "", aprNumb = "", expDate = "";

            String aprNumb = "", expDate = "";

            /* get the irb set from the list */
            IRBSet irbSet = pf.getIrbSet(theUser.getIrbId());

            if (irbSet != null) {

                // if (!irbSet.irb_logo.equalsIgnoreCase("")){
                // logo = irbSet.irb_logo;
                // }

                if (!irbSet.approvalNumber.equalsIgnoreCase("")) {
                    aprNumb = irbSet.approvalNumber;
                }
                if (!irbSet.expirDate.equalsIgnoreCase("")) {
                    expDate = irbSet.expirDate;
                }
            } else {
                out.println("<p>Can't find the IRB set in list with the user's IRB ID</p>");
                return;
            }

            /* get the consent form */
            ConsentForm consentForm = pf.getConsentFormSurveyIrb(theUser.getCurrentSurvey().getId(), theUser.getId());
            if (consentForm == null) {
                out.println("<p>Error: can't find consent form with the specified IRB/Survey ID</p>");
                return;
            }

            /* print out the consent form */
            String consentHtml = "", consentHeader = "", consentNotes = "";
            String consentHeaderHtml = "", consentP = "", consentUl = "", consentS = "";
            String title, subTitle, consentTitle = "";

            consentHeaderHtml = consentForm.consentHeaderHtml;
            consentP = consentForm.consentP;
            consentS = consentForm.consentS;
            consentUl = consentForm.consentUl;
            title = consentForm.title;
            subTitle = consentForm.subTitle;

            // compose the common header
            consentHeader += "<HTML><HEAD><TITLE>Cancer Screening Update: Consent Form</TITLE>";
            consentHeader += "<META http-equiv=Content-Type content='text/html; charset=iso-8859-1'>";
            consentHeader += "<SCRIPT> function FormSubmit(answerVal) { ";
            consentHeader += "document.form.answer.value = answerVal; document.form.submit(); } </SCRIPT>";
            consentHeader += "<LINK href='" + SurveyorApplication.getInstance().getSharedFileUrl()
                    + "style.css' type=text/css rel=stylesheet>";
            consentHeader += "<META content='MSHTML 6.00.2800.1170' name=GENERATOR></HEAD>";

            /* let style.css take care of background color and text color */
            // consent_header += "<body text=#000000 bgColor=#ffffcc>";
            consentHeader += "<body>";
            consentHeader += "<table cellSpacing=3 cellPadding=9 width=100% align=left border=0>";

            /* compose the common part at bottom */
            consentNotes += "<tr><td align=middle>";
            consentNotes += "<FORM name=form action='consent_record' method='post'>";
            consentNotes += "<DIV align=center><INPUT type=hidden name=answer>";
            consentNotes += "<A href=\"javascript:FormSubmit('yes')\"><IMG src='"
                    + "imageRender?img=accept.gif' border=0></A>";
            consentNotes += "&nbsp;<A href=\"javascript:FormSubmit('no')\"><IMG src='"
                    + "imageRender?img=decline.gif' border=0></A></DIV>";
            consentNotes += "</FORM></td></tr>";

            /* add the header part */
            consentHtml += consentHeader;

            /* compose the consent title */
            if (!title.equalsIgnoreCase("")) {
                consentTitle += "<B><FONT face='Arial, Helvetica, sans-serif' size=3>" + title + "</FONT></B>";
            }
            if (!subTitle.equalsIgnoreCase("")) {
                consentTitle += "<B> &#8212; <BR><FONT face='Arial, Helvetica, sans-serif' size=2>" + subTitle
                        + "</FONT></B><BR>";
            }

            /* add the custormerized html code if it exits */
            if (!consentHeaderHtml.equalsIgnoreCase("")) {
                consentHtml += "<tr><td align=center>" + consentHeaderHtml + "</td></tr>";
            }

            if (!consentTitle.equalsIgnoreCase("")) {
                consentHtml += "<tr><td align=center>" + consentTitle + "</td></tr>";
            }

            if (!consentP.equalsIgnoreCase("")) {
                consentHtml += "<tr><td align=left>" + consentP + "</td></tr>";
            }

            if (!consentUl.equalsIgnoreCase("")) {
                consentHtml += "<tr><td align=left>" + consentUl + "</td></tr>";
            }

            /* add in the acceptance cell */
            consentHtml += "<tr><td align=center>";
            consentHtml += "<TABLE cellSpacing=2 cellPadding=7 width=640 align=center border=1>";
            consentHtml += "<TR><TD align=center valign=top bgColor=#ffffff>";
            consentHtml += "<B><FONT face='Times New Roman, Times, serif' size=3>ACCEPTANCE</FONT></B>";
            consentHtml += "</TD></TR></TABLE>";
            consentHtml += "</td></tr>";

            /* add the bottom sign */
            consentHtml += "<tr><td align=left>" + consentS + "</td></tr>";

            /* add the bottom part to consent form */
            consentHtml += consentNotes;
            consentHtml += "<tr><td><p align=left><font size=2><b>IRB Number: " + aprNumb + "<br>";
            consentHtml += "Expiration Date: " + expDate + "</b></font></p>";
            consentHtml += "</td></tr><tr><td>";
            consentHtml += "<DIV align=center><FONT face='Arial, Helvetica, sans-serif' size=1>";
            consentHtml += "<I>This survey system is powered by the Web-based Interactive Survey Environment (WISE) at UCLA. ";
            consentHtml += "<a href='mailto:merg@mednet.ucla.edu'>Click here</a> ";
            consentHtml += "to report questions or problems regarding the use of the system.</font></I></FONT></DIV>";
            consentHtml += "</td></tr>";

            consentHtml += "</table></body></html>";

            /* print out the html form */
            out.println(consentHtml);
        } else {
            LOGGER.error("WISE - CONSENT GENERATE: can't find the preface object", null);
        }
        return;
    }

}
