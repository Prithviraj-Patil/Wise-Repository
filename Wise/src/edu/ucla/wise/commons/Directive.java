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
import java.util.Hashtable;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a subclass of Page_Item and represents a directive object on
 * the page.
 */

public class Directive extends PageItem {
    /** Instance Variables */
    public String text;
    public boolean hasPrecondition = false;
    public Condition cond;

    /**
     * constructor: parse a directive node from XML
     * 
     * @param n
     *            XML node from which directive object information is obtained.
     * 
     */
    public Directive(Node n) {

        /* parse the page item properties */
        super(n);
        try {

            /* convert to the translated question stem */
            if (this.translationId != null) {
                this.text = this.questionTranslated.text;
            } else {
                Node node = n;
                NodeList childNodes = node.getChildNodes();

                for (int i = 0; i < childNodes.getLength(); i++) {

                    Node childNode = childNodes.item(i);
                    if (childNode.getNodeName().equalsIgnoreCase("Precondition")) {
                        node.removeChild(childNode);
                    }
                }

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                StringWriter sw = new StringWriter();
                transformer.transform(new DOMSource(node), new StreamResult(sw));
                this.text = sw.toString();
            }

            /* parse the precondition */
            NodeList nodelist = n.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Precondition")) {
                    this.hasPrecondition = true;

                    /* create the condition object */
                    this.cond = new Condition(nodelist.item(i));
                }
            }
        } catch (DOMException e) {
            LOGGER.error("WISE - DIRECTIVE: " + e.toString(), null);
            return;
        } catch (TransformerConfigurationException e) {
            LOGGER.error("WISE - DIRECTIVE: " + e.toString(), null);
            return;
        } catch (TransformerException e) {
            LOGGER.error("WISE - DIRECTIVE: " + e.toString(), null);
            return;
        }

    }

    @Override
    public int countFields() {
        return 0;
    }

    @Override
    public void knitRefs(Survey mySurvey) {
        this.html = this.makeHtml();
    }

    @Override
    public String[] listFieldNames() {
        return new String[0];
    }

    /**
     * Renders form for directive item.
     * 
     * @return String HTML format.
     */
    public String makeHtml() {
        String s = "";
        s += "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
        s += "<tr>";
        s += "<td><font face='Verdana, Arial, Helvetica, sans-serif' size='-1'>" + this.text + "</font></td>";
        s += "</tr>";
        s += "</table>";
        return s;
    }

    /**
     * print survey for directive item - used for admin tool: print survey
     * 
     * @return String HTML format.
     */
    @Override
    public String printSurvey() {
        String s = "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
        s += "<tr>";
        s += "<td>" + this.text + "</td>";
        s += "</tr>";
        s += "</table>";
        return s;
    }

    /**
     * Render results for directive item.
     * 
     * @param data
     *            Hashtable which contains results.
     * @param whereclause
     *            whereclause to restrict the invitee selection.
     * @return String HTML format of the results is returned.
     */
    @SuppressWarnings("rawtypes")
    public String renderResults(Hashtable data, String whereclause) {
        String s = "<table cellspacing='0' cellpadding='0' width=100%' border='0'>";
        s += "<tr>";
        s += "<td><i>" + this.text + "</i></td>";
        s += "</tr>";
        s += "</table>";
        return s;
    }

    /** print information about a directive item */
    /*
     * public String print() { String s = "DIRECTIVE<br>"; s += super.print(); s
     * += "Text: "+text+"<br>"; s += "<p>"; return s; }
     */
}
