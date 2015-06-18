<%@page import="edu.ucla.wise.commons.WISEApplication"%>
<%@page import="edu.ucla.wise.initializer.Version"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>About</title>
</head>
<body>
Version: <%= Version.VERSION %>
<br/>
Properties:
<br/><pre>
<%WISEApplication.getInstance().getWiseProperties().store(out, "properties");%>
</pre><hr/>

<h2><a name="WISE">WISE Log</a></h2><pre>
<%= Version.getChangeLogText() %>
</pre><br/><hr/>

</body>
</html>