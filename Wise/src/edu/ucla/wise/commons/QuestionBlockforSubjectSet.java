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
package edu.ucla.wise.commons;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucla.wise.commons.databank.DataBank;

/**
 * Represents a question block that evaluates a subject set
 */
public class QuestionBlockforSubjectSet extends QuestionBlock {
    public static final Logger LOGGER = Logger.getLogger(QuestionBlockforSubjectSet.class);

    /** Additional Instance Variables */
    private SubjectSet subjectSet;
    private String subjectSetID;

    /* Condition is a precondition to apply for the subject set */
    public Condition ssCond;

    public String[] subjectSetLabels;
    public String[] stemFieldNames;

    /**
     * Constructor: parse a question block node that has a subject set reference
     * note: subject_set strings copied into inherited "subjectSetLabels" array
     * field names copied into inherieted stemFieldNames array as
     * QuestionID_SubjectName (with replacemt of spaces and dashes)
     * 
     * 
     * @param n
     *            XML DOM node which has the details to build this object.
     */
    public QuestionBlockforSubjectSet(Node n) {

        /* assign the parent attributes of the page item */
        super(n);
        try {
            NodeList nodelist = n.getChildNodes();

            /*
             * parse other nodes: response set, response set ref, subject set
             * ref, stem etc.
             */
            for (int i = 0; i < nodelist.getLength(); i++) {

                /* search out the subject set reference */
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Subject_Set_Ref")) {
                    this.subjectSetID = nodelist.item(i).getAttributes().getNamedItem("Subject_Set").getNodeValue();

                    /*
                     * this reference to be resolved in second pass, after full
                     * parse. check for a precondition defined in the subject
                     * set reference node
                     */
                    NodeList nodeL = nodelist.item(i).getChildNodes();
                    for (int t = 0; t < nodeL.getLength(); t++) {
                        Node NodeC = nodeL.item(t);
                        if (NodeC.getNodeName().equalsIgnoreCase("Precondition")) {
                            /* parse out the Subject Set condition object */
                            this.ssCond = new Condition(NodeC);
                        }
                    }
                }
            }
        } catch (DOMException e) {
            LOGGER.error("WISE - QUESTION BLOCK for subjectset: " + e.toString(), null);
            return;
        } catch (NullPointerException e) {
            LOGGER.error("WISE - QUESTION BLOCK for subjectset: " + e.toString(), null);
            return;
        }
    }

    /**
     * Initializes the response set and html specific to this question and also
     * sets up stem field values.
     * 
     * @param mySurvey
     *            the survey to which this question is linked.
     */
    @Override
    public void knitRefs(Survey mySurvey) {
        super.knitRefs(mySurvey);
        if (this.subjectSetID != null) {
            this.subjectSet = mySurvey.getSubjectSet(this.subjectSetID);

            /* declare the stem name & value arrays */
            this.subjectSetLabels = this.subjectSet.subjectLabels;
            this.stemFieldNames = new String[this.subjectSet.subjectCount];

            /*
             * construct field names as: QuestionName+suffix, delegated to SS
             * [should be "_ID"]
             */
            for (int k = 0; k < this.subjectSet.subjectCount; k++) {
                this.stemFieldNames[k] = this.name + this.subjectSet.getfieldNamesuffix(k);
            }
        }
    }

    /**
     * get the table creation syntax (the survey data table) for a question
     * block
     */
    // public String create_table()
    // {
    // try
    // {
    // //connect to the database
    // Connection conn = page.survey.getDBConnection();
    // Statement stmt = conn.createStatement();
    // String sql="";
    //
    // //first check if this subject set table already exists
    // boolean found=false;
    // ResultSet rs=stmt.executeQuery("show tables");
    // while(rs.next())
    // {
    // if(rs.getString(1).equalsIgnoreCase(page.survey.id +
    // "_"+SubjectSet_name+"_data"))
    // {
    // found=true;
    // break;
    // }
    // }
    // if(!found)
    // {
    // //if the subject set table doesn't exist, then create the new table
    // sql = "CREATE TABLE "+ page.survey.id + "_"+SubjectSet_name+ "_data (";
    // sql += "invitee int(6) not null,";
    // sql += "subject int(6) not null,";
    // sql += " "+name+" int(6),";
    // sql += "INDEX (invitee),";
    // sql += "FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE";
    // sql += ") TYPE=INNODB ";
    // boolean dbtype = stmt.execute(sql);
    // }
    // else
    // {
    // //if the table already exists, then check if the column (question block
    // name) exists
    // sql="describe "+ page.survey.id + "_"+SubjectSet_name+ "_data ";
    // boolean result = stmt.execute(sql);
    // rs = stmt.getResultSet();
    // boolean column_exist=false;
    // while(rs.next())
    // {
    // if(name.equalsIgnoreCase(rs.getString(1)))
    // {
    // //find the column
    // column_exist=true;
    // break;
    // }
    // }
    // //if the column doesn't exist
    // //then alter the current table to append a new column representing this
    // question_block
    // if(!column_exist)
    // {
    // sql = "ALTER TABLE "+ page.survey.id + "_"+SubjectSet_name+ "_data ";
    // sql += "ADD "+name+" int(6)";
    // boolean dbtype = stmt.execute(sql);
    // }
    // }
    // stmt.close();
    // conn.close();
    // }
    // catch (Exception e)
    // {
    // Study_Util.email_alert("WISE - QUESTION BLOCK CREATE SUBJECT SET TABLE: "+e.toString());
    // }
    // return "";
    // }

    /**
     * Reads paramaeters passed from data source that apply to field names;
     * delegate value processing to each PageItem that the page contains.
     * 
     * @param params
     *            The Http parameters to this page.
     * @return Hashtable Contains the answers of the survey so far for this
     *         page.
     */
    @Override
    public Hashtable<String, String> readForm(Hashtable<String, String> params) {
        Hashtable<String, String> answers = super.readForm(params);
        answers.put("__SubjectSet_ID__", this.subjectSetID);
        return answers;
    }

    /**
     * Renders FORM for a question block
     * 
     * @param theUser
     *            The user for whom the question has to be rendered.
     * @return String HTML format of the question.
     */
    public String renderForm(User theUser) {
        String s = "";

        /* check the question block's own precondition */
        if (this.cond != null) {
            /*
             * check the precondition for the whole question block if not
             * satisfied, then skip writing the entire question block
             */
            boolean writeQb = this.cond.checkCondition(theUser);

            /* return an empty string */
            if (!writeQb) {
                return s;
            }
        }

        /* check the precondition vector for the subject set reference */
        boolean[] ssCondVector = new boolean[this.stemFieldNames.length];

        /* if the subject set reference has the precondition defined */
        if (this.ssCond != null) {
            boolean anyTrue = false;

            /*
             * check if the precondition is met by calculating the comparison
             * result
             */
            ssCondVector = this.ssCond.checkCondition(this.subjectSetID, this.stemFieldNames, theUser);

            /* render each stem of the question block */
            for (int i = 0; i < this.subjectSetLabels.length; i++) {

                /*
                 * if the current stem meets the precondition, then it will be
                 * displayed
                 */
                if (ssCondVector[i]) {
                    anyTrue = true;
                    break;
                }
            }

            /*
             * if no stem meets the precondition, skip displaying the entire
             * subject set
             */
            if (!anyTrue) {
                return s;
            }
        }
        s = this.renderQBheader();

        /*
         * if there is a precondition defined for subject set, test vector for
         * each stem
         */
        if (this.ssCond != null) {
            for (int i = 0; i < this.subjectSetLabels.length; i++) {

                /* if the current stem meets the precondition, then display it */
                if (ssCondVector[i]) {
                    s += this.renderSubjectSetLabels(i);
                }
            }
        } else {

            /* otherwise, display subjectSetLabels without testing */
            for (int i = 0; i < this.subjectSetLabels.length; i++) {
                s += this.renderSubjectSetLabels(i);
            }
        }
        s += "</table>";
        return s;
    }

    /**
     * Renders the subject subjectSetLabels which meet the precondition defined
     * in subject set reference
     * 
     * @param i
     *            The index of the subject label that has to be returned.
     * @return String The HTML format of the subject set label.
     */
    public String renderSubjectSetLabels(int i) {
        String s = "";

        /* render each stem of the question block */
        int startV = Integer.parseInt(this.responseSet.startvalue);
        int len = this.responseSet.getSize();
        int levels = Integer.parseInt(this.responseSet.levels);
        int num = startV;

        if ((i % 2) == 0) {
            s += "<tr class=\"shaded-bg\">";
        } else {
            s += "<tr class=\"unshaded-bg\">";
        }
        s += "<td>" + StudySpace.font + this.subjectSetLabels[i] + "</font></td>";

        if (levels == 0) {
            for (int j = startV, k = 0; j < (len + startV); j++, k++) {
                if (this.responseSet.values.get(k).equalsIgnoreCase("-1")) {
                    s += "<td><center>" + StudySpace.font;
                    s += "<input type='radio' name='" + this.stemFieldNames[i].toUpperCase() + "' value='" + num + "'>";
                    s += "</center></font></td>";
                    num = num + 1;
                } else {
                    s += "<td><center>" + StudySpace.font;
                    s += "<input type='radio' name='" + this.stemFieldNames[i].toUpperCase() + "' value='"
                            + this.responseSet.values.get(k) + "'>";
                    s += "</center></font></td>";
                    num = num + 1;
                }
            }
        } else {
            for (int j = 1; j <= levels; j++) {
                s += "<td><center>" + StudySpace.font;
                s += "<input type='radio' name='" + this.stemFieldNames[i].toUpperCase() + "' value='" + num + "'>";
                s += "</center></font></td>";
                num = num + 1;
            }
        }
        return s;
    }

    /**
     * Renders RESULTS for a subject set question block.
     * 
     * @param pg
     *            Page Object for which the results are to be rendered.
     * @param db
     *            Data Bank object to connect to the database.
     * @param whereclause
     *            whereclause to restrict the invitee selection.
     * @param data
     *            Hashtable which contains results.
     * @return String HTML format of the results is returned.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String renderResults(Page pg, DataBank db, String whereclause, Hashtable data) {

        return db.renderQuestionBlockResults(pg, this, db, whereclause, data);

    }

    /** returns a comma delimited list of all the fields on a page */
    /*
     * public String list_fields() { String s = ""; for (int i = 0; i <
     * subjectSetLabels.length; i++) s += stemFieldNames[i]+","; return s; }
     */

    /**
     * Prints out the question block information
     * 
     * @return String Information about question block.
     */
    @Override
    public String toString() {
        String s = "QUESTION BLOCK for subject set<br>";
        s += super.toString();

        s += "Instructions: " + this.instructions + "<br>";
        s += "Response Set: " + this.responseSet.id + "<br>";
        s += "subjectSetLabels:<br>";

        for (int i = 0; i < this.subjectSetLabels.length; i++) {
            s += this.stemFieldNames[i] + ":" + this.subjectSetLabels[i] + "<br>";
        }
        if (this.cond != null) {
            s += this.cond.toString();
        }
        s += "<p>";
        return s;
    }
}
