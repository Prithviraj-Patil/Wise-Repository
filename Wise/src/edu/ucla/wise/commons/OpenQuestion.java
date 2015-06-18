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

import org.w3c.dom.Node;

/**
 * This class is a subclass of Question and represents an open ended question on
 * the page
 * 
 */
public class OpenQuestion extends Question {

    /**
     * Constructor for an open ended question
     * 
     * @param n
     *            XML DOM structure from which the details of the question are
     *            obtained.
     */
    public OpenQuestion(Node n) {
        /* get the attributes for page item & question */
        super(n);
    }

    /**
     * Counts number of field for an open ended question - each has only one
     * field.
     * 
     * @return int Always returns one since the open ended question will have
     *         just one field.
     */
    @Override
    public int countFields() {
        return 1;
    }

    /**
     * Just renders html; unlike closed- and question blocks, no shared-element
     * references to knit.
     * 
     * @param mySurvey
     *            The survey to which this question is linked.
     */
    @Override
    public void knitRefs(Survey mySurvey) {
        this.html = this.makeHtml();
    }

    /**
     * Renders a form for an open ended question at the time of loading the
     * survey.
     * 
     * @return String HTML format of the open ended question Block.
     */
    public String makeHtml() {
        String s = "\n<table cellspacing='0' cellpadding='0' width=100%' border='0'><tr><td>"
                + "\n<table cellspacing='0' cellpadding='0' width=100%' border='0'><tr><td>";

        /* add/open the question stem row */
        s += super.makeStemHtml();

        /* start new row if it is not requested by one-line layout */
        if (!this.oneLine) {
            s += "\n<tr>";
            s += "<td width=10>&nbsp;</td>";
            s += "<td width=20>&nbsp;</td>";
            s += "<td width=570>";
        } else {
            s += "&nbsp;&nbsp;";
        }
        s += this.formFieldHtml();
        s += "</td></tr></table>\n</td></tr></table>";
        return s;
    }

    /**
     * Placeholder; subclasses need to override
     * 
     * @return String returns empty string here.
     */
    public String formFieldHtml() {
        return "";
    }

    /** renders results for an open ended question */
    /*
     * public String render_results(Hashtable data, String whereclause) { return
     * super.render_results(data, whereclause); }
     */

    /** returns a comma delimited list of all the fields on a page */
    /*
     * public String list_fields() { return name+","; }
     */
    /** prints information for an open ended question */
    /*
     * public String print() { return super.print(); }
     */

    /**
     * print survey for an open ended question - used for admin tool: print
     * survey
     */
    /*
     * public String print_survey() { String s = super.render_form(); s +=
     * this.common_render_form(); return s; }
     */
}
