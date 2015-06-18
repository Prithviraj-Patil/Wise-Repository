package edu.ucla.wise.studyspacewizard;

import org.apache.log4j.Logger;

import edu.ucla.wise.shared.properties.AbstractWiseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceDatabaseProperties;

public class StudySpaceWizardProperties extends AbstractWiseProperties implements StudySpaceDatabaseProperties {

    private static final long serialVersionUID = 1L;
    private static Logger LOGGER = Logger.getLogger(StudySpaceWizardProperties.class);

    private static final String DATABASE_SERVER_URL = "database.server.url";
    private static final String DATABASE_ROOT_USERNAME = "database.root.username";
    private static final String DATABASE_ROOT_PASSWORD = "database.root.password";

    private static final String ADMIN_USERNAME = "admin.username";
    private static final String ADMIN_PASSWORD = "admin.password";
    private static final String WEB_RESPOSNE_ENCRYPTION_KEY = "web.response.encryption.key";

    public StudySpaceWizardProperties(String propertiesFolderPath) {
        super(propertiesFolderPath + "/studyspacewizard.properties", "StudySpaceWizard");
        LOGGER.info("The database username provided is " + this.getDatabaseRootUsername());

        if (!this.isValid()) {
            throw new IllegalArgumentException("The properties file is invalid");
        }

    }

    public String getAdminUsername() {
        return this.getStringProperty(ADMIN_USERNAME);
    }

    public String getAdminPassword() {
        return this.getStringProperty(ADMIN_PASSWORD);
    }

    public String getWebResponseEncryptionKey() {
        return this.getStringProperty(WEB_RESPOSNE_ENCRYPTION_KEY);
    }

    @Override
    public String getDatabaseRootUsername() {
        return this.getStringProperty(DATABASE_ROOT_USERNAME);
    }

    @Override
    public String getDatabaseRootPassword() {
        return this.getStringProperty(DATABASE_ROOT_PASSWORD);
    }

    @Override
    public String getDatabaseServerUrl() {
        return this.getStringProperty(DATABASE_SERVER_URL);
    }

    @Override
    protected boolean isValid() {
        boolean result = true;

        result = result && this.isNotNullOrEmpty(this.getAdminUsername());
        result = result && this.isNotNullOrEmpty(this.getAdminPassword());
        result = result && this.isNotNullOrEmpty(this.getDatabaseServerUrl());
        result = result && this.isNotNullOrEmpty(this.getDatabaseRootUsername());
        result = result && this.isNotNullOrEmpty(this.getWebResponseEncryptionKey());

        return result;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
