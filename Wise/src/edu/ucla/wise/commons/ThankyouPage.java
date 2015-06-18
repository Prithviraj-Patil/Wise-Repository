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
 * This class contains a thank you page object set in the preface.
 */
public class ThankyouPage {
    private static final Logger LOGGER = Logger.getLogger(ThankyouPage.class);

    /** Instance Variables */
    public String id;
    public String title;
    public String banner;
    public String logo;
    public String surveyId;
    // public String irb_id;
    public String pageContents;

    public Preface preface;

    /**
     * Constructor: parse a thank you node from XML
     * 
     * @param n
     *            Node from the XML that has to be parsed to get the information
     *            about Thank you page.
     * @param p
     *            Preface object to which this ThankYou is linked with.
     */
    public ThankyouPage(Node n, Preface p) {
        try {
            this.preface = p;
            // assign id & survey id (required) no ID
            // id = n.getAttributes().getNamedItem("ID").getNodeValue();
            // survey_id =
            // n.getAttributes().getNamedItem("Survey_ID").getNodeValue();

            // assign various attributes
            // Node node_child = n.getAttributes().getNamedItem("Title");
            // if(node_child !=null)
            // title = node_child.getNodeValue();
            // else
            // title = "";
            this.title = "Thank You";
            Node nodeChild = n.getAttributes().getNamedItem("BannerFileName");
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
            // node_child = n.getAttributes().getNamedItem("IRB_ID");
            // if(node_child !=null)
            // irb_id = node_child.getNodeValue();
            // else
            // irb_id = "";

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
            // WISE_Application.email_alert("WISE - THANKYOU PAGE : Preface = "+p.project_name+"; Study = "+p.study_space.id+" --> "+e.toString());
            LOGGER.error("WISE - THANKYOU PAGE : Preface = " + p.projectName + "--> " + e.toString(), e);
            return;
        }
    }
}