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
 * This class contains a subject set and all its possible answers
 */
public class SubjectSet {
    public static final Logger LOGGER = Logger.getLogger(SubjectSet.class);
    /** Instance Variables */
    public String id;
    private int[] subjectIDs;
    public String[] subjectLabels;
    public Survey survey;
    public int subjectCount;

    /**
     * Constructor: parse a subject set node from XML
     * 
     * @param n
     *            Node from the XML that has to be parsed to get details.
     * @param p
     *            Preface object to which this SubjectSet is linked with.
     */
    public SubjectSet(Node n, Survey s) {
        try {
            this.survey = s;

            /* assign various attributes */
            this.id = n.getAttributes().getNamedItem("ID").getNodeValue();
            NodeList nlist = n.getChildNodes();
            this.subjectCount = nlist.getLength();
            this.subjectIDs = new int[this.subjectCount];
            this.subjectLabels = new String[this.subjectCount];

            /* get each subject name and its value in the subject set */
            for (int j = 0; j < this.subjectCount; j++) {
                int idNum = 1;
                Node subj = nlist.item(j);
                if (subj.getNodeName().equalsIgnoreCase("Subject")) {

                    /* get the subject value */
                    Node sIDnode = subj.getAttributes().getNamedItem("IDnum");
                    if (sIDnode == null) {

                        /*
                         * ID value is not specified in XML, assign the
                         * currentindex as its value
                         */
                        this.subjectIDs[j] = idNum++;
                    } else {
                        this.subjectIDs[j] = Integer.parseInt(sIDnode.getNodeValue());
                        idNum = Math.max(idNum, this.subjectIDs[j]);
                        idNum++;
                    }

                    /* record the subject name */
                    this.subjectLabels[j] = subj.getFirstChild().getNodeValue();
                }
            }
        } catch (DOMException e) {
            LOGGER.error(
                    "WISE - SUBJECT SET : ID = " + this.id + "; Survey = " + this.id + "; Study = "
                            + s.getStudySpace().id + " --> " + e.toString(), null);
            return;
        }
    }

    /**
     * Returns the id of the subject for the given Index. It is used by the
     * question block class to get the Id of the subject which meets the
     * precondition
     * 
     * @param index
     *            The Index whose subject ID is needed.
     * @return String The subject ID.
     */
    public String getfieldNamesuffix(int index) {
        if (index < this.subjectCount) {
            return "_" + Integer.toString(this.subjectIDs[index]);
        } else {
            return ""; // not entirely safe, but should never be out of bounds
        }
    }

    /**
     * Converts the subject ids and labels to and string and returns it.
     * 
     * @return String The string format of the subject Ids and labels.
     */
    @Override
    public String toString() {
        String s = "<p><b>SubjectSet</b><br>";
        s += "ID: " + this.id + "<br>";

        for (int i = 0; i < this.subjectCount; i++) {
            s += "   " + this.subjectIDs[i] + ": " + this.subjectLabels[i];
        }
        return s;
    }

}
