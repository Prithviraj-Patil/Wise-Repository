<%@page import="edu.ucla.wise.admin.AdminUserSession"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ page language="java"%>
<%@ page
	import="edu.ucla.wise.commons.*, java.sql.*, java.util.Date, java.util.*, java.net.*, java.io.*,
org.xml.sax.*, org.w3c.dom.*, javax.xml.parsers.*,  java.lang.*,
javax.xml.transform.*, javax.xml.transform.dom.*, javax.servlet.jsp.JspWriter,
javax.xml.transform.stream.*, com.oreilly.servlet.MultipartRequest"%>
<html>
<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8" />
<script language="javascript">
  var vid;
  var list_id;
  //vid - invited user type: all users, users with specific irb number
  //list_id - user ID list
  //check the option of the selected user
  function check_invite_list()
  {
    if(vid=="alluser")
    {
      for(i=0; i < document.form1.length; i++)
      {
        if(document.form1.elements[i].type=="checkbox" && document.form1.elements[i].name =="user")
          document.form1.elements[i].checked=true;
      }
    }
    if(vid=="irb")
    {
       if(document.form1.user.length)
       {
         for(i=0; i< document.form1.user.length; i++)
         {
            if(list_id.indexOf(" "+document.form1.user[i].value+" ")!=-1)
              document.form1.user[i].checked=true;
            else
              document.form1.user[i].checked=false;
         }
      }
      else
      {
         if(list_id.indexOf(" "+document.form1.user.value+" ")!=-1)
            document.form1.user.checked=true;
         else
            document.form1.user.checked=false;
      }
        
    }
  }
</script>
<%
        //get the server path
        String path=request.getContextPath();
%>
<link rel="stylesheet" href="<%=path%>/style.css" type="text/css">
<title>WISE Administration Tools - Send Initial Invitation</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<table width="90%" border="0" align="center" cellpadding="8">
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>Send
		Initial Invitation</b></font></td>
		<td width="160" align=center><a href="javascript: history.go(-1)"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
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
        //get the survey ID from the request
        String s_id = request.getParameter("s");
        
      //security features changes
        if(SanityCheck.sanityCheck(s_id)){
        	response.sendRedirect(path + "/" + WiseConstants.ADMIN_APP + "/sanity_error.html");
    	    return;
        }
        s_id=SanityCheck.onlyAlphaNumeric(s_id);        
        //End of security changes
        
        boolean isReminder = (request.getParameter("reminder") != null) ? Boolean.valueOf(request.getParameter("reminder")) : false;
        
        if(adminUserSession == null || s_id == null )
        {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }
        //get the IRB groups
        Hashtable irbgroup = new Hashtable();
        irbgroup = adminUserSession.getIrbGroups();
        if(irbgroup == null)
        {
            response.sendRedirect(path + "/error_pages/error.htm");
            return;
        }
%>
<table width="90%" border="0" align="center" cellpadding="8">
	<tr>
		<td><%=adminUserSession.renderInitialInviteTable(s_id, isReminder)%></td>
		<td width="250">
		<p>&nbsp;</p>
		<p>&nbsp;</p>
		<p><em><strong><a href='#'
			onClick='javascript: vid="alluser"; check_invite_list();'>Select
		All Invitees</a> </strong></em></p>
		<p><a href='#' onClick="document.form1.reset()"><em><strong>Clear
		Selection</strong></em></a></p>
<!-- 		<p>To further limit your selection, you can also enter a logic
		statement below using SQL syntax. For example, to select only invitees
		with ID &gt; 200 AND emails ending in ".edu", you can click "Select
		All" and enter "invitee.id > 200 and email like "%.edu" below.</p>
		<p align=center><input type='text' name='whereclause' size='40'
			maxlength='100'></p> -->
		</td>
	</tr>
</table>
</form>
</body>
</html>
