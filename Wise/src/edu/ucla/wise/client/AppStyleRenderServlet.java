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

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.databank.DataBank;

/**
 * AppStyleRenderServlet class is used to get an Style sheets from database.
 * 
 */
@WebServlet("/survey/styleRender")
public class AppStyleRenderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AppStyleRenderServlet.class);

    /**
     * Gets the css from the database.
     * 
     * @param request
     *            HTTP Request.
     * @param response
     *            HTTP Response.
     * @throws IOException
     *             and ServletException.
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cssName = request.getParameter("css");
        String appName = request.getParameter("app");

        HttpSession session = request.getSession(true);

        /* if session is new, then show the session expired info */
        if (session.isNew()) {
            return;
        }

        StudySpace studySpace = (StudySpace) session.getAttribute("STUDYSPACE");

        if (studySpace == null) {
            LOGGER.info("Study space name is null");
            return;
        }

        DataBank db = studySpace.getDB();
        InputStream cssStream = null;
        int bufferSize = 2 << 12;// 16kb buffer
        byte[] byteBuffer;

        try {
            cssStream = db.getFileFromDatabase(cssName, appName);
            if (cssStream != null) {
                response.reset();
                response.setContentType("text/css;charset=UTF-8");
                int count = 1;// initializing to a value > 0
                while (count > 0) {
                    byteBuffer = new byte[bufferSize];
                    count = cssStream.read(byteBuffer, 0, bufferSize);
                    response.getOutputStream().write(byteBuffer, 0, bufferSize);
                }
                response.getOutputStream().flush();
            } else {
                LOGGER.error("The css file '" + cssName + "' does not exist for'" + appName + "'");
                return;
            }
        } catch (IOException e) {
            LOGGER.error("File not found: " + e.getMessage());
        } catch (NullPointerException e) {
            LOGGER.error("Error while reading file from file system due to null buffer: " + e.getMessage());
        } catch (IndexOutOfBoundsException e) {
            LOGGER.error("Error while reading file from file system due to illegal buffer lengths " + e.getMessage());
        } finally {
            if (cssStream != null) {
                try {
                    cssStream.close();
                } catch (IOException e) {
                    LOGGER.error(e);
                }
            }
        }
    }
}
