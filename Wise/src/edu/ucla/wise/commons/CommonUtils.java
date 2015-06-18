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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.Logger;

/**
 * This provides some common methods that are used across the application.
 */
public class CommonUtils {

    public static final Logger LOGGER = Logger.getLogger(CommonUtils.class);

    /**
     * Loads the resources form file system using the path.
     * 
     * @param relPath
     *            Relative path from which the resource has to be read.
     * @return InputStream The stream to which the resource has been read.
     */
    public static InputStream loadResource(String relPath) {
        ClassLoader c1 = Thread.currentThread().getContextClassLoader();
        URL url = c1.getResource(relPath);
        InputStream fileInputStream = null;
        if (url != null) {
            try {
                fileInputStream = new FileInputStream(URLDecoder.decode(url.getFile(), "UTF-8"));
            } catch (FileNotFoundException e) {
                LOGGER.error("File not found", e);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Encoding not supported", e);
            }
        }
        return fileInputStream;
    }

    /**
     * Returns absolute path for the relative path given.
     * 
     * @param relPath
     *            Relative path with respect to current URL
     * @return String Absolute path that is complete URL.
     */
    @SuppressWarnings("deprecation")
    public static String getAbsolutePath(String relPath) {

        String absolutePath = null;
        ClassLoader c1 = Thread.currentThread().getContextClassLoader();
        URL url = c1.getResource(relPath);
        if (url != null) {
            absolutePath = URLDecoder.decode(url.getPath());
        }
        return absolutePath;
    }

    /**
     * Encodes the given string passed, this function is used to make the URLs
     * which users use to access the system.
     * 
     * @param surveyId
     *            String to be encoded.
     * @return String Encoded String.
     */
    public static String base64Encode(String surveyId) {

        if (surveyId == null) {
            return surveyId;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream b64os = null;
        try {
            b64os = MimeUtility.encode(baos, "base64");
            b64os.write(surveyId.getBytes());

        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                b64os.close();
            } catch (IOException e) {
            }
        }
        return new String(baos.toByteArray());
    }

    /**
     * Decodes the encoded string passed, this function is used to get the
     * information about the users and surveys from the URL.
     * 
     * @param surveyId
     *            String to be decoded.
     * @return String Decoded String.
     */
    public static String base64Decode(String encodedSurveyId) {
        if (encodedSurveyId == null) {
            return encodedSurveyId;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(encodedSurveyId.getBytes());
        InputStream b64is = null;
        try {
            b64is = MimeUtility.decode(bais, "base64");
        } catch (MessagingException e) {
            LOGGER.error(e);
        }
        byte[] tmp = new byte[encodedSurveyId.getBytes().length];
        int n = 0;
        try {
            n = b64is.read(tmp);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        byte[] res = new byte[n];
        System.arraycopy(tmp, 0, res, 0, n);
        return new String(res);
    }

    public static void main(String args[]) {

        System.out.println("Encoding string = " + base64Encode("BIP_User"));
        System.out.println("Decoding string = " + base64Decode(base64Encode("BIP_User")));
    }
}
