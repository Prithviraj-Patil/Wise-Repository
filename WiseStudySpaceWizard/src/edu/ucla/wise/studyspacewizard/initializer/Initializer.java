package edu.ucla.wise.studyspacewizard.initializer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.studyspacewizard.StudySpaceWizardProperties;

public class Initializer implements ServletContextListener {
    private static final String WISE_STUDY_SPACE_WIZARD_HOME = "WISE_SSW_HOME";
    private static final Logger LOGGER = Logger.getLogger(Initializer.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // do nothing
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            String wiseSSWPropertiesFolderPath = System.getenv(WISE_STUDY_SPACE_WIZARD_HOME);

            if (!Strings.isNullOrEmpty(wiseSSWPropertiesFolderPath)) {
                LOGGER.info("Initializing WISEStudySpaceWizard.");
                String rootFolderPath = servletContextEvent.getServletContext().getRealPath("/");

                StudySpaceWizardProperties properties = new StudySpaceWizardProperties(wiseSSWPropertiesFolderPath);
                StudySpaceWizard.initialize(properties, rootFolderPath);
                LOGGER.info("WISEStudySpaceWizard initialized.");
            } else {
                LOGGER.error("Could not locate the properties file. The environment variable '"
                        + WISE_STUDY_SPACE_WIZARD_HOME
                        + "' needs to be set to point to the folder containing the properties file");
            }

        } catch (RuntimeException e) {
            LOGGER.error(e);
        }
    }
}
