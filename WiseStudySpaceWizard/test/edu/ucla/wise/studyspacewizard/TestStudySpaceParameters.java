package edu.ucla.wise.studyspacewizard;

import org.junit.Test;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestStudySpaceParameters extends AbstractSSWTest {

    @Test
    public void testStudySpaceParameters() {
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        System.out.println(databaseConnector.getStudySpaceParameters("test"));
    }
}
