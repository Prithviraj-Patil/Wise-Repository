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
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.ucla.wise.client.web.WiseHttpRequestParameters;

/**
 * XsltViewServlet is a class used when user tries to print an overview list of
 * the pages in a survey
 * 
 */
public class XsltViewServlet extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(XsltViewServlet.class);
    static final long serialVersionUID = 1000;

    /**
     * Generates an xslt version of the survey.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html");

        /* get information from java session */
        String path = request.getContextPath();
        HttpSession session = request.getSession(true);
        if (session.isNew()) {
            response.sendRedirect(path + "/index.htm");
            return;
        }

        String surveyName = request.getParameter("FileName");

        WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(request);

        /* check if the session is still valid */
        AdminUserSession adminUserSession = parameters.getAdminUserSessionFromHttpSession();
        if ((surveyName == null) || (adminUserSession == null)) {
            LOGGER.error("Wise Admin - XSLT View Error: can't get the admin info", null);
            return;
        }

        /* get the file xml and processor xslt */
        String fXml = adminUserSession.getStudyXmlPath() + surveyName;

        String fXslt = "/CME/WISE_pages/style/survey_all_pg.xslt";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(fXml);

            /* use a Transformer for output */
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new StreamSource(fXslt));

            /* reserve the DOCTYPE setting in XML file */
            if (document.getDoctype() != null) {
                String systemValue = (new File(document.getDoctype().getSystemId())).getName();
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
            }

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(response.getOutputStream());
            transformer.transform(source, result);
        } catch (ParserConfigurationException e) {
            System.out.println("  " + e.getMessage());
            LOGGER.error("Wise Admin - XSLT View Error: " + e.getMessage(), e);
        } catch (SAXException e) {
            System.out.println("  " + e.getMessage());
            LOGGER.error("Wise Admin - XSLT View Error: " + e.getMessage(), e);
        } catch (TransformerConfigurationException e) {
            System.out.println("  " + e.getMessage());
            LOGGER.error("Wise Admin - XSLT View Error: " + e.getMessage(), e);
        } catch (TransformerException e) {
            System.out.println("  " + e.getMessage());
            LOGGER.error("Wise Admin - XSLT View Error: " + e.getMessage(), e);
        }
    }
}
