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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucla.wise.commons.databank.DataBank;

/**
 * This class is a subclass of PageItem and represents a question block on the
 * page.
 */
public class QuestionBlock extends PageItem {
    public static final Logger LOGGER = Logger.getLogger(QuestionBlock.class);
    public static String sqlDatatype = "int(6)";

    /** Instance Variables */
    public String instructions = "NONE";
    public ResponseSet responseSet;
    public String responseSetID;
    // public Subject_Set subject_set;
    private String subjectSetName;

    public ArrayList<StemDifferentiator> stems = new ArrayList<StemDifferentiator>();
    public ArrayList<String> stemFieldNames = new ArrayList<String>();
    // P public String[] stems;
    // P public String[] stem_fieldNames;

    /*
     * hasPrecondition is a flag to check the precondition attribute of a
     * subject set reference.
     */
    public boolean hasPrecondition = false;
    public boolean hasSubjectSetRef = false;
    public Condition cond;

    /**
     * Constructor: Parse a question block node from the XML DOM node
     * 
     * @param n
     *            DOM node for this question.
     */
    public QuestionBlock(Node n) {

        /* assign the attributes of the page item */
        super(n);
        try {
            NodeList nodelist = n.getChildNodes();
            // P int num_stems = 0;

            /*
             * P // parse subject stem // count the total number of the subject
             * stems for (int i = 0; i < nodelist.getLength(); i++) { if
             * (nodelist.item(i).getNodeName().equalsIgnoreCase("Sub_Stem"))
             * num_stems++; } // declare the string array for stem name & value
             * based on the // subject stem size stems = new String[num_stems];
             * stem_fieldNames = new String[num_stems];
             */

            /* get the sub stem name & value and assign them to the two arrays */
            for (int i = 0, j = 0; i < nodelist.getLength(); i++) {
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Sub_Stem")
                        || nodelist.item(i).getNodeName().equalsIgnoreCase("Sub_Head")) {
                    Node node = nodelist.item(i);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    StringWriter sw = new StringWriter();
                    transformer.transform(new DOMSource(node), new StreamResult(sw));
                    String stemType = nodelist.item(i).getNodeName().toUpperCase();
                    // P stems[j] = sw.toString();
                    this.stems.add(this.new StemDifferentiator(stemType, sw.toString()));

                    /* each stem name is the question name plus the index number */
                    // P stem_fieldNames[j] = name + "_" + (j + 1);
                    this.stemFieldNames.add(this.name + "_" + (j + 1));
                    j++;
                }
            }

            /*
             * parse other nodes: response set, response set ref, subject set
             * ref, stem etc.
             */
            for (int i = 0; i < nodelist.getLength(); i++) {

                /* parse the response set */
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Response_Set")) {
                    this.responseSetID = nodelist.item(i).getAttributes().getNamedItem("ID").getNodeValue();
                }

                /* parse the response set reference */
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Response_Set_Ref")) {
                    this.responseSetID = nodelist.item(i).getAttributes().getNamedItem("Response_Set").getNodeValue();
                }

                /* parse the stem */
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Stem")) {
                    Node node = nodelist.item(i);
                    Transformer transformer = TransformerFactory.newInstance().newTransformer();
                    StringWriter sw = new StringWriter();
                    transformer.transform(new DOMSource(node), new StreamResult(sw));
                    this.instructions = sw.toString();
                }

                /*
                 * parse the precondition set for the question block note: this
                 * precondition is not the precondition set for child node -
                 * subject set reference
                 */
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Precondition")) {

                    /* create the condition object */
                    this.cond = new Condition(nodelist.item(i));
                }
            }
        } catch (TransformerConfigurationException e) {
            LOGGER.error("WISE - QUESTION BLOCK: " + e.toString(), null);
            return;
        } catch (DOMException e) {
            LOGGER.error("WISE - QUESTION BLOCK: " + e.toString(), null);
            return;
        } catch (TransformerException e) {
            LOGGER.error("WISE - QUESTION BLOCK: " + e.toString(), null);
            return;
        }
    }

    /**
     * Initializes the response set and html specific to this question.
     * 
     * @param mySurvey
     *            the survey to which this question is linked.
     */
    @Override
    public void knitRefs(Survey mySurvey) {
        this.responseSet = mySurvey.getResponseSet(this.responseSetID);
        this.html = this.makeHtml();
    }

    /**
     * Counts number of fields/options in the question block
     * 
     * @return int number of the fields(stems) pertaining to this question.
     */
    @Override
    public int countFields() {

        /* the number of fields is the total number of subject stems */
        return this.stems.size();
    }

    /**
     * Returns all the stem names related to this question, each stem name is
     * name of the question + index.
     * 
     * @return Array Array of strings which contains the names of all the stems
     *         related to this question
     * 
     */
    @Override
    public String[] listFieldNames() {

        // P return stem_fieldNames;
        return this.stemFieldNames.toArray(new String[this.stemFieldNames.size()]);
    }

    /**
     * get the table creation syntax (the series subject set table) for subject
     * set references
     */
    // public void create_subjectset_table()
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
    // return;
    // }

    /**
     * Renders the {@link QuestionBlock} HTML {@link PageItem}. Renders a static
     * html at the time of loading the survey.
     * 
     * @return String HTML format of the question Block.
     */
    public String makeHtml() {
        String s = "";
        int len = this.responseSet.getSize();
        int startV = Integer.parseInt(this.responseSet.startvalue);
        int num = startV;
        // String t1, t2;
        int levels = Integer.parseInt(this.responseSet.levels);

        /* Print the instruction above the table top */
        s += "<p rowspan=2'>";
        if (!this.instructions.equalsIgnoreCase("NONE")) {
            s += "<br />" + this.instructions;
        } else {
            s += "&nbsp;";
        }
        s += "</p>";

        /* cells for question block that doesnt require classified levels */
        String noClassifiedLevelColumns = "";
        for (int j = startV, k = 0; j < (len + startV); j++, k++) {
            noClassifiedLevelColumns += "<td class=\"header-row\"><center>" + this.responseSet.responses.get(k)
                    + "</center></td>";
        }

        /* cells for question block that requires classified levels */
        String classifiedLevelColumns = "";
        classifiedLevelColumns += "<td class=\"header-row\" colspan=" + levels + " width='60%'>";
        classifiedLevelColumns += "<table cellpadding='3' border='0' width='100%' cellspacing='0'>";
        classifiedLevelColumns += "<tr class='shaded-bg'>";// make one row
        // 'stead of many
        if ((this.responseSet.responses.size() == 2) && (levels > 2)) {
            classifiedLevelColumns += "<td class='header-row' align='left'>";
            classifiedLevelColumns += startV + ". " + this.responseSet.responses.get(0);
            classifiedLevelColumns += "<td class=\"header-arrows\"align='center' width='10%'>&larr;&rarr;</td>";
            classifiedLevelColumns += "<td class='header-row' align='right'>";
            classifiedLevelColumns += ((startV + levels) - 1) + ". " + this.responseSet.responses.get(1);

        } else {
            int step = Math.round((levels - 1) / (len - 1));
            for (int j = 1, k = 0, currentLevel = startV; j <= levels; j++, currentLevel++) {
                int det = (j - 1) % step;
                if (det == 0) {
                    // classified_level_columns += "<tr>";
                    if (j == 1) {
                        classifiedLevelColumns += "<td align='left'>";
                    } else if (j == levels) {
                        classifiedLevelColumns += "<td align='right'>";
                    } else {
                        classifiedLevelColumns += "<td align='center'>";
                    }
                    classifiedLevelColumns += currentLevel + ". " + this.responseSet.responses.get(k);
                    classifiedLevelColumns += "</td>";
                    // classified_level_columns += "</tr>";
                    k++;
                }
            }
        }
        classifiedLevelColumns += "</tr>";// moved row closing to here
        classifiedLevelColumns += "</table>";
        classifiedLevelColumns += "</td>";
        classifiedLevelColumns += "</tr>";
        classifiedLevelColumns += "<tr class=\"header-row shaded-bg\">";
        classifiedLevelColumns += "<td class=\"header-row\">";
        classifiedLevelColumns += "&nbsp;";
        classifiedLevelColumns += "</td>";
        for (int j = startV; j < (levels + startV); j++) {
            classifiedLevelColumns += "<td class=\"header-row\"><center>" + j + "</center></td>";
        }

        /*
         * to specify background of a row render row for each stem of the
         * question block
         */
        int rowBackgroundColorIndex = 0;
        for (int i = 0; i < this.stems.size(); i++) {
            boolean isSubHead = this.stems.get(i).stemType.equalsIgnoreCase("Sub_Head");
            boolean isSubStem = this.stems.get(i).stemType.equalsIgnoreCase("Sub_Stem");
            if (i == 0) {

                /* open the question block table */
                s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";

                /*
                 * render header row if the question block doesn't require
                 * classified level
                 */
                if (levels == 0) {
                    s += "<tr class=\"shaded-bg\">";
                    s += "<td class=\"header-row sub_head\">";
                    if (isSubHead) {
                        s += this.stems.get(i).stemValue;
                    } else {
                        s += "&nbsp;";
                    }
                    s += "</td>";
                    s += noClassifiedLevelColumns;
                    s += "</tr>";
                    rowBackgroundColorIndex++;
                    if (isSubStem) {
                        if ((rowBackgroundColorIndex++ % 2) == 0) {
                            s += "<tr class=\"shaded-bg\">";
                        } else {
                            s += "<tr class=\"unshaded-bg\">";
                        }
                        s += "<td>" + this.stems.get(i).stemValue + "</td>";
                        num = startV;
                        for (int j = startV, k = 0; j < (len + startV); j++, k++) {
                            if (this.responseSet.values.get(k).equalsIgnoreCase("-1")) {
                                s += "<td><center>";
                                s += "<input type='radio' name='" + this.stemFieldNames.get(i).toUpperCase()
                                        + "' value='" + num + "'>";
                                s += "</center></td>";
                                num = num + 1;
                            } else {
                                s += "<td><center>";
                                s += "<input type='radio' name='" + this.stemFieldNames.get(i).toUpperCase()
                                        + "' value='" + this.responseSet.values.get(k) + "'>";
                                s += "</center></td>";
                                num = num + 1;
                            }
                        }
                        s += "</tr>";
                    }
                } // if classified level is required for the question block
                else {
                    if ((rowBackgroundColorIndex++ % 2) == 0) {
                        s += "<tr class=\"shaded-bg\">";
                    } else {
                        s += "<tr class=\"unshaded-bg\">";
                    }
                    s += "<td class=\"header-row sub_head\">";
                    if (isSubHead) {
                        s += this.stems.get(i).stemValue;
                    } else {
                        s += "&nbsp;";
                    }
                    s += "</td>";
                    s += classifiedLevelColumns;
                    s += "</tr>";
                    if (isSubStem) {
                        if ((rowBackgroundColorIndex++ % 2) == 0) {
                            s += "<tr class=\"shaded-bg\">";
                        } else {
                            s += "<tr class=\"unshaded-bg\">";
                        }
                        s += "<td>" + this.stems.get(i).stemValue + "</td>";
                        num = startV;

                        for (int j = 1; j <= levels; j++) {
                            s += "<td><center>";
                            s += "<input type='radio' name='" + this.stemFieldNames.get(i).toUpperCase() + "' value='"
                                    + num + "'>";
                            s += "</center></td>";
                            num = num + 1;
                        }
                        s += "</tr>";
                    }
                }
            } else {
                if ((rowBackgroundColorIndex++ % 2) == 0) {
                    s += "<tr class=\"shaded-bg\">";
                } else {
                    s += "<tr class=\"unshaded-bg\">";
                }
                if (isSubHead) {
                    s += "<td class=\"sub_head\">" + this.stems.get(i).stemValue + "</td>";
                    if (levels == 0) {
                        s += noClassifiedLevelColumns;
                        s += "</tr>";
                    } // if classified level is required for the question block
                    else {
                        s += classifiedLevelColumns;
                        s += "</tr>";
                    }
                } else {
                    s += "<td>" + this.stems.get(i).stemValue + "</td>";
                    num = startV;

                    /* if the question block doesn't require classified level */
                    if (levels == 0) {
                        for (int j = startV, k = 0; j < (len + startV); j++, k++) {
                            if (this.responseSet.values.get(k).equalsIgnoreCase("-1")) {
                                s += "<td><center>";
                                s += "<input type='radio' name='" + this.stemFieldNames.get(i).toUpperCase()
                                        + "' value='" + num + "'>";
                                s += "</center></td>";
                                num = num + 1;
                            } else {
                                s += "<td><center>";
                                s += "<input type='radio' name='" + this.stemFieldNames.get(i).toUpperCase()
                                        + "' value='" + this.responseSet.values.get(k) + "'>";
                                s += "</center></td>";
                                num = num + 1;
                            }
                        }
                    } else {

                        /*
                         * if classified level is required for the question
                         * block
                         */
                        for (int j = 1; j <= levels; j++) {
                            s += "<td><center>";
                            s += "<input type='radio' name='" + this.stemFieldNames.get(i).toUpperCase() + "' value='"
                                    + num + "'>";
                            s += "</center></td>";
                            num = num + 1;
                        }
                    }
                }
            }
        }
        s += "</table>";
        return s;
    }

    /**
     * Prints question block - admin tool: print survey.
     * 
     * @return String HTML format of the this question block to print the
     *         survey.
     */
    @Override
    public String printSurvey() {
        String s = "";
        int len = this.responseSet.getSize();
        int startV = Integer.parseInt(this.responseSet.startvalue);
        int num = startV;
        // String t1, t2;
        int levels = Integer.parseInt(this.responseSet.levels);

        /* render top part of the question block */
        if (levels == 0) {
            s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
            s += "<tr bgcolor=#FFFFFF><td>";
            if (!this.instructions.equalsIgnoreCase("NONE")) {
                s += "<b>" + this.instructions + "</b>";
            } else {
                s += "&nbsp;";
            }
            s += "</td>";
            for (int j = startV, i = 0; j < (len + startV); j++, i++) {
                s += "<td align=center>" + this.responseSet.responses.get(i) + "</td>";
            }
            s += "</tr>";
        } else {
            s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
            s += "<tr bgcolor=#FFFFFF>";
            s += "<td rowspan=2 width='70%'>";
            if (!this.instructions.equalsIgnoreCase("NONE")) {
                s += "<b>" + this.instructions + "</b>";
            } else {
                s += "&nbsp;";
            }
            s += "</td>";

            s += "<td colspan=" + levels + " width='20%'>";
            s += "<table cellpadding='0' border='0' width='100%'>";
            int step = Math.round((levels - 1) / (len - 1));
            // int k = 1;
            for (int j = 1, i = 0, l = startV; j <= levels; j++, l++) {
                int det = (j - 1) % step;
                if (det == 0) {
                    s += "<tr>";
                    if (j == 1) {
                        s += "<td align='left'>";
                    } else if (j == levels) {
                        s += "<td align='right'>";
                    } else {
                        s += "<td align='center'>";
                    }
                    s += l + ". " + this.responseSet.responses.get(i);
                    s += "</td></tr>";
                    i++;
                }
            }
            s += "</table>";
            s += "</td>";
            s += "</tr>";

            s += "<tr bgcolor=#FFFFFF>";
            for (int j = startV; j < (levels + startV); j++) {
                s += "<td><center>" + j + "</center></td>";
            }
            s += "</tr>";
        }

        /* render each stem of the question block */
        for (int i = 0; i < this.stems.size(); i++) {
            if ((i % 2) == 0) {
                s += "<tr bgcolor=#CCCCCC>";
            } else {
                s += "<tr bgcolor=#FFFFFF>";
            }
            s += "<td>" + this.stems.get(i).stemValue + "</td>";
            num = startV;
            if (levels == 0) {
                for (int j = startV; j < (len + startV); j++) {
                    s += "<td align=center>";
                    s += "<img src='" + WISEApplication.getInstance().getWiseProperties().getServerRootUrl() + "/WISE"
                            + "/" + WiseConstants.SURVEY_APP + "/" + "imageRender?img=checkbox.gif' border='0'></a>";
                    s += "</td>";
                    num = num + 1;
                }
            } else {
                for (int j = 1; j <= levels; j++) {
                    s += "<td align=center>";
                    s += "<img src='" + WISEApplication.getInstance().getWiseProperties().getServerRootUrl() + "/WISE"
                            + "/" + WiseConstants.SURVEY_APP + "/" + "imageRender?img=checkbox.gif' border='0'></a>";
                    s += "</td>";
                    num = num + 1;
                }
            }
        }
        s += "</table>";
        return s;
    }

    /**
     * Renders the HTML header for this question block.
     * 
     * @return String HTML format of the header.
     */
    protected String renderQBheader() {
        String s = "";
        int len = this.responseSet.getSize();
        int startV = Integer.parseInt(this.responseSet.startvalue);
        int levels = Integer.parseInt(this.responseSet.levels);

        /* render top part of the question block */
        if (levels == 0) {
            s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
            s += "<tr>";
            s += "<td class=\"header-row\">";
            if (!this.instructions.equalsIgnoreCase("NONE")) {
                s += "<b>" + this.instructions + "</b>";
            } else {
                s += "&nbsp;";
            }
            s += "</td>";
            for (int j = startV, i = 0; j < (len + startV); j++, i++) {
                s += "<td class=\"header-row\"><center>" + this.responseSet.responses.get(i) + "</center></td>";
            }
            s += "</tr>";
        } else {
            s += "<table cellspacing='0' cellpadding='7' width=100%' border='0'>";
            s += "<tr>";
            s += "<td class=\"header-row\" rowspan=2 width='70%'>";
            if (!this.instructions.equalsIgnoreCase("NONE")) {
                s += "<b>" + this.instructions + "</b>";
            } else {
                s += "&nbsp;";
            }
            s += "</td>";

            s += "<td class=\"header-row\" colspan=" + levels + " width='20%'>";
            s += "<table cellpadding='0' border='0' width='100%'>";
            int step = Math.round((levels - 1) / (len - 1));
            for (int j = 1, i = 0, l = startV; j <= levels; j++, l++) {
                int det = (j - 1) % step;
                if (det == 0) {
                    s += "<tr>";
                    if (j == 1) {
                        s += "<td align='left'>";
                    } else if (j == levels) {
                        s += "<td align='right'>";
                    } else {
                        s += "<td align='center'>";
                    }
                    s += l + ". " + this.responseSet.responses.get(i);
                    s += "</td></tr>";
                    i++;
                }
            }
            s += "</table>";
            s += "</td>";
            s += "</tr>";

            s += "<tr class=\"header-row\">";
            for (int j = startV; j < (levels + startV); j++) {
                s += "<td><center>" + j + "</center></td>";
            }
            s += "</tr>";
        }
        return s;
    }

    /**
     * Returns the results for a question block in form of string.
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
        return db.renderResultsForQuestionBlock(pg, this, db, whereclause, data);
    }

    /**
     * Renders the HTML header for this question block's result.
     * 
     * @return String HTML format of the header.
     */
    public String renderQBResultHeader() {
        String s = "";
        int levels = Integer.valueOf(this.responseSet.levels).intValue();
        int startValue = Integer.valueOf(this.responseSet.startvalue).intValue();
        s += "<span class='itemID'>" + this.name + "</span></td></tr></table><br>";

        /* display the question block */
        s += "<table cellspacing='0' cellpadding='1' bgcolor=#FFFFF5 width=600 border='1'>";
        s += "<tr><td bgcolor=#BA5D5D rowspan=2 width='60%'>";
        s += "<table><tr><td width='95%'>";
        // display the instruction if it has
        if (!this.instructions.equalsIgnoreCase("NONE")) {
            s += "<b>" + this.instructions + "</b>";
        } else {
            s += "&nbsp;";
        }
        s += "</td><td width='5%'>&nbsp;</td></tr></table></td>";

        String t1, t2;

        /* display the level based on the size of the question block */
        if (levels == 0) {
            s += "<td colspan=" + this.responseSet.responses.size() + " width='40%'>";
            s += "<table bgcolor=#FFCC99 width=100% cellpadding='1' border='0'>";

            for (int j = 0; j < this.responseSet.responses.size(); j++) {

                t2 = String.valueOf(j + startValue);
                t1 = this.responseSet.responses.get(j);
                s += "<tr>";

                if (j == 0) {
                    s += "<td align=left>";
                } else if ((j + 1) == this.responseSet.responses.size()) {
                    s += "<td align=right>";
                } else {
                    s += "<td align=center>";
                }
                s += t2 + ". " + t1 + "</td>";
                s += "</tr>";
            }
            s += "</table>";
            s += "</td>";
            s += "</tr>";
            int width = 40 / this.responseSet.responses.size();

            for (int j = 0; j < this.responseSet.responses.size(); j++) {

                t2 = String.valueOf(j + startValue);
                s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>" + t2 + "</center></b></td>";
            }
        } else {

            /* display the classified level */
            s += "<td colspan=" + levels + " width='40%'>";
            s += "<table bgcolor=#FFCC99 cellpadding='0' border='0' width='100%'>";

            /* calculate the step between levels */
            int step = Math.round((levels - 1) / (this.responseSet.responses.size() - 1));

            for (int j = 1, i = 0, l = startValue; j <= levels; j++, l++) {
                int det = (j - 1) % step;
                if (det == 0) {
                    s += "<tr>";
                    if (j == 1) {
                        s += "<td align='left'>";
                    } else if (j == levels) {
                        s += "<td align='right'>";
                    } else {
                        s += "<td align='center'>";
                    }
                    s += l + ". " + this.responseSet.responses.get(i);
                    s += "</td></tr>";
                    i++;
                }
            }
            s += "</table>";
            s += "</td>";
            s += "</tr>";

            int width = 40 / levels;
            for (int j = 0; j < levels; j++) {
                t2 = String.valueOf(j + startValue);
                s += "<td bgcolor=#BA5D5D width='" + width + "%'><b><center>" + t2 + "</center></b></td>";
            }
        }
        s += "</tr>";
        return s;
    }

    /** returns a comma delimited list of all the fields on a page */
    /*
     * public String list_fields() { String s = ""; for (int i = 0; i <
     * stems.length; i++) s += stem_fieldNames[i]+","; return s; }
     */

    /**
     * Prints out the question block information
     * 
     * @return String Information about question block.
     */
    @Override
    public String toString() {
        String s = "QUESTION BLOCK<br>";
        s += super.toString();

        s += "Instructions: " + this.instructions + "<br>";
        s += "Response Set: " + this.responseSet.id + "<br>";
        s += "Stems:<br>";

        for (int i = 0; i < this.stems.size(); i++) {
            s += this.stemFieldNames.get(i) + ":" + this.stems.get(i).stemValue + "<br>";
        }
        if (this.cond != null) {
            s += this.cond.toString();
        }
        s += "<p>";
        return s;
    }

    public String getSubjectSetName() {
        return subjectSetName;
    }

    public void setSubjectSetName(String subjectSetName) {
        this.subjectSetName = subjectSetName;
    }

    /**
     * Class to store stemType and stemValue together.
     * 
     * @author Douglas Bell
     * @version 1.0
     */
    public class StemDifferentiator {
        public String stemType;
        public String stemValue;

        public StemDifferentiator(String type, String value) {
            this.stemType = type;
            this.stemValue = value;
        }
    }
}
