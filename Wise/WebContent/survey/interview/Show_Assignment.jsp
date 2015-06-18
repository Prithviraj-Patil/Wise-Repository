<%@page import="org.apache.log4j.Logger"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page language="java"%><%@ page
	import="edu.ucla.wise.commons.*,edu.ucla.wise.client.interview.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>
<STYLE>
.tw {
	FILTER: progid:                           
		DXImageTransform.Microsoft.dropShadow(  
		
		  
		        
		       Color=          
		          #999999, offX=                     4, offY=
		                  
		 4, positive=     
		        
		      true);
	BORDER-LEFT-COLOR: #333;
	BORDER-BOTTOM-COLOR: #333;
	BORDER-TOP-COLOR: #333;
	BORDER-RIGHT-COLOR: #333;
	BORDER-COLLAPSE: collapse;
	Border-spacing: 1
}
</STYLE>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="javascript">
	function open_profile() {
		var profilewin = window.open('Edit_Profile.jsp', 'edit_win',
				'height=300, width=400, toolbar=no');
		if (profilewin.opener == null)
			profilewin.opener = self;
	}
</script>
<%
	final Logger LOGGER = Logger.getLogger(this.getClass());
	//get the path
	String path = request.getContextPath();
	session = request.getSession(true);
	Interviewer inv = null;
	String interviewer_id = null;

	//if the call came from the admin - interviewer manage page
	String study_id = request.getParameter("SID");
	interviewer_id = request.getParameter("InterviewerID");
	if (study_id != null && !study_id.equalsIgnoreCase("")) {
		StudySpace theStudy = StudySpaceMap.getInstance().get(study_id);
		//create interviewer object
		inv = InterviewManager.getInstance().getInterviewer(theStudy, interviewer_id);
		if (inv == null) {
			out.println("Show Assignment Error: Can not get the interviewer with id = "
					+ interviewer_id);
			return;
		}
		//save the interviewer into the session
		session.setAttribute("INTERVIEWER", inv);
	}
	//if the call came from the interviewer login page
	else {
		inv = (Interviewer) session.getAttribute("INTERVIEWER");
		if (inv != null) {
			interviewer_id = inv.getId();
			study_id = inv.getStudySpace().id;
		} else {
			out.println("Show Assignment Error: Can not get the interviewer from session");
			return;
		}
	}

	//create the after-logon page
%>
<link rel="stylesheet" href="styleRender?app=&css=style.css"
	type="text/css">
<title>WATI Interview Show Assignment</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
	<center>
		<table width=100% cellpadding=2 cellpadding="0" cellspacing="0"
			border=0>
			<tr>
				<td align=center><font color="#954A00" size=4><b>W E
							L C O M E&nbsp;&nbsp;to&nbsp;&nbsp;W A T I</b></font></td>
				<td align=center valign=top width="30%">
					<table width=100% class=tw border=0 cellpadding="0" cellspacing="0"
						bgcolor=#F0F0FF>
						<tr>
							<TD width="1" height="98%" bgColor=#6699CC rowSpan=7></TD>
							<TD width="98%" height="1" bgColor=#6699CC colspan=2></TD>
							<TD width="1" height="98%" bgColor=#6699CC rowSpan=7></TD>
						</tr>
						<tr bgcolor=#6699CC>
							<td align=center colspan=2><font color=white>Interviewer
									Profile</font></td>
						</tr>
						<tr>
							<td class=spt>&nbsp;First Name</td>
							<td align="center"><%=inv.getFirstName()%></td>
						</tr>
						<tr>
							<td class=spt>&nbsp;Last Name</td>
							<td align="center"><%=inv.getLastName()%></td>
						</tr>
						<tr>
							<td class=spt>&nbsp;Email</td>
							<td align="center"><%=inv.getEmail()%></td>
						</tr>
						<tr>
							<td class=spt>&nbsp;Login Date</td>
							<td align="center"><%=inv.getLoginTime()%></td>
						</tr>
						<tr>
							<TD width="98%" height=1 colspan=2 bgColor=#6699CC></TD>
						</tr>
					</table>
				</td>
			</tr>
		</table>
		<p>
		<p>
		<table width=100% border=0 cellpadding="2" cellspacing="2"
			bgcolor=#FFFFCC>
			<tr>
				<td align=center>Hello <%=inv.getFirstName().toUpperCase()%>!
					Please select the invitee/survey pair
				</td>
			</tr>
			<tr>
				<td align=center>
					<table class=tth width=100% border=1 cellpadding="2"
						cellspacing="2" bgcolor=#FFFFF5>
						<tr bgcolor=#954A00>
							<td align=center colspan=8><font color=white>I N T E
									R V I E W</font></td>
						</tr>
						<tr>
							<td class=sfon align=center>Invitee ID</td>
							<td class=sfon align=center>Invitee Name</td>
							<td class=sfon align=center>Invitee's Phone Number</td>
							<td class=sfon align=center>Survey ID</td>
							<td class=sfon align=center>Survey Title</td>
							<td class=sfon align=center>Assigned Date</td>
							<td class=sfon align=center>Functions</td>
						</tr>

						<%
							try {
								//connect to the database
								Connection conn = inv.getStudySpace().getDB().getDBConnection();
								Statement stm = conn.createStatement();
								Statement stm_a = conn.createStatement();
								Statement stm_b = conn.createStatement();

								//verify the interviewer
								String sql = "select * from interview_assignment where interviewer='" + inv.getId()
										+ "' and pending=1";
								boolean exe_results = stm.execute(sql);
								ResultSet rs = stm.getResultSet();

								while (rs.next()) {
									String invitee_fn = null;
									String invitee_ln = null;
									String invitee_phone = null;
									String survey_title = null;

									String assign_id = rs.getString("id");
									String invitee_id = rs.getString("invitee");
									String survey_id = rs.getString("survey");
									String assign_date = rs.getString("assign_date");

									String sql_a = "select * from invitee where id='" + invitee_id + "'";
									boolean results_a = stm_a.execute(sql_a);
									ResultSet rs_a = stm_a.getResultSet();
									if (rs_a.next()) {
										invitee_fn = rs_a.getString("firstname");
										invitee_ln = rs_a.getString("lastname");
										//invitee_phone = rs_a.getString("phone");
									}

									String sql_b = "select * from surveys where id='" + survey_id + "'";
									boolean results_b = stm_b.execute(sql_b);
									ResultSet rs_b = stm_b.getResultSet();
									while (rs_b.next()) {
										survey_title = rs_b.getString("title");
									}
						%>
						<tr>
							<td align="center"><%=invitee_id%></td>
							<td align="center"><%=invitee_fn.toUpperCase()%> <%=invitee_ln.toUpperCase()%></td>
							<td align="center">N/A</td>
							<td align="center"><%=survey_id%></td>
							<td align="center"><%=survey_title%></td>
							<td align="center"><%=assign_date%></td>
							<td>
								<table border=0 cellpadding=2>
									<tr>
										<td align=center><a
											href="Begin_Interview.jsp?assignid=<%=assign_id%>&inviteeid=<%=invitee_id%>&surveyid=<%=survey_id%>"
											target="_blank">Begin Interview</a></td>
									</tr>
								</table>
							</td>
						</tr>
						<%
							} //end of while

							} //end of try
							catch (Exception e) {
								LOGGER.error("WATI - SHOW_ASSIGNMENT: " + e.toString(), e);
								return;
							}
						%>


					</table>
				</td>
			</tr>
		</table>
		<p>
			<a href="<%=path%>/survey/interview_logout"> <img
				src="<%=path%>/survey/imageRender?img=log_out.gif" border="0"></a>&nbsp;









		
	</center>
</body>
</html>