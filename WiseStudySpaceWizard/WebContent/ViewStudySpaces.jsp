<%@page import="edu.ucla.wise.studyspacewizard.initializer.StudySpaceWizard"%>
<%@page import="edu.ucla.wise.studyspacewizard.web.StudySpaceParametersAcceptor"%>
<%@page import="edu.ucla.wise.studyspacewizard.database.DatabaseConnector,java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Create Study Space</title>
<!-- Add twitter bootstrap libraries -->
<link href="css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
	<!-- Make input boxes for entering all parameters -->
	<div class="hero-unit">
		<h3>WISE Current Study Spaces</h3>
	</div>
	<div class='container-fluid'>
	<table class = 'table table-striped'>
	<%
	DatabaseConnector databaseConnector = StudySpaceWizard.getInstance().getDatabaseConnector();
	for( Map<String,String> studySpaceParams : databaseConnector.getAllStudySpaceParameters()){

		out.write("<tr><td><h3>");
		out.write(studySpaceParams.get(StudySpaceParametersAcceptor.STUDY_SPACE_NAME));
		out.write("</h3><td>");
		out.write("<td>"+studySpaceParams+"</td>");
		out.write("</tr>");

	}
	%>
	</table>
	</div>
	<div>
		<a href="WebCommand?command=RELOAD_STUDY_SPACES">Click here to reload study space parameters on all connected studies</a>
	</div>
</body>
</html>