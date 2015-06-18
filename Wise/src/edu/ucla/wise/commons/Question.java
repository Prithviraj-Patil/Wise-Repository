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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a subclass of PageItem and represents a question on the page.
 */
public class Question extends PageItem {
    public static final Logger LOGGER = Logger.getLogger(Question.class);
    /** Instance Variables */
    public String stem;
    public String requiredField;
    public boolean oneLine;

    /**
     * Constructor: To fill out the stem of the question and parse the
     * precondition.
     * 
     * @param n
     *            DOM node for the question.
     */
    public Question(Node n) {

        /* get the attributes of page item */
        super(n);
        try {

            /* if there is a translation node, display the translated stem */
            if (this.translationId != null) {
                this.stem = this.questionTranslated.stem;
            } else {

                /* otherwise, display the stem transformed through jaxp parser */
                NodeList nodelist = n.getChildNodes();
                for (int i = 0; i < nodelist.getLength(); i++) {
                    if (nodelist.item(i).getNodeName().equalsIgnoreCase("Stem")) {
                        Node node = nodelist.item(i);
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        StringWriter sw = new StringWriter();
                        transformer.transform(new DOMSource(node), new StreamResult(sw));
                        this.stem = sw.toString();
                    }
                }
            }
            // //parse the precondition
            // NodeList nodelist = n.getChildNodes();
            // for (int i=0; i < nodelist.getLength();i++)
            // {
            // if
            // (nodelist.item(i).getNodeName().equalsIgnoreCase("Precondition"))
            // {
            // //hasPrecondition = true;
            // //create the condition object
            // cond = new Condition(nodelist.item(i));
            // }
            // }

            /* assign other attributes */
            NamedNodeMap nnm = n.getAttributes();

            /* if the question has the required field to fillup */
            Node n1 = nnm.getNamedItem("requiredField");
            if (n1 != null) {
                this.requiredField = n1.getNodeValue();
            } else {
                this.requiredField = "false";
            }

            /* if the question requests one-line presence/layout */
            n1 = nnm.getNamedItem("oneLine");
            if (n1 != null) {
                this.oneLine = new Boolean(n1.getNodeValue()).booleanValue();
            } else {
                this.oneLine = false;
            }
        } catch (TransformerException e) {
            LOGGER.error("WISE - QUESTION: " + e.toString(), null);
            return;
        } catch (TransformerFactoryConfigurationError e) {
            LOGGER.error("WISE - QUESTION: " + e.toString(), null);
            return;
        } catch (DOMException e) {
            LOGGER.error("WISE - QUESTION: " + e.toString(), null);
            return;
        }
    }

    /**
     * Default field count for question
     * 
     * @return int returns one always which is considered as default.
     */
    @Override
    public int countFields() {
        return 1;
    }

    /**
     * Default field names for question
     * 
     * @return Array return array with one element as "name" which is considered
     *         as default.
     */
    @Override
    public String[] listFieldNames() {

        /* default is to wrap item name in an array */
        String[] fieldNames = new String[1];
        fieldNames[0] = this.name;
        return fieldNames;
    }

    @Override
    public boolean isRequired() {
        if (this.requiredField.equalsIgnoreCase("true")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the stem for indication purpose if the required field is not
     * filled out.
     * 
     * @return String Required field value.
     */
    @Override
    public String getRequiredStem() {

        /*
         * assign value "A" to the unfilled required field to let the JavaScript
         * distinguish
         */
        String s = "A";
        return s;
    }

    /**
     * Gets the average results of this question from survey data table
     * 
     * @param page
     *            Page in the survey which contains this question.
     * @param whereclause
     *            The invitee's whose responses are to be picked up for average
     *            calculation.
     * @return float Average of the response.
     */
    public float getAvg(Page page, String whereclause) {

        return page.getSurvey().getDB().getAvgForQuestion(page, this.name, whereclause);

    }

    /**
     * Returns table row for the question stem (partial, or complete if not
     * oneLine).
     * 
     * @return String HTML form of the question.
     */
    public String makeStemHtml() {
        String s = "<tr><td width=10>&nbsp;</td>";

        /*
         * display the question start from a new line if it is not requested by
         * one-line layout
         */
        if (this.requiredField.equalsIgnoreCase("true")) {
            if (!this.oneLine) {
                s += "<td colspan='2'>" + this.stem + " <b>(required)</b></td></tr>";
            } else {
                s += "<td align=left>" + this.stem + " <b>(required)</b>";
            }
        } else {
            if (!this.oneLine) {
                s += "<td colspan='2'>" + this.stem + "</td></tr>";
            } else {
                s += "<td align=left>" + this.stem;
            }
        }

        return s;
    }

    /**
     * renders the question form by printing the stem - used for admin tool:
     * view survey
     */
    // public String render_form()
    // {
    //
    // String s =
    // "<table cellspacing='0' cellpadding='0' width=600' border='0'>";
    // s += "<tr>";
    // s += "<td width=10>&nbsp;</td>";
    // //display the question stem
    // //start from a new line if it is not requested by one-line layout
    // if (requiredField.equalsIgnoreCase("true"))
    // {
    // if(!oneLine)
    // s += "<td colspan='2'>"+stem+" <b>(required)</b>";
    // else
    // s += "<td align=left>"+stem+" <b>(required)</b>";
    // }
    // else
    // {
    // if(!oneLine)
    // s += "<td colspan='2'>"+stem;
    // else
    // s += "<td align=left>"+stem;
    // }
    // if(!oneLine)
    // s += "</td></tr>";
    // return s;
    // }

    /**
     * Prints out the information about a question.
     * 
     * @return String Information to be printed.
     */
    @Override
    public String toString() {
        String s = super.toString();
        s += "Stem: " + this.stem + "<br>";
        s += "Required: " + this.isRequired() + "<br>";
        if (this.cond != null) {
            s += this.cond.toString();
        }
        return s;
    }
}
