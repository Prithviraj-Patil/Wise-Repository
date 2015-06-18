<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%>
<%@page import="edu.ucla.wise.admin.healthmon.HealthMonitoringManager"%>
<%@page import="edu.ucla.wise.admin.healthmon.HealthStatus"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
	//get the server path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Load Invitee</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
	<center>
		<table cellpadding=2 cellspacing="0" border=0>
			<tr>
				<td width="160" align=center><img
					src="admin_images/somlogo.gif" border="0"></td>
				<td width="400" align="center"><img
					src="admin_images/title.jpg" border="0"></td>
				<td width="160" align=center>&nbsp;</td>
			</tr>
			<%
				String dbCellColor = HealthStatus.getInstance().isDbIsAlive() ? "#008000"
						: "#FF0000";
				String smtpCellColor = HealthStatus.getInstance().isSmtpIsAlive() ? "#008000"
						: "#FF0000";
			%>
			<table cellpadding=2 cellspacing="0" border=0>
				<tr>
					<td align=center>
						<table class=tth border=1 cellpadding="6" cellspacing="0"
							bgcolor=#FFFFF5>
							<tr>
								<td bgcolor="#CC6666" align=center><font color=white><b>Manage
											Health</b></font></td>
								<td bgcolor="#CC6666"></td>
							</tr>
							<tr>
								<td><b>Component</b></td>
								<td><b>Health</b></td>
							</tr>
							<tr>
								<td><i>Database Server</i></td>
								<td bgcolor="<%=dbCellColor%>" />
							</tr>
							<tr>
								<td><i>Smtp Server</i></td>
								<td bgcolor="<%=smtpCellColor%>" />
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</table>
	</center>
</body>
</html>