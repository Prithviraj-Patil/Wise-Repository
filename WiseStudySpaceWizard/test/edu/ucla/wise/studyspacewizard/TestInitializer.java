package edu.ucla.wise.studyspacewizard;

import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

public class TestInitializer {

    public static void initialize() {
        StudySpaceWizardProperties properties = new StudySpaceWizardProperties(TestConstants.PATH_TO_PROPERTIES_FOLDER);
        StudySpaceWizard.initialize(properties, "/");
    }

}
