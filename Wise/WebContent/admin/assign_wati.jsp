<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,
			java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,
			javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.servlet.jsp.JspWriter,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
	//get the path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Assign Wati</title>
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
	String s_id = request.getParameter("s");
	//security features changes
	if(SanityCheck.sanityCheck(s_id)) {
		response.sendRedirect(path + "/admin/error_pages/sanity_error.html");
		return;
	}
	s_id=SanityCheck.onlyAlphaNumeric(s_id);
	//end of security features changes
	
	if (adminUserSession == null || s_id == null) {
		response.sendRedirect(path + "/error_pages/error.htm");
		return;
	}
%>
<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>Assign
		WATI</b></font></td>
		<td width="160" align=center><a href="javascript: history.go(-1)"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<p>
<p>
<p>
<form method='post' action='<%=path%>/save_wati.jsp'><input
	type='hidden' name='survey' value='<%=s_id%>'>
<hr>
<%=adminUserSession.printInterviewer()%>
<hr>
<table class=tth border=1 cellpadding="2" cellspacing="0"
	bgcolor=#FFFFF5>
	<tr>
		<td>Enter WHERE clause for invitees <input type='text'
			name='whereclause' width=60></td>
	</tr>
	<tr>
		<td>OR <input type='checkbox' name='alluser' value='ALL'>
		Select all invitees</td>
	</tr>
	<tr>
		<td>OR <input type='checkbox' name='nonrespuser' value='ALL'>
		Select all non-responders</td>
	</tr>
	<tr>
		<td>OR select invitees for the assignments:</td>
	</tr>
</table>
<%=adminUserSession.printInvite()%>
<hr>
<center><input type="image" alt="submit"
	src="admin_images/assign.gif">
</form>
</center>
</body>
</html>
