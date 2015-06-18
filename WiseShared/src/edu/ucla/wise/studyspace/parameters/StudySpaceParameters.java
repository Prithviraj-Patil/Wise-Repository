package edu.ucla.wise.studyspace.parameters;

import java.util.Map;

//Class to encapsulate study space parameters
public class StudySpaceParameters {
	
	private final String name;
	private final String id;
	private final String serverUrl;
	private final String serverApplication;
	private final String sharedFiles_linkName;
	private final String folderName;
	private final String databaseName;
	private final String databaseUsername;
	private final String databasePassword;
	private final String projectTitle;
	private final String databaseEncryptionKey;
	private final String emailSendingTime;

	public StudySpaceParameters(String name, String id, String serverUrl,
			String serverApplication, String sharedFiles_linkName,
			String folderName, String databaseName, String databaseUsername,
			String databasePassword, String projectTitle,
			String databaseEncryptionKey, String emailSendingTime) {

		this.name = name;
		this.id = id;
		this.serverUrl = serverUrl;
		this.serverApplication = serverApplication;
		this.sharedFiles_linkName = sharedFiles_linkName;
		this.folderName = folderName;
		this.databaseName = databaseName;
		this.databaseUsername = databaseUsername;
		this.databasePassword = databasePassword;
		this.projectTitle = projectTitle;
		this.databaseEncryptionKey = databaseEncryptionKey;
		this.emailSendingTime = emailSendingTime;

	}

	public StudySpaceParameters(Map<String, String> parametersMap) {

		this.name = parametersMap
				.get(DatabaseRelatedConstants.STUDY_SPACE_NAME);
		this.id = parametersMap
				.get(DatabaseRelatedConstants.STUDY_SPACE_ID);
		this.serverUrl = parametersMap
				.get(DatabaseRelatedConstants.SERVER_URL);
		this.serverApplication = parametersMap
				.get(DatabaseRelatedConstants.SERVER_APPLICATION);
		this.sharedFiles_linkName = parametersMap
				.get(DatabaseRelatedConstants.SHARED_FILES_LINK_NAME);
		this.folderName = parametersMap
				.get(DatabaseRelatedConstants.DIRECTORY_NAME);
		this.databaseName = parametersMap
				.get(DatabaseRelatedConstants.DATABASE_NAME);
		this.databaseUsername = parametersMap
				.get(DatabaseRelatedConstants.DATABASE_USER);
		this.databasePassword = parametersMap
				.get(DatabaseRelatedConstants.DATABASE_PASSWORD);
		this.projectTitle = parametersMap
				.get(DatabaseRelatedConstants.PROJECT_TITLE);
		this.databaseEncryptionKey = parametersMap
				.get(DatabaseRelatedConstants.DATABASE_CRYPTIC_KEY);
		this.emailSendingTime = parametersMap
				.get(DatabaseRelatedConstants.EMAIL_SENDING_TIME);

	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the serverUrl
	 */
	public String getServerUrl() {
		return serverUrl;
	}

	/**
	 * @return the serverApplication
	 */
	public String getServerApplication() {
		return serverApplication;
	}

	/**
	 * @return the sharedFiles_linkName
	 */
	public String getSharedFiles_linkName() {
		return sharedFiles_linkName;
	}

	/**
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * @return the databaseName
	 */
	public String getDatabaseName() {
		return databaseName;
	}

	/**
	 * @return the databaseUsername
	 */
	public String getDatabaseUsername() {
		return databaseUsername;
	}

	/**
	 * @return the databasePassword
	 */
	public String getDatabasePassword() {
		return databasePassword;
	}

	/**
	 * @return the projectTitle
	 */
	public String getProjectTitle() {
		return projectTitle;
	}

	/**
	 * @return the databaseEncryptionKey
	 */
	public String getDatabaseEncryptionKey() {
		return databaseEncryptionKey;
	}

	@Override
	public String toString() {
		StringBuilder parameters = new StringBuilder();

		parameters.append(name).append('|');
		parameters.append(id).append('|');
		parameters.append(serverUrl).append('|');
		parameters.append(serverApplication).append('|');
		parameters.append(sharedFiles_linkName).append('|');
		parameters.append(folderName).append('|');
		parameters.append(databaseUsername).append('|');
		parameters.append(databasePassword).append('|');
		parameters.append(projectTitle).append('|');
		parameters.append(databaseEncryptionKey).append('|');
		parameters.append(emailSendingTime).append("||");

		return parameters.toString();

	}

	/**
	 * @return the emailSendingTime
	 */
	public String getEmailSendingTime() {
		return emailSendingTime;
	}

}
