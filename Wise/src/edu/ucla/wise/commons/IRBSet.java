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

/**
 * This class contains a IRB set object in the preface.
 */

public class IRBSet {
    public static final Logger LOGGER = Logger.getLogger(IRBSet.class);

    /** Instance Variables */
    public String id;
    public String irbName;
    public String expirDate;
    public String approvalNumber;
    public String irbLogo;
    public Preface preface;

    /**
     * Constructor: parse a IRB set node from Preface.xml
     * 
     * @param n
     *            IRB XML DOM node to be parsed.
     * @param p
     *            Preface to which this IRBSet is to be linked.
     */
    public IRBSet(Node n, Preface p) {
        try {
            this.preface = p;

            /* assign id (required) */
            this.id = n.getAttributes().getNamedItem("ID").getNodeValue();

            /* assign various attributes */
            Node node_child = n.getAttributes().getNamedItem("Name");
            if (node_child != null) {
                this.irbName = node_child.getNodeValue();
            } else {
                this.irbName = "";
            }
            node_child = n.getAttributes().getNamedItem("Expiration_Date");
            if (node_child != null) {
                this.expirDate = node_child.getNodeValue();
            } else {
                this.expirDate = "";
            }
            node_child = n.getAttributes().getNamedItem("IRB_Approval_Number");
            if (node_child != null) {
                this.approvalNumber = node_child.getNodeValue();
            } else {
                this.approvalNumber = "";
            }
            node_child = n.getAttributes().getNamedItem("Logo_File");
            if (node_child != null) {
                this.irbLogo = node_child.getNodeValue();
            } else {
                this.irbLogo = "";
            }
        } catch (DOMException e) {
            LOGGER.error("WISE - IRB SET : ID = " + this.id + "; Preface = " + p.projectName + " --> " + e.toString(),
                    null);
            return;
        }
    }
}
