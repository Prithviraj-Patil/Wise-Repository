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
package edu.ucla.wise.persistence.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import edu.ucla.wise.persistence.data.Answer;
import edu.ucla.wise.persistence.data.RepeatingItemInstance;

public class InviteeDataService {

    /**
     * Return the latest data submitted by the invitee
     * 
     * @param inviteeId
     * @return
     */
    public Map<String, String> getTextData(int inviteeId, int surveyId) {

        String sql = "SELECT questionId,answer FROM (SELECT * FROM main_data_text WHERE " + "inviteeId=" + inviteeId
                + " AND surveyId=" + surveyId + " ORDER BY id DESC) AS x GROUP BY questionId";

        Map<String, String> answerMap = new HashMap<>();
        try {
            Connection connection = this.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String questionId = rs.getString(1);
                String answerId = rs.getString(2);
                answerMap.put(questionId, answerId);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return answerMap;
    }

    public Map<String, Integer> getIntegerData(int inviteeId, int surveyId) {
        String sql = "SELECT questionId,answer FROM (SELECT * FROM main_data_integer WHERE " + "inviteeId=" + inviteeId
                + " AND surveyId=" + surveyId + " ORDER BY id DESC) AS x GROUP BY questionId";

        Map<String, Integer> answerMap = new HashMap<>();
        try {
            Connection connection = this.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String questionId = rs.getString(1);
                int answerId = Integer.parseInt(rs.getString(2));
                answerMap.put(questionId, answerId);
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return answerMap;
    }

    public void saveDataText(Map<String, String> answers) {

        String sql = "INSERT INTO `main_data_text` (`surveyId`, `inviteeId`, `questionId`, `answer`) VALUES (?, ?, ?, ?);";

        try {
            Connection connection = this.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setString(1, "1");
            stmt.setString(2, "1");
            stmt.setString(3, "questionFromCode");
            stmt.setString(4, "answerFromCode");

            stmt.execute();

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void saveDataInteger(Map<String, Integer> answers) {

        String sql = "INSERT INTO `main_data_integer` (`surveyId`, `inviteeId`, `questionId`, `answer`) VALUES (?, ?, ?, ?);";

        try {
            Connection connection = this.getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setString(1, "1");
            stmt.setString(2, "1");
            stmt.setString(3, "questionFromCode");
            stmt.setInt(4, 1);

            stmt.execute();

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public String getAllDataForRepeatingSet(String repeatingSetName) {

        String sqlToGetData = "SELECT instance_pseudo_id,questionId,answer FROM data_repeat_set_instance,data_rpt_ins_id_to_ques_id,data_text "
                + " WHERE "
                + "data_repeat_set_instance.id = data_rpt_ins_id_to_ques_id.rpt_ins_id"
                + " AND "
                + "data_rpt_ins_id_to_ques_id.ques_id = data_text.id"
                + " AND "
                + "data_repeat_set_instance.repeat_set_name='"
                + repeatingSetName
                + "'"
                + " AND "
                + "data_repeat_set_instance.inviteeId=1" + " ORDER BY instance_pseudo_id";

        java.util.List<RepeatingItemInstance> repeatingItemInstances = new ArrayList<>();

        try {
            PreparedStatement stmtToGetData = this.getConnection().prepareStatement(sqlToGetData);
            ResultSet rs = stmtToGetData.executeQuery();

            RepeatingItemInstance currentInstance = null;
            while (rs.next()) {
                String instancePseudoId = rs.getString(1);
                String questionId = rs.getString(2);
                String answer = rs.getString(3);

                if (currentInstance == null) {
                    currentInstance = new RepeatingItemInstance(repeatingSetName, instancePseudoId);
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
            System.out.println(e);
        }

        return new Gson().toJson(repeatingItemInstances);
    }

    private Connection getConnection() throws SQLException {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver());
        return DriverManager.getConnection("jdbc:mysql://" + "localhost" + "/" + "wisedev" + "?user=" + "wisedev"
                + "&password=" + "password" + "&autoReconnect=true");
    }

    public static void main(String[] args) {

        InviteeDataService service = new InviteeDataService();

        // System.out.println(service.getTextData(1, 1));
        // System.out.println(service.getIntegerData(1, 1));
        // service.saveDataInteger(new HashMap<String, Integer>());

        // service.saveData(new HashMap<String, String>());

        System.out.println(service.getAllDataForRepeatingSet("EMP_NAME"));

    }
}
