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

import java.io.IOException;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import edu.ucla.wise.initializer.WiseProperties;
import edu.ucla.wise.utils.TemplateUtil;
import freemarker.template.Configuration;

/*
 Admin information set -- 
 The class represents that Admin application
 Instances represent administrator user sessions
 TODO (med): untangle Frank's survey uploading spaghetti in load_data.jsp
 */
/**
 * This class represents the Admin information when running the Admin
 * application. Instance represent administrator user session.
 */
public class AdminApplication {

    private static AdminApplication adminApplication;

    private final Logger log = Logger.getLogger(AdminApplication.class);

    private final String dbBackupPath;
    private final String imageRootPath;

    private final Configuration htmlTemplateConfiguration;

    private final Configuration sqlTemplateConfiguration;

    public static String ApplicationName = null;

    public static String sharedFileUrl;
    public static String sharedImageUrl;
    public static String servletUrl;

    public AdminApplication(String appContext, String rootFolderPath, WiseProperties properties) throws IOException {
        this.imageRootPath = properties.getImagesPath();
        this.dbBackupPath = properties.getDatabaseBackupPath() + System.getProperty("file.separator");
        this.htmlTemplateConfiguration = TemplateUtil.createTemplateConfiguration(rootFolderPath, "admin/templates");
        this.sqlTemplateConfiguration = TemplateUtil.createTemplateConfiguration(rootFolderPath, "admin/sql_templates");
    }

    public static String forceInit(String appContext, String rootFolderPath, WiseProperties properties)
            throws IOException {
        String initErr = null;
        initialize(appContext, rootFolderPath, properties);
        if (ApplicationName == null) {
            initErr = "Wise Admin Application -- uncaught initialization error";
        }
        return initErr;
    }

    public static void initialize(String appContext, String rootFolderPath, WiseProperties properties)
            throws IOException {
        if (adminApplication == null) {
            adminApplication = new AdminApplication(appContext, rootFolderPath, properties);
        }
    }

    public static AdminApplication getInstance() {
        if (adminApplication == null) {
            throw new IllegalStateException("The admin application is not initialized");
        }
        return adminApplication;
    }

    /**
     * Decodes a string that has been encoded by encode function.
     * 
     * @param charId
     *            String to decode
     * @return String Decoded string.
     */
    @Deprecated
    public static String decode(String charId) {
        String result = new String();
        int sum = 0;
        for (int i = charId.length() - 1; i >= 0; i--) {
            char c = charId.charAt(i);
            int remainder = c - 65;
            sum = (sum * 26) + remainder;
        }

        sum = sum - 97654;
        int remain = sum % 31;
        if (remain == 0) {
            sum = sum / 31;
            result = Integer.toString(sum);
        } else {
            result = "invalid";
        }
        return result;
    }

    /**
     * Encodes a given string.
     * 
     * @param userId
     *            String to encode.
     * @return String Encoded string.
     */
    @Deprecated
    public static String encode(String userId) {
        int baseNumb = (Integer.parseInt(userId) * 31) + 97654;
        String s1 = Integer.toString(baseNumb);
        String s2 = Integer.toString(26);
        BigInteger b1 = new BigInteger(s1);
        BigInteger b2 = new BigInteger(s2);

        int counter = 0;
        String charId = new String();
        while (counter < 5) {
            BigInteger[] bs = b1.divideAndRemainder(b2);
            b1 = bs[0];
            int encodeValue = bs[1].intValue() + 65;
            charId = charId + (new Character((char) encodeValue).toString());
            counter++;
        }
        return charId;
    }

    public String getDbBackupPath() {
        return this.dbBackupPath;
    }

    public String getImageRootPath() {
        return this.imageRootPath;
    }

    public Configuration getHtmlTemplateConfiguration() {
        return this.htmlTemplateConfiguration;
    }

    public Configuration getSQLTemplateConfiguration() {
        return this.sqlTemplateConfiguration;
    }
}
