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

import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.Survey;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.User;
import edu.ucla.wise.commons.WiseConstants;

/**
 * ViewOpenResultsServlet creates a summary report for each individual open
 * question
 * 
 */

@WebServlet("/survey/view_open_results")
public class ViewOpenResultsServlet extends HttpServlet {
	public static final Logger LOGGER = Logger.getLogger(ViewOpenResultsServlet.class);
	static final long serialVersionUID = 1000;

	/**
	 * Displays results of an open question while viewing the results from wise
	 * admin page.
	 * 
	 * @param req
	 *            HTTP Request.
	 * @param res
	 *            HTTP Response.
	 * @throws ServletException
	 *             and IOException.
	 */
	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException,
			IOException {

		/* prepare to write */
		PrintWriter out;
		res.setContentType("text/html");
		out = res.getWriter();
		String path = req.getContextPath();

		HttpSession session = req.getSession(true);

		// Surveyor_Application s = (Surveyor_Application) session
		// .getAttribute("SurveyorInst");

		/* if session is new, then show the session expired info */
		if (session.isNew()) {
			res.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl() + "error"
					+ WiseConstants.HTML_EXTENSION);
			return;
		}

		/* get the user or the user group whose results will get presented */
		String whereClause = (String) session.getAttribute("WHERECLAUSE");
		if (whereClause == null) {
			whereClause = "";
		}

		/* get the unanswered question number */
		String unanswered = req.getParameter("u");

		/* get the question id */
		String question = req.getParameter("q");

		/* get the page id */
		String page = req.getParameter("t");

		if (SanityCheck.sanityCheck(unanswered) || SanityCheck.sanityCheck(question)
				|| SanityCheck.sanityCheck(page)) {
			res.sendRedirect(path + "/admin/error_pages/sanity_error.html");
			return;
		}
		unanswered = SanityCheck.onlyAlphaNumeric(unanswered);
		question = SanityCheck.onlyAlphaNumeric(question);
		page = SanityCheck.onlyAlphaNumeric(page);

		StudySpace studySpace;
		Survey survey;

		/* get the user from session */
		User theUser = (User) session.getAttribute("USER");

		if (theUser == null) {

			/* theUser is null means this view came from admin */
			studySpace = (StudySpace) session.getAttribute("STUDYSPACE");
			survey = (Survey) session.getAttribute("SURVEY");

			page = (String) session.getAttribute("PAGEID");
			if (page != null) {
				if (survey.previousPage(page) != null) {
					page = survey.previousPage(page).getId();
				} else {
					page = survey.firstPage().getId();
				}
			} else {
				page = survey.lastPage().getId();
			}

		} else {
			Survey currentSurvey = theUser.getCurrentSurvey();
			studySpace = currentSurvey.getStudySpace();
			survey = currentSurvey;
		}

		/* get the question stem */
		String qStem = "";
		Page pg = survey.getPage(page);
		if (pg != null) {
			qStem = pg.getTitle();
		}

		// find the question stem
		// for(int i=0; i<pg.items.length; i++)
		// {
		// if(pg.items[i].name!=null &&
		// pg.items[i].name.equalsIgnoreCase(question))
		// {
		// Question theQ = (Question) pg.items[i];
		// q_stem = theQ.stem;
		// break;
		// }
		// }

		/* display the report */
		out.println("<html><head>");
		out.println("<title>VIEW RESULTS - QUESTION:" + question.toUpperCase() + "</title>");
		out.println("<LINK href='" + SurveyorApplication.getInstance().getSharedFileUrl()
				+ "style.css' rel=stylesheet>");
		out.println("<style>");
		out.println(".tth {	border-color: #CC9933;}");
		out.println(".sfon{	font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 8pt; font-weight: bold; color: #996633;}");
		out.println("</style>");
		out.println("<script type='text/javascript' language='javascript' src=''></script>");
		out.println("</head><body text=#333333><center>");
		// out.println("</head><body text=#333333 bgcolor=#FFFFCC><center>");
		out.println("<table class=tth border=1 cellpadding=2 cellspacing=2 bgcolor=#FFFFF5>");
		out.println("<tr bgcolor=#BA5D5D>");
		out.println("<td align=left><font color=white>");
		out.println("<b>Question:</b> " + qStem + " <font size=-2><i>(" + question + ")</i></font>");
		out.println("</font>");
		out.println("</tr><tr>");
		out.println("<th width=200 class=sfon align=left><b>Answer:</b></th>");
		out.println("</tr>");

		out.print(studySpace.viewOpenResults(question, survey, page, whereClause, unanswered));

		out.println("<center><a href='../admin/view_result.jsp?s=" + survey.getId() + "'>");
		out.println("<img src='" + "imageRender?img=back.gif' /></a></center>");
		out.close();
	}
}
