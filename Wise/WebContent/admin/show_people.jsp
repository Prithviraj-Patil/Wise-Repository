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
<title>WISE Administration Tools - Group Invitees</title>
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
<p>
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
        String survey_id = request.getParameter("s");
        String state_id = request.getParameter("st");
     	 //security feature changes
    	if(SanityCheck.sanityCheck(survey_id) || SanityCheck.sanityCheck(state_id)) {
    		response.sendRedirect(path + "/admin/error_pages/sanity_error.html");
    		return;
    	}
    	survey_id=SanityCheck.onlyAlphaNumeric(survey_id);
    	state_id=SanityCheck.onlyAlphaNumeric(state_id);
    	if(survey_id == null || survey_id.isEmpty() || state_id==null || state_id.isEmpty()){
    		response.sendRedirect(path + "/admin/parameters_error.html");
    		return;
    	}
    	//end of security features changes
    	
        //if the session is invalid, display the error
        if(adminUserSession == null || survey_id == null) {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }

        String state_title="";
		if (state_id.equals("not_invited"))
	state_title = "Potential users who have not been invited";
		else if (state_id.equals("invited"))
	state_title = "Invitees who have received initial invites but have not responded";
		else if (state_id.equals("started"))
	state_title = "Invitees who seem to be taking the survey now (no interrupt detected)";
		else if (state_id.equals("declined"))
	state_title = "Invitees who have explicitly declined";
		else if (state_id.equals("interrupted"))
	state_title = "Incompletes (interrupt detected) awaiting first completion reminder";
		else if (state_id.equals("start_reminder"))
	state_title = "Non-responders who have received one or more start reminders";
		else if (state_id.equals("non_responder"))
	state_title = "Final non-responders, after all start reminders";
		else if (state_id.equals("completion_reminder"))
	state_title = "Incompletes who have received one or more completion reminders";
		else if (state_id.equals("incompleter"))
	state_title = "Final incompletes, after all completion reminders";
		else if (state_id.equals("completed"))
	state_title = "Invitees who completed the survey";
		else if (state_id.equals("all"))
	state_title = "All users in our system";

        //print out the user groups identified by their state
%>

<table cellpadding=2 cellspacing="0" border=1 bgcolor=#FFFFF5>
	<tr>
		<td height=30 bgcolor="#6666CC" align=center colspan=7><font
			color=white><b><%=state_title%></b></font></td>
	</tr>
	<%=adminUserSession.printUserState(state_id, survey_id)%>
</table>
<p>
</center>
</body>
</html>
