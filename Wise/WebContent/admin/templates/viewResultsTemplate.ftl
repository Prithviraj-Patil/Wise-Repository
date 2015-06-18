<head>
<meta http-equiv="Content-Type"
	content="text/html; charset=UTF-8">
<script src="javascript/viewResult.js"></script>
<link rel="stylesheet" href="admin/style.css" type="text/css">
<title>WISE Administration Tools - View Results</title>
</head>
<body text="#333333" bgcolor="#FFFFCC">
<center>
<table cellpadding=2 cellspacing="0" border=0>
	<tr>
		<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
		<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"><br>
		<br>
		<font color="#CC6666" face="Times New Roman" size="4"><b>VIEW
		RESULTS</b></font></td>
		<td width="160" align=center><a href="tool.jsp"><img
			src="admin_images/back.gif" border="0"></a></td>
	</tr>
</table>
<p>
<p>
<form name="form1" method='post' action='${path}/admin/survey_result'><input
	type='hidden' name='s' value='${surveyId}'> <br>

<table class=tth border=1 cellpadding="2" cellspacing="0"
	bgcolor=#FFFFF5>
	<tr>
		<td colspan=4>Enter WHERE clause for invitees in the data table:
		<input type='text' name='whereclause' width=60></td>
	</tr>
	<tr>
		<td colspan=4>OR <input type='checkbox' name='alluser'
			value='ALL' onClick='javascript: remove_check_oneuser()'>
		Select ALL recipients</td>
	</tr>
	<tr>
		<td colspan=4>OR select recipients for the message:</td>
	</tr>
	${results}
</table>
<br>
<center><input type="image" alt="submit"
	src="admin_images/viewresults.gif"><br>
</form>
<hr>
</center>
</body>
</html>
