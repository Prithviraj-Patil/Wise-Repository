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
<script language="javascript">
  var vid;
  var list_id;
  //vid - invited user type: all users, reponders only or completers only
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
    if(vid=="incomplete" || vid=="nonrespond" || vid=="irb")
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
<title>WISE Administration Tools - Send Other Survey Messages</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
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
        String survey_id = request.getParameter("s");
        if(adminUserSession == null || survey_id == null )
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

        //String nonresponder_id = " ";
        //String incompleter_id = " ";
        String [] sp_user = new String[2];

        adminUserSession.getNonrespondersIncompleters(sp_user, survey_id);

        String nonresponder_id = sp_user[0];
        String incompleter_id = sp_user[1];
%>
<center>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>Invite
		Users</b></font></td>
		<td width="160" align=center><a href="javascript: history.go(-1)"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<p>
<p>
<p>
<form name="form1" method='post' action='other_messages_send.jsp'>
<input type='hidden' name='survey' value='<%=survey_id%>'> <input
	type='hidden' name='irb' value=''> <br>
<hr>
<table class=tth border=1 cellpadding="3" cellspacing="2"
	bgcolor=#FFFFF5>
	<tr>
		<td width=400>Click one of the following buttons to select
		subsets of the invitees:</td>
		<td width=70 rowspan=6 align=center valign=middle><input
			type="image" alt="submit" src="admin_images/send.gif"></td>
	</tr>
	<tr>
		<td align=center width=400><img name='alluser' alt="all users"
			src="admin_images/allrecipients.gif" border=0
			onClick='javascript: vid="alluser"; check_invite_list();'>
		&nbsp; <img name='nonresponder' alt="non-responders"
			src="admin_images/nonresponders.gif" border=0
			onClick='javascript: list_id="<%=nonresponder_id%>"; vid="nonrespond"; check_invite_list();'>
		&nbsp; <img name='incompleters' alt="incompleters"
			src="admin_images/incompleters.gif" border=0
			onClick='javascript: list_id="<%=incompleter_id%>"; vid="incomplete"; check_invite_list();'>
		<%
                for (Enumeration e = irbgroup.keys(); e.hasMoreElements();)
                {
                   String irb_key = (String) e.nextElement();
                   String irb_id_list = (String) irbgroup.get(irb_key);
                   out.println("&nbsp;<a href='#' onClick='javascript: list_id=\""+irb_id_list+"\"; vid=\"irb\"; check_invite_list(); return false;'>IRB ID "+irb_key+"</a>");
                }
%> &nbsp; <img alt="reset" src="admin_images/reset.gif"
			onClick="document.form1.reset()"></td>
	</tr>
	<tr>
		<td align=left width=400>If you want to further limit your
		selection, you can also write an SQL WHERE clause. It will apply in
		addition to your selections below. For example, If you want all
		invitees with ID &gt; 200 and emails ending in ".edu", you can click
		"Select All" and enter "invitee.id > 200 and email like "%.edu"
		<p align=center><input type='text' name='whereclause' size='60'
			maxlength='100'></p>
		</td>
	</tr>
	<tr>
		<td width=400>You can manually select/deselect the one you want
		among the following invitees:</td>
	</tr>
</table>
<%=adminUserSession.printInviteeWithState(survey_id)%> <br>
<center><input type="image" alt="submit"
	src="admin_images/send.gif"><br>
</form>
<hr>
</center>
</body>
</html>
