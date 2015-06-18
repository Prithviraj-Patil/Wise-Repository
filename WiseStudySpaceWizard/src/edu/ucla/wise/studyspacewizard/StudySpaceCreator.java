package edu.ucla.wise.studyspacewizard;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class StudySpaceCreator {

    private static Logger log = Logger.getLogger(StudySpaceCreator.class);

    public static boolean createStudySpace(String studySpaceName, String password, String sqlFilePath) {
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        // first create a study space database
        if (!databaseConnector.createDatabase(studySpaceName)) {
            log.error("Database creation failed for " + studySpaceName);
            return false;
        }

        // give privileges to the user with studySpaceName and password

        if (!databaseConnector.grantUserPriviledges(studySpaceName, studySpaceName, password)) {
            log.error("Could not grant priviledges for " + studySpaceName);
            return false;
        }

        // create all the tables in the study space

        if (!databaseConnector.executeSqlScript(sqlFilePath, studySpaceName)) {
            log.error("Database table creation failed for " + studySpaceName + " with sql file at " + sqlFilePath);
            return false;
        }

        log.info("Study Space creation is successful:" + studySpaceName);
        return true;

    }
}