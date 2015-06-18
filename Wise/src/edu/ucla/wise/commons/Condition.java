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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is a subclass of Page_Item and represents a Precondition object on
 * the page.
 */
public class Condition extends PageItem {
    /** Instance Variables */
    private String preField, preFieldSecond = "";
    private Integer intConstant = null;
    private int operatrInt = 0;
    Condition cond, cond2;
    private final StringBuffer jsExpression = new StringBuffer("");

    // private Page page;

    /** constructor: */

    /**
     * Constructor: parse a Precondition node from XML.
     * 
     * @param n
     *            DOM node from where the Condition is populated.
     */
    public Condition(Node n) {

        /* get the page item properties */
        super(n);
        try {

            /* assign various attributes */
            NodeList subnodes = n.getChildNodes();
            Node node1 = subnodes.item(1);
            Node node2 = subnodes.item(3);
            Node node3 = subnodes.item(5);
            String node1Name = node1 != null ? node1.getNodeName() : "";
            String node2Name = node2 != null ? node2.getNodeName() : "";
            String node3Name = node3 != null ? node3.getNodeName() : "";

            /*
             * option 1: if the Condition is a leaf node, first node is a
             * "field"
             */
            if (node1Name.equalsIgnoreCase("field")) {

                /*
                 * parse the leaf node note XML Schema enforces order of: field,
                 * operator, (constant OR field)
                 */
                this.preField = node1.getFirstChild().getNodeValue();
                this.jsExpression.append("(a['" + this.preField + "']");
                if (this.preField.equals("")) {
                    throw new Exception("Invalid Precondition: Empty field name before " + node2Name);
                }

                /*
                 * represent comparison operator as integer, since eval needs
                 * 'switch,' which can't take strings
                 */
                if (node2Name.equalsIgnoreCase("gt")) {
                    this.operatrInt = 11;
                    this.jsExpression.append(" > ");
                } else if (node2Name.equalsIgnoreCase("lt")) {
                    this.operatrInt = 12;
                    this.jsExpression.append(" < ");
                } else if (node2Name.equalsIgnoreCase("geq")) {
                    this.operatrInt = 13;
                    this.jsExpression.append(" >= ");
                } else if (node2Name.equalsIgnoreCase("leq")) {
                    this.operatrInt = 14;
                    this.jsExpression.append(" <= ");
                } else if (node2Name.equalsIgnoreCase("eq")) {
                    this.operatrInt = 15;
                    this.jsExpression.append(" == ");
                } else if (node2Name.equalsIgnoreCase("neq")) {
                    this.operatrInt = 16;
                    this.jsExpression.append(" != ");
                } else {
                    throw new Exception("Invalid operator in Precondition: " + node2Name);
                }

                /* obtain the value for comparison */
                if (node3Name.equalsIgnoreCase("cn")) {
                    String constStr = node3.getFirstChild().getNodeValue();
                    if (constStr != null) {
                        this.intConstant = new Integer(constStr);
                        this.jsExpression.append(constStr);
                        this.jsExpression.append(")");
                    }
                } else if (node3Name.equalsIgnoreCase("field")) {
                    this.preFieldSecond = node3.getFirstChild().getNodeValue();
                    this.jsExpression.append(this.preFieldSecond);
                    this.jsExpression.append(")");
                    if (this.preFieldSecond.equals("")) {
                        throw new Exception("Invalid Precondition: Empty field name after " + node1Name);
                    }
                } else {
                    throw new Exception("Invalid comparator in Precondition: " + node3Name);
                }
            } else if (node1Name.equalsIgnoreCase("apply")) {

                /* option 2, syntax: apply, and|or, apply */

                /* recursively parse the nested preconditions */
                this.cond = new Condition(node1); // the apply node is itself a
                                                  // predicate
                this.jsExpression.append(this.cond.getJsExpression());
                if (node2Name.equalsIgnoreCase("and")) {
                    this.operatrInt = 1;
                    this.jsExpression.append(" && ");
                } else if (node2Name.equalsIgnoreCase("or")) {
                    this.operatrInt = 2;
                    this.jsExpression.append(" || ");
                } else {
                    throw new Exception("Invalid boolean operator in Precondition: " + node2Name);
                }

                /*
                 * recursively parse the 2nd apply node - another nested
                 * precondition
                 */
                if (node3Name.equalsIgnoreCase("apply")) {
                    this.cond2 = new Condition(node3);
                    this.jsExpression.append(this.cond2.getJsExpression());
                } else {
                    throw new Exception("Invalid righthand predicate in Precondition: " + node3Name);
                }
            } else {
                throw new Exception("Invalid Precondition node starting at: " + node1Name);
            }

        }// end of try
        catch (DOMException e) {
            LOGGER.error("WISE - CONDITION parse: " + e.toString(), null);
            return;
        } catch (Exception e) {
            LOGGER.error("WISE - CONDITION parse: " + e.toString(), null);
            return;
        }
    }

    /**
     * Counts number of fields/options in the Condition. Should never be called
     * 
     * @return int number of the fields(stems) pertaining to this question.
     */
    @Override
    public int countFields() {
        return 0;
    }

    /**
     * Executes the condition check recursively.
     * 
     * @param user
     *            User on whom the check should be performed.
     * @return boolean Returns a boolean if the condition is satisfied or not.
     */
    public boolean checkCondition(UserAnswers user) {
        boolean result = false;

        /*
         * if the recursion has not reached to the leaf level, then continue to
         * check the condition
         */

        if (this.operatrInt < 10) {

            /* not a leaf node */
            boolean applyResult = this.cond.checkCondition(user);
            boolean apply2Result = this.cond2.checkCondition(user);
            switch (this.operatrInt) {
            case 1:
                result = (applyResult && apply2Result);
                break;
            case 2:
                result = (applyResult || apply2Result);
                break;
            }
        } else {

            /*
             * recursion has reached a leaf node - attempt lookup of value for
             * field name(s) from user
             */
            Integer fieldVal1 = user.getFieldValue(this.preField);

            /*
             * check whether a 2-field compare vs. field-constant compare uses
             * pre_field_second to signal since pre_cn can't hold null as an int
             */
            if (this.preFieldSecond.equals("")) {
                result = this.compare(fieldVal1, this.operatrInt, this.intConstant);
            } else {
                result = this.compare(fieldVal1, this.operatrInt, user.getFieldValue(this.preFieldSecond));
            }
        }
        return result;
    }

    /**
     * Compares two operators based on the type of operation and returns the
     * result.
     * 
     * @param fieldInt1
     *            First operand
     * @param op
     *            Operator
     * @param fieldInt2
     *            Second operand
     * @return boolean true or false based on the operation.
     */
    private boolean compare(Integer fieldInt1, int op, Integer fieldInt2) {
        boolean result = false;

        /*
         * check for 2 special cases of nulls; all others containing null should
         * be false
         */
        if ((fieldInt1 == null) && (fieldInt2 == null) && (op == 15)) {
            return true;
        } else if ((fieldInt1 == null) || (fieldInt2 == null)) {
            return op == 16; // if one is null then other must not be here and
                             // != would be true
        }
        int fieldVal1 = fieldInt1.intValue();
        int fieldVal2 = fieldInt2.intValue();
        switch (op) {
        case 11:
            result = (fieldVal1 > fieldVal2);
            break;
        case 12:
            result = (fieldVal1 < fieldVal2);
            break;
        case 13:
            result = (fieldVal1 >= fieldVal2);
            break;
        case 14:
            result = (fieldVal1 <= fieldVal2);
            break;
        case 15:
            result = (fieldVal1 == fieldVal2);
            break;
        case 16:
            result = (fieldVal1 != fieldVal2);
            break;
        default:
            break;
        }
        return result;
    }

    /**
     * Check precondition for each member of a SubjectSet (to display stems in a
     * question block)
     * 
     * @param SubjectSetName
     *            Name of the subject set for which preconditions are checked.
     * @param SubjectSet
     *            All subject sets.
     * @param theUser
     *            User object on whom the checks are done.
     * @return boolean Array If the check on each subject set is true or false.
     */
    public boolean[] checkCondition(String SubjectSetName, String[] SubjectSet, User theUser) {
        boolean[] resultVector = new boolean[SubjectSet.length];
        int i;

        /*
         * the current prediction node has the apply child not a leaf node
         */
        if (this.operatrInt < 10) {

            /* each sub-call returns the full vector of results for each string */
            boolean[] applyResult = this.cond.checkCondition(SubjectSetName, SubjectSet, theUser);
            boolean[] apply2Result = this.cond2.checkCondition(SubjectSetName, SubjectSet, theUser);

            /* apply comparison to each paired element */
            switch (this.operatrInt) {
            case 1:
                for (i = 0; i < SubjectSet.length; i++) {
                    resultVector[i] = (applyResult[i] && apply2Result[i]);
                }
                break;
            case 2:
                for (i = 0; i < SubjectSet.length; i++) {
                    resultVector[i] = (applyResult[i] || apply2Result[i]);
                }
                break;
            }
        } else {

            /* get the value set for the field name */
            int[] preFv = this.getValuelist(theUser, SubjectSetName, SubjectSet, this.preField);
            /*
             * if the comparison pair is field vs. field, then get another value
             * set of the 2nd field name
             */
            if (this.preFieldSecond.equals("")) {
                i = 0;
                while (i < SubjectSet.length) {
                    // resultVector[i] = compare(pre_fv[i], operatr_int,
                    // int_constant);
                    i++;
                }
            } else {
                int[] pre_fv2 = this.getValuelist(theUser, SubjectSetName, SubjectSet, this.preFieldSecond);
                i = 0;
                while (i < SubjectSet.length) {
                    // resultVector[i] = compare(pre_fv[i], operatr_int, new
                    // Integer (pre_fv2[i])); //Quick patch to compile here
                    i++;
                }
            }
        }
        return resultVector;
    }

    /**
     * Searchs by field name, get the value set from the subject data table that
     * user conducted.
     * 
     * @param theUser
     * @param SubjectSetName
     * @param SubjectSet
     * @param fieldName
     * @return
     */
    public int[] getValuelist(User theUser, String SubjectSetName, String[] SubjectSet, String fieldName) {
        int[] listValue = new int[SubjectSet.length];
        // String[] list_v = new String[SubjectSet.length];
        // Hashtable DataSet;
        // try
        // {
        // //connect to the database
        // Connection conn = page.survey.getDBConnection();
        // Statement stmt = conn.createStatement();
        // String sql="";
        // DataSet = new Hashtable();
        // //get data from database for subject
        // sql = "select subject, "+ field_name.toUpperCase()+" from ";
        // sql += page.survey.id + "_"+SubjectSetName+ "_data ";
        // sql += "where invitee = "+ theUser.id;
        // boolean dbtype = stmt.execute(sql);
        // ResultSet rs = stmt.getResultSet();
        // while(rs.next())
        // {
        // String val=rs.getString(field_name.toUpperCase());
        // if(val==null || val.equalsIgnoreCase(""))
        // val="0";
        // DataSet.put(rs.getString("subject"), val);
        // }
        // stmt.close();
        // conn.close();
        //
        // //get the array of column values from hashtable
        // if(!DataSet.isEmpty())
        // {
        // for(int i=0; i<SubjectSet.length; i++)
        // {
        // String
        // current_key=SubjectSet[i].substring(SubjectSet[i].lastIndexOf("_")+1);
        // //Study_Util.email_alert("CONDITION GET VALUELIST: - check keys: " +
        // current_key);
        // list_v[i]= (String) DataSet.get(current_key);
        // if(list_v[i]==null || list_v[i].equalsIgnoreCase("null") ||
        // list_v[i].equalsIgnoreCase("") )
        // list_value[i]=0;
        // else
        // list_value[i]= Integer.parseInt(list_v[i]);
        // }
        // }
        // else
        // {
        // Study_Util.email_alert("CONDITION GET VALUELIST: the hashtable is empty");
        // }
        //
        // }
        // catch (Exception e)
        // {
        // Study_Util.email_alert("CONDITION GET VALUELIST: "+e.toString());
        // }
        return listValue;
    }

    /**
     * Prints out the Condition information
     * 
     * @return String Information about Condition.
     */
    @Override
    public String toString() {
        if (this.operatrInt < 10) {
            // not a leaf node
            return "apply node<br>";
        } else if (this.preFieldSecond.equals("")) {
            return "leaf node: (" + this.preField + ") op (" + this.intConstant.toString() + ")<br>";
        } else {
            return "leaf node: (" + this.preField + ") op (" + this.preFieldSecond + ")<br>";
        }
    }

    /**
     * Returns the Javascript expression for checking preconditions.
     * 
     * @return String Javascript expression
     */
    public String getJsExpression() {
        return this.jsExpression.toString();
    }
}
