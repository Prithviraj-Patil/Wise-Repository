package edu.ucla.wise.studyspacewizard;

import org.junit.BeforeClass;

import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class AbstractSSWTest {
    @BeforeClass
    public static void before() {
        StudySpaceWizardProperties properties = new StudySpaceWizardProperties(TestConstants.PATH_TO_PROPERTIES_FOLDER);
        StudySpaceWizard.initialize(properties, "/");
    }

}
