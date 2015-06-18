<%@page import="com.google.common.base.Strings"%>
<%@page import="org.apache.log4j.Logger"%>
<%@ page contentType="text/html;charset=UTF-8"%><%@ page language="java"%><%@ page
	import="edu.ucla.wise.commons.*,java.sql.*,java.util.Date,java.util.*,java.net.*,java.io.*,org.xml.sax.*,org.w3c.dom.*,javax.xml.parsers.*,java.lang.*,javax.xml.transform.*,javax.xml.transform.dom.*,javax.xml.transform.stream.*,com.oreilly.servlet.MultipartRequest"%><html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<script language="javascript">
	function getCookie(c_name) {
		var c_value = document.cookie;
		var c_start = c_value.indexOf(" " + c_name + "=");
		if (c_start == -1) {
			c_start = c_value.indexOf(c_name + "=");
		}
		if (c_start == -1) {
			c_value = null;
		} else {
			c_start = c_value.indexOf("=", c_start) + 1;
			var c_end = c_value.indexOf(";", c_start);
			if (c_end == -1) {
				c_end = c_value.length;
			}
			c_value = unescape(c_value.substring(c_start, c_end));
		}
		return c_value;
	}

	function submit_inv() {
		document.form2.SID.value = getCookie('JSESSIONID');
		console.log("The SID value is:''" + document.form2.SID.value + "'");
		//document.form2.submit();
	}
</script>

<%
	Logger logger = Logger.getLogger("new_invitee.jsp");

	//get the server path
	String path = request.getContextPath();
	String surveyId_encode = request.getParameter("s");
	String spaceid_encode = request.getParameter("t");

	if (Strings.isNullOrEmpty(surveyId_encode) || Strings.isNullOrEmpty(spaceid_encode)) {
		response.sendRedirect(path + "/admin/parameters_error.html");
		return;
	}

	String surveyId = CommonUtils.base64Decode(surveyId_encode);
	// get the encoded study space ID
	// decode study space ID
	String spaceid_decode = WISEApplication.decode(spaceid_encode);

	StudySpace theStudy = StudySpaceMap.getInstance().get(spaceid_decode);
	if (theStudy == null || theStudy.getSurvey(surveyId) == null) {
		response.sendRedirect(SurveyorApplication.getInstance().getSharedFileUrl()
				+ "link_error" + WiseConstants.HTML_EXTENSION);
		return;
	}
%>
<link rel="stylesheet"
	href="styleRender?app=<%=theStudy.studyName%>&css=style.css"
	type="text/css">

<title>WISE New Invitee Information</title>
</head>
<body>
	<!-- <body bgcolor="#FFFFCC" text="#000000"> -->

	<center>
		<table width=100% cellspacing=1 cellpadding=9 border=0>
			<tr>
				<td width=98 align=center valign=top><img
					src='imageRender?app=<%=theStudy.studyName%>&img=w1_logo.jpg'
					border=0 align=middle></td>
				<td width=695 align=center valign=middle><img
					src='imageRender?app=<%=theStudy.studyName%>&img=title.gif'
					border=0 align=middle></td>
				<td rowspan=6 align=center width=280>&nbsp;</td>
			</tr>
			<tr>
				<td width=98 rowspan=3>&nbsp;</td>
				<td class=head>WELCOME</td>
			</tr>
			<tr>
				<td width=695 align=left colspan=1>
					<p>You are using our Survey Software for the first time. Before
						beginning the survey we request you to please fill in the
						information in the table below. Thank you for your cooperation!</p>
				</td>
			</tr>
			<tr>
				<td align=center>
					<form name="form2" method="post" action="save_anno_user">
						<table class=tth border=1 cellpadding="6" cellspacing="0"
							bgcolor=#FFFFF5>
							<tr>
								<td width=400 bgcolor="#CC6666" align=center><font
									color=white><b>New Invitee</b></font></td>
							</tr>
							<%
								out.println(theStudy.db.displayAddInvitee(surveyId));
							%>
							<input type='hidden' name="save" value="true">
							<input type='hidden' name='SID'>
							<input type='hidden' name='t' value='<%=spaceid_encode%>'>
						</table>
					</form>
				</td>
			</tr>
		</table>
	</center>
</body>
</html>