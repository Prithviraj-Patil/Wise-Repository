package edu.ucla.wise.studyspacewizard.initializer;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.wise.shared.web.WebCommandUtil;
import edu.ucla.wise.shared.web.WebRequester;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;
import edu.ucla.wise.studyspacewizard.StudySpaceWizardProperties;
import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;

public class StudySpaceWizard {

    private static StudySpaceWizard studySpaceWizard;

    private final StudySpaceWizardProperties properties;
    private final DatabaseConnector databaseConnector;
    private final String rootFolderPath;

    private static final Logger LOGGER = Logger.getLogger(StudySpaceWizard.class);

    private StudySpaceWizard(StudySpaceWizardProperties properties, String rootFolderPath) {
        this.properties = properties;
        this.databaseConnector = new DatabaseConnector(this.properties, rootFolderPath);
        this.rootFolderPath = rootFolderPath;
    }

    public static StudySpaceWizard getInstance() {
        return studySpaceWizard;
    }

    public static void initialize(StudySpaceWizardProperties properties, String rootFolderPath) {

        studySpaceWizard = new StudySpaceWizard(properties, rootFolderPath);

    }

    public static void destroy() {

    }

    public DatabaseConnector getDatabaseConnector() {
        return this.databaseConnector;
    }

    public StudySpaceWizardProperties getStudySpaceWizardProperties() {
        return this.properties;
    }

    /**
     * Call the reload URL to reload study space parameters on all managed
     * instances.
     * 
     * @throws IOException
     */
    public void reloadStudySpaceParametersOnManagedInstances() throws IOException {

        Map<String, StudySpaceParameters> mapOfStudySpaceParameters = this.databaseConnector
                .getMapOfStudySpaceParameters();

        for (String studyName : mapOfStudySpaceParameters.keySet()) {
            LOGGER.info("Reloading parameters for study space: '" + studyName + "'");
            StudySpaceParameters studySpaceParameters = mapOfStudySpaceParameters.get(studyName);

            String serverUrl = studySpaceParameters.getServerUrl();
            String serverApp = studySpaceParameters.getServerApplication();

            String url = WebCommandUtil.getUrlStringForReloadingStudies(serverUrl, serverApp,
                    this.properties.getWebResponseEncryptionKey());
            LOGGER.debug("The URL is: '" + url + "'");
            WebRequester webRequester = new WebRequester(url);
            webRequester.getResponseUsingGET();
        }

    }

}
