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

import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents a set of questions that can repeat based on user input.
 * For example adding answers to the question: "Tell us about different schools
 * that you attended".
 */
public class RepeatingItemSet extends PageItem {
    public static final Logger LOGGER = Logger.getLogger(RepeatingItemSet.class);

    /*
     * Instance Variables
     */
    public String id; // Not sure why, but is there in Subject_set
    public String title;
    public ArrayList<PageItem> itemSet = new ArrayList<PageItem>();
    public Condition preCondition;
    public ArrayList<String> itemSetAsXml = new ArrayList<String>();

    /**
     * Parses a Repeating Question set from survey file and constructs the
     * RepeatingItemSet Object
     * 
     * @param iNode
     *            XML node from which all the details are obtained.
     */
    public RepeatingItemSet(Node iNode) {

        super(iNode);// Avoiding the "Implicit super constructor error"
        try {
            this.id = iNode.getAttributes().getNamedItem("ID").getNodeValue();
            NodeList nodeList = iNode.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node childNode = nodeList.item(i);

                // Check if its a page_item. Else it might be a precondition
                if (PageItem.IsPageItemNode(childNode)) {
                    PageItem currentItem = PageItem.MakeNewItem(childNode);
                    if (currentItem == null) {
                        throw new NullPointerException("Null item parse at " + i);
                    }

                    this.itemSet.add(currentItem);
                } else {
                    if (childNode.getNodeName().equalsIgnoreCase("Precondition")) {
                        this.preCondition = new Condition(childNode);
                    }
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error("WISE - survey parse failure at Repeating Item Set [" + this.id + "] " + e.toString() + "\n"
                    + this.toString(), null);
            return;
        }
    }

    /**
     * Renders the repeating item set question as HTML this used while the
     * survey is being displayed for the user.
     * 
     * @param iUser
     *            The user for whom this question is being displayed.
     * @param itemIndex
     *            Used for the assigning the ID for the repeating Item set
     *            question in the html.
     * @return
     */
    public String renderRepeatingItemSet(UserAnswers iUser, int itemIndex) {
        StringBuffer htmlContent = new StringBuffer("");

        if (this.preCondition != null) {
            htmlContent.append("<script>");
            htmlContent.append("page_function_array[\"q" + itemIndex + "\"]");
            htmlContent.append("= function " + "q" + itemIndex + "(A)");
            htmlContent.append("{");
            htmlContent.append("return");

            htmlContent.append(this.cond.getJsExpression().toUpperCase());
            htmlContent.append(";");
            htmlContent.append("};");
            htmlContent.append("</script>");
        }

        // html_content.append(get_javascript_html());
        htmlContent.append("<div id=q" + itemIndex + " class='repeating_item_set'");
        if (this.preCondition != null) {

            /* check if the value of data meets the precondition */
            boolean writeQuestion = this.cond.checkCondition(iUser);

            /*
             * if it doesn't meet the precondition, skip writing this question
             * by return an empty string
             */
            if (!writeQuestion) {
                htmlContent.append(" style=\"display:none\" ");
            }

        }
        htmlContent.append('>');
        htmlContent.append("<div id='repeating_set_with_id_" + this.getNameForRepeatingSet() + "'>");

        htmlContent.append("<div style='display: block;'>");
        htmlContent.append("<input type='text' class='repeat_item_name span3' placeholder='Enter short name for a "
                + this.getNameForRepeatingSet() + "' />");
        htmlContent.append("<a href='#' class='add_repeat_instance_name_button btn btn-primary btn-medium'>Add</a>");
        htmlContent.append("</div>");
        htmlContent.append("<div class='add_item_to_repeating_set' style='display:none'>");
        for (int i = 0; i < this.itemSet.size(); i++) {
            htmlContent.append(this.itemSet.get(i).renderForm(iUser, (100 * itemIndex) + i));// 100
                                                                                             // is
                                                                                             // multiplied
                                                                                             // to
                                                                                             // get
                                                                                             // diff
                                                                                             // div
            // id, not good
        }
        htmlContent.append("<div class='wrapper_for_add_cancel' style='display:block;text-align:center'>");
        htmlContent.append("<a href='#' class='add_repeat_item_save_button'>Add</a>");
        htmlContent.append("<a href='#' class='add_repeat_item_cancel_button'>Cancel</a>");
        htmlContent.append("</div>");
        htmlContent.append("</div>");
        htmlContent.append("<div class = 'repeating_question' Name=" + this.getNameForRepeatingSet() + ">");
        htmlContent.append("</div>");
        htmlContent.append("</div>");
        htmlContent.append("</div>");
        return htmlContent.toString();
    }

    @Override
    public int countFields() {
        int fieldCount = 0;
        for (PageItem repeatingItem : this.itemSet) {
            fieldCount += repeatingItem.countFields();
        }
        return fieldCount;
    }

    @Override
    public char getValueType() {
        return 'z'; // arbitrary string to satisfy the caller. Do something
        // about this!!!
    }

    @Override
    public String renderForm(UserAnswers iUser, int itemIndex) {
        return this.renderRepeatingItemSet(iUser, itemIndex);
    }

    @Override
    public void knitRefs(Survey iSurvey) {
        try {
            for (PageItem repeatingItem : this.itemSet) {
                repeatingItem.knitRefs(iSurvey);
            }
        } catch (Exception e) {
            // DO something with the exception Pralav
        }
    }

    /**
     * Returns all the field names that are present in the Repeating Item set,
     * this is used by data bank class to create the repeating set table.
     * 
     * @return Array Array of string that contains all the field name for a
     *         repeating item set question.
     */
    @Override
    public String[] listFieldNames() {
        ArrayList<String> fieldNames = new ArrayList<String>();

        for (PageItem repeatingItem : this.itemSet) {
            String[] itemFieldNames = repeatingItem.listFieldNames();
            Collections.addAll(fieldNames, itemFieldNames);
        }

        ArrayList<String> returnFieldNames = new ArrayList<String>();

        for (String fieldName : fieldNames) {
            returnFieldNames.add("repeat_" + fieldName);
        }

        String[] fieldNamesArray = new String[returnFieldNames.size()];

        for (int i = 0; i < fieldNamesArray.length; i++) {
            fieldNamesArray[i] = returnFieldNames.get(i);
        }
        return fieldNamesArray;

    }

    /**
     * Returns the name of the repeating item set question.
     * 
     * @return String Name of the repeating item set.
     */
    public String getNameForRepeatingSet() {
        return this.id;
    }

    /**
     * Returns the type of values for the repeating item sets.
     * 
     * @return Array Array of the types.
     */
    public char[] getValueTypeList() {
        ArrayList<Character> valueList = new ArrayList<Character>();
        for (PageItem repeatingItem : this.itemSet) {
            String[] itemFieldNames = repeatingItem.listFieldNames();
            for (int i = 0; i < itemFieldNames.length; i++) {
                valueList.add(repeatingItem.getValueType());
            }
        }

        char[] valueListArray = new char[valueList.size()];

        for (int i = 0; i < valueListArray.length; i++) {
            valueListArray[i] = valueList.get(i).charValue();
        }

        return valueListArray;
    }

    /* static methods follow */
    /**
     * Calls the constructor for creating a new repeating item set object.
     * 
     * @param n
     *            XML node from the survey for the repeating item set.
     * @return RepeatingItemSet Newly created repeating item set of the XML node
     *         is returned
     */
    public static RepeatingItemSet MakeNewItem(Node n) {
        String nname = null;
        RepeatingItemSet repeatingItem = null;
        try {
            nname = n.getNodeName();
            if (nname.equalsIgnoreCase("Repeating_Item_Set")) {
                repeatingItem = new RepeatingItemSet(n);
            }
        } catch (NullPointerException e) {
            LOGGER.error("PAGE ITEM Creation attempt failed for " + nname + ": " + e, null);
        }
        return repeatingItem;
    }

    /**
     * Returns of the DOM node passed corresponds to a repeating item set or
     * not.
     * 
     * @param n
     *            DOM node from the survey to check for the repeating item set.
     * @return boolean If the DOM node is repeating item set or not..
     */
    public static boolean IsRepeatingItemSetNode(Node n) {
        String nname = null;
        boolean answer = false;
        nname = n.getNodeName();
        if (nname != null) {
            answer = nname.equalsIgnoreCase("Repeating_Item_Set");
        }
        return answer;
    }

}
