<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<link rel="stylesheet" href="admin/css/style.css" type="text/css">
<title>WISE Administration Tools - Saving the WATI assignments</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>Saving
		WATI Assignments</b></font></td>
		<td width="160" align=center><a href='javascript: history.go(-1)'><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
</center>
<center>
<form method='post' action='reassign_wati.jsp'>

<table class=tth width=600 border=1 cellpadding="2" cellspacing="2"
	bgcolor=#FFFFE1>
	<tr bgcolor=#003366>
		<td align=center><font color=white>ASSIGN&nbsp;&nbsp;WATI</font></td>
	</tr>
	<tr bgcolor=#996600>
		<td align=center><font color=white>Survey ID:${surveyId}</font></td>
	</tr>
	<tr>
		<td align=center>
${output}
		</td>
	</tr>
	<tr>
		<td align=center>Now you can go to the WATI page of the current
		interviewer: <a href='${url}'><img
			src="admin_images/go_view.gif" border="0"></a> OR go back to admin
		page by clicking the back button above.</td>
	</tr>
</table>

</form>
</center>
</body>
</html>
