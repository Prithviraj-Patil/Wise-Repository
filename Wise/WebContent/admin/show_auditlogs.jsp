<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,
java.sql.*, java.util.Date, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, 
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
        //get the server path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Audit Logs</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
		<td width="160" align=center><a href="javascript: history.go(-1)"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<p>
<%
	session = request.getSession(true);
        //if the session is expired, go back to the logon page
        if (session.isNew())
        {
            response.sendRedirect(path+"/index.html");
            return;
        }

        //get the admin info object from the session
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
             //print out the user groups identified by their state
%>

<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#6666CC" align=center colspan=7><font
			color=white><b><%="AUDIT LOGS"%></b></font></td>
	</tr>
	<%=adminUserSession.printAuditLogs()%>
</table>
<p>
</center>
</body>
</html>
