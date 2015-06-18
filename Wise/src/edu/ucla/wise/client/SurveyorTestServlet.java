/**
 * Copyright (c) 2014, Regenimport java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.SurveyorApplication;
e copyright notice, 
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
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucla.wise.commons.StudySpace;
import edu.ucla.wise.commons.StudySpaceMap;
import edu.ucla.wise.commons.SurveyorApplication;
import edu.ucla.wise.commons.WISEApplication;

/**
 * SurveyorTestServlet initializes the surveyor application and displays some of
 * the parameters.
 * 
 */
public class SurveyorTestServlet extends HttpServlet {
    static final long serialVersionUID = 1000;

    /**
     * Loads the surveryor application and study space and prints some of the
     * parameters for testing.
     * 
     * @param req
     *            HTTP Request.
     * @param res
     *            HTTP Response.
     * @throws ServletException
     *             and IOException.
     */
    @Override
    public void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        // FIND server's default file location
        // File test_file = new File("whereAmI");
        // FileOutputStream tstout = new FileOutputStream(test_file);
        // tstout.write(100);
        // tstout.close();
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();

        HttpSession session = req.getSession(true);
        session.getServletContext();

        // Surveyor_Application s = (Surveyor_Application) session
        // .getAttribute("SurveyorInst");

        /* get the encoded study space ID */
        String spaceidEncode = req.getParameter("t");

        /* get the email message ID */
        String msgidEncode = req.getParameter("msg");

        StudySpace myStudySpace = null;
        String thesharedFile = "";
        // String id2 = "";
        if (spaceidEncode != null) {
            myStudySpace = StudySpaceMap.getInstance().get(spaceidEncode); // instantiates
            // the study
            if (myStudySpace != null) {
                // id2 = myStudySpace.id;
                thesharedFile = myStudySpace.sharedFileUrlRoot;
                // Message_Sequence[] msa = myStudySpace.preface
                // .get_message_sequences("Enrollmt");
                // for (int i = 0; i < msa.length; i++)
                // id2 += "; " + msa[1].toString();
                // }
            }

            out.println("<HTML><HEAD><TITLE>Begin Page</TITLE>"
                    + "<LINK href='../file_product/style.css' type=text/css rel=stylesheet>"
                    + "<body text=#000000 bgColor=#ffffcc><center><table>"
                    + "<tr><td>Successful test. StudySpace id [t]= "
                    + spaceidEncode
                    + "</td></tr>"
                    + "<tr><td>Root URL= "
                    + WISEApplication.getInstance().getWiseProperties().getServerRootUrl()
                    + "</td></tr>"
                    + "<tr><td>XML path = "
                    + WISEApplication.getInstance().getWiseProperties().getXmlRootPath()
                    + "</td></tr>"
                    + "<tr><td>SS file path = "
                    + thesharedFile
                    + "</td></tr>"
                    // + "<tr><td>Image path = " +
                    // Surveyor_Application.image_root_path + "</td></tr>"
                    // + "<tr><td>DB backup path = " +
                    // Surveyor_Application.db_backup_path + "</td></tr>"
                    + "<tr><td>Context Path= " + SurveyorApplication.ApplicationName + "</td></tr>"
                    + "<tr><td>Servlet Path= " + SurveyorApplication.getInstance().getServletUrl() + "</td></tr>"
                    + "<tr><td>message id= " + msgidEncode + "</td></tr>" + "</table></center></body></html>");
        }
    }
}