<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Destroy study space</title>
<link href="css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
	<div class="hero-unit">
		<h3>WISE Destroy Study Space</h3>
	</div>
		<form name="destroyStudySpaceForm" class="form-horizontal" action="destroyStudySpace"
		method="get" >
		<div class="control-group">
			<label class="control-label" for="studySpaceName">Study Space
				Name:</label>
			<div class="controls">
				<input type="text" name="studySpaceName">
				<input type="submit" class="btn" value="Submit">	
			</div>
		</div>
		
		</form>
</body>
</html>