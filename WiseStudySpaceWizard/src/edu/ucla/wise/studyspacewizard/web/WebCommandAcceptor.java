package edu.ucla.wise.studyspacewizard.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard;

@WebServlet("/WebCommand")
public class WebCommandAcceptor extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(WebCommandAcceptor.class);

    public static final String COMMAND = "command";

    public static enum Command {
        RELOAD_STUDY_SPACES
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {

        PrintWriter out = res.getWriter();
        String commandString = req.getParameter(COMMAND);

        LOGGER.debug("commandString: '" + commandString + "'");

        Command command = Command.valueOf(commandString);

        switch (command) {
        case RELOAD_STUDY_SPACES:
            LOGGER.debug("Reloading study space parameters in connected instances");
            this.reloadStudySpacesInManagedInstances();
            break;
        default:
            out.write("The functionality to execute '" + command + "' is not implemented");
        }

    }

    private void reloadStudySpacesInManagedInstances() throws IOException {

        StudySpaceWizard.getInstance().reloadStudySpaceParametersOnManagedInstances();

    }
}
