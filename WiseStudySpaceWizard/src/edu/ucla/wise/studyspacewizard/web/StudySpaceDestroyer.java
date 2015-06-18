package edu.ucla.wise.studyspacewizard.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import edu.ucla.wise.studyspacewizard.database.DatabaseConnector;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

@WebServlet("/destroyStudySpace")
public class StudySpaceDestroyer extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
        try {
            PrintWriter out = response.getWriter();

            String studySpaceName = request.getParameter("studySpaceName");

            if (Strings.isNullOrEmpty(studySpaceName)) {
                out.write("<div>Please provide a study space name</div>");
                return;
            }

            if (databaseConnector.destroyStudySpace(studySpaceName)) {

                out.write("<div>Study space destroyed: " + studySpaceName + "</div>");
            } else {
                out.write("<div>Could not destroy study space: " + studySpaceName + "</div>");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
