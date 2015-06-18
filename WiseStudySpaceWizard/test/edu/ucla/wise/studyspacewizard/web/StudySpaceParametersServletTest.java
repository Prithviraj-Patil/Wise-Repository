package edu.ucla.wise.studyspacewizard.web;

import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.Test;

import com.google.gson.Gson;

import edu.ucla.wise.studyspace.parameters.StudySpaceParameters;
import edu.ucla.wise.studyspacewizard.Constants;

public class StudySpaceParametersServletTest {

	@Test
	public void testGetAllStudySpaceParameters() throws IOException{
		Gson gson = new Gson();
		String response = decryptAndGetResponse(Constants.ALL);
		List<Map<String,String>> parameters = gson.fromJson(response, List.class);
		System.out.println(parameters);
		
		
	}

	@Test
	public void testGetStudySpaceParameter() throws IOException{
		Gson gson = new Gson();
		Map<String,String> parameters = gson.fromJson(decryptAndGetResponse("wisedev"), Map.class);
		System.out.println(parameters);		

	}
	
	private String decryptAndGetResponse(String studySpaceNameParameter) throws IOException{
		String response = getResponse(studySpaceNameParameter).toString();
		System.out.println(response);
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("password");
		String decryptedResponse = textEncryptor.decrypt(response);
		System.out.println(decryptedResponse);
		return decryptedResponse;
	}

	private StringWriter getResponse(String studySpaceNameParameter) throws IOException{
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);  

		when(request.getParameter(Constants.STUDY_SPACE_NAME)).thenReturn(studySpaceNameParameter);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		when(response.getWriter()).thenReturn(printWriter);

		new StudySpaceParametersServlet().doGet(request, response);

		return stringWriter;
	}
}
