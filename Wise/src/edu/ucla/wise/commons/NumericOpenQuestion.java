/**
 * Copyright (c) 2014, Regentimport java.util.HashMap;
import java.util.Hashtable;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
cation, are permitted provided that the following conditions are met:
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

import java.util.HashMap;
import java.util.Hashtable;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.ucla.wise.commons.databank.DBConstants;
import edu.ucla.wise.commons.databank.DataBank;

/**
 * This class is a subclass of OpenQuestion and represents an numeric open ended
 * question on the page
 */
public class NumericOpenQuestion extends OpenQuestion {
    /** Instance Variables */
    public String maxSize;
    public String width;
    public String minValue;
    public String maxValue;
    public String decimalPlaces;

    // TODO: (med) add methods Survey to track maxSize and decimalPlaces or at
    // least the max() of these

    /**
     * Constructor for a numeric open question
     * 
     * @param n
     *            XML DOM structure from which the details of the question are
     *            obtained.
     */
    public NumericOpenQuestion(Node n) {

        /* get the attributes for open question */
        super(n);
        try {
            NodeList nodelist = n.getChildNodes();
            for (int i = 0; i < nodelist.getLength(); i++) {
                if (nodelist.item(i).getNodeName().equalsIgnoreCase("Numeric_Open_Response")) {
                    /* assign various attributes */
                    this.maxSize = nodelist.item(i).getAttributes().getNamedItem("MaxSize").getNodeValue();
                    this.minValue = nodelist.item(i).getAttributes().getNamedItem("MinValue").getNodeValue();
                    this.maxValue = nodelist.item(i).getAttributes().getNamedItem("MaxValue").getNodeValue();

                    Node node = nodelist.item(i).getAttributes().getNamedItem("Width");
                    if (node != null) {
                        this.width = node.getNodeValue();
                    } else {
                        this.width = this.maxSize;
                    }
                    node = nodelist.item(i).getAttributes().getNamedItem("DecimalPlaces");
                    if (node != null) {
                        this.decimalPlaces = node.getNodeValue();
                    } else {
                        this.decimalPlaces = "0";
                    }
                }
            }
        } catch (DOMException e) {
            LOGGER.error("WISE - NUMERIC OPEN QUESTION: " + e.toString(), null);
            return;
        }
    }

    @Override
    public char getValueType() {
        return DBConstants.decimalValueTypeFlag;
    }

    /**
     * Renders form for numeric open question
     * 
     * @return String returns empty string here.
     */
    @Override
    public String formFieldHtml() {
        String s = "";

        /*
         * display the form field start from a new line if it is not requested
         * by one-line layout
         */
        s += "<input type='text' name='" + this.name.toUpperCase() + "' maxlength='" + this.maxSize + "' ";
        s += "size='" + this.width + "' onChange='RangeCheck(this," + this.minValue + "," + this.maxValue + ");'>";
        return s;
    }

    /**
     * print survey for numeric open question - used for admin tool: print
     * survey.
     * 
     * @return String HTML format of the this question block to print the
     *         survey.
     */
    @Override
    public String printSurvey() {
        String s = super.makeStemHtml();
        s += "";

        /* start from a new line if it is not requested by one-line layout */
        if (!this.oneLine) {
            s += "<td width=570>";
        }
        int widthPlus = Integer.parseInt(this.width) + 20;
        s += "<table cellpadding=0 cellspacing=0 border=1><tr>";
        s += "<td width=" + widthPlus + " height=15 align=center>";
        s += "</td></tr></table>";
        s += "</td>";
        s += "</tr>";
        s += "</table>";
        return s;
    }

    /**
     * Renders results for numeric open question.
     * 
     * @param pg
     *            Page Object for which the results are to be rendered.
     * @param db
     *            Data Bank object to connect to the database.
     * @param whereclause
     *            whereclause to restrict the invitee selection.
     * @param data
     *            Hashtable which contains results.
     * @return String HTML format of the results is returned.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String renderResults(Page page, DataBank db, String whereclause, Hashtable data) {
        String html = "";

        /* min and max values of the question answer */
        float fieldMin = 0, fieldMax = 0;
        int binCount = 10;
        int unanswered = 0;
        float minBinWidth = 0, lMbw = 0, tLMbw = 0, binBaseUnit = 0, binWidthPrelim = 0, scaleStart = 0, binWidthFinal = 0;
        HashMap<String, String> binCountMap = new HashMap<String, String>();

        /* get the question value from the hashtable */
        String subjAns = (data == null) ? "null" : (String) data.get(this.name.toUpperCase());

        /* convert the value from string type into the float type */
        float fAns = 0;
        if (!subjAns.equalsIgnoreCase("null")) {
            fAns = Float.valueOf(subjAns).floatValue();
        }

        /*
         * get average value of the question results within the scope of
         * whereclause
         */
        // TODO: Help!
        float avg = this.getAvg(page, whereclause);

        /*
         * Number of BINS and width get min and max values based on all results
         * within the scope of whereclause
         */
        HashMap<String, Float> minMaxMap = db.getMinMaxForItem(page, this.name, whereclause);
        fieldMin = minMaxMap.get("min");
        fieldMax = minMaxMap.get("max");

        minBinWidth = (fieldMax - fieldMin) / binCount;
        if (minBinWidth == 0) {
            minBinWidth = 1;
        }
        lMbw = (float) Math.log(minBinWidth) * (1 / (float) Math.log(10));
        tLMbw = (float) Math.floor(lMbw);
        binBaseUnit = (float) Math.pow(10, tLMbw);
        binWidthPrelim = binBaseUnit * ((float) Math.floor(minBinWidth / binBaseUnit) + 1);
        scaleStart = binWidthPrelim * ((float) Math.floor(fieldMin / binWidthPrelim));
        binWidthFinal = binBaseUnit * ((float) Math.floor((fieldMax - scaleStart) / (binCount * binBaseUnit)) + 1);

        /* get bins on that question from database */
        binCountMap = db.getHistogramForItem(page, this.name, scaleStart, binWidthFinal, whereclause);
        unanswered = binCountMap.get("unanswered") == null ? 0 : Integer.parseInt(binCountMap.get("unanswered"));

        html += "<table width=400 border=0>";
        html += "<tr><td align=right><span class='itemID'>" + this.name + "</span></td></tr>";
        html += "<tr><td>";
        html += "<table bgcolor=#FFFFFC cellspacing='0' cellpadding='1' width=400 border='1'>";
        html += "<tr>";
        html += "<td bgcolor=#FFFFE0 rowspan=2 width='30%'>";

        /* 3rd table layout */
        html += "<table><tr><td width='2%'>&nbsp;</td><td><font color=green>" + this.stem + "</font>";
        html += "<p><div align='right'><font color=green size='-2'><b>mean:</b>" + avg + "</font></div>";
        String su = "";
        if (unanswered == 1) {
            su = binCountMap.get("null");
            html += "<div align='right'>";
            if (fAns == 0) {
                html += "<span style=\"background-color: '#FFFF77'\">";
                html += "<font size='-2'>unanswered:&nbsp;&nbsp;" + su + "</font>";
                html += "</span>";
            } else {
                html += "<font size='-2'>unanswered:&nbsp;&nbsp;" + su + "</font>";
            }
            html += "</div>";
        }
        html += "<p align=left><a href='" + "view_open_results?u=" + su + "&q=" + this.name + "&t=" + page.getId()
                + "' >";
        html += "<img src='" + "imageRender?img=go_view.gif' border=0></a>";
        html += "</td></tr></table>";

        /* end of 3rd table */
        html += "</td>";
        html += "<td width='70%'>";
        int colSpan = (binCount * 2) + 2;
        html += "<table width='100%' border='0'>";
        html += "<tr><td colspan='" + colSpan
                + "' align='center'><font size='-2'>Histogram of values reported</font></td></tr>";
        html += "<tr>";
        html += "<td>&nbsp;</td>";
        String cellCount;
        float f2 = scaleStart;
        float f3 = scaleStart;
        int totalSum = 0;
        for (int j = 0; j < binCount; j++) {
            cellCount = binCountMap.get(Integer.toString(j));
            totalSum += (cellCount == null) ? 0 : Float.valueOf(cellCount).floatValue();
        }
        for (int j = 0; j < binCount; j++) {
            cellCount = binCountMap.get(Integer.toString(j));

            if (cellCount == null) {
                cellCount = "0";
            }

            float cellPercent = (Float.valueOf(cellCount).floatValue() / totalSum) * 100;
            int roundedCellPercent = Math.round(cellPercent);
            int imageHeight = roundedCellPercent / 2;

            f3 = f2 + binWidthFinal;
            if ((fAns > f2) && (fAns < f3)) {

                /* this is the user's answer */
                html += "<td colspan='2' bgcolor='#FFFF77' align='center'>";
            } else {
                html += "<td colspan='2' align='center'>";
            }
            // TODO -- Help Sumedh!
            html += "<font color=green size='-2'>" + Integer.toString(roundedCellPercent) + "%</font><br><img src='"
                    + "imageRender?img=vertical/bar_" + Integer.toString(imageHeight) + ".gif' ";
            html += "width='10' height='50'>";
            html += "</td>";

            f2 = f3;
        }
        html += "<td>&nbsp;</td>";
        html += "</tr>";
        html += "<tr>";
        for (int j = 0; j < (binCount + 1); j++) {
            html += "<td colspan='2' align='center'><font size='-2'>|</font></td>";
        }
        html += "</tr>";

        int p;
        float f1 = scaleStart;
        html += "<tr>";
        for (int j = 0; j < (binCount + 1); j++) {
            p = Math.round(f1);
            // String ps = Integer.toString(p);
            html += "<td colspan='2' align='center'>";
            html += "<font color=green size='-2'>" + p + "</font>";
            html += "</td>";
            f1 = f1 + binWidthFinal;
        }

        html += "</tr></table>";
        html += "</td></tr></table>";
        html += "</td></tr></table>";
        return html;
    }
    // public Hashtable read_form(Hashtable params)
    // {
    // Hashtable answers = new Hashtable();
    // String fieldName = name.toUpperCase();
    // String answerVal = (String) params.get(fieldName);
    // answers.put(fieldName, answerVal);
    // return answers;
    // }
    //
    // /** read out the question field name & value from the hashtable and put
    // them into two arrays respectively */
    // public int read_form(Hashtable params, String[] fieldNames, String[]
    // fieldValues, int fieldIndex)
    // {
    // fieldNames[fieldIndex] = name.toUpperCase();
    // fieldValues[fieldIndex] = (String) params.get(name.toUpperCase());
    // //have to keep the same index for name & value
    // if ( (fieldValues[fieldIndex] == null) ||
    // (fieldValues[fieldIndex].equalsIgnoreCase("")) )
    // fieldValues[fieldIndex] = "null";
    // fieldIndex++;
    // return 1;
    // }

    /** print numeric open question information */
    /*
     * public String print() { String s = "NUMERIC OPEN QUESTION<br>"; s +=
     * super.print(); s += "MaxSize: "+maxSize+"<br>"; s +=
     * "Width: "+width+"<br>"; s += "MinValue: "+minValue+"<br>"; s +=
     * "MaxValue: "+maxValue+"<br>"; s +=
     * "DecimalPlaces: "+decimalPlaces+"<br>"; s += "<p>"; return s; }
     */

}
