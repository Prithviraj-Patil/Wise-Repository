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
 * This class contains a welcome page object set in the preface.
 */
public class WelcomePage {
    private static final Logger LOGGER = Logger.getLogger(WelcomePage.class);
    /** Instance Variables */
    public String id;
    public String title;
    public String banner;
    public String logo;
    public String surveyId;
    public String irbId;
    public String pageContents;

    public Preface preface;

    /**
     * Constructor: parse a response set node from XML
     * 
     * @param n
     *            Node from the XML that has to be parsed to get the information
     *            about welcome page.
     * @param p
     *            Preface object to which this WelcomePage is linked with.
     */
    public WelcomePage(Node n, Preface p) {
        try {
            this.preface = p;

            /* assign id & survey id (required) */
            this.id = n.getAttributes().getNamedItem("ID").getNodeValue();
            this.surveyId = n.getAttributes().getNamedItem("Survey_ID").getNodeValue();

            /* assign various attributes */
            Node nodeChild = n.getAttributes().getNamedItem("Title");
            if (nodeChild != null) {
                this.title = nodeChild.getNodeValue();
            } else {
                this.title = "";
            }

            nodeChild = n.getAttributes().getNamedItem("BannerFileName");
            if (nodeChild != null) {
                this.banner = nodeChild.getNodeValue();
            } else {
                this.banner = "title.gif";
            }

            nodeChild = n.getAttributes().getNamedItem("LogoFileName");
            if (nodeChild != null) {
                this.logo = nodeChild.getNodeValue();
            } else {
                this.logo = "proj_logo.gif";
            }

            nodeChild = n.getAttributes().getNamedItem("IRB_ID");
            if (nodeChild != null) {
                this.irbId = nodeChild.getNodeValue();
            } else {
                this.irbId = "";
            }

            NodeList nodeP = n.getChildNodes();
            this.pageContents = "";
            for (int j = 0; j < nodeP.getLength(); j++) {
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("p")) {
                    this.pageContents += "<p>" + nodeP.item(j).getFirstChild().getNodeValue() + "</p>";
                }
                if (nodeP.item(j).getNodeName().equalsIgnoreCase("html_content")) {
                    NodeList nodeN = nodeP.item(j).getChildNodes();
                    for (int k = 0; k < nodeN.getLength(); k++) {
                        if (nodeN.item(k).getNodeName().equalsIgnoreCase("#cdata-section")) {
                            this.pageContents += nodeN.item(k).getNodeValue();
                        }
                    }
                }
            }
        } catch (DOMException e) {
            LOGGER.error("WISE - WELCOME PAGE : ID = " + this.id + "; Preface Project name = " + p.projectName
                    + "; --> " + e.toString(), null);
            return;
        }
    }
}
