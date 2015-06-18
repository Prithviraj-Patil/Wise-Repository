package edu.ucla.wise.studyspacewizard.authentication;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

import edu.ucla.wise.studyspacewizard.StudySpaceWizardProperties;
import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        StudySpaceWizardProperties properties = StudySpaceWizard.getInstance().getStudySpaceWizardProperties();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(password)) {
            response.getWriter().println("Authentication failed, either username or password is null");
            return;
        }

        if (properties.getAdminUsername().equals(username) && properties.getAdminPassword().equals(password)) {
            request.getSession().setAttribute("authenticated", "true");
            response.sendRedirect("LoginPage.jsp");
            return;
        }

        response.getWriter().println("Invalid login, please try again");

    }
}
