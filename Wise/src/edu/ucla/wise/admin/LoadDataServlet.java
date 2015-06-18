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
package edu.ucla.wise.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.oreilly.servlet.MultipartRequest;

import edu.ucla.wise.admin.web.AdminSessionServlet;
import edu.ucla.wise.commons.WISEApplication;
import freemarker.template.TemplateException;

/**
 * LoadDataServlet is a class which is used to load data into the system. Wise
 * admin can upload a csv containing the details of the invitees. Wise admin can
 * upload a xml file containing survey questions. Wise admin can upload image
 * file that are used in survey.
 * 
 */

@WebServlet("/admin/load_data")
public class LoadDataServlet extends AdminSessionServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoadDataServlet.class);

    private enum FileType {
        CSV, XML, IMAGE
    }

    /**
     * Updates the survey information into the database when uploading the
     * survey xml file
     * 
     * @param doc
     *            Parsed xml file document.
     * @param out
     *            Output to the screen.
     * @param stmt
     *            SQL statement for execution of queries.
     * @return Returns the filename of the uploaded survey xml into the
     *         database.
     */
    private String processSurveyFile(AdminUserSession adminSession, Document doc, PrintWriter out) {
        return adminSession.getMyStudySpace().processSurveyFile(doc);

    }

    /**
     * Updates the invitee information in the database when uploading the
     * invitee csv file
     * 
     * @param f
     *            csv file to load.
     * @param out
     *            Output to the screen.
     * @param stmt
     *            SQL statement for execution of queries.
     * @return Returns the filename of the uploaded survey xml into the
     *         database.
     * @throws SQLException
     *             , IOException.
     */
    public void processInviteesCsvFile(AdminUserSession adminSession, File f, PrintWriter out) throws SQLException,
            IOException {

        adminSession.getMyStudySpace().processInviteesCsvFile(f);

    }

    /**
     * Uploads the csv/xml/image files into database.
     * 
     * @param multi
     *            Multi part Request to get the file saved on disk.
     * @param filename
     *            file name to uplaod.
     * @param tableName
     *            database table into which the file has to be saved into.
     */
    private void saveFileToDatabase(AdminUserSession adminSession, MultipartRequest multi, String filename,
            String tableName) throws SQLException {

        adminSession.getMyStudySpace().saveFileToDatabase(multi, filename, tableName, adminSession.getStudyName());

    }

    @Override
    public void getMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException, TemplateException {
        this.postMethod(request, response, adminUserSession);
    }

    public static FileType getFileType(String fileType) {
        if ((fileType.indexOf("csv") != -1) || (fileType.indexOf("excel") != -1) || (fileType.indexOf("plain") != -1)) {
            return FileType.CSV;
        } else if ((fileType.indexOf("css") != -1) || (fileType.indexOf("jpg") != -1)
                || (fileType.indexOf("jpeg") != -1) || (fileType.indexOf("gif") != -1)) {
            return FileType.IMAGE;
        } else {
            return FileType.XML;
        }
    }

    /**
     * Uploads the csv/xml/image files into database from Wise admin.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void postMethod(HttpServletRequest request, HttpServletResponse response, AdminUserSession adminUserSession)
            throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        File xmlDir = new File(WISEApplication.getInstance().getWiseProperties().getXmlRootPath());
        String xmlTempLoc = xmlDir.getAbsolutePath() + System.getProperty("file.separator");

        try {
            MultipartRequest multi = new MultipartRequest(request, xmlTempLoc, 250 * 1024);
            String filename = multi.getFilesystemName("file");
            xmlTempLoc = xmlTempLoc + filename;
            String fileType = multi.getContentType("file");

            switch (getFileType(fileType)) {
            case CSV:
                LOGGER.debug("Processing an invitee csv file '" + filename + "'");
                out.println("<p>Processing an Invitee CSV file...</p>");
                /* parse csv file and put invitees into database */
                File file = multi.getFile("file");
                this.processInviteesCsvFile(adminUserSession, file, out);
                /* delete the file */
                file.delete();
                String disp_html = "<p>The CSV named " + filename + " has been successfully uploaded.</p>";
                out.println(disp_html);
                break;
            case IMAGE:
                LOGGER.debug("Processing an image/css file '" + filename + "'");
                this.saveFileToDatabase(adminUserSession, multi, filename, "wisefiles");
                out.println("<p>The image named " + filename + " has been successfully uploaded.</p>");
                break;
            case XML:
                LOGGER.debug("Processing an xml file '" + filename + "'");
                /* Get parser and an XML document */
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(new FileInputStream(xmlTempLoc));

                NodeList nodelist = doc.getChildNodes();
                for (int i = 0; i < nodelist.getLength(); i++) {
                    Node node = nodelist.item(i);
                    if (node.getNodeName().equalsIgnoreCase("Survey")) {
                        LOGGER.debug("Found survey node, processing...");
                        String newFileName = this.processSurveyFile(adminUserSession, doc, out);
                        if (!newFileName.equalsIgnoreCase("NONE")) {
                            this.saveFileToDatabase(adminUserSession, multi, newFileName, "xmlfiles");
                            File f = multi.getFile("file");
                            String remoteURL = adminUserSession.makeRemoteURL("survey", newFileName);
                            f.delete();
                            response.sendRedirect(remoteURL);
                        } else {
                            /* delete the file */
                            File f = multi.getFile("file");
                            f.delete();
                        }
                        break;
                    } else if (node.getNodeName().equalsIgnoreCase("Preface")) {
                        String fileNewName = "preface.xml";

                        this.saveFileToDatabase(adminUserSession, multi, fileNewName, "xmlfiles");

                        File f = multi.getFile("file");
                        f.delete();
                        String dispHtml = null;
                        if (adminUserSession.parseMessageFile()) {
                            dispHtml = "<p>PREFACE file is uploaded with name changed to be preface.xml</p>";
                        } else {
                            dispHtml = "<p>PREFACE file upload failed.</p>";
                        }
                        out.println(dispHtml);
                        String remoteURL = adminUserSession.makeRemoteURL("preface", fileNewName);
                        response.sendRedirect(remoteURL);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("WISE - ADMIN load_data.jsp", e);
            out.println("<h3>Upload of the file has failed.  Please try again.</h3>");
        } catch (Exception e) {
            LOGGER.error("WISE - ADMIN load_data.jsp", e);
            out.println("<h3>Invalid XML document submitted.  Please try again.</h3>");

            /*
             * Security feature change. Error should not be printed out on
             * console
             */
            // out.println("<p>Error: " + e.toString() + "</p>");
        }
        out.println("<p><a href= tool.jsp>Return to Administration Tools</a></p>\n" + "         </center>\n"
                + "                <pre>\n" +
                // file_loc [adminUserSession.study_xml_path]: file_loc%>
                // css_path [adminUserSession.study_css_path]: <%=css_path%>
                // image_path [adminUserSession.study_image_path]:
                // <%=image_path%>
                "               </pre>\n" + "</p>\n" + "</body>\n" + "</html>");

    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}