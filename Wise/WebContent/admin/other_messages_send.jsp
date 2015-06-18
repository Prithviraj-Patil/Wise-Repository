<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%><%@ page
	import="edu.ucla.wise.commons.*, 
java.sql.*, java.util.Date, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, javax.servlet.jsp.JspWriter,
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%><html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
        //get the path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Results of Sending Other
Messages</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<%
	session = request.getSession(true);
        if (session.isNew())
        {
            response.sendRedirect(path+"/index.html");
            return;
        }

        //get the admin info obj
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
        if(adminUserSession == null)
        {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }
        
        String survey = request.getParameter("survey");
        String message = request.getParameter("msg");
        String irb = request.getParameter("irb");
        if (message == null || survey==null || irb==null)       
        {
%>
<p>Error: You must select a message</p>
<%
         return;
        }
        
        String user[] = request.getParameterValues("user");
        String whereStr = request.getParameter("whereclause");
        
        if(user==null)
        {
           if (whereStr == null || whereStr.equals(""))
           {
              out.println("<p>Error: You must select at least one invitee.</p>");
              return;
           }           
        }
        else
        {
           if (whereStr != null && !whereStr.equals(""))
            whereStr += " and invitee.id in (";
           else
            whereStr = "invitee.id in (";
           for (int i = 0; i < user.length; i++)
              whereStr += user[i] + ",";
           whereStr = whereStr.substring(0,whereStr.lastIndexOf(',')) + ")";
        }

%>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>Results
		of Sending Messages</b></font></td>
		<td width="160" align=center><a href='tool.jsp'><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<center>
<table cellpadding=2 cellspacing="0" width=400 border=0>
	<tr>
		<td><%=adminUserSession.sendMessages(message, irb, survey, whereStr, false)%></td>
	</tr>
</table>
</center>
</body>
</html>
