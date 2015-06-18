<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.servlet.jsp.JspWriter,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
	//get the path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Go to Wati</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<%
	session = request.getSession(true);
	if (session.isNew()) {
		response.sendRedirect(path + "/index.html");
		return;
	}

	//get the admin info obj
	AdminUserSession adminUserSession = (AdminUserSession) session
	.getAttribute("ADMIN_USER_SESSION");
	String id = request.getParameter("interview_id");
	if (adminUserSession == null || id == null) {
		response.sendRedirect(path + "/error_pages/error.htm");
		return;
	}

	//  String url = adminUserSession.study_server+"file_product/interview/Show_Assignment.jsp?SID="+adminUserSession.study_id+"&InterviewerID="+id; 
	String url = adminUserSession.getMyStudySpace().serverUrl
	+ "/WISE/survey/interview/Show_Assignment.jsp?SID="
	+ adminUserSession.getStudyId() + "&InterviewerID=" + id;
	response.sendRedirect(url);
%>
</body>
</html>

