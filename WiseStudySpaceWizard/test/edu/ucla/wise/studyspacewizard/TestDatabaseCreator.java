package edu.ucla.wise.studyspacewizard;

import org.junit.Test;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestDatabaseCreator extends AbstractSSWTest {

    private static final String TESTING_DATABASE = "testing_database";

    @Test
    public void createDatabaseTest() {
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        if (databaseConnector.checkDBExists(TESTING_DATABASE)) {
            System.out.println("Database exists, skipping");
        } else {
            if (databaseConnector.createDatabase(TESTING_DATABASE)) {
                System.out.println("Database creation suceesful: " + TESTING_DATABASE);
            } else {
                System.out.println("Database creation failed: " + TESTING_DATABASE);
            }
        }

    }
}
