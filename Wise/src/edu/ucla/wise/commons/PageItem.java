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

import edu.ucla.wise.commons.databank.DBConstants;
import edu.ucla.wise.commons.databank.DataBank;

/**
 * This abstract class represents a single page item.
 */
public abstract class PageItem {
    public static final Logger LOGGER = Logger.getLogger(PageItem.class);
    /** Instance Variables */
    public String name;
    public String itemType;
    public String translationId = null;
    public TranslationItem questionTranslated;

    public String html = "";
    public Condition cond = null; // any page item can be conditional on

    // previous responses

    /**
     * Checks if the given DOM node is a Page Item or not based on the question
     * type.
     * 
     * @param n
     *            DOM Node whose validated is checked.
     * @return boolean True if the Node is one of defined type else false.
     */
    public static boolean IsPageItemNode(Node n) {
        String nname = null;
        boolean answer = false;
        try {
            nname = n.getNodeName();
            if (nname != null) {
                answer = nname.equalsIgnoreCase("Open_Question") || nname.equalsIgnoreCase("Closed_Question")
                        || nname.equalsIgnoreCase("Question_Block") || nname.equalsIgnoreCase("Directive")
                        || nname.equalsIgnoreCase("Repeating_Item_Set");
            }
        } catch (NullPointerException e) {
            LOGGER.error("PAGE ITEM test attempt failed for " + n + ": " + e, null);
        }
        return answer;
    }

    /**
     * CLASS HIERARCHY is triaged here - Edit this static function to add new
     * subclasses.
     * 
     * @param n
     *            DOM Node whose hierarchy is checked.
     * @return PageItem Appropriate Item is returned based on the type of
     *         question.
     */
    public static PageItem MakeNewItem(Node n) {
        String nname = null;
        PageItem item = null;
        try {
            nname = n.getNodeName();
            if (nname.equalsIgnoreCase("Open_Question")) {
                NodeList nodelist2 = n.getChildNodes();
                for (int j = 0; j < nodelist2.getLength(); j++) {
                    if (nodelist2.item(j).getNodeName().equalsIgnoreCase("Numeric_Open_Response")) {
                        item = new NumericOpenQuestion(n);
                    } else if (nodelist2.item(j).getNodeName().equalsIgnoreCase("Text_Open_Response")) {
                        item = new TextOpenQuestion(n);
                    }
                }
            } else if (nname.equalsIgnoreCase("Closed_Question")) {
                item = new ClosedQuestion(n);
            } else if (nname.equalsIgnoreCase("Question_Block")) {
                NodeList nodelist2 = n.getChildNodes();
                for (int j = 0; j < nodelist2.getLength(); j++) {
                    if (nodelist2.item(j).getNodeName().equalsIgnoreCase("Subject_Set_Ref")) {
                        item = new QuestionBlockforSubjectSet(n);
                    }
                }
                item = new QuestionBlock(n);
            } else if (nname.equalsIgnoreCase("Directive")) {
                item = new Directive(n);
            } else if (nname.equalsIgnoreCase("Repeating_Item_Set")) {
                item = new RepeatingItemSet(n);
            } else {
                LOGGER.error("PAGE ITEM Creation attempt failed for " + nname, null);
            }
        } catch (NullPointerException e) {
            LOGGER.error("PAGE ITEM Creation attempt failed for " + nname + ": " + e, null);
        }
        return item;
    }

    /**
     * Constructor: root constructor for page items - parse values common to all
     * from the DOM node.
     * 
     * @param n
     *            DOM node from where the page item is populated.
     */
    public PageItem(Node n) {
        try {

            /* name - page item's ID */
            Node node = n.getAttributes().getNamedItem("Name");
            if (node != null) {
                this.name = node.getNodeValue().toUpperCase();
            }

            /* item_type - page item's type */
            this.itemType = n.getNodeName();

            /* parse the precondition */
            NodeList nodelist = n.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Precondition")) {
                    // hasPrecondition = true;
                    // create the condition object
                    this.cond = new Condition(nodelist.item(i));
                }
            }
        } catch (DOMException e) {
            LOGGER.error("PAGE ITEM ROOT CONSTRUCTOR: " + e, null);
            return;
        } catch (NullPointerException e) {
            LOGGER.error("PAGE ITEM ROOT CONSTRUCTOR: " + e, null);
            return;
        }
    }

    /**
     * Throws exception, subclasses have to override this method.
     * 
     * @param mySurvey
     *            Current running survey.
     */
    public void knitRefs(Survey mySurvey) {
        try {
            throw new Exception("knitRefs called on " + this.itemType + " " + this.name);
        } catch (Exception e) {
            LOGGER.error("Unimplemented PageItem method: " + e, null);
        }
    }

    /**
     * Throws exception when called, subclasses have to override this method.
     * 
     * @param String
     *            [] Return null always.
     */
    public String[] listFieldNames() {
        try {
            throw new Exception("listFieldNames() called on " + this.itemType + " " + this.name);
        } catch (Exception e) {
            LOGGER.error("Unimplemented Page_item method: " + e, null);
        }
        return null;
    }

    /**
     * Default item stores value as an integer; override necessary only for
     * other value types.
     * 
     * @return char The flag type is returned.
     */
    public char getValueType() {
        return DBConstants.intValueTypeFlag;
    }

    /**
     * Returns boolean if field is required but default is always false - will
     * be overwritten by subclass
     * 
     * @return boolean Always returns false.
     */
    public boolean isRequired() {
        return false;
    }

    /** stub function which is overwritten by subclasses */
    // public String render_results(Hashtable data, String whereclause) {
    // return "";
    // }

    /**
     * Returns the results of survey in form of string -- be overwritten by
     * subclass.
     * 
     * @param pg
     *            Page Object for which the results are to be rendered.
     * @param db
     *            Data Bank object to connect to the database.
     * @param whereclause
     *            whereclause to restrict the invitee selection.
     * @param data
     *            Hashtable which contains results.
     * @return String Empty string is returned.
     */
    public String renderResults(Page pg, DataBank db, String whereclause, Hashtable data) {
        return "";
    }

    /**
     * Prints of survey in form of string -- which is overwritten by subclasses
     * 
     * @return String Empty string is returned.
     */
    public String printSurvey() {
        return "";
    }

    /**
     * Returns the stem if the field is required and not filled out - will be
     * overwritten by Question class.
     * 
     * @return String Empty string is returned.
     */
    public String getRequiredStem() {
        return "";
    }

    /**
     * stub function which is overwritten by subclasses.
     * 
     * @return Hashtable Null is returned.
     */
    public Hashtable<String, String> readForm(Hashtable<String, String> params) {
        try {
            throw new Exception("read_form called on " + this.itemType + " " + this.name);
        } catch (Exception e) {
            LOGGER.error("Unimplemented Page_item method: " + e, null);
        }
        return null;
    }

    /**
     * Returns the instance variable.
     * 
     * @return String instance html variable.
     * 
     */
    public String renderForm() {
        return this.html;
    }

    /**
     * Condition-checking for all item-types initiated here (all same).
     * 
     * @param User
     *            The user whose pre-conditions are checked.
     * @param elementNumber
     *            Element number of the question.
     * @return String Html of the PageItem.
     */
    public String renderForm(UserAnswers theUser, int elementNumber) {

        /*
         * if (cond != null) { // check if the value of data meets the
         * precondition boolean write_question = cond.check_condition(theUser);
         * // if it doesn't meet the precondition, skip writing this question //
         * by return an empty string if (!write_question) return ""; } return
         * html;
         */
        StringBuffer pageItemHtml = new StringBuffer("");
        if (this.cond != null) {
            pageItemHtml.append("<script>");
            pageItemHtml.append("page_function_array[\"q" + elementNumber + "\"]");
            pageItemHtml.append("= function " + "q" + elementNumber + "(A)");
            pageItemHtml.append("{");
            pageItemHtml.append("return");

            pageItemHtml.append(this.cond.getJsExpression().toUpperCase());

            pageItemHtml.append(";");
            pageItemHtml.append("};");
            pageItemHtml.append("</script>");
        }
        pageItemHtml.append("<div ");
        pageItemHtml.append("id=\"q" + elementNumber + "\"");
        if (this.cond != null) {

            /* check if the value of data meets the precondition */
            boolean writeQuestion = this.cond.checkCondition(theUser);
            /*
             * if it doesn't meet the precondition, skip writing this question
             * by return an empty string
             */
            if (!writeQuestion) {
                pageItemHtml.append(" style=\"display:none\" ");
            }
        }
        pageItemHtml.append(">");
        pageItemHtml.append(this.html);
        pageItemHtml.append("</div>");
        return pageItemHtml.toString();
    }

    /***************************************************************/

    /** stub function which is overwritten by subclasses */
    // public int read_form(Hashtable params, String[] fieldNames, String[]
    // fieldValues, int fieldIndex)
    // {
    // return 0;
    // }
    // /** stub function which is overwritten by subclass - question block */
    // public int read_form(Hashtable params, String[] fieldNames, String[]
    // fieldValues, int fieldIndex, User theUser)
    // {
    // return 0;
    // }

    /**
     * stub function which is overwritten by subclasses.
     * 
     * @return int number of fields in the page item.
     */
    public abstract int countFields();

    /**
     * Prints out the name of the item
     * 
     * @return String Name of the page item.
     */
    @Override
    public String toString() {
        String s = "Name: " + this.name + "<br>";
        return s;
    }

}
