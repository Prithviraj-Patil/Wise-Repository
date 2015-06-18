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
 * This class is a subclass of Open_Question and represents an text open ended
 * question on the page.
 */
public class TextOpenQuestion extends OpenQuestion {
    public static final Logger LOGGER = Logger.getLogger(TextOpenQuestion.class);
    /** Instance Variables */
    public String maxSize;
    public String multiLine;
    public String width;
    public String height;

    /**
     * Constructor for creating a text open question from XML
     * 
     * @param n
     *            Node from the XML that has to be parsed to get the information
     *            about text open ended question.
     */
    public TextOpenQuestion(Node n) {

        /* get the attributes for open question */
        super(n);
        try {
            NodeList nodelist = n.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                Node nodeTOR = nodelist.item(i);
                if (nodeTOR.getNodeName().equalsIgnoreCase("Text_Open_Response")) {

                    /* assign various attributes */
                    this.maxSize = nodeTOR.getAttributes().getNamedItem("MaxSize").getNodeValue();
                    this.multiLine = nodeTOR.getAttributes().getNamedItem("MultiLine").getNodeValue();
                    Node node = nodeTOR.getAttributes().getNamedItem("Width");
                    if (node != null) {
                        this.width = node.getNodeValue();
                    } else {
                        this.width = this.maxSize;
                    }
                    node = nodeTOR.getAttributes().getNamedItem("Height");
                    if (node != null) {
                        this.height = node.getNodeValue();
                    } else {
                        this.height = "1";
                    }
                    // NodeList nodeL = nodeTOR.getChildNodes();
                }
            }

        } catch (DOMException e) {
            LOGGER.error("WISE - TEXT OPEN QUESTION: " + e.toString(), null);
            return;
        }
    }

    @Override
    public char getValueType() {
        return DBConstants.textValueTypeFlag;
    }

    /**
     * Renders form text field part of open question
     * 
     * @return String Rendered form text feild.
     */
    @Override
    public String formFieldHtml() {
        String s = "";

        /*
         * display the form field start from a new line if it is not requested
         * by one-line layout.
         */
        if (this.multiLine.equals("false")) {
            s += "<input type='text' name='" + this.name.toUpperCase() + "' maxlength='" + this.maxSize + "' size='"
                    + this.width + "' >";
        } else {
            s += "<textarea name='" + this.name.toUpperCase() + "' cols='" + this.width + "' rows='" + this.height
                    + "' onchange='SizeCheck(this," + this.maxSize + ");'></textarea>";
        }
        return s;
    }

    /**
     * Prints the text open question - used for admin tool: print survey
     * 
     * @return String The text open question in printable format.
     */
    @Override
    public String printSurvey() {
        StringBuffer sb = new StringBuffer("");

        /* WISEDEV-8: adding following html to render question block correctly */
        sb.append("\n<table cellspacing='0' width='100%' cellpadding='0' border='0'><tr>\n<td>"
                + "<table cellspacing='0' width='100%' cellpadding='0' border='0'><tr><td>");

        /* display the question stem */
        // sb.append("<p style=\"margin-left:10em;\">");
        sb.append(super.makeStemHtml());
        // sb.append("</p");
        sb.append("</table");
        sb.append("</td>\n");
        sb.append("\n<table cellspacing='0' width='100%' cellpadding='0' border='0'><tr>\n<td>"
                + "<table cellspacing='0' width='100%' cellpadding='0' border='0'><tr><td>");

        /* start from a new line if it is not requested by one-line layout */
        if (!this.oneLine) {
            sb.append("<td width=570>");
        }
        if (this.multiLine.equals("false")) {
            int width_plus = Integer.parseInt(this.width) + 20;
            sb.append("<table cellpadding=0 cellspacing=0 border=1><tr>");
            sb.append("<td width=" + (width_plus * 8 * 80) + " height=15 align=center>");
            sb.append("</td></tr></table>");
        } else {
            for (int j = 0; j < Integer.parseInt(this.height); j++) {

                /* print the underline field to fill the answer above */
                for (int i = 0; i < (Integer.parseInt(this.width) + 20); i++) {
                    sb.append("_");
                }
                sb.append("<br>");
            }
        }
        // sb.append("</p>");
        sb.append("</table");
        sb.append("</td>\n");
        sb.append("</td>");
        sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }

    @Override
    public String renderResults(Page page, DataBank db, String whereclause, Hashtable data) {
        String s = "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
        s += "<tr>";
        s += "<td colspan=2 align=right>";
        s += "&nbsp;&nbsp;<span class='itemID'><i>" + this.name + "</i></span>";
        s += "</td></tr><tr>";
        s += "<td width='2%'>&nbsp;</td>";
        s += "<td colspan='4'><font color=green>" + this.stem + "</font>&nbsp;&nbsp;&nbsp;&nbsp;";

        /* add a link to view the answer */
        s += "<a href='view_open_results?q=" + this.name + "' >";
        s += "<img src='" + "imageRender?img=go_view.gif' border=0></a>";
        s += "</td></tr>";
        s += "<tr>";
        s += "<td>&nbsp;</td>";
        s += "</tr>";
        s += "</table>";
        return s;
    }

    /**
     * read out the question field name & value from the hashtable and put them
     * into two arrays respectively
     */
    // public int read_form(Hashtable params, String[] fieldNames, String[]
    // fieldValues, int fieldIndex)
    // {
    // fieldNames[fieldIndex] = name.toUpperCase();
    // String s = (String) params.get(name.toUpperCase());
    // //if the string has a single quote, fix it with double quote
    // s = Study_Util.fixquotes(s);
    // fieldValues[fieldIndex] = "'"+s+"'";
    // if ( (fieldValues[fieldIndex] == null) ||
    // (fieldValues[fieldIndex].equalsIgnoreCase("")) )
    // fieldValues[fieldIndex] = "'null'";
    // fieldIndex++;
    // return 1;
    // }

    /** print information about a text open question */
    /*
     * public String print() { String s = "TEXT OPEN QUESTION<br>"; s +=
     * super.print(); s += "MaxSize: "+maxSize+"<br>"; s +=
     * "MultiLine: "+multiLine+"<br>"; s += "Width: "+width+"<br>"; s +=
     * "Height: "+height+"<br>"; s += "<p>"; return s; }
     */
}
