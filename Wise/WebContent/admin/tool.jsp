<%@page import="edu.ucla.wise.admin.view.SurveyHealthInformation"%>
<%@page import="edu.ucla.wise.admin.view.SurveyInformation"%>
<%@page import="edu.ucla.wise.admin.view.ToolView"%>
<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@page import="edu.ucla.wise.client.web.WiseHttpRequestParameters"%>
<%@page import="edu.ucla.wise.commons.WiseConstants"%>
<%@page import="edu.ucla.wise.commons.WiseConstants.SURVEY_STATUS"%>
<%@page import="org.apache.catalina.authenticator.Constants"%>
<%@page import="edu.ucla.wise.admin.healthmon.HealthMonitoringManager"%>
<%@page import="edu.ucla.wise.admin.healthmon.HealthStatus"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%>
<%@ page
	import="edu.ucla.wise.commons.*,edu.ucla.wise.commons.WISEApplication,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,java.text.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%>
<html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
    ToolView toolView = new ToolView();
	//get the server path
	String path = request.getContextPath();
	path = path + "/";
	Date today1 = new Date();
	DateFormat f = new SimpleDateFormat("E");
	String wkday = f.format(today1);
	AdminUserSession adminUserSession;
	    WiseHttpRequestParameters parameters = new WiseHttpRequestParameters(
		    request);
    try {
		session = request.getSession(true);
		//if the session is expired, go back to the logon page
		if (session.isNew()) {
		    response.sendRedirect(path + WiseConstants.ADMIN_APP
			    + "/index.html");
		    return;
		}
		//get the admin info object from session
		adminUserSession = parameters
			.getAdminUserSessionFromHttpSession();
		if (adminUserSession == null) {
		    response.sendRedirect(path + WiseConstants.ADMIN_APP
			    + "/error_pages/error.htm");
		    return;
		}
		adminUserSession.loadRemote(WiseConstants.SURVEY_HEALTH_LOADER,
			adminUserSession.getStudyName());
		//get the weekday format of today to name the data backup file
    } catch (Exception e) {
		//WISE_Application.log_error("WISE ADMIN - TOOL init: ", e); 

		PrintWriter out2 = response.getWriter();
		out2.print("******There has Been and exception********");
		return;
    }
    SurveyHealthInformation healthInfo = toolView.healthStatusInfo(adminUserSession.getMyStudySpace());
		List<SurveyInformation> currentSurveysInfo = adminUserSession.getMyStudySpace().getCurrentSurveys();
%>
<link rel="stylesheet" href="css/style.css" type="text/css"></link>
<script src="javascript/console.js"></script>
<title>WISE Administration Tools</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
	<center>
		<table cellpadding=2 cellspacing=0 border=0>
			<tr>
				<td width="160" align=center><img
					src="admin_images/somlogo.gif" border="0"></td>
				<td width="400" align="center"><img
					src="admin_images/title.jpg" border="0"><br> <br> <font
					color="#CC6666" face="Times New Roman" size="4"><b><%=adminUserSession.getStudyTitle()%></b></font>
				</td>
				<td width="160" align=center><a name="logout-button" href="<%=path + WiseConstants.ADMIN_APP%>/logout"><img
						src="admin_images/logout_b.gif" border="0"></a></td>
			</tr>
		</table>
	</center>
	<p>
	<p>
	<center>
		<table border=0>
			<tr>
				<td align=left valign=middle>
					<table class=tth border=1 cellpadding="2" cellspacing="0"
						bgcolor=#FFFFF5>
						<tr>
							<td height=30 width=400 bgcolor="#FF9900" align=center><font
								color=white><b>File Upload</b></font></td>
						</tr>
						<tr>
							<td align="center" valign="middle" width="400">
								<FORM action="load_data" method="post"
									encType="multipart/form-data">
									Select a survey(xml), message(xml), preface(xml), invitee(csv),
									consent form(xml), style sheet(css) or image(jpg/gif) to
									upload:<br> &nbsp;<br> <INPUT type=file name=file>
									&nbsp; <input type="image" alt="submit"
										src="admin_images/upload.gif">
								</FORM>
							</td>
						</tr>
					</table>
					<table class=tth border=1 cellpadding="2" cellspacing="0"
						bgcolor=#FFFFF5>
						<tr>
							<td height=30 width=400 bgcolor="#FF9900" align=center><font
								color=white><b>Health</b></font></td>
						</tr>
						<tr>
							<%

							%>

							<td><b><i>Database</i> <font
									color="<%=healthInfo.dbCellColor%>"> <%=healthInfo.dbStatus%>
								</font></b>&nbsp; <b><i>Mail System</i> <font
									color="<%=healthInfo.smtpCellColor%>"> <%=healthInfo.smtpStatus%>
								</font></b>&nbsp; <b>Survey Server <i><%=adminUserSession.getStudyName()%></i>
									<font color="<%=healthInfo.surveyCellColor%>"> <%=healthInfo.surveyStatus%>
								</font></b></td>
						</tr>

					</table>

				</td>
				<td align=right valign=middle>
					<table class=tth border=1 cellpadding="2" cellspacing="2"
						bgcolor=#FFFFF5>
						<tr>
							<td height=30 width=250 bgcolor="#339999" align=center><font
								color=white><b>All-Survey Functions</b></font></td>
						</tr>
						<tr>
							<td align=left width=250><font size=-1><a
									href="load_invitee.jsp">Manage</a> invitees<br> &nbsp;<br>
									<a href="list_interviewer.jsp">Manage</a> interviewers<br>
									&nbsp;<br> <a
									href="download_file?fileName=preface.xml">Download</a>
									preface file<br> &nbsp;<br> <a
									href="download_file?fileName=style.css">Download</a> online
									style sheet<br> &nbsp;<br> <a
									href="download_file?fileName=print.css">Download</a>
									printing style sheet<br> &nbsp;<br> <a
									href="download_file?fileName=<%=adminUserSession.getStudyName()%>_<%=wkday%>.sql">Download</a>
									MySQL dump file </font></td>
						</tr>
					</table>
				</td>
			</tr>
		</table>
	</center>
	<p>
	<center>
		<table class=tth border=1 cellpadding="2" cellspacing="0"
			bgcolor=#FFFFF5>
			<tr bgcolor=#CC6666>
				<th align=center colspan=4><font color=white>CURRENT
						ACTIVE SURVEYS</font></th>
			</tr>
			<tr>
				<th class=sfon>Survey ID, <b>Title</b>, Uploaded Date & (<i>Status</i>)
				</th>
				<th class=sfon>User State</th>
				<th class=sfon>User Counts</th>
				<th class=sfon width=40%>Actions</th>
			</tr>
			<%
			    for (SurveyInformation currentSurveyInfo : currentSurveysInfo) {
			%>
			<tr>
				<td align="center"><%=currentSurveyInfo.id%><br> <br>
					<b><%=currentSurveyInfo.title%></b><br> <br><%=currentSurveyInfo.uploaded%><br>
					<br> (<i><%=currentSurveyInfo.surveyMode%> Mode</i>)<br>Copy-Paste
					link for anonymous survey users<br> <a
					href='<%=currentSurveyInfo.anonymousInviteUrl%>'> <%=currentSurveyInfo.anonymousInviteUrl%></a><br></td>
				<td align="center" colspan=2><%=adminUserSession
			    .getUserCountsInStates(currentSurveyInfo.id)%>
				</td>
				<td align="center">
					<table width=100% border=0 cellpadding=2>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="initial_invite.jsp?s=<%=currentSurveyInfo.id%>">Send
										Initial Invitation</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="initial_invite.jsp?s=<%=currentSurveyInfo.id%>&reminder=true">Resend
										Invitation</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="other_invite.jsp?s=<%=currentSurveyInfo.id%>">Send
										Other Messages</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/view_survey?s=<%=currentSurveyInfo.id%>">View
										Survey</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="view_result.jsp?s=<%=currentSurveyInfo.id%>">View
										Results</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/print_survey?a=FIRSTPAGE&s=<%=currentSurveyInfo.id%>">Print
										Survey</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="download_file?fileName=<%=currentSurveyInfo.filename%>">Download
										Current Survey File </a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/download_file?fileName=<%=currentSurveyInfo.id%>.csv">Download
										Main Survey Data Table(CSV)</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="<%=path + WiseConstants.ADMIN_APP%>/download_file?fileName=repeat_set_project.csv">Download
										Repeating Item Set Data (CSV)</a></font></td>
						</tr>
						<%
						    //if the survey is in the developing mode
								    if (currentSurveyInfo.status.equalsIgnoreCase("D")) {
						%>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: jid="<%=currentSurveyInfo.id%>"; jstatus="<%=currentSurveyInfo.status%>"; remove_confirm();'>Clear
										Survey Data</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: jid="<%=currentSurveyInfo.id%>"; jstatus="R"; remove_confirm();'>Delete
										Survey</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: sid="<%=currentSurveyInfo.internalId%>"; change_mode();'>Change
										to Production Mode</a></font></td>
						</tr>
						<%
						    }
								    //if the survey is in the production mode
								    if (currentSurveyInfo.status.equalsIgnoreCase("P")) {
						%>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href="assign_wati.jsp?s=<%=currentSurveyInfo.id%>">Assign
										Interviewers</a></font></td>
						</tr>
						<tr>
							<td width=7>&nbsp;</td>
							<td width=200><font size='-1'><a
									href='javascript: jid="<%=currentSurveyInfo.id%>"; jstatus="<%=currentSurveyInfo.status%>"; remove_confirm();'>Close
										& Archive Survey</a></font></td>
						</tr>
						<%
						    }
						%>
					</table>
				</td>
			</tr>
			<%
			    } //end of for loop
			%>
		</table>
		<p>
		<p>
		<p>
	</center>
</body>
</html>


