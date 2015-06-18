/**
 * Copyright (c) 2014, Regents of the University of California
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation 
 * and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.ucla.wise.commons.databank;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import edu.ucla.wise.commons.WiseConstants;
import edu.ucla.wise.commons.databank.model.DataInteger;
import edu.ucla.wise.commons.databank.model.DataRepeatSetInstance;
import edu.ucla.wise.commons.databank.model.DataRepeatSetInstanceToQuestionId;
import edu.ucla.wise.commons.databank.model.DataText;
import edu.ucla.wise.commons.databank.model.MainData;
import edu.ucla.wise.commons.databank.model.MainData.DataType;
import edu.ucla.wise.persistence.data.Answer;
import edu.ucla.wise.persistence.data.RepeatingItemInstance;
import edu.ucla.wise.persistence.data.WiseTables;
import edu.ucla.wise.utils.SQLTemplateUtil;
import freemarker.template.TemplateException;

/**
 * A subordinate of the DataBank class to isolate functionality related to
 * providing results.
 * 
 */
public class ResultDataProvider {

	private final DataBankInterface databank;
	private final WiseTables wiseTables;

	private final SQLTemplateUtil sqlTemplateUtil;

	private static final Logger LOGGER = Logger.getLogger(ResultDataProvider.class);

	public ResultDataProvider(DataBankInterface databank) {
		this.databank = databank;
		this.wiseTables = databank.getWiseTables();
		this.sqlTemplateUtil = databank.getSqlTemplateUtil();
	}

	/**
	 * Table with all the answers for all invitees who have provided them.
	 * 
	 * @param surveyId
	 * @param questionLevel
	 * @return a csv string that can be opened in a spreadsheet.
	 */
	public String getInviteeAnswersTable(String surveyId, int questionLevel) {

		StringBuilder resultBuilder = new StringBuilder();

		List<String> invitees = this.getDistinctInviteeIdsFromDataTable(surveyId, questionLevel);
		List<String> questions = this.getDistinctQuestionIds(surveyId, questionLevel);

		// print header row

		resultBuilder.append("inviteeId").append(WiseConstants.COMMA);
		resultBuilder.append(DBStringUtils.convertListToCSVString(questions)).append(
				WiseConstants.NEWLINE);
		for (String invitee : invitees) {
			resultBuilder.append(invitee);
			Map<String, String> answers = this.getAnswersForInvitee(surveyId,
					Integer.parseInt(invitee), questionLevel);
			for (String question : questions) {
				resultBuilder.append(WiseConstants.COMMA);
				resultBuilder.append(answers.get(question));

			}
			resultBuilder.append(WiseConstants.NEWLINE);
		}
		return resultBuilder.toString();
	}

	private List<DataRepeatSetInstanceToQuestionId> getRepeatSetToQuestionIdMapping(
			long repeatInstanceId) {

		List<DataRepeatSetInstanceToQuestionId> result = new ArrayList<>();
		String sql = "SELECT * FROM " + this.wiseTables.getDataRepeatInstanceToQuestionId()
				+ " WHERE rpt_ins_id=?";

		try {

			Connection connection = this.databank.getDBConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, repeatInstanceId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				result.add(new DataRepeatSetInstanceToQuestionId(rs.getLong(1), rs.getLong(2), rs
						.getLong(3), rs.getString(4)));
			}
		} catch (SQLException e) {
			LOGGER.error("Could not get instances for repeat set", e);
		}

		return result;
	}

	public String getRepeatSetData(String surveyId, String repeatSetName) {
		StringBuilder sb = new StringBuilder();
		List<String> distinctRepeatSets = this.getDistinctRepeatSetNames(surveyId);

		for (String repeatSet : distinctRepeatSets) {
			sb.append(repeatSet).append(WiseConstants.NEWLINE);
			List<DataRepeatSetInstance> instances = this.getAllInstancesForRepeatSet(surveyId,
					repeatSet);

			Set<String> questions = this.getQuestionsInRepeatingSet(instances);

			for (String question : questions) {
				sb.append(question).append(WiseConstants.COMMA);
			}

			sb.append(WiseConstants.NEWLINE);

			// for each instance get all the questions and answers
			for (DataRepeatSetInstance instance : instances) {
				sb.append(instance.getInviteeId()).append(WiseConstants.COMMA);
				sb.append(instance.getInstancePseudoId()).append(WiseConstants.COMMA);
				// get all questions and answers
				List<DataRepeatSetInstanceToQuestionId> instanceToQuestionId = this
						.getRepeatSetToQuestionIdMapping(instance.getId());

				Map<String, Object> questionAnswer = new HashMap<>();

				for (DataRepeatSetInstanceToQuestionId mapping : instanceToQuestionId) {
					switch (mapping.getQuestionType()) {
					case "A":
						DataText dataText = (DataText) this.getDataByRowId(mapping.getQuestionId(),
								DataType.TEXT);
						questionAnswer.put(dataText.getQuestionId(), dataText.getAnswer());
						break;
					case "N":
						DataInteger dataInteger = (DataInteger) this.getDataByRowId(
								mapping.getQuestionId(), DataType.INTEGER);
						questionAnswer.put(dataInteger.getQuestionId(), dataInteger.getAnswer());
						break;
					}
				}

				for (String questionId : questions) {
					Object answer = questionAnswer.get(questionId);
					if (answer == null) {
						sb.append(WiseConstants.NULL).append(WiseConstants.COMMA);
					} else {
						sb.append(answer.toString()).append(WiseConstants.COMMA);
					}
				}
				sb.append(WiseConstants.NEWLINE);
			}

		}

		return sb.toString();
	}

	private Set<String> getQuestionsInRepeatingSet(List<DataRepeatSetInstance> instances) {
		Set<String> questions = new HashSet<>();
		for (DataRepeatSetInstance instance : instances) {
			// get all questions
			List<DataRepeatSetInstanceToQuestionId> instanceToQuestionId = this
					.getRepeatSetToQuestionIdMapping(instance.getId());

			for (DataRepeatSetInstanceToQuestionId mapping : instanceToQuestionId) {
				switch (mapping.getQuestionType()) {
				case "A":
					DataText dataText = (DataText) this.getDataByRowId(mapping.getQuestionId(),
							DataType.TEXT);
					questions.add(dataText.getQuestionId());
					break;
				case "N":
					DataInteger dataInteger = (DataInteger) this.getDataByRowId(
							mapping.getQuestionId(), DataType.INTEGER);
					questions.add(dataInteger.getQuestionId());
					break;
				}
			}
		}
		return questions;
	}

	private MainData getDataByRowId(long rowId, DataType dataType) {
		String sqlForText = "select * from " + this.wiseTables.getMainDataText() + " where id=?";
		String sqlForInteger = "select * from " + this.wiseTables.getMainDataInteger()
				+ " where id=?";
		switch (dataType) {
		case INTEGER:
			return this.getMainData(rowId, sqlForInteger);
		case TEXT:
			return this.getMainData(rowId, sqlForText);
		default:
			throw new IllegalStateException("Data type is not recognized");
		}

	}

	private MainData getMainData(long rowId, String sql) {
		DataText result = null;
		try {

			Connection connection = this.databank.getDBConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setLong(1, rowId);
			ResultSet rs = stmt.executeQuery();

			rs.next();

			result = new DataText(rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4),
					rs.getInt(6), rs.getString(5));

		} catch (SQLException e) {
			LOGGER.error("Could not get data by row id", e);
		}
		return result;
	}

	private List<DataRepeatSetInstance> getAllInstancesForRepeatSet(String surveyId,
			String repeatSetName) {
		List<DataRepeatSetInstance> instancesForRepeatSet = new ArrayList<>();
		String sql = "SELECT * FROM " + this.wiseTables.getDataRepeatSetToInstance()
				+ " WHERE survey=? AND repeat_set_name=?";

		try {

			Connection connection = this.databank.getDBConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);

			stmt.setString(1, surveyId);
			stmt.setString(2, repeatSetName);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				instancesForRepeatSet.add(new DataRepeatSetInstance(rs.getLong(1), rs.getString(2),
						rs.getString(3), rs.getLong(4), rs.getString(5)));
			}

		} catch (SQLException e) {
			LOGGER.error("Could not get instances for repeat set", e);
		}

		return instancesForRepeatSet;
	}

	private List<String> getDistinctRepeatSetNames(String surveyId) {
		String sql = "select distinct(repeat_set_name) from "
				+ this.wiseTables.getDataRepeatSetToInstance() + " where survey=?";
		List<String> result = new ArrayList<>();
		try {
			Connection connection = this.databank.getDBConnection();
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, surveyId);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				result.add(rs.getString(1));
			}
		} catch (SQLException e) {
			LOGGER.error("Exception while getting repeat set data: surveyId '" + surveyId + "'", e);
		}
		if (result.size() == 1) {
			LOGGER.debug("No result sets found in survey '" + surveyId + "'");
		}
		return result;

	}

	public Map<String, String> getAnswersForInvitee(String surveyId, int userId, int questionLevel) {
		String sqlForText = "SELECT questionId,answer FROM (SELECT * FROM "
				+ this.wiseTables.getMainDataText() + " WHERE level=" + questionLevel
				+ " AND inviteeId=" + userId + " AND survey='" + surveyId
				+ "' ORDER BY id DESC) AS x GROUP BY questionId";
		String sqlForInteger = "SELECT questionId,answer FROM (SELECT * FROM "
				+ this.wiseTables.getMainDataInteger() + " WHERE level=" + questionLevel
				+ " AND inviteeId=" + userId + " AND survey='" + surveyId
				+ "' ORDER BY id DESC) AS x GROUP BY questionId";

		LOGGER.debug("SQL:" + sqlForText);
		LOGGER.debug("SQL:" + sqlForInteger);

		Map<String, String> answerMap = new HashMap<>();
		try {
			Connection connection = this.databank.getDBConnection();
			PreparedStatement stmtForText = connection.prepareStatement(sqlForText);

			ResultSet rs = stmtForText.executeQuery();

			while (rs.next()) {
				String questionId = rs.getString(1);
				String answerId = rs.getString(2);
				answerMap.put(questionId, answerId);
			}

			PreparedStatement stmtForInteger = connection.prepareStatement(sqlForInteger);

			rs = stmtForInteger.executeQuery();

			while (rs.next()) {
				String questionId = rs.getString(1);
				String answerId = rs.getString(2);
				answerMap.put(questionId, answerId);
			}

		} catch (SQLException e) {
			LOGGER.error("Exception while getting invitee data: id '" + userId + "'", e);
		}
		LOGGER.debug(answerMap);
		return answerMap;

	}

	public List<String> getDistinctInviteeIdsFromDataTable(String surveyId, int level) {
		return this.getListFromDataTable("inviteeId", surveyId, level);
	}

	public List<String> getDistinctQuestionIds(String surveyId, int level) {
		return this.getListFromDataTable("questionId", surveyId, level);
	}

	private List<String> getListFromDataTable(String columnName, String surveyId, int level) {
		String sqlForText = "select distinct(" + columnName
				+ ") from wisedev.data_text where survey=? AND level=?";
		String sqlForInteger = "select distinct(" + columnName
				+ ") from wisedev.data_integer where survey=? AND level=?";
		List<String> distinctQuestionIds = new ArrayList<>();

		try {
			Connection connection = this.databank.getDBConnection();

			PreparedStatement stmt = connection.prepareStatement(sqlForText);
			stmt.setString(1, surveyId);
			stmt.setInt(2, level);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				distinctQuestionIds.add(rs.getString(1));
			}

			PreparedStatement stmtForInteger = connection.prepareStatement(sqlForInteger);
			stmtForInteger.setString(1, surveyId);
			stmtForInteger.setInt(2, level);

			ResultSet rsForInteger = stmtForInteger.executeQuery();

			while (rsForInteger.next()) {
				distinctQuestionIds.add(rsForInteger.getString(1));
			}
		} catch (SQLException e) {
			LOGGER.error("Error while fetching distinctQuestionIds for survey '" + surveyId
					+ "' for level " + level, e);
		}
		if (distinctQuestionIds.size() == 0) {
			LOGGER.info("No question ids found for '" + surveyId + "' at level '" + level + "'");
		}
		return distinctQuestionIds;

	}

	public String getAnswersInRepeatingSetForInvitee(String surveyId, int userId,
			String repeatingSetName) {

		Map<String, Object> mapOfParameters = new HashMap<>();
		mapOfParameters.put("repeat_set_instance", this.wiseTables.getDataRepeatSetToInstance());
		mapOfParameters.put("rpt_set_ins_to_ques_id",
				this.wiseTables.getDataRepeatInstanceToQuestionId());
		mapOfParameters.put("data_table", this.wiseTables.getMainDataText());
		java.util.List<RepeatingItemInstance> repeatingItemInstances = new ArrayList<>();

		try {
			String sqlToGetData = this.sqlTemplateUtil.getSQLFromTemplate(mapOfParameters,
					"QueryRepeatingSet.ftl");

			LOGGER.debug("SQL:" + sqlToGetData);

			PreparedStatement stmtToGetData = this.databank.getDBConnection().prepareStatement(
					sqlToGetData);
			stmtToGetData.setString(1, surveyId);
			stmtToGetData.setInt(2, userId);
			stmtToGetData.setString(3, "repeat_set_" + repeatingSetName);
			ResultSet rs = stmtToGetData.executeQuery();
			RepeatingItemInstance currentInstance = null;
			while (rs.next()) {
				String instancePseudoId = rs.getString(1);
				String questionId = rs.getString(2);
				String answer = rs.getString(3);

				if (currentInstance == null) {
					currentInstance = new RepeatingItemInstance(repeatingSetName, instancePseudoId);
					currentInstance.addAnswer(questionId, new Answer(answer, Answer.Type.TEXT));
				} else if (currentInstance.getInstanceName().equals(instancePseudoId)) {
					currentInstance.addAnswer(questionId, new Answer(answer, Answer.Type.TEXT));
				} else {
					repeatingItemInstances.add(currentInstance);
					currentInstance = new RepeatingItemInstance(repeatingSetName, instancePseudoId);
					currentInstance.addAnswer(questionId, new Answer(answer, Answer.Type.TEXT));
				}
			}
			if (currentInstance != null) {
				repeatingItemInstances.add(currentInstance);
			}
		} catch (SQLException e) {
			LOGGER.error("Could not get data for repeating set", e);
		} catch (IOException e) {
			LOGGER.error("Could not get data for repeating set", e);
		} catch (TemplateException e) {
			LOGGER.error("Could not get data for repeating set", e);
		}
		String response = new Gson().toJson(repeatingItemInstances);
		LOGGER.debug("Repeating Set Name='" + repeatingSetName + "' Response:" + response);
		return response;

	}
}
