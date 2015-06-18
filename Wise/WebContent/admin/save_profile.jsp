<%@page import="edu.ucla.wise.client.interview.InterviewManager"%>
<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=windows-1252"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=windows-1252">
<%
	//get the path
	String path = request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>Save Interviewer Profile</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
	<center>
		<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
			<tr>
				<td width="160" align=center><img
					src="admin_images/somlogo.gif" border="0"></td>
				<td width="400" align="center"><img
					src="admin_images/title.jpg" border="0"><br> <br> <font
					color="#CC6666" face="Times New Roman" size="4"><b>Saving
							WATI Assignments</b></font></td>
				<td width="160" align=center>&nbsp;</td>
			</tr>
		</table>
	</center>
	<br>
	<center>
		<table class=tth width=500 border=1 cellpadding="2" cellspacing="2"
			bgcolor=#FFFFE1>
			<tr bgcolor=#003366>
				<td align=center><font color=white>UPDATE&nbsp;&nbsp;RESULT</font></td>
			</tr>
			<tr>
				<td align=center>
					<%
						session = request.getSession(true);
						if (session.isNew()) {
							response.sendRedirect(path + "/index.html");
							return;
						}
						AdminUserSession adminUserSession = (AdminUserSession) session
								.getAttribute("ADMIN_USER_SESSION");
						StudySpace studySpace = adminUserSession.getMyStudySpace();

						//get the admin info obj
						AdminApplication adminInfo = (AdminApplication) session.getAttribute("ADMIN_INFO");
						String newId = null;

						Interviewer[] inv = (Interviewer[]) session.getAttribute("INTERVIEWER");
						if (inv == null) {
							response.sendRedirect(path + "/error.htm");
							return;
						}

						if (inv != null) {
							if (session.getAttribute("EditType") != null) {

								inv[0].userName = (String) request.getParameter("username_" + inv[0].id);
								inv[0].firstName = (String) request.getParameter("firstname_" + inv[0].id)
										.toLowerCase();
								inv[0].lastName = (String) request.getParameter("lastname_" + inv[0].id)
										.toLowerCase();
								inv[0].salutation = (String) request.getParameter("salutation_" + inv[0].id);
								inv[0].email = (String) request.getParameter("email_" + inv[0].id);
								newId = InterviewManager.getInstance().addInterviewer(studySpace, inv[0]);
								//remove the session attributes
								session.removeAttribute("EditType");

							} else {
								for (int i = 0; i < inv.length; i++) {
									inv[i].userName = (String) request.getParameter("username_" + inv[i].id);
									inv[i].firstName = (String) request.getParameter("firstname_" + inv[i].id)
											.toLowerCase();
									inv[i].lastName = (String) request.getParameter("lastname_" + inv[i].id)
											.toLowerCase();
									inv[i].salutation = (String) request
											.getParameter("salutation_" + inv[i].id);
									inv[i].email = (String) request.getParameter("email_" + inv[i].id);
									//record the changes of profile
									newId = InterviewManager.getInstance().saveProfile(studySpace, inv[i]);

								}
							}
						} else {
							out.println("Error - can not get the interviewer object.");
						}
					%> <%
 	if (newId != null) {
 %> Record Added/Updated successfully! <%
 	} else {
 %> Record Add/Update failed!
				</td>
				<%
					}
				%>
			</tr>
		</table>
		<p>
		<p>
		<p>
		<p>
			<a href="list_interviewer.jsp"><img src="admin_images/back.gif"
				border="0"></a>
	</center>

</body>
</html>
