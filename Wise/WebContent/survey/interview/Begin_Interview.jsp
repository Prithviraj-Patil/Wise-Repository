<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,edu.ucla.wise.client.interview.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
	//get the path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="styleRender?app=&css=style.css"
	type="text/css">
<title>Begin WATI Interview Session</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<%
	session = request.getSession(true);

	String assign_id = (String) request.getParameter("assignid");
	String invitee_id = (String) request.getParameter("inviteeid");
	String survey_id = (String) request.getParameter("surveyid");
	Interviewer inv = (Interviewer) session.getAttribute("INTERVIEWER");
	String study_id = inv.getStudySpace().id;

	User user = (User) session.getAttribute("USER");
	if (user != null)
		session.removeAttribute("USER");

	//assign the assigned id to interview
	inv.interviewAssignId = assign_id;
	//create a record in the survey message use table and get the msg ID
	String survey_msgid = inv.createSurveyMessage(invitee_id,
	survey_id);
	//Forward to the begin servlet in WISE
	if (survey_msgid != null)
		response.sendRedirect(path + "survey?i=interview&msg="
		+ WISEApplication.encode(survey_msgid) + "&t="
		+ WISEApplication.encode(study_id));
	else
		out.println("Begin Interivew Error: Can't create a message record in the survey message use table");
%>
</body>
</html>
