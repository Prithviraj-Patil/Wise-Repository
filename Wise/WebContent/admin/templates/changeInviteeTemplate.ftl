<html>
	<head>
		<meta http-equiv="Content-Type"	content="text/html; charset=UTF-8">
		<script src="javascript/changeInvitee.js"></script>
		<link rel="stylesheet" href="style.css" type="text/css">
		<title>WISE Administration Tools - Edit Invitee Table</title>
	</head>
	
	<body text="#333333" bgcolor="#FFFFCC">
		<center>
			<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
			<tr>
			<td width="160" align=center><img src="admin_images/somlogo.gif"
			border="0"></td>
			<td width="400" align="center"><img src="admin_images/title.jpg"
			border="0"></td>
			<td width="160" align=center>&nbsp;</td>
			</tr>
			</table>
			<p>

			${crudMessage}
			
			<table cellpadding=2 cellpadding="0" cellspacing="0" border=0>
			<tr>
			<td align=center>
			<form name="form1" method="post" onsubmit="return check_submit()"
			action="change_invitee.jsp">
				<table class=tth border=1 cellpadding="4" cellspacing="0"
				bgcolor=#FFFFF5>
				<tr>
					<td bgcolor="#CC6666" align=center colspan=3><font color=white><b>Edit
					Invitee Tables</b></font></td>
				</tr>
				<tr>
					<td width="150" align=center><b>Name</b></td>
					<td width="150" align=center><b>Type</b></td>
					<td width="50" align=center><b>Default</b></td>
				</tr>
				
				${inviteeTableDescription}
				
				<tr>
				<td align=center bgcolor="#CC6666" colspan=3><font color=white><b>Add
				New Column</b></font></td>
			</tr>
			<tr>
				<td align=center>Column Name:</td>
				<td colspan=2><input type="text" name="cname" maxlength="30"
					size="20" value=""></td>
			</tr>
			<tr>
				<td align=center>Column Type:</td>
				<td colspan=2><input type="text" name="ctype" maxlength="30"
					size="20" value=""></td>
			<tr>
				<td align=center>Default Value:</td>
				<td colspan=2><input type="text" name="cdefault" maxlength="30"
					size="20" value="null"> <input type="hidden" name="cedit"
					value="add"> <input type="hidden" name="coname" value="">
				</td>
			</tr>
			<tr>
				<td colspan=3 align=center><input type="image" alt="submit"
					src="admin_images/submit.gif">&nbsp;&nbsp; <img alt="reset"
					src="admin_images/reset.gif" onClick="document.form1.reset()"></td>
				</td>
			</tr>
		</table>
		</form>
		</td>
	<tr>
		<td align=center>
		<p><a href="load_invitee.jsp">Return to Manage Invitees</a>
		</td>
	</tr>
</table>
</center>
</body>
</html>
		