/**
 * 
 */
package edu.ucla.wise.studyspace.parameters;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

/**
 * @author Pralav
 *
 */

@WebServlet("/getStudySpaceParams")
public class ParametersProviderServlet extends HttpServlet {

	public static final String STUDY_SPACE_NAME_PARAMETER = "studyName";
	public static final String STUDY_SPACE_KEY_PARAMETER = "studyKey";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
	{
		PrintWriter out;
		try {
			out = response.getWriter();
			//TODO: First get the parameters in the post method and do some sort of authentication
			String studySpaceName = request.getParameter(STUDY_SPACE_NAME_PARAMETER);
			String studySpaceKey = request
					.getParameter(STUDY_SPACE_KEY_PARAMETER);
			
			if (Strings.isNullOrEmpty(studySpaceName) || Strings.isNullOrEmpty(studySpaceKey)) {
				out.write("FAILURE");
				return;
			}
			/*
			 * if (StringEncoderDecoder.checkStringWithKey(studySpaceName,
			 * studySpaceKey)) {
			 * 
			 * // TODO: once verified, just go to the study space
			 * out.write(DatabaseConnector
			 * .getStudySpaceParamsAsJSON(studySpaceName)); } else { out.write(
			 * "Failed to authenticate properly. Please check the Study Space Name-Key pair, or ask the developers for assitance"
			 * ); }
			 */
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
