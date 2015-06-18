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

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains an answer set called response set and all its possible
 * answers The closed question & question block contain this response set.
 */
public class ResponseSet {

    public static final Logger LOGGER = Logger.getLogger(ResponseSet.class);
    /** Instance Variables */
    public String id;
    public String levels;
    public String startvalue;

    public ArrayList<String> responses;
    public ArrayList<String> values;

    public Survey survey;

    /**
     * @param id
     * @param levels
     * @param startvalue
     * @param responses
     * @param values
     * @param survey
     */
    public ResponseSet(String id, String levels, String startvalue, ArrayList<String> responses,
            ArrayList<String> values, Survey survey) {
        super();
        this.id = id;
        this.levels = levels;
        this.startvalue = startvalue;
        this.responses = responses;
        this.values = values;
        this.survey = survey;
    }

    /**
     * Constructor: parse a response set node from XML
     * 
     * @param n
     *            Node from the XML that has to be parsed to get the information
     *            about response set.
     * @param s
     *            Survey object to which this ResponseSet is linked to.
     */
    public ResponseSet(Node n, Survey s) {
        try {
            this.survey = s;

            /* assign various attributes */
            this.id = n.getAttributes().getNamedItem("ID").getNodeValue();

            /* assign the number of levels to classify */
            Node node1 = n.getAttributes().getNamedItem("Levels");
            if (node1 != null) {
                this.levels = node1.getNodeValue();
            } else {
                this.levels = "0";
            }

            /* assign the start value of the 1st level */
            node1 = n.getAttributes().getNamedItem("StartValue");
            if (node1 != null) {
                this.startvalue = node1.getNodeValue();
            } else {
                this.startvalue = "1";
            }
            NodeList nodelist = n.getChildNodes();
            this.responses = new ArrayList<String>();
            this.values = new ArrayList<String>();

            /* assign answer option & its value */
            for (int i = 0; i < nodelist.getLength(); i++) {
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Response_Option")) {
                    String str = nodelist.item(i).getFirstChild().getNodeValue();
                    this.responses.add(str);
                    Node node2 = nodelist.item(i).getAttributes().getNamedItem("value");
                    if (node2 != null) {
                        this.values.add(node2.getNodeValue());
                    } else {
                        this.values.add("-1");
                    }
                }
            }
        } catch (DOMException e) {
            LOGGER.error(
                    "WISE - RESPONSE SET : ID = " + this.id + "; Survey = " + s.getId() + "; Study = "
                            + s.getStudySpace().id + " --> " + e.toString(), null);
            return;
        }
    }

    public static ResponseSet getDemoResponseSet(Survey survey) {
        return new ResponseSet("id", "levels", "startValue", new ArrayList<String>(), new ArrayList<String>(), survey);
    }

    /**
     * Returns the number of responses in the set
     * 
     * @return int Number of responses.
     */
    public int getSize() {
        return this.responses.size();
    }

    /** prints out a response set - used for admin tool: print survey */
    /*
     * public String print() { String s = "RESPONSE SET<br>"; s +=
     * "ID: "+id+"<br>"; s += "Levels: "+levels+"<br>"; s +=
     * "StartValue: "+startvalue+"<br>"; s += "Responses: <br>"; for (int i = 0;
     * i < responses.size(); i++) s +=
     * values.get(i)+":"+responses.get(i)+"<br>"; s += "<p>"; return s; }
     */
}
