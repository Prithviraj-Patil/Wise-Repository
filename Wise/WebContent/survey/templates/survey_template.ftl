<!doctype html>
<html lang='en'>
<head>
	<title>Web-based Interactive Survey Environment (WISE)</title>
	<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>
	<meta http-equiv='X-UA-Compatible' content='IE=Edge'/>
	<script type='text/javascript' src='${sharedUrl}/js/main.js'></script>
	<script type='text/javascript' src='${sharedUrl}/js/survey.js'></script>
	<script type='text/javascript' language='javascript'>
		top.fieldVals = null;
		top.requiredFields = null;
		var userId = ${userId};
	</script>
	<link href='css/progress.css' type='text/css' rel='stylesheet'>
</head> 
<body onload='javascript: setFields();check_preconditions();'>
	<div id='content'>
	${pageHtml}
	</div>
	<div id='progress_bar'>
	${progressBarHtml}
	</div>
	<div class='modal'><!-- Place at bottom of page --></div>
</body>
</html>