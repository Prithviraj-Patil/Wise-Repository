package edu.ucla.wise.studyspacewizard;

import org.junit.Test;

public class TestStudySpaceCreator extends AbstractSSWTest {

    @Test
    public void testStudySpaceCreator() {
        if (!StudySpaceCreator.createStudySpace("testingSSCreator", "test", "SqlScripts\\studydb_template.sql")) {
            System.out.println("Study space creator failed");
        }
    }

}
