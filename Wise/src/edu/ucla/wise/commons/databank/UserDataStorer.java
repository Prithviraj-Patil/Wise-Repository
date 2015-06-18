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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

import edu.ucla.wise.persistence.data.Answer;
import edu.ucla.wise.persistence.data.GeneratedKeysForDataTables;
import edu.ucla.wise.persistence.data.WiseTables;

public class UserDataStorer {
    private static final Logger LOGGER = Logger.getLogger(UserDataStorer.class);
    private final DataBankInterface dataBankInterface;
    private final WiseTables wiseTables;

    public UserDataStorer(DataBankInterface dataBankInterface) {
        this.dataBankInterface = dataBankInterface;
        this.wiseTables = dataBankInterface.getWiseTables();
    }

    public void insertRepeatSetInstance(String surveyId, String userId, String repeatSetName, String instanceName,
            Map<String, Answer> answers) {
        String sqlForRepeatSetIdToInstance = "INSERT INTO " + this.wiseTables.getDataRepeatSetToInstance()
                + "(repeat_set_name,instance_pseudo_id,inviteeId, survey) VALUES (?,?,?,?)";
        String sqlForRepeatSetInstanceToQuestionId = "INSERT INTO "
                + this.wiseTables.getDataRepeatInstanceToQuestionId() + "(rpt_ins_id, ques_id, type) VALUES (?,?,?)";

        try {
            Connection connection = this.dataBankInterface.getDBConnection();

            PreparedStatement stmtRptIdToInstance = connection.prepareStatement(sqlForRepeatSetIdToInstance,
                    Statement.RETURN_GENERATED_KEYS);

            stmtRptIdToInstance.setString(1, repeatSetName);
            stmtRptIdToInstance.setString(2, instanceName);
            stmtRptIdToInstance.setString(3, userId);
            stmtRptIdToInstance.setString(4, surveyId);

            stmtRptIdToInstance.execute();

            ResultSet keySet = stmtRptIdToInstance.getGeneratedKeys();

            int repeatInstanceId = -1;
            while (keySet.next()) {
                repeatInstanceId = keySet.getInt(1);
            }

            if (repeatInstanceId == -1) {
                throw new IllegalStateException("Insert id was not retrieved for the update statement");
            }

            GeneratedKeysForDataTables generatedKeys = this.saveData(surveyId, userId, answers, 1);

            PreparedStatement stmtForRptInsToQuesId = connection.prepareStatement(sqlForRepeatSetInstanceToQuestionId);

            for (int foreignKey : generatedKeys.getTextTableKeys()) {
                stmtForRptInsToQuesId.setInt(1, repeatInstanceId);
                stmtForRptInsToQuesId.setInt(2, foreignKey);
                stmtForRptInsToQuesId.setString(3, "A");
                stmtForRptInsToQuesId.execute();
            }

            for (int foreignKey : generatedKeys.getIntegerTableKeys()) {
                stmtForRptInsToQuesId.setInt(1, repeatInstanceId);
                stmtForRptInsToQuesId.setInt(2, foreignKey);
                stmtForRptInsToQuesId.setString(3, "N");
            }

        } catch (SQLException e) {
            LOGGER.error("Could not save user answers for invitee " + userId);
            LOGGER.error(answers, e);
        }
    }

    /**
     * 
     * @param questionAnswer
     * @param questionLevel
     * @return Set of generated keys.
     */
    public GeneratedKeysForDataTables saveData(String surveyId, String userId, Map<String, Answer> questionAnswer,
            int questionLevel) {
        String sqlForText = "INSERT INTO " + this.wiseTables.getMainDataText()
                + " (`survey`, `inviteeId`, `questionId`, `answer`, `level`) VALUES (?,?,?,?,?)";
        String sqlForInteger = "INSERT INTO " + this.wiseTables.getMainDataInteger()
                + " (`survey`, `inviteeId`, `questionId`, `answer`, `level`) VALUES (?,?,?,?,?)";

        if (questionAnswer.isEmpty()) {
            LOGGER.info("An empty Map provided as input. INVESTIGATE");
        }

        LOGGER.debug("SQL:" + sqlForText);
        LOGGER.debug("SQL:" + sqlForInteger);

        Set<Integer> generatedKeysForIntegerTable = new HashSet<>();
        Set<Integer> generatedKeysForTextTable = new HashSet<>();
        try {
            Connection connection = this.dataBankInterface.getDBConnection();

            PreparedStatement stmtForText = connection.prepareStatement(sqlForText, Statement.RETURN_GENERATED_KEYS);
            PreparedStatement stmtForInteger = connection.prepareStatement(sqlForInteger,
                    Statement.RETURN_GENERATED_KEYS);

            for (Entry<String, Answer> entry : questionAnswer.entrySet()) {
                Answer answer = entry.getValue();
                if (answer.getType() == Answer.Type.TEXT) {
                    stmtForText.setString(1, surveyId);
                    stmtForText.setString(2, userId);
                    stmtForText.setString(3, entry.getKey());
                    stmtForText.setString(4, answer.toString());
                    stmtForText.setInt(5, questionLevel);
                    stmtForText.execute();
                    ResultSet keySet = stmtForText.getGeneratedKeys();

                    while (keySet.next()) {
                        generatedKeysForTextTable.add(keySet.getInt(1));
                    }

                } else {
                    stmtForInteger.setString(1, surveyId);
                    stmtForInteger.setString(2, userId);
                    stmtForInteger.setString(3, entry.getKey());
                    stmtForInteger.setInt(4, (int) answer.getAnswer());
                    stmtForInteger.setInt(5, questionLevel);
                    stmtForInteger.execute();
                    ResultSet keySet = stmtForInteger.getGeneratedKeys();

                    while (keySet.next()) {
                        generatedKeysForIntegerTable.add(keySet.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.error("Could not save user answers for invitee " + userId);
            LOGGER.error(questionAnswer, e);
        }

        GeneratedKeysForDataTables generatedKeys = new GeneratedKeysForDataTables();
        generatedKeys.setIntegerTableKeys(generatedKeysForIntegerTable);
        generatedKeys.setTextTableKeys(generatedKeysForTextTable);

        return generatedKeys;
    }

    public void storeMainData(String surveyId, String userId, String[] names, char[] valTypes, String[] vals) {

        int questionLevel = 0;

        if ((names.length != valTypes.length) || (names.length != vals.length)) {
            throw new IllegalArgumentException("Code to store main data failed");
        }

        Map<String, Answer> answers = new HashMap<>();
        for (int i = 0; i < names.length; i++) {

            if (Strings.isNullOrEmpty(names[i]) || Strings.isNullOrEmpty(vals[i])) {
                LOGGER.error("Trying to save null values, INVESTIGATE");
                continue;
            }

            if (valTypes[i] == 'a') {
                answers.put(names[i], new Answer(vals[i], Answer.Type.TEXT));
            } else {
                answers.put(names[i], new Answer(Integer.parseInt(vals[i]), Answer.Type.INTEGER));
            }
        }

        // store data
        this.saveData(surveyId, userId, answers, questionLevel);

    }

    /**
     * Deletes repeating item sets.
     * 
     * @param inviteeId
     *            Invitee whose repeating item has to be deleted.
     * @param tableName
     *            Repeating item table name from which the item has to be
     *            deleted.
     * @param instanceName
     *            Row's instance name which has to be deleted.
     * @return boolean If the delete was successful or not.
     */
    public boolean deleteRowFromTable(String userId, String itemSetName, String instanceName, String survey) {

        String sqlStatement = "DELETE FROM " + this.wiseTables.getDataRepeatSetToInstance() + " WHERE inviteeId=?"
                + " AND instance_pseudo_id=? AND repeat_set_name=? AND survey=?";
        PreparedStatement statement = null;

        try {
            statement = this.dataBankInterface.getDBConnection().prepareStatement(sqlStatement);
            statement.setInt(1, Integer.parseInt(userId));
            statement.setString(2, instanceName);
            statement.setString(3, itemSetName);
            statement.setString(4, survey);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error for SQL statement: " + sqlStatement, e);
            return false;
        } catch (NumberFormatException e) {
            LOGGER.error("deleteRowFromTable Invalid User ID" + e.toString(), e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.error("deleteRowFromTable:" + e.toString(), null);
                }
            }
        }
        return true;
    }
}
