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

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a skip list set and all its properties
 * 
 */
public class SkipList {

    public static final Logger LOGGER = Logger.getLogger(SkipList.class);

    /** Instance Variables */
    public String[] values;
    public String[] pages;
    public ClosedQuestion question;

    /**
     * Constructor: parse a skip list node from the XML
     * 
     * @param n
     *            XML node that has to be parsed to get the details of the skip.
     * @param cq
     *            parent of the node that is passed.
     */
    public SkipList(Node n, ClosedQuestion cq) {
        try {

            /* assign its parent node - the closed question */
            this.question = cq;
            NodeList nodelist = n.getChildNodes();
            this.values = new String[nodelist.getLength()];
            this.pages = new String[nodelist.getLength()];
            for (int i = 0; i < nodelist.getLength(); i++) {
                NamedNodeMap nnm1 = nodelist.item(i).getAttributes();
                Node n2 = nnm1.getNamedItem("Value");
                this.values[i] = n2.getNodeValue();
                n2 = nnm1.getNamedItem("Page");
                this.pages[i] = n2.getNodeValue();
            }
        } catch (DOMException e) {
            LOGGER.error("WISE - SKIP LIST CONSTRUCTOR: " + e.toString(), null);
            return;
        }
    }

    /**
     * when render the closed question, directly skip to a target defined in the
     * skip list
     * 
     * @param value
     *            The value is the option index or its value in closed question
     * @return String Page to which the survey has to be skipped to or it is
     *         empty in case of the page is DONE
     */
    public String renderFormElement(int value) {

        String v = String.valueOf(value);
        String target = "DONE";
        for (int i = 0; i < this.values.length; i++) {

            /*
             * if the option value is a value set in the skip list, assign the
             * page ID then after submission, the survey will skip to that page
             * directly by using JavaScript
             */
            if (this.values[i].equalsIgnoreCase(v)) {
                target = this.pages[i];
                break;
            }
        }
        String element = "onClick=\"PageSkip('" + target + "');\"";
        if (target.equalsIgnoreCase("DONE")) {
            element = "";
        }
        return element;
    }

    /**
     * when render the closed question, directly skip to a target defined in the
     * skip list
     * 
     * @param value
     *            The value is the option index or its value in closed question
     * @return String Page to which the survey has to be skipped to or it is
     *         empty in case of the page is DONE
     */
    public String renderFormElement(String value) {
        String v = value;
        String target = "DONE";
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i].equalsIgnoreCase(v)) {
                target = this.pages[i];
                break;
            }
        }
        String element = "onClick=\"PageSkip('" + target + "');\"";
        if (target.equalsIgnoreCase("DONE")) {
            element = "";
        }
        return element;
    }

    /**
     * Renders the form element to skip to a target
     * 
     * @param value
     *            The value is the option index or its value in closed question.
     * @return String The Font of the skip list or empty if the page is already
     *         Done.
     */
    public String renderIdentifier(int value) {
        String v = String.valueOf(value);
        String target = "DONE";
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i].equalsIgnoreCase(v)) {
                target = this.pages[i];
                break;
            }
        }
        String element = "<FONT FACE='Wingdings'>&egrave;</FONT>";
        if (target.equalsIgnoreCase("DONE")) {
            element = "";
        }
        return element;
    }

    /**
     * Renders the form element to skip to a target
     * 
     * @param value
     *            The value is the option index or its value in closed question.
     * @return String The Font of the skip list or empty if the page is already
     *         Done.
     */
    public String renderIdentifier(String value) {
        String v = value;
        String target = "DONE";
        for (int i = 0; i < this.values.length; i++) {
            if (this.values[i].equalsIgnoreCase(v)) {
                target = this.pages[i];
                break;
            }
        }
        String element = "<FONT FACE='Wingdings'>&egrave;</FONT>";
        if (target.equalsIgnoreCase("DONE")) {
            element = "";
        }
        return element;
    }

    /**
     * Returns the number of targets in a skip list
     * 
     * @return int Number of targets.
     */
    public int getSize() {
        return this.values.length;
    }

    /** prints out a skip_list */
    /*
     * public String print() { String s = "SKIP LIST<br>"; s += "Targets: <br>";
     * for (int i = 0; i < values.length; i++) s +=
     * values[i]+":"+pages[i]+"<br>"; s += "<p>"; return s; }
     */

}
