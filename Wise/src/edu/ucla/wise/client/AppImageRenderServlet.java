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
package edu.ucla.wise.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.CommonUtils;
import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.WISEApplication;
import edu.ucla.wise.commons.databank.DataBank;

/**
 * AppImageRenderServlet class is used to get an image form database and in-case
 * it fails it tries to get it from file system.
 * 
 */
@WebServlet("/survey/imageRender")
public class AppImageRenderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AppImageRenderServlet.class);

    /**
     * Gets the image from the database or file system based on the name of the
     * image and populates the response accordingly.
     * 
     * @param request
     *            HTTP Request.
     * @param response
     *            HTTP Response.
     * @throws IOException
     *             and ServletException.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String appName = request.getParameter("app");
        if (appName == null) {
            appName = "";
        }
        String imageName = request.getParameter("img");

        int bufferSize = 2 << 12;// 16kb buffer
        byte[] byteBuffer = new byte[bufferSize];
        response.setContentType("image");

        HttpSession session = request.getSession(true);

        if (session.isNew()) {
            getFromFileSystem(response, imageName, appName, true);
            return;
        }

        StudySpace studySpace = (StudySpace) session.getAttribute("STUDYSPACE");

        if (studySpace == null) {

            /* retrieve image from directory [duplicated code] */
            LOGGER.info("Fetching image from file system");
            getFromFileSystem(response, imageName, appName, true);
            return;
        }

        DataBank db = studySpace.getDB();
        InputStream imageStream = null;
        try {
            imageStream = db.getFileFromDatabase(imageName, appName);
            if (imageStream != null) {
                response.reset();
                if (imageName.contains("gif")) {
                    response.setContentType("image/gif");
                } else {
                    response.setContentType("image/jpg");
                }
                int count = 1;// initializing to a value > 0
                while (count > 0) {
                    count = imageStream.read(byteBuffer, 0, bufferSize);
                    response.getOutputStream().write(byteBuffer, 0, bufferSize);
                }
                response.getOutputStream().flush();
            } else {
                LOGGER.info("Fetching image from file system: " + imageName);
                getFromFileSystem(response, imageName, appName, true);
            }

            if (imageStream != null) {
                imageStream.close();
            }
        } catch (IOException e) {
            LOGGER.error("File not found", e);
        }
    }

    /**
     * Gets the image from the file system based on the name of the image.
     * 
     * @param res
     *            HTTP Response.
     * @param imageName
     *            Name of the image to be fetched from the file system.
     * @param appName
     *            Name of the App to which this image corresponds to.
     * @param isImage
     *            If the content being fetched is image or not.
     */
    public static void getFromFileSystem(HttpServletResponse response, String imageName, String appName, boolean isImage) {
        int bufferSize = 2 << 12;// 16kb buffer
        byte[] byteBuffer = new byte[bufferSize];
        InputStream imageStream = null;
        try {

            /* Retrieve the image from the correct directory */
            String pathToImages = WISEApplication.getInstance().getWiseProperties().getImagesPath();

            String pathWithStudyName;
            if ("".equals(appName)) {
                pathWithStudyName = pathToImages + System.getProperty("file.separator") + imageName;
            } else {
                pathWithStudyName = pathToImages + System.getProperty("file.separator") + appName
                        + System.getProperty("file.separator") + imageName;
            }

            imageStream = CommonUtils.loadResource(pathWithStudyName);
            if (imageStream == null) {
                /* trying to load the file, will 100% fail! */
                imageStream = new FileInputStream(pathWithStudyName);
            }
            response.reset();
            response.setContentType("image/jpg");
            int count = 1;// initializing to a value > 0
            while (count > 0) {
                count = imageStream.read(byteBuffer, 0, bufferSize);
                response.getOutputStream().write(byteBuffer, 0, bufferSize);
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Error while reading file from file system", e);
        } catch (NullPointerException e) {
            e.printStackTrace();
            LOGGER.error("Error while reading file from file system due to null buffer", e);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            LOGGER.error("Error while reading file from file system due to illegal buffer lengths", e);
        } finally {
            if (imageStream != null) {
                try {
                    imageStream.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }
}
