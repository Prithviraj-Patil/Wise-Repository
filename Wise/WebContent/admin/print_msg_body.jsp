<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page
	language="java"%>
<%@ page
	import="edu.ucla.wise.commons.*, 
java.sql.*, java.util.Date, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, 
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%>
<html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<%
        //get the path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE MESSAGE - VIEW CONTENTS</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td width="150" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="350" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
		<td width="150" align=center><a href="javascript:window.close()">
		<img src="admin_images/close.gif" border="0"></a></td>
	</tr>
</table>
<p>
<%
	session = request.getSession(true);
        if (session.isNew())
        {
            response.sendRedirect(path+"/index.html");
            return;
        }

        //get the admin info obj
        AdminUserSession adminUserSession = (AdminUserSession) session.getAttribute("ADMIN_USER_SESSION");
        String seq = request.getParameter("seqID");
        String msg_id = request.getParameter("msgID");
        
        //Security feature changes
    	
    	if(SanityCheck.sanityCheck(seq) || SanityCheck.sanityCheck(msg_id)) {
    		response.sendRedirect(path + "/admin/error_pages/sanity_error.html");
    		return;
    	}
    	
    	seq = SanityCheck.onlyAlphaNumeric(seq);
    	msg_id= SanityCheck.onlyAlphaNumeric(msg_id);
    	
    	//End changes
        
        if(adminUserSession == null || msg_id == null || seq == null )
        {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }
%> <%=adminUserSession.renderMessageBody(seq, msg_id)%>
</center>
</body>
</html>
