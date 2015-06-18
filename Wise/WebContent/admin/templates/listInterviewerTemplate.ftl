<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<link rel="stylesheet" href="admin/css/style.css" type="text/css">
<title>List All the Interviewers</title>
<STYLE>
.tw {
	FILTER: progid : DXImageTransform.Microsoft.dropShadow (   Color =
		#999999, offX = 4, offY = 4, positive = true );
	BORDER-LEFT-COLOR: #333;
	BORDER-BOTTOM-COLOR: #333;
	BORDER-TOP-COLOR: #333;
	BORDER-RIGHT-COLOR: #333;
	BORDER-COLLAPSE: collapse;
	Border-spacing: 1
}
</STYLE>
<script language="javascript">
function remove_profile()
{
 document.form1.edit_type.value = "1";
 document.form1.submit();
}
</script>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<p>
<p>
<p>
<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>EDIT
		INTERVIEWER</b></font></td>
		<td width="160" align=center><a href="tool.jsp">
		<img src="admin_images/finish.gif" border="0"></a></td>
	</tr>
</table>
<form name="form1" method="post" action="edit_profile.jsp">
<table width=400 border=1 cellpadding="2" cellspacing="0">
	<tr bgcolor=#6699CC>
		<td align=center colspan=2><font color=white>Edition Tools</font></td>
	</tr>
	<tr>
		<td align=center><a href="edit_profile.jsp" target="_self"> <img
			src="admin_images/add.gif" border="0"></a></td>
		<td>Add a new interviewer</td>
	</tr>
	<tr>
		<td align=center><input type="image" alt="submit"
			src="admin_images/edit.gif"></td>
		<td>Select and update the existing interviewers</td>
	</tr>
	<tr>
		<td align=center><a href="javascript: remove_profile()"
			target="_self"> <img src="admin_images/remove.gif" border="0"></a>
		</td>
		<td>Select and remove the existing interviewers</td>
	</tr>
	<tr>
		<td colspan=2>
		<p><input type="hidden" name="edit_type" value="0">
		</td>
	</tr>
</table>
<p>
<p>
<table width=600 class=tw border=1 cellpadding="2" cellspacing="2"
	bgcolor=#FFFFE1>
	<tr>
		<TD width="1" height="98%" bgColor=#6699CC rowSpan=100></TD>
		<TD width="98%" height="1" bgColor=#6699CC colspan=7></TD>
		<TD width="1" height="98%" bgColor=#6699CC rowSpan=100></TD>
	</tr>
	<tr bgcolor=#6699CC>
		<td align=center colspan=7><font color=white>Current
		Interviewer List</font></td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td align=center class=spt>&nbsp;User Name</td>
		<td align=center class=spt>&nbsp;Salutation</td>
		<td align=center class=spt>&nbsp;First Name</td>
		<td align=center class=spt>&nbsp;Last Name</td>
		<td align=center class=spt>&nbsp;Email</td>
		<td align=center class=spt>&nbsp;Go to WATI</td>
	</tr>

${output}

	<tr>
		<TD width="98%" height=1 colspan=7 bgColor=#6699CC></TD>
	</tr>
</table>
<p>
<p>
</form>
</center>
</body>
</html>
