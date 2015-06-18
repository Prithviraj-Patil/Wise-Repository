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
package edu.ucla.wise.persistence.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import edu.ucla.wise.shared.persistence.GenericDAO;
import edu.ucla.wise.shared.persistence.HibernateUtil;

@Entity
@Table(schema = TextAnswer.SCHEMA_NAME, name = TextAnswer.TABLE_NAME)
public class TextAnswer {

    public static final String SCHEMA_NAME = "wisedev";
    public static final String TABLE_NAME = "main_data_text";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private int id;

    @Column(name = "surveyId", nullable = false)
    private int survey;

    @Column(name = "inviteeId", nullable = false)
    private int invitee;

    @Column(name = "questionId", nullable = false)
    private String question;

    @Column(name = "answer", nullable = false)
    private String answer;

    public TextAnswer(int surveyId, int inviteeId, String questionId, String answer) {
        this.survey = surveyId;
        this.invitee = inviteeId;
        this.question = questionId;
        this.answer = answer;
    }

    protected TextAnswer() {
        // for hibernate
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSurvey() {
        return this.survey;
    }

    public void setSurvey(int survey) {
        this.survey = survey;
    }

    public int getInvitee() {
        return this.invitee;
    }

    public void setInvitee(int invitee) {
        this.invitee = invitee;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public static TextAnswer getEmptyExample() {
        return new TextAnswer();
    }

    public static class TextAnswerDAO extends GenericDAO<TextAnswer, Integer> {

        public TextAnswerDAO(HibernateUtil hibernateUtil) {
            super(hibernateUtil);
        }
    }

}
