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
import java.util.regex.Pattern;

/**
 * This class is used to check if the inputs that application get are valid and
 * there are attacks using these inputs
 */
public class SanityCheck {

    /* Possible patterns to find attacks */
    private static Pattern[] inputPatterns = new Pattern[] {

            /* Script fragments */
            Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),

            /* src='...' */
            Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                    | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                    | Pattern.DOTALL),

            /* lonely script tags */
            Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            /* eval(...) */
            Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
            /* expression(...) */
            Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            /* javascript:... */
            Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),

            /* vbscript:... */
            Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
            /* onload(...)=... */
            Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),

            /* iframe */
            Pattern.compile("<iframe(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("<frame(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),

            /* alert */
            Pattern.compile("alert", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),

            /* to Catch mouse events */
            Pattern.compile("mouse", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),

            /* sql injection patterns */
            // Pattern.compile("(insert|or)",Pattern.CASE_INSENSITIVE)
            Pattern.compile("\\'\\+\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\'\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("&&", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("%", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("@@", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("--", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\+\\+", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("/\\*", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\*/", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\' having", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\\\.\\.", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("(\\d+)\\+(\\d+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\'=\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\|\\|", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\'and\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE),
            Pattern.compile("\\.html", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE) };

    /*
     * BlackList = Array("--", ";", "/*", "<Comment closing >", "@@", "@",
     * "char", "nchar", "varchar", "nvarchar", "alter", "begin", "cast",
     * "create", "cursor", "declare", "delete", "drop", "end", "exec",
     * "execute", "fetch", "insert", "kill", "open", "select", "sys",
     * "sysobjects", "syscolumns", "table", "update")
     */

    /**
     * Checks if the string passed contains some suspicious string patterns.
     * 
     * @param value
     *            String to be checked.
     * @return boolean true if there is pattern match else false.
     */
    public static boolean sanityCheck(String value) {
        boolean res = false;
        if (!((value == null) || value.isEmpty())) {
            for (Pattern scriptPattern : inputPatterns) {
                res = scriptPattern.matcher(value).find();
                if (res) {
                    return res;
                }
            }
        }
        return res;
    }

    /**
     * Checks if the array of string passed contains some suspicious string
     * patterns.
     * 
     * @param valueArray
     *            Array of strings which have to be checked.
     * @return boolean True if there is pattern matches one of the string in the
     *         ArrayList else false.
     */
    public static boolean sanityCheck(ArrayList<String> valueArray) {
        boolean res = false;
        for (String value : valueArray) {
            if (!((value == null) || value.isEmpty())) {
                for (Pattern scriptPattern : inputPatterns) {
                    res = scriptPattern.matcher(value).find();
                    if (res) {
                        return res;
                    }
                }
            }
        }
        return res;
    }

    /**
     * Encodes the html special characters in the String
     * 
     * @param value
     *            String to be encoded.
     * @return String Encoded String.
     */
    public static String encodeHTML(String value) {
        String encodedString = value;
        if (!((value == null) || value.isEmpty())) {
            if (value.contains("&") || value.contains("<") || value.contains(">") || value.contains("\"")
                    || value.contains("\'") || value.contains("/")) {
                if (value.indexOf("&") != -1) {
                    encodedString = encodedString.replaceAll("&", "&amd");
                }
                if (value.indexOf("<") != -1) {
                    encodedString = encodedString.replaceAll("<", "&lt");
                }
                if (value.indexOf(">") != -1) {
                    encodedString = encodedString.replaceAll(">", "&gt");
                }
                if (value.indexOf("\"") != -1) {
                    encodedString = encodedString.replaceAll("\"", "&quot");
                }
                if (value.indexOf("\'") != -1) {
                    encodedString = encodedString.replaceAll("'", "&#x27");
                }
                if (value.indexOf("/") != -1) {
                    encodedString = encodedString.replaceAll("/", "&#x2F");
                }
            }
        }
        return encodedString;
    }

    /**
     * Encodes the html special characters in the array of Strings
     * 
     * @param valueArray
     *            String array to be encoded.
     * @return Array Encoded array of strings.
     */
    public static String[] encodeHTML(String[] valueArray) {

        String[] encodedString = new String[valueArray.length];
        int i = 0;
        for (String value : valueArray) {

            if (value.contains("&") || value.contains("<") || value.contains(">") || value.contains("\"")
                    || value.contains("\'") || value.contains("/")) {
                if (value.indexOf("&") != -1) {
                    encodedString[i] = value.replaceAll("&", "&amd");
                } else if (value.indexOf("<") != -1) {
                    encodedString[i] = value.replaceAll("<", "&lt");
                } else if (value.indexOf(">") != -1) {
                    encodedString[i] = value.replaceAll(">", "&gt");
                } else if (value.indexOf("\"'") != -1) {
                    encodedString[i] = value.replaceAll("\"", "&quot");
                } else if (value.indexOf("\'") != -1) {
                    encodedString[i] = value.replaceAll("'", "&#x27");
                } else if (value.indexOf("/") != -1) {
                    encodedString[i] = value.replaceAll("/", "&#x2F");
                }
            }
            encodedString[i] = value;
            i++;
        }
        return encodedString;
    }

    /**
     * Filters all the malicious content in the input parameters and returns
     * only strings with alpha numeric characters
     * 
     * @param value
     *            String which has to be modified.
     * @return String Changed String.
     */
    public static String onlyAlphaNumeric(String value) {
        String filteredString = value;
        if (!((value == null) || value.isEmpty())) {
            filteredString = filteredString.replaceAll("[^A-Za-z0-9._=]", "");
            return filteredString;
        }
        return filteredString;
    }

    /**
     * Filters all the malicious content in the input parameters and returns
     * only strings with alpha numeric characters
     * 
     * @param value
     *            ArrayList of String which has to be modified.
     * @return String Changed ArrayList.
     */
    public static ArrayList<String> onlyAlphaNumeric(ArrayList<String> valueArray) {
        ArrayList<String> filteredStringArray = new ArrayList<String>();
        for (String value : valueArray) {
            if (!((value == null) || value.isEmpty())) {
                filteredStringArray.add(value.replaceAll("[^A-Za-z0-9._]", ""));
            } else {
                filteredStringArray.add(value);
            }
        }

        return filteredStringArray;
    }

    /**
     * Filters all the malicious content in the input parameters and returns
     * only strings with alpha numeric characters and some special characters.
     * 
     * @param value
     *            String which has to be modified.
     * @return String Changed String.
     */
    public static String onlyAlphaNumericandSpecial(String value) {
        String filteredString = value;
        if (!((value == null) || value.isEmpty())) {
            filteredString = filteredString.replaceAll("[^A-Za-z0-9._'@\\-:() ]", "");
            return filteredString;
        }
        return filteredString;
    }

    /**
     * Filters all the malicious content in the input parameters and returns
     * only strings with alpha numeric characters and some special characters.
     * 
     * @param value
     *            ArrayList of String which has to be modified.
     * @return String Changed ArrayList.
     */
    public static ArrayList<String> onlyAlphaNumericandSpecial(ArrayList<String> valueArray) {
        ArrayList<String> filteredStringArray = new ArrayList<String>();
        for (String value : valueArray) {
            if (!((value == null) || value.isEmpty())) {
                filteredStringArray.add(value.replaceAll("[^A-Za-z0-9._'@\\-:() ]", ""));
            } else {
                filteredStringArray.add(value);
            }
        }

        return filteredStringArray;
    }

}
