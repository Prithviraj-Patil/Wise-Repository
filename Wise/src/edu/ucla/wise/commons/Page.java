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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucla.wise.client.web.TemplateUtils;
import freemarker.template.TemplateException;

/**
 * Page class represents a page in the survey and its various attributes.
 */
public class Page {

    public static final Logger LOGGER = Logger.getLogger(Page.class);

    /** Instance Variables */
    private final String id;

    private final String title;

    public String getTitle() {
        return this.title;
    }

    private final String instructions;
    private final Survey survey;

    public Survey getSurvey() {
        return this.survey;
    }

    private final PageItem[] items;

    public int getFieldCount() {
        return this.fieldCount;
    }

    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    public String getInstructions() {
        return this.instructions;
    }

    public PageItem[] getItems() {
        return this.items;
    }

    public String[] getAllFieldNames() {
        return this.allFieldNames;
    }

    public char[] getAllValueTypes() {
        return this.allValueTypes;
    }

    public ArrayList<RepeatingItemSet> getRepeatingItems() {
        return this.repeatingItems;
    }

    public Condition getCond() {
        return this.cond;
    }

    private final String[] allFieldNames;
    private final char[] allValueTypes; // just 'a' for string, 'n' for numeric;
                                        // may add dates, PRN

    /* adding this to create tables when new survey is loaded */
    private final ArrayList<RepeatingItemSet> repeatingItems = new ArrayList<RepeatingItemSet>();

    // public boolean blank_page = true; same as fieldCount=0
    private final boolean finalPage;

    public boolean isFinalPage() {
        return this.finalPage;
    }

    private final String nextPage;

    public String getNextPage() {
        return this.nextPage;
    }

    // public String meta_charset=null;
    private final Condition cond;
    int fieldCount; // for item count, get items.length

    /** CLASS FUNCTIONS */

    /**
     * Constructor
     */
    public Page(String id, String title, String instructions, Survey survey, PageItem[] items, String[] allFieldNames,
            char[] allValueTypes, ArrayList<RepeatingItemSet> repeatingItems, boolean finalPage, String nextPage,
            Condition condition) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.survey = survey;
        this.items = items;
        this.allFieldNames = allFieldNames;
        this.allValueTypes = allValueTypes;
        this.repeatingItems.addAll(repeatingItems);
        this.finalPage = finalPage;
        this.nextPage = nextPage;
        this.cond = condition;
    }

    /**
     * Constructor - parse out the survey page node from xml and create a page
     * with its attributes.
     * 
     * @param n
     *            DOM node for the page.
     * @param s
     *            Survey to which this page should be linked to.
     */
    public Page(Node n, Survey s) {

        /* assign the survey object */
        this.survey = s;

        /*
         * parse the page node with attributes: page ID, title, instruction, ID
         * of next page & final page
         */
        this.id = n.getAttributes().getNamedItem("ID").getNodeValue();
        this.title = n.getAttributes().getNamedItem("Title").getNodeValue();
        Node node1 = n.getAttributes().getNamedItem("Instructions");
        if (node1 != null) {
            this.instructions = node1.getNodeValue();
        } else {
            this.instructions = "NONE";
        }
        node1 = n.getAttributes().getNamedItem("nextPage");
        if (node1 != null) {
            this.nextPage = node1.getNodeValue();
        } else {
            this.nextPage = "NONE";
        }
        boolean isFinalPage = false;
        node1 = n.getAttributes().getNamedItem("finalPage");
        if (node1 != null) {
            String fp = node1.getNodeValue();
            if (fp.equalsIgnoreCase("true")) {
                isFinalPage = true;
            }
        }
        this.finalPage = isFinalPage;

        /* initialize the number of form field on the page */
        NodeList nodelist = n.getChildNodes();

        /* count the number of page items */
        int pageItemCount = 0;
        this.fieldCount = 0;
        for (int i = 0; i < nodelist.getLength(); i++) {
            if (PageItem.IsPageItemNode(nodelist.item(i))) {
                pageItemCount++;
            }
        }
        this.items = new PageItem[pageItemCount];

        Condition pagePrecondition = null;

        /* parse & store the page items and any precondition */
        for (int i = 0, k = 0; i < nodelist.getLength(); i++) {
            node1 = nodelist.item(i);
            if (PageItem.IsPageItemNode(node1)) {

                // Pralav- adding repeating item code here to keep the
                // object reference constant
                if (RepeatingItemSet.IsRepeatingItemSetNode(node1)) {
                    RepeatingItemSet repeatingSet = RepeatingItemSet.MakeNewItem(node1);
                    if (repeatingSet == null) {
                        throw new IllegalArgumentException("Error parsing repeating item" + k);
                    }
                    this.repeatingItems.add(repeatingSet);
                    this.items[k++] = repeatingSet;
                } else {
                    PageItem pi = PageItem.MakeNewItem(node1);
                    if (pi == null) {
                        throw new IllegalArgumentException("Null item parse at " + k);
                    }
                    this.items[k++] = pi;
                }

            } else if (node1.getNodeName().equalsIgnoreCase("Precondition")) {

                /* create the condition object */
                pagePrecondition = new Condition(node1);
            }
        }
        this.cond = pagePrecondition;

        /*
         * iterate over items & knit references also collect full list of
         * fieldnames; consider also collecting main names & ss refs separately
         */
        for (int i = 0; i < pageItemCount; i++) {

            /*
             * note this req here as MultiSelect Q field counts depend on refs
             * being resolved
             */
            this.items[i].knitRefs(this.survey);
            this.fieldCount += this.items[i].countFields();
        }
        this.allFieldNames = new String[this.fieldCount];
        this.allValueTypes = new char[this.fieldCount];
        for (int i = 0, allStart = 0; i < pageItemCount; i++) {

            if (this.items[i] instanceof RepeatingItemSet) {
                // ignore these
            } else {
                String[] fieldnames = this.items[i].listFieldNames();
                char valType = this.items[i].getValueType();
                int j = 0;
                for (; j < fieldnames.length; j++) {

                    /* if multiple fields, copy same valType across all */
                    String fn = fieldnames[j];
                    this.allFieldNames[allStart + j] = fn;
                    this.allValueTypes[allStart + j] = valType;
                }
                allStart += j;
            }
        }
    }

    /**
     * @return id for the page.
     */
    public final String getId() {
        return this.id;
    }

    /**
     * Returns all the field names related to the questions in this page of the
     * survey.
     * 
     * @return Array Array of string which contains the field names.
     */
    public String[] getFieldList() {

        /* modifications to add repeating questions */
        return this.allFieldNames;
    }

    /**
     * Returns the number of the fields in this page of the survey.
     * 
     * @return int Number of the fields.
     */
    public int getItemCount() {
        return this.allFieldNames.length;
    }

    /**
     * Returns the type of the response for the questions in the page of the
     * survey.
     * 
     * @return Array character array which contains the response type of each
     *         question
     */
    public char[] getValueTypeList() {
        return this.allValueTypes;
    }

    /**
     * Gets the charset from the item on this page if there is a translation
     * item.
     * 
     * @return String The charset of the item in this page.
     */
    public String requiredCharset() {
        String currentCharset = null;
        for (int i = 0; i < this.items.length; i++) {
            if (this.items[i].translationId != null) {
                TranslationItem translatedItem = this.survey.getTranslationItem(this.items[i].translationId);
                currentCharset = translatedItem.charset;
                break;
            }
        }
        return currentCharset;
    }

    /**
     * Creates a string including all the required fields which is used for
     * JavaScript.
     * 
     * @return String All the required feilds in this page.
     */
    public String requiredFields() {
        int flag = 0;
        String str = "{";
        for (int i = 0; i < this.items.length; i++) {
            if (this.items[i].isRequired()) {

                /* {REQUIRED_FIELD_NAME:A, ... etc.} */
                str = str + "'" + this.items[i].name.toUpperCase() + "':'" + this.items[i].getRequiredStem() + "',";
                flag = 1;
            }
        }
        if (flag == 1) {
            /* eliminate the last comma */
            int len = str.length();
            str = str.substring(0, len - 1);
        }
        str = str + "}";
        return str;
    }

    /**
     * Returns repeating sets, used for database table creation.
     * 
     * @return ArrayList List of the repeating items in this page.
     */
    public ArrayList<RepeatingItemSet> getRepeatingItemSets() {
        return this.repeatingItems;
    }

    /**
     * create the table creation syntax for all the page items public String
     * create_table() { String sql = ""; for (int i = 0; i < items.length; i++)
     * sql += items[i].create_table(); return sql; }
     */

    /**
     * Render a survey page as an html form for the user to fill out.
     * 
     * @param theUser
     *            The user to whom the page has to be rendered.
     * @return String HTML format of this page.
     */
    public String renderPage(UserAnswers theUser) {

        StringBuilder responseHtml = new StringBuilder();

        if (!this.checkPrecondition(theUser)) {
            // TODO: just return empty html?
        }

        Map<String, Object> htmlTemplateParameters = new HashMap<>();

        htmlTemplateParameters.put("title", this.title.equalsIgnoreCase("NONE") ? "" : this.title);
        htmlTemplateParameters.put("requiredFields",
                this.requiredFields().equalsIgnoreCase("{}") ? "null" : this.requiredFields());
        /* get the field name:value pair for JavaScript */

        String fieldVals = theUser.getJSValues();
        htmlTemplateParameters.put("fieldVals", fieldVals.equalsIgnoreCase("{}") ? "null" : fieldVals);
        htmlTemplateParameters.put("studyName", this.survey.getStudySpace().studyName);
        htmlTemplateParameters.put("font", StudySpace.font);
        htmlTemplateParameters.put("instructions", this.instructions.equalsIgnoreCase("NONE") ? "" : this.instructions);

        htmlTemplateParameters.put("formHtml", this.getPageFormHtml(theUser));
        try {
            responseHtml.append(TemplateUtils.getHtmlFromTemplate(htmlTemplateParameters, "page_template.ftl"));
        } catch (IOException e) {
            LOGGER.error(e);
        } catch (TemplateException e) {
            LOGGER.error(e);
        }
        return responseHtml.toString();
    }

    private String getNextPageName() {
        String nextPage = "";
        if ((this.survey.isLastPage(this.id)) || (this.finalPage)) {
            nextPage = "DONE";
        } else {
            if (this.nextPage.equalsIgnoreCase("NONE")) {
                nextPage = this.survey.nextPage(this.id).id;
            } else {
                nextPage = this.nextPage;
            }
        }
        return nextPage;
    }

    private String getPageItems(UserAnswers theUser) {
        StringBuilder response = new StringBuilder();
        /* DISPLAY the ITEMS */
        for (int i = 0; i < this.items.length; i++) {
            response.append(this.items[i].renderForm(theUser, i) + "\n");
        }
        return response.toString();
    }

    private String getNextImageName() {
        String s = "";
        if ((this.survey.isLastPage(this.id)) || (this.finalPage)) {
            if ((this.survey.getEduModule() != null) && !this.survey.getEduModule().equalsIgnoreCase("")) {
                s = "proceed";
            } else {
                s = "done";
            }
        } else {
            s = "save_and_next_page";
        }
        return s;
    }

    private String getPageFormHtml(UserAnswers theUser) {
        StringBuilder responseHtml = new StringBuilder();
        Map<String, Object> htmlTemplateParameters = new HashMap<>();
        htmlTemplateParameters.put("nextPage", this.getNextPageName());
        htmlTemplateParameters.put("items", this.getPageItems(theUser));
        htmlTemplateParameters.put("submitImage", this.getNextImageName());
        try {
            responseHtml.append(TemplateUtils.getHtmlFromTemplate(htmlTemplateParameters, "form_template.ftl"));
        } catch (IOException e) {
            LOGGER.error(e);
        } catch (TemplateException e) {
            LOGGER.error(e);
        }
        return responseHtml.toString();
    }

    /** render survey result page for completers' review */
    // public String render_results(User theUser, String whereclause)
    // {
    // String s = "<html>";
    // //display html header
    // s += "<head>";
    // if (!title.equalsIgnoreCase("NONE"))
    // s += "<title>"+title+"</title>";
    // s +="<LINK href='"+ survey.study_space.style_path
    // +"style.css' rel=stylesheet>";
    // s +="<script type='text/javascript' language='javascript'>";
    // s +="function open_help_win(){";
    // s +=" var helpwin=window.open('"+Study_Space.file_path
    // +"result_help.htm', 'help_win', 'height=500, width=500, scrollbars=yes, toolbar=no');";
    // s +=" if (helpwin.opener==null) helpwin.opener = self; }";
    // s +=" </script>";
    // if (!title.equalsIgnoreCase("NONE"))
    // s += "<title>"+title+"</title>";
    // s += "</head>";
    // s += "<body text='#000000' bgcolor='#FFFFCC'>";
    // s +=
    // "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>";
    // s += "<tr><td width=160 align=center>";
    // s +="<img src='"+Study_Space.file_path
    // +"images/somlogo.gif' border=0>";
    // s +="</td><td width=400 align=center>";
    // s +="<img src='"+Study_Space.file_path
    // +"images/title.gif' border=0><br><br>";
    // s
    // +="<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>";
    // s +="</td><td width=160 align=center>";
    // s +="</td></tr></table></center><br><br>";
    // //display the help info
    // s += "<table cellpadding=5><tr><td>";
    // s += "For each question, ";
    // s +=
    // "the graphs below show the <b>percentage</b> of people choosing each answer. ";
    // s += "Percentages may not sum to 100 because of rounding. ";
    // s += "Click <a href='javascript: open_help_win()'>";
    // s += "here</a> for more explanation of results.";
    // s +=
    // "<p><b><font color=green>"+get_pagedone_numb(whereclause)+" </font></b>people have completed this page.<p>";
    // //display the main body
    // if (!instructions.equalsIgnoreCase("NONE"))
    // s += "<h4><i>"+instructions+"</i></h4>";
    // //get the survey data within the scope of whereclause
    // //the default whereclause is an empty string - means view all the
    // users'
    // results
    // Hashtable data = new Hashtable();
    // if (field_count > 0)
    // {
    // //data = get_survey_data(whereclause);
    // data = theUser.get_data();
    // for (int i = 0; i < items.length; i++)
    // {
    // s += items[i].render_results(data, whereclause);
    // s += "<p>";
    // }
    // }
    // else
    // {
    // for (int i = 0; i < items.length; i++)
    // {
    // s += items[i].render_results(data, whereclause);
    // s += "<p>";
    // }
    // }
    // s += "<center>";
    // //display the image link
    // if (survey.is_last_page(survey.get_page_index(id)))
    // s +="<a href='"+Study_Space.file_path + "thanks" +
    // Study_Space.html_ext+"'><img src='"+Study_Space.file_path
    // +"images/done.gif' border='0'></a>";
    // else
    // s +=
    // "<a href='view_results?page="+survey.next_page(id).id+"'><img src='"+Study_Space.file_path
    // +"images/next.gif' border='0'></a>";
    //
    // s += "</center>";
    // s += "</td></tr></table>";
    // s += "</body>";
    // s += "</html>";
    //
    // return s;

    // }

    private boolean checkPrecondition(UserAnswers theUser) {
        boolean writePage = true;
        /* check the precondition, if this page has the precondition node - */
        if (this.cond != null) {
            /*
             * check if the user's current input meets the required
             * preconditions
             */
            writePage = this.cond.checkCondition(theUser);

            /*
             * if it doesn't meet the precondition, then skip writing this whole
             * page by return an empty string
             */

            this.LOGGER.info("Precondition for page is " + writePage);
            // if (!write_page)
            // return s;
        }
        return writePage;
    }

    /**
     * Counts the number of users within the whereclause scope who completed the
     * current page
     * 
     * @param whereclause
     * @return
     */
    public int getPagedoneNumb(String whereClause) {
        return this.getSurvey().getStudySpace().getDB().getPageDoneNumb(this.getSurvey(), this, whereClause);
    }

    /**
     * get the survey data (hash table) within the range delimited by
     * whereclause selection
     */
    /*
     * public Hashtable get_survey_data(String whereclause) { Hashtable h = new
     * Hashtable(); try { //connect to the database Connection conn =
     * survey.getDBConnection(); Statement stmt = conn.createStatement(); // get
     * data from database for subject String sql = "select * from "+
     * survey.id+"_data"; if(!whereclause.equalsIgnoreCase("")) sql +=
     * " where "+whereclause;
     * 
     * boolean dbtype = stmt.execute(sql); ResultSet rs = stmt.getResultSet();
     * ResultSetMetaData metaData = rs.getMetaData(); int columns =
     * metaData.getColumnCount(); //the data hash table takes the column name as
     * the key //and the user's anwser as its value if(rs.next()) { String
     * col_name, ans; for (int i = 1; i <= columns; i++) { col_name =
     * metaData.getColumnName(i); ans = rs.getString(col_name); //input a string
     * called null if the column value is null //to avoid the hash table has the
     * null value if (ans == null) ans = "null"; h.put(col_name,ans); } }
     * rs.close(); stmt.close(); conn.close(); } catch (Exception e) {
     * Study_Util.email_alert("PAGE GET SURVEY DATA: "+e.toString()); } return
     * h; }
     */

    /**
     * Renders a survey page for view - admin tool: view survey
     * 
     * @param ss
     *            StudySpace whose page has to be rendered.
     * @return String HTML format of this page to view.
     */
    public String renderAdminPage(StudySpace ss) {
        String s = "<html>";

        /* form the html header */
        s += "<head>";
        if (!this.title.equalsIgnoreCase("NONE")) {
            s += "<title>" + this.title + "</title>";
        }
        s += "<LINK href='" + "styleRender?app=" + ss.studyName + "&css=style.css" + "' type=text/css rel=stylesheet>";
        s += "<script type='text/javascript' language='JavaScript1.1' src='"
                + SurveyorApplication.getInstance().getSharedFileUrl() + "/js/survey.js'></script>";
        s += "</head>";

        /* form the html body */
        s += "<body>";
        s += "<table cellpadding=5 width='100%'><tr><td>";
        if (!this.instructions.equalsIgnoreCase("NONE")) {
            s += "<h4>" + this.instructions + "</h4>";
        }
        for (int i = 0; i < this.items.length; i++) {
            s += this.items[i].renderForm();
            s += "<p>";
        }
        s += "<p>";
        s += "<center>";

        /* form the image link */
        if (this.survey.isLastPage(this.id)) {

            /* Servlet to render */
            s += "<a href='" + WISEApplication.getInstance().getWiseProperties().getAdminServer()
                    + "/tool.jsp'><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                    + "/WISE/survey/imageRender?img=done.gif' border='0'></a>";
        } else {

            /* Servlet to render */
            s += "<a href='admin_view_form'><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                    + "/WISE/survey/imageRender?img=next.gif' border='0'></a>";
        }
        s += "</center>";
        s += "</td></tr></table>";
        s += "</body>";
        s += "</html>";
        return s;
    }

    /**
     * Prints survey page - admin tool: print survey.
     * 
     * @return String HTML format of the this page to print the survey.
     */
    public String printSurveyPage() {
        String s = "";

        /* print body header */
        s += "<table cellpadding=5 width=100%>";
        s += "<tr><td align=left width=50%><font color=#003399 size=-2><b>" + this.survey.getTitle()
                + "</b></font></td>";
        s += "<td align=right width=50%><font color=#003399 size=-2>Page " + (this.survey.getPageIndex(this.id) + 1)
                + " of " + this.survey.getPages().length + "</font></td>";
        s += "</tr></table><p><p>";

        /* print main body */
        s += "<table cellpadding=5 width='100%'><tr><td>";
        if (!this.instructions.equalsIgnoreCase("NONE")) {
            s += "<b>" + this.instructions + "</b>";
        }
        for (int i = 0; i < this.items.length; i++) {
            s += this.items[i].printSurvey();
            s += "<p>";
        }

        /* print image link */
        s += "<p>";
        s += "<center><table><tr><td align=center valign=middle height=30>";
        if (this.survey.isLastPage(this.id)) {
            s += "<br><a href='" + WISEApplication.getInstance().getWiseProperties().getAdminServer()
                    + "/tool.jsp'><b>DONE</b></a><br>";
            s += "<p align=center><b>Thank you for completing the survey.</b></p>";
        } else {
            /* Servlet to render */
            s += "<a href='admin_print_survey'><b>Go To Next Page </b><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                    + "/WISE/survey/imageRender?img=nextpg.gif' border='0'></a>";
        }
        s += "</td></tr></table></center>";
        s += "</td></tr></table>";
        return s;
    }

    /**
     * Renders survey results - admin tool: view results.
     * 
     * @param whereclause
     *            Invitees of whom the result has to be displayed.
     * @return String HTML format of the results of the selected invitees.
     */
    public String renderAdminResults(String whereclause) {
        String s = "<html>";

        /* display header part */
        s += "<head>";
        if (!this.title.equalsIgnoreCase("NONE")) {
            s += "<title>" + this.title + "</title>";
        }
        s += "<LINK href='" + "styleRender?app=" + this.survey.getStudySpace().studyName + "&css=style.css"
                + "' type=text/css rel=stylesheet>";
        s += "<script type='text/javascript' language='javascript' src='"
                + SurveyorApplication.getInstance().getSharedFileUrl() + "openhelpwin.js'></script>";
        s += "</head>";

        /* display body part */
        s += "<body>";
        s += "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>";
        s += "<tr><td width=160 align=center>";

        /* Servlet to render */
        s += "<img src='" + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                + "/WISE/survey/imageRender?img=somlogo.gif' border=0>";
        s += "</td><td width=400 align=center>";

        /* Servlet to render */
        s += "<img src='" + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                + "/WISE/survey/imageRender?img=title.jpg' border=0><br><br>";
        s += "<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>";
        s += "</td><td width=160 align=center>";
        s += "<a href='javascript: history.go(-1)'>";

        /* Servlet to render */
        s += "<img src='" + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                + "/WISE/survey/imageRender?img=back.gif' border=0></a>";
        s += "</td></tr></table></center><br><br>";

        /* display the help info */
        s += "<table cellpadding=5><tr><td>";
        s += "For each question, ";
        s += "the graphs below show the <b>percentage</b> of people choosing each answer. ";
        s += "Percentages may not sum to 100 because of rounding. ";
        s += "Click <a href='javascript: open_helpwin()'>";
        s += "here</a> for more explanation of results.";
        // TODO: DEBUG INACCURATE count algo; (try using page_submit table)
        // s +=
        // "<p><b><font color=green>"+get_pagedone_numb(whereclause)+" </font></b>people have completed this page.<p>";

        /* display the results of questions */
        if (!this.instructions.equalsIgnoreCase("NONE")) {
            s += "<h4><i>" + this.instructions + "</i></h4>";
        }

        /*
         * get the survey data conducted by users within the scope of
         * whereclause
         */
        for (int i = 0; i < this.items.length; i++) {
            // TODO: Help!
            s += this.items[i].renderResults(this, this.survey.getDB(), whereclause, null);
            s += "<p>";
        }
        s += "<center>";

        /* display the image link */
        if (this.survey.isLastPage(this.survey.getPageIndex(this.id))) {
            /* Servlet to render */
            s += "<a href='" + WISEApplication.getInstance().getWiseProperties().getAdminServer()
                    + "/view_result.jsp?s=" + this.survey.getId() + "'><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                    + "/WISE/survey/imageRender?img=done.gif' border='0'></a>";
        } else {
            s += "<a href='admin_view_results'><img src='"
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                    + "/WISE/survey/imageRender?img=next.gif' border='0'></a>";
        }
        s += "</center>";
        s += "</td></tr></table>";
        s += "</body>";
        s += "</html>";

        return s;
    }

    /**
     * Reads paramaeters passed from data source that apply to field names;
     * delegate value processing to each PageItem that the page contains.
     * 
     * @param params
     *            The Http parameters to this page.
     * @return Hashtable Contains the answers of the survey so far for this
     *         page.
     */
    public Hashtable<String, Hashtable<String, String>> readForm(Hashtable<String, String> params) {
        Hashtable<String, Hashtable<String, String>> pageAnswerSets = new Hashtable<String, Hashtable<String, String>>();

        /* don't bother if page is only directivespage */
        if (this.allFieldNames.length > 0) {
            Hashtable<String, String> mainAnswers = new Hashtable<String, String>();
            for (int i = 0; i < this.items.length; i++) {
                Hashtable<String, String> itemAnswers = this.items[i].readForm(params);

                /* subjectsets push their id into hash */
                String ssid = itemAnswers.get("__SubjectSet_ID__");
                if (ssid != null) {
                    itemAnswers.remove("__SubjectSet_ID__");
                    pageAnswerSets.put(ssid, itemAnswers);
                } else {
                    mainAnswers.putAll(itemAnswers);
                }
            }
            pageAnswerSets.put("__WISEMAIN__", mainAnswers);
        }
        return pageAnswerSets;
    }

    /** query database to get the total number of respondents for a page */
    /*
     * public int get_total() { if (!blank_page) { int total = 0; try {
     * Connection conn = survey.getDBConnection(); Statement stmt =
     * conn.createStatement(); // print total number who have answered this page
     * String sql = "select count(*) from "+survey.id+"_data"; boolean dbtype =
     * stmt.execute(sql); ResultSet rs = stmt.getResultSet(); rs.next(); total =
     * rs.getInt(1); rs.close(); stmt.close(); conn.close(); } catch (Exception
     * e) { Study_Util.email_alert("WISE - PAGE GET TOTAL: "+e.toString()); }
     * return total; } else return 0; }
     */
    /** render a page as results */
    /*
     * public String render_results(User theUser, String whereclause) { String s
     * = "<html>"; s += "<head>"; if (!title.equalsIgnoreCase("NONE")) s +=
     * "<title>"+title+"</title>"; s +="<LINK href='"+
     * theUser.currentSurvey.study_space.style_path
     * +"style.css' rel=stylesheet>"; s
     * +="<script type='text/javascript' language='javascript'>"; s
     * +="function open_help_win(){"; s
     * +=" var helpwin=window.open('"+Study_Space.file_path +
     * "result_help.htm', 'help_win', 'height=500, width=500, scrollbars=yes, toolbar=no');"
     * ; s +=" if (helpwin.opener==null) helpwin.opener = self; }"; s
     * +=" </script>"; if (!title.equalsIgnoreCase("NONE")) s +=
     * "<title>"+title+"</title>"; s += "</head>"; s +=
     * "<body text='#000000' bgcolor='#FFFFCC'>"; s +=
     * "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>"; s
     * += "<tr><td width=160 align=center>"; s
     * +="<img src='"+Study_Space.file_path +"images/somlogo.gif' border=0>"; s
     * +="</td><td width=400 align=center>"; s
     * +="<img src='"+Study_Space.file_path
     * +"images/title.gif' border=0><br><br>"; s +=
     * "<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>"
     * ; s +="</td><td width=160 align=center>"; s
     * +="</td></tr></table></center><br><br>";
     * 
     * s += "<table cellpadding=5><tr><td>"; s += "For each question, "; s +=
     * "the graphs below show the <b>percentage</b> of people choosing each answer. "
     * ; s += "Percentages may not sum to 100 because of rounding. "; s +=
     * "Click <a href='javascript: open_help_win()'>"; s +=
     * "here</a> for more explanation of results."; s +=
     * "<p><b><font color=green>"+get_pagedone_numb(whereclause)+
     * " </font></b>people have completed this page.<p>";
     * 
     * if (!instructions.equalsIgnoreCase("NONE")) s +=
     * "<h4><i>"+instructions+"</i></h4>";
     * 
     * if (!blank_page) { Hashtable data = theUser.get_data(); for (int i = 0; i
     * < items.length; i++) { s += items[i].render_results(data, whereclause); s
     * += "<p>"; } } else { Hashtable data = new Hashtable(); for (int i = 0; i
     * < items.length; i++) { s += items[i].render_results(data, whereclause); s
     * += "<p>"; } }
     * 
     * s += "<center>";
     * 
     * if (survey.is_last_page(survey.get_page_index(id))) s
     * +="<a href='"+Study_Space.file_path + "thanks" + Study_Space.html_ext
     * +"'><img src='"+Study_Space.file_path
     * +"images/done.gif' border='0'></a>"; else s +=
     * "<a href='view_results?page="
     * +survey.next_page(id).id+"'><img src='"+Study_Space.file_path
     * +"images/next.gif' border='0'></a>";
     * 
     * s += "</center>";
     * 
     * s += "</td></tr></table>";
     * 
     * s += "</body>"; s += "</html>";
     * 
     * return s; }
     */
    /** render an admin page as results */
    /*
     * public String render_admin_results(String whereclause) { String s =
     * "<html>"; s += "<head>"; if (!title.equalsIgnoreCase("NONE")) s +=
     * "<title>"+title+"</title>"; s +="<LINK href='"+
     * this.survey.study_space.style_path +"style.css' rel=stylesheet>"; s
     * +="<script type='text/javascript' language='javascript' src='"
     * +Study_Space.file_path +"openhelpwin.js'></script>"; s += "</head>";
     * 
     * s += "<body text='#000000' bgcolor='#FFFFCC'>"; s +=
     * "<center><table cellpadding=2 cellpadding=0 cellspacing=0 border=0>"; s
     * += "<tr><td width=160 align=center>"; s
     * +="<img src='"+Study_Space.file_path +"images/somlogo.gif' border=0>"; s
     * +="</td><td width=400 align=center>"; s
     * +="<img src='"+Study_Space.file_path
     * +"images/title.jpg' border=0><br><br>"; s +=
     * "<font color='#CC6666' face='Times New Roman' size=4><b>View Survey Results</b></font>"
     * ; s +="</td><td width=160 align=center>"; s
     * +="<a href='javascript: history.go(-1)'>"; s
     * +="<img src='"+Study_Space.file_path +"images/back.gif' border=0></a>"; s
     * +="</td></tr></table></center><br><br>";
     * 
     * s += "<table cellpadding=5><tr><td>"; s += "For each question, "; s +=
     * "the graphs below show the <b>percentage</b> of people choosing each answer. "
     * ; s += "Percentages may not sum to 100 because of rounding. "; s +=
     * "Click <a href='javascript: open_helpwin()'>"; s +=
     * "here</a> for more explanation of results."; s +=
     * "<p><b><font color=green>"+get_pagedone_numb(whereclause)+
     * " </font></b>people have completed this page.<p>";
     * 
     * if (!instructions.equalsIgnoreCase("NONE")) s +=
     * "<h4><i>"+instructions+"</i></h4>";
     * 
     * Hashtable data = new Hashtable(); // data could hold averages if
     * wanted... for (int i = 0; i < items.length; i++) { s +=
     * items[i].render_results(data, whereclause); s += "<p>"; } s +=
     * "<center>";
     * 
     * if (survey.is_last_page(survey.get_page_index(id))) s +=
     * "<a href='"+WISE_Application
     * .admin_server+"tool.jsp'><img src='"+Study_Space.file_path
     * +"images/done.gif' border='0'></a>"; else //s +=
     * "<a href='admin.view_results?a=NEXTPAGE&s="
     * +survey.id+"&p="+survey.next_page
     * (id).id+"'><img src='"+Study_Space.file_path
     * +"images/next.gif' border='0'></a>"; s +=
     * "<a href='admin.view_results'><img src='"+Study_Space.file_path
     * +"images/next.gif' border='0'></a>"; s += "</center>";
     * 
     * s += "</td></tr></table>";
     * 
     * s += "</body>"; s += "</html>";
     * 
     * return s; }
     */

    /**
     * Prints overview of the page contents.
     * 
     * @return String Printable format of this page.
     */
    @Override
    public String toString() {
        String s = "<B>-=Page=-  </B> ";
        s += "ID: " + this.id + "; ";
        s += "Title: " + this.title + "<br>";
        // s += "Instructions: "+instructions+"<br>";
        s += "Fields ";
        String[] fields = this.getFieldList();
        for (int i = 0; i < this.fieldCount; i++) {
            s += fields[i] + "; ";
        }
        s += " (n=" + this.fieldCount + "):<br>";

        for (int i = 0; i < this.items.length; i++) {
            s += this.items[i].toString();
        }
        s += "Types: ";
        for (int i = 0; i < this.allValueTypes.length; i++) {
            s += this.allValueTypes[i] + ";";
        }
        s += "<br> ";
        return s;
    }

}
