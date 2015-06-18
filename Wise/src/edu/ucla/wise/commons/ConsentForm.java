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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains a consent form object set in the preface.
 */

public class ConsentForm {
    public static final Logger LOGGER = Logger.getLogger(ConsentForm.class);
    /** Instance Variables */
    public String id;
    public String irbId;
    public String title;
    public String subTitle;
    public String surveyId;
    public String consentHeaderHtml = "", consentP = "", consentUl = "", consentS = "";

    public Preface preface;

    /**
     * Constructor: parse a Consent form node from XML
     * 
     * @param n
     *            XML DOM node for consent form.
     * @param p
     *            Preface object to which this object has to be linked to.
     */
    public ConsentForm(Node n, Preface p) {
        try {
            this.preface = p;

            /* assign id, irb id & survey id (required) */
            this.id = n.getAttributes().getNamedItem("ID").getNodeValue();
            this.irbId = n.getAttributes().getNamedItem("IRB_ID").getNodeValue();
            this.surveyId = n.getAttributes().getNamedItem("Survey_ID").getNodeValue();

            /* assign various attributes */
            Node nodeChild = n.getAttributes().getNamedItem("Title");
            if (nodeChild != null) {
                this.title = nodeChild.getNodeValue();
            } else {
                this.title = "";
            }
            nodeChild = n.getAttributes().getNamedItem("Sub_Title");
            if (nodeChild != null) {
                this.subTitle = nodeChild.getNodeValue();
            } else {
                this.subTitle = "";
            }
            NodeList nodeP = n.getChildNodes();
            for (int j = 0; j < nodeP.getLength(); j++) {
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("p")) {
                    this.consentP += "<p>" + nodeP.item(j).getFirstChild().getNodeValue() + "</p>";
                }
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("s")) {
                    this.consentS += "<p>" + nodeP.item(j).getFirstChild().getNodeValue() + "</p>";
                }
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("html_header")) {
                    NodeList nodeN = nodeP.item(j).getChildNodes();
                    for (int k = 0; k < nodeN.getLength(); k++) {
                        if (nodeN.item(k).getNodeName().equalsIgnoreCase("#cdata-section")) {
                            this.consentHeaderHtml += nodeN.item(k).getNodeValue();
                        }
                    }
                }
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("bullets")) {
                    this.consentUl += "<ul>";
                    NodeList nodeB = nodeP.item(j).getChildNodes();
                    for (int k = 0; k < nodeB.getLength(); k++) {
                        if (nodeB.item(k).getNodeName().equalsIgnoreCase("bullet_item")) {
                            NodeList nodeC = nodeB.item(k).getChildNodes();
                            this.consentUl += "<li>";
                            for (int t = 0; t < nodeC.getLength(); t++) {
                                if (nodeC.item(t).getNodeName().equalsIgnoreCase("item_subject")) {
                                    this.consentUl += "<b>" + nodeC.item(t).getFirstChild().getNodeValue() + "</b><br>";
                                }
                                if (nodeC.item(t).getNodeName().equalsIgnoreCase("item_content")) {
                                    this.consentUl += nodeC.item(t).getFirstChild().getNodeValue();
                                    NodeList nodeU = nodeC.item(t).getChildNodes();
                                    for (int tt = 0; tt < nodeU.getLength(); tt++) {
                                        if (nodeU.item(tt).getNodeName().equalsIgnoreCase("#cdata-section")) {
                                            this.consentUl += nodeU.item(tt).getNodeValue();
                                        }
                                    }
                                    this.consentUl += "<br>";
                                }
                            }
                            this.consentUl += "<br>";
                        }
                    }
                    this.consentUl += "</ul><br>";
                }
            }
        } catch (DOMException e) {
            LOGGER.error(
                    "WISE - CONSENT FORM : ID = " + this.id + "; Preface = " + p.projectName + " --> " + e.toString(),
                    null);
            return;
        }
    }
}
