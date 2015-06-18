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
package edu.ucla.wise.commons;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.utils.TemplateUtil;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

/**
 * Class to represent common elements for *this* local installation of the wise
 * surveyor java application NOT the possibly-remote surveyor information for a
 * given StudySpace. Never instantiated; just initialize static variables.
 */
public class SurveyorApplication {

	private static SurveyorApplication surveyorApplication;
	public static String ApplicationName = "WISE";
	private final String sharedFileUrl;
	private final String sharedImageUrl;
	private final String servletUrl;

	public final String initErrorHtmlHead;

	private final String rootFolderPath;
	private final Configuration sqlTemplateConfiguration;
	private final Configuration htmlTemplateConfiguration;

	public static final String initErrorHtmlFoot = "</td></tr></table></center></body></html>";

	public SurveyorApplication(String appContext, String rootFolderPath, WiseProperties properties)
			throws IOException {
		if (ApplicationName == null) {
			SurveyorApplication.ApplicationName = appContext;
		}
		this.sharedFileUrl = properties.getServerRootUrl() + "/"
				+ SurveyorApplication.ApplicationName + "/survey/";
		this.sharedImageUrl = this.sharedFileUrl + "images/";
		this.servletUrl = properties.getServerRootUrl() + "/" + SurveyorApplication.ApplicationName
				+ "/" + "survey" + "/";

		this.initErrorHtmlHead = "<HTML><HEAD><TITLE>WISE survey system -- Startup error</TITLE>"
				+ "<LINK href='" + this.sharedFileUrl + "style.css' type=text/css rel=stylesheet>"
				+ "<body text=#000000 bgColor=#ffffcc><center><table>"
				+ "<tr><td>Sorry, the WISE Surveyor application failed to initialize. "
				+ "Please contact the system administrator with the following information.";

		if ((ApplicationName == null) || (this.servletUrl == null) || (this.sharedFileUrl == null)) {
			throw new IllegalArgumentException(
					"They survey application is not initialized correctly");
		}

		this.rootFolderPath = rootFolderPath;
		this.htmlTemplateConfiguration = this.createHtmlTemplateConfiguration(rootFolderPath);
		this.sqlTemplateConfiguration = TemplateUtil.createTemplateConfiguration(rootFolderPath,
				"admin/sql_templates");
	}

	private Configuration createHtmlTemplateConfiguration(String rootFolderPath) throws IOException {
		Configuration cfg = new Configuration();
		cfg.setDirectoryForTemplateLoading(new File(rootFolderPath + "survey/templates"));
		cfg.setIncompatibleImprovements(new Version(1, 0, 0));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setLocale(Locale.US);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		return cfg;
	}

	/**
	 * Checks if the static variables are initialized or not. If not the it
	 * initializes then in case of error it responds with error message.
	 * 
	 * @param appContext
	 *            Name of the application.
	 * @return String Error message in case of error or null
	 * @throws IOException
	 */
	public static void initialize(String appContext, String rootFolderPath,
			WiseProperties properties) throws IOException {
		if (surveyorApplication == null) {
			surveyorApplication = new SurveyorApplication(appContext, rootFolderPath, properties);
		}
	}

	public static void reload(String appContext, WiseProperties properties) throws IOException {
		surveyorApplication = new SurveyorApplication(appContext, SurveyorApplication.getInstance()
				.getRootFolderPath(), properties);
	}

	public static SurveyorApplication getInstance() {
		return surveyorApplication;
	}

	public String getSharedFileUrl() {
		return this.sharedFileUrl;
	}

	public String getSharedImageUrl() {
		return this.sharedImageUrl;
	}

	public String getServletUrl() {
		return this.servletUrl;
	}

	public Configuration getHtmlTemplateConfiguration() {
		return this.htmlTemplateConfiguration;
	}

	public String getRootFolderPath() {
		return this.rootFolderPath;
	}

	public Configuration getSQLTemplateConfiguration() {
		// TODO Auto-generated method stub
		return this.sqlTemplateConfiguration;
	}
}
