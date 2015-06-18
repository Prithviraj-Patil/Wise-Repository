<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>WISE Error</title>
</head>
<body>
	<div>
		<%=request.getParameter("error") %>
	</div>
	<div>
		<%=request.getAttribute("error") %>
	</div>
</body>
</html>