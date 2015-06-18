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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.Charsets;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.admin.AdminUserSession;
import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.commons.AdminApplication;
import edu.ucla.wise.commons.FileExtensions;
import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.utils.FileUtils;
import freemarker.template.TemplateException;

@WebServlet("/admin/download_file")
public class DownloadFile extends AdminSessionServlet {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = Logger.getLogger(DownloadFile.class);

	@Override
	public void getMethod(HttpServletRequest request, HttpServletResponse response,
			AdminUserSession adminUserSession) throws IOException, TemplateException {

		PrintWriter out = response.getWriter();

		// get the server path
		String path = request.getContextPath();
		String fileExt = null;
		String filepath = null;
		String outputStr = null;

		// get the download file name from the request
		String filename = request.getParameter("fileName");

		if (Strings.isNullOrEmpty(filename)) {
			response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/error_pages/error.htm");
			return;
		}

		// if the file is the stylesheet
		if (filename.indexOf(".css") != -1) {
			InputStream cssStream = adminUserSession.getMyStudySpace().getDB()
					.getFileFromDatabase(filename, adminUserSession.getMyStudySpace().studyName);
			outputStr = FileUtils.getStringFromInputStream(cssStream);
		} else if (filename.indexOf(".sql") != -1) {
			// if the file is the database backup
			filepath = AdminApplication.getInstance().getDbBackupPath(); // dbase
																			// mysqldump
																			// file
			fileExt = FileExtensions.sql.name();
			outputStr = adminUserSession.buildSql(filepath, filename);
		} else if (filename.indexOf(".csv") != -1) {
			// if the file is the csv file (MS Excel)
			// no more creating the csv file (could be either the survey data or
			// the invitee list)
			outputStr = adminUserSession.buildCsvString(filename);
			fileExt = FileExtensions.csv.name();
		} else {
			InputStream xmlStream = adminUserSession.getMyStudySpace().getDB()
					.getXmlFileFromDatabase(filename);
			outputStr = FileUtils.getStringFromInputStream(xmlStream);
		}

		response.setContentType("text/" + fileExt);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		out.write(outputStr);
		out.close();
	}

	@Override
	public void postMethod(HttpServletRequest request, HttpServletResponse response,
			AdminUserSession adminUserSession) throws IOException {
		// do nothing
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

}
