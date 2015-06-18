package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestStudySpaceParametersTableEntry extends AbstractSSWTest {

    public void testSSWParametersTable() {
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        if (!databaseConnector.writeStudySpaceParams("studySpaceName", "serverURL", "serverAppName",
                "serverSharedLinkName", "directoryName", "dbUsername", "dbName", "dbPassword", "projectTitle",
                "databaseEncryptionKey","emailsendtime")) {
            System.out.println("Failed to write parameters to the database");
            ;
        } else {
            System.out.println("Wrote parameters to the database successfully");
        }
    }
}
