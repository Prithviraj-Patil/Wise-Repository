<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@page import="com.google.common.base.Strings"%>
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
<title>WISE Administration Tools - Results of Sending Initial
	Invitation</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
	<center>
		<%
			session = request.getSession(true);
			if (session.isNew()) {
				response.sendRedirect(path + "/index.html");
				return;
			}

			//get the admin info obj
			AdminUserSession adminUserSession = (AdminUserSession) session
					.getAttribute("ADMIN_USER_SESSION");
			if (adminUserSession == null) {
				response.sendRedirect(path + "/error_pages/error.htm");
				return;
			}

			String seq_id = request.getParameter("seq");
			String svy_id = request.getParameter("svy");
			
			//security features changes
			        if(SanityCheck.sanityCheck(seq_id) || SanityCheck.sanityCheck(svy_id)){
			        	response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
			    	    return;
			        }
			        seq_id=SanityCheck.onlyAlphaNumeric(seq_id);
			        svy_id=SanityCheck.onlyAlphaNumeric(svy_id);
			        //End of security changes
			
			boolean isReminder = Boolean.valueOf(request
					.getParameter("reminder"));
			if (seq_id == null || svy_id == null) {
		%>
		<p>Error: Message sequence or survey identity missing</p>
		<%
			return;
			}

			String user[] = request.getParameterValues("user");
			String whereStr = request.getParameter("whereclause");
			
			//security features changes
			ArrayList<String> inputUsers = new ArrayList<String>();
			for(String temp:user){
				inputUsers.add(temp);
			}
	        if(SanityCheck.sanityCheck(whereStr) || SanityCheck.sanityCheck(inputUsers)){
	        	response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
	    	    return;
	        }
	        //End of security changes
			

			if (user == null) {
				if (whereStr == null || whereStr.equals("")) {
					out.println("<p>Error: You must select at least one invitee.</p>");
					return;
				}
			} else {
				if (!Strings.isNullOrEmpty(whereStr))
					whereStr += " and invitee.id in (";
				else
					whereStr = "invitee.id in (";
				for (int i = 0; i < user.length; i++)
					whereStr += user[i] + ",";
				whereStr = whereStr.substring(0, whereStr.lastIndexOf(','))
						+ ")";
			}
		%>
		<table cellpadding=2 cellspacing="0" border=0>
			<tr>
				<td width="160" align=center><img
					src="admin_images/somlogo.gif" border="0"></td>
				<td width="400" align="center"><img
					src="admin_images/title.jpg" border="0"><br> <br> <font
					color="#CC6666" face="Times New Roman" size="4"><b>Invitation
							attempt - Results</b></font></td>
				<td width="160" align=center><a href='tool.jsp'><img
						src="admin_images/back.gif" border="0"></a></td>
			</tr>
		</table>
		<table cellpadding=2 cellspacing="0" width=400 border=0>
			<tr>
				<td><%=adminUserSession.sendMessages("invite", seq_id, svy_id,
					whereStr, isReminder)%></td>
			</tr>
		</table>
	</center>
</body>
</html>
