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
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.Page;
import edu.ucla.wise.commons.SanityCheck;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.Survey;

/**
 * ViewResultsServlet class used to view the survey results (with the summary of
 * data) by page (viewed by admin on behalf of Admin Application). URL:
 * /survey/admin_view_results.
 */
@WebServlet("/survey/admin_view_results")
public class ViewResultsServlet extends HttpServlet {
	static final long serialVersionUID = 1000;

	/**
	 * Sets the session parameters for the next page to be get the results and
	 * also prints the results for current page.
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

		/* prepare for writing */
		res.setContentType("text/html");
		PrintWriter out = res.getWriter();
		String path = req.getContextPath();

		HttpSession session = req.getSession(true);
		String studyId, surveyId;

		/* check if it is the first link */
		String a = req.getParameter("a");
		a = SanityCheck.onlyAlphaNumeric(a);

		/* create session info from the first URL link */
		if ((a != null) && a.equalsIgnoreCase("FIRSTPAGE")) {
			studyId = req.getParameter("SID");
			surveyId = req.getParameter("s");
			studyId = SanityCheck.onlyAlphaNumeric(studyId);
			surveyId = SanityCheck.onlyAlphaNumeric(surveyId);

			StudySpace studySpace = StudySpaceMap.getInstance().get(studyId);
			Survey survey = studySpace.getSurvey(surveyId);
			String pageId = survey.getPages()[0].getId();
			String whereStr = req.getParameter("whereclause");

			session.setAttribute("STUDYSPACE", studySpace);
			session.setAttribute("SURVEY", survey);
			session.setAttribute("PAGEID", pageId);

			/* get the user or the user group whose results will be presented */

			if (whereStr == null) {
				whereStr = "";
			}
			if (whereStr.equals("")) {

				/* check if specific users selected */
				String allUser = req.getParameter("alluser");
				if (SanityCheck.sanityCheck(allUser)) {
					res.sendRedirect(path + "/admin/error_pages/sanity_error.html");
					return;
				}
				if ((allUser == null) || allUser.equals("") || allUser.equalsIgnoreCase("null")) {
					String user = req.getParameter("user");
					if (SanityCheck.sanityCheck(user)) {
						res.sendRedirect(path + "/admin/error_pages/sanity_error.html");
						return;
					}

					/* get the specified user list */
					if (user == null) {
						out.println("Please select at least one invitee");
						return;
					}
					whereStr = "invitee in (" + user + ")";
				}
			}

			/* update whereclause in a session */
			session.removeAttribute("whereStr");
			session.setAttribute("WHERECLAUSE", whereStr);

			/* call itself to display the page */
			res.sendRedirect("admin_view_results");
		} else {

			/* get the survey from the session */
			Survey survey = (Survey) session.getAttribute("SURVEY");

			/* get the page id from the session */
			String pageId = (String) session.getAttribute("PAGEID");

			/* get the where string from the session */
			String whereClause = (String) session.getAttribute("WHERECLAUSE");

			if ((survey == null) || (whereClause == null) || (pageId == null)) {
				out.println("<p>ADMIN VIEW RESULTS Error: "
						+ "can't get the study where string/survey/page info.</p>");
				return;
			}

			/* get the current page */
			Page pg = survey.getPage(pageId);

			/* update the page id to be the next page */
			session.removeAttribute("PAGEID");
			if (!survey.isLastPage(pageId)) {
				session.setAttribute("PAGEID", survey.nextPage(pageId).getId());
			}

			/* display the result on current page */
			out.println(pg.renderAdminResults(whereClause));
		}
		out.close();
	}
}
