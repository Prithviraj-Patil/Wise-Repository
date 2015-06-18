package edu.ucla.wise.studyspacewizard.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspace.parameters.DatabaseRelatedConstants;
import edu.ucla.wise.studyspace.parameters.StudySpaceDatabaseProperties;
import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;
import edu.ucla.wise.studyspacewizard.Constants;

public class DatabaseConnector {

	private static final Logger LOGGER = Logger.getLogger(DatabaseConnector.class);

	private final StudySpaceDatabaseProperties properties;

	private static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
	private static final String URL = "jdbc:mysql://";

	public DatabaseConnector(StudySpaceDatabaseProperties properties, String rootFolderPath) {
		try {
			Class.forName(DRIVER_NAME).newInstance();
			LOGGER.info("*** Driver loaded ***");
		} catch (Exception e) {
			LOGGER.error(e);
			throw new IllegalStateException("Please provide the driver jars");
		}

		this.properties = properties;

		// check if database is up
		if (this.checkDBExists(Constants.COMMON_DATABASE_NAME)) {
			LOGGER.info("The common database " + Constants.COMMON_DATABASE_NAME + " is alive");
		} else {
			LOGGER.info("The common database " + Constants.COMMON_DATABASE_NAME + " is NOT created");
			this.executeSqlScript(rootFolderPath + Constants.CREATE_COMMON_DATABASE_SQL_FILEPATH,
					"");
		}

		Map<String, StudySpaceParameters> currentStudySpaces = this.getMapOfStudySpaceParameters();

		LOGGER.info("Printing current study spaces");

		for (String studySpaceName : currentStudySpaces.keySet()) {
			LOGGER.info("StudySpace: '" + studySpaceName + "'");
		}

	}

	public boolean checkDBExists(String dbName) {
		try {
			LOGGER.info("Checking if '" + dbName + "' exists in the database");
			LOGGER.info("Connecting to database: " + URL + this.properties.getDatabaseServerUrl());
			Connection conn = this.getConnection();
			ResultSet resultSet = conn.getMetaData().getCatalogs();

			LOGGER.info("Databases found on the current server:");
			while (resultSet.next()) {
				String databaseName = resultSet.getString(1);
				LOGGER.info("DB name: '" + databaseName + "'");
				if (databaseName.equals(dbName)) {
					return true;
				}
			}
			resultSet.close();
		} catch (SQLException e) {
			LOGGER.error("Exception while checking if DB " + dbName + " exists", e);
		}
		return false;
	}

	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL + this.properties.getDatabaseServerUrl() + "/",
				this.properties.getDatabaseRootUsername(),
				this.properties.getDatabaseRootPassword());
	}

	public Connection getConnection(String databaseName) throws SQLException {

		return DriverManager.getConnection(URL + this.properties.getDatabaseServerUrl() + "/"
				+ databaseName, this.properties.getDatabaseRootUsername(),
				this.properties.getDatabaseRootPassword());
	}

	public boolean executeSqlScript(String sqlScriptPath, String databaseName) {

		ArrayList<String> sqlStatementList = SqlScriptExecutor.createQueries(sqlScriptPath);

		for (String sqlStatement : sqlStatementList) {
			if (this.executeSqlStatement(sqlStatement, databaseName)) {
				LOGGER.info("Executed: " + sqlStatement);
			} else {
				LOGGER.info("Could not execute: " + sqlStatement);
				return false;
			}
		}
		return true;
	}

	public boolean executeSqlStatement(String statement, String databaseName) {

		try {
			Connection connection = this.getConnection(databaseName);
			Statement stmt = connection.createStatement();

			stmt.executeUpdate(statement);

		} catch (SQLException e) {
			LOGGER.error(e);
			return false;
		}

		return true;
	}

	public boolean createDatabase(String databaseName) {
		try {
			Connection connection = this.getConnection();
			Statement stmt = connection.createStatement();

			stmt.executeUpdate("CREATE DATABASE " + databaseName);
		} catch (SQLException e) {
			LOGGER.error("could not create database", e);
			return false;
		}
		return true;
	}

	public boolean grantUserPriviledges(String databaseName, String username, String password) {

		try {
			Connection connection = this.getConnection();
			Statement stmt = connection.createStatement();

			stmt.executeUpdate("GRANT USAGE ON *.* TO " + username + "@localhost IDENTIFIED BY '"
					+ password + "'");
			stmt.executeUpdate("GRANT ALL PRIVILEGES ON " + databaseName + ".* TO " + username
					+ "@localhost");
			System.out.println("Database:" + databaseName + " privileges granted for user "
					+ username);
		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return true;
	}

	public boolean writeStudySpaceParams(String studySpaceName, String serverURL,
			String serverAppName, String serverSharedLinkName, String directoryName,
			String dbUsername, String dbName, String dbPassword, String projectTitle,
			String databaseEncryptionKey, String emailSendingTime) {

		try {
			Connection connection = this.getConnection(Constants.COMMON_DATABASE_NAME);
			PreparedStatement stmt = connection
					.prepareStatement("INSERT INTO "
							+ Constants.STUDY_SPACE_METADATA_TABLE_NAME
							+ "(studySpaceName, server_url, serverApp, sharedFiles_linkName,dirName, dbuser, dbpass, dbname, proj_title, db_crypt_key,emailSendingTime) values (?,?,?,?,?,?,?,?,?,?,?)");
			System.out.println(stmt.toString());
			stmt.setString(1, studySpaceName);
			stmt.setString(2, serverURL);
			stmt.setString(3, serverAppName);
			stmt.setString(4, serverSharedLinkName);
			stmt.setString(5, directoryName);
			stmt.setString(6, dbUsername);
			stmt.setString(7, dbPassword);
			stmt.setString(8, dbName);
			stmt.setString(9, projectTitle);
			stmt.setString(10, databaseEncryptionKey);
			stmt.setString(11, emailSendingTime);

			if (stmt.executeUpdate() == 1) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			LOGGER.error(e);
			return false;
		}

	}

	public Map<String, String> getStudySpaceParameters(String studySpaceName) {

		String sql = "SELECT * FROM " + Constants.STUDY_SPACE_METADATA_TABLE_NAME
				+ " WHERE studySpaceName='" + studySpaceName + "'";

		Map<String, String> parametersMap = new HashMap<>();
		try {
			Connection connection = this.getConnection(Constants.COMMON_DATABASE_NAME);
			PreparedStatement statement = connection.prepareStatement(sql);

			ResultSet resultSet = statement.executeQuery();
			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

			while (resultSet.next()) {

				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					String columnName = resultSetMetaData.getColumnName(i);
					String value = resultSet.getString(columnName);

					parametersMap.put(columnName, value);
				}

			}
			return parametersMap;

		} catch (SQLException e) {
			LOGGER.error(e);
			return parametersMap;
		}
	}

	public Map<String, StudySpaceParameters> getMapOfStudySpaceParameters() {

		Map<String, StudySpaceParameters> allSpaceParametersMap = new HashMap<String, StudySpaceParameters>();

		List<Map<String, String>> listWithAllSpaces = this.getAllStudySpaceParameters();

		for (Map<String, String> singleSpace : listWithAllSpaces) {

			String spaceName = singleSpace.get(DatabaseRelatedConstants.STUDY_SPACE_NAME);

			StudySpaceParameters spaceParams = new StudySpaceParameters(singleSpace);

			allSpaceParametersMap.put(spaceName, spaceParams);

		}

		return allSpaceParametersMap;

	}

	public List<Map<String, String>> getAllStudySpaceParameters() {

		List<Map<String, String>> studySpaceParametersList = new ArrayList<Map<String, String>>();

		Connection connection;
		try {
			connection = this.getConnection(Constants.COMMON_DATABASE_NAME);
			PreparedStatement statement = connection.prepareStatement("SELECT * FROM "
					+ Constants.STUDY_SPACE_METADATA_TABLE_NAME);

			ResultSet resultSet = statement.executeQuery();

			ResultSetMetaData resultSetMetaData = resultSet.getMetaData();

			while (resultSet.next()) {

				HashMap<String, String> rowValues = new HashMap<String, String>();

				for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
					String columnName = resultSetMetaData.getColumnName(i);
					String value = resultSet.getString(columnName);
					rowValues.put(columnName, value);
				}

				studySpaceParametersList.add(rowValues);

			}

		} catch (SQLException e) {
			LOGGER.error(e);
		}
		return studySpaceParametersList;
	}

	public boolean destroyStudySpace(String studySpaceName) {
		try {
			Connection connection = this.getConnection();

			Statement stmt = connection.createStatement();

			stmt.executeUpdate("DROP SCHEMA " + studySpaceName);

			// remove entry from parameters table
			PreparedStatement stmt2 = connection
					.prepareStatement("DELETE FROM `study_space_parameters`.`parameters` WHERE `studySpaceName`=?");
			stmt2.setString(1, studySpaceName);

			stmt2.executeUpdate();

			return true;
		} catch (SQLException e) {
			LOGGER.error(e);
			return false;
		}
	}

}
