package edu.ucla.wise.studyspacewizard.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import edu.ucla.wise.studyspacewizard.Constants;
import edu.ucla.wise.studyspacewizard.StudySpaceCreator;
import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

@WebServlet("/submitStudySpaceParams")
public class StudySpaceParametersAcceptor extends HttpServlet {

	public static final String STUDY_SPACE_NAME = "studySpaceName";
	public static final String SERVER_URL = "serverURL";
	public static final String SERVER_APP_NAME = "serverAppName";
	public static final String SERVER_SHAREDLINK_NAME = "sharedFiles_linkName";
	public static final String DIRECTORY_NAME = "dirName";
	public static final String DB_USERNAME = "dbuser";
	public static final String DB_PASSWORD = "dbpass";
	public static final String PROJECT_TITLE = "projectTitle";
	public static final String DATABASE_ENCRYPTION_KEY = "dbCryptKey";
	public static final String EMAIL_SENDING_TIME = "emailSendTime";

	private static final long serialVersionUID = 1L;

	@Override
	public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException,
			IOException {

		PrintWriter out = res.getWriter();

		ServletContext context = this.getServletContext();
		DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();

		String studySpaceName = req.getParameter(STUDY_SPACE_NAME);
		String serverURL = req.getParameter(SERVER_URL);
		String serverAppName = req.getParameter(SERVER_APP_NAME);
		String serverSharedLinkName = req.getParameter(SERVER_SHAREDLINK_NAME);
		String directoryName = req.getParameter(DIRECTORY_NAME);
		String databaseUsername = req.getParameter(DB_USERNAME);
		String databasePassword = req.getParameter(DB_PASSWORD);
		String projectTitle = req.getParameter(PROJECT_TITLE);
		String databaseEncryptionKey = req.getParameter(DATABASE_ENCRYPTION_KEY);
		String emailSendingTime = req.getParameter(EMAIL_SENDING_TIME);
		String databaseName = studySpaceName;

		// If any of the parameters are null or empty, return an error response
		if (Strings.isNullOrEmpty(serverURL) || Strings.isNullOrEmpty(serverAppName)
				|| Strings.isNullOrEmpty(serverSharedLinkName)
				|| Strings.isNullOrEmpty(directoryName) || Strings.isNullOrEmpty(databaseUsername)
				|| Strings.isNullOrEmpty(databasePassword) || Strings.isNullOrEmpty(projectTitle)
				|| Strings.isNullOrEmpty(databaseEncryptionKey)) {
			out.write("<div>Please ensure that none of the parameters are empty</div>");
			out.write("<div>" + studySpaceName + serverURL + serverAppName + serverSharedLinkName
					+ directoryName + databaseUsername + databasePassword + projectTitle
					+ databaseEncryptionKey + "</div>");
			return;
		}

		if (StudySpaceCreator.createStudySpace(studySpaceName, databasePassword,
				context.getRealPath(Constants.CREATE_STUDY_SPACE_SQL_FILEPATH))) {
			// TODO: register the newly created study space in the study space
			// table

			if (databaseConnector.writeStudySpaceParams(studySpaceName, serverURL, serverAppName,
					serverSharedLinkName, directoryName, databaseUsername, databaseName,
					databasePassword, projectTitle, databaseEncryptionKey,emailSendingTime)) {
				out.write("<div>Study space has been registered to Study Space tables</div>");
			} else {
				out.write("<div>Study space registration failed, please do it manually</div>");
			}
			out.write("<div>Study space has been successfully created</div>");
		} else {
			out.write("<div>Study space could not be successfully created. Check server logs for errors</div>");
		}
		return;

	}

}
