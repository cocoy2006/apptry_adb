<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
	<script type="text/javascript" src="resources/js/jquery.js"></script>
	<script type="text/javascript">
	<!--
	//window.location = "index";
	$(document).ready(function() {
		$("#apptry").load("resources/vnc_auto.html");	
	});
	
	//-->
	</script>
  </head>
  
  <body>
    This is my JSP page. <br>
    <a href="resources/vnc_auto.html">vnc auto</a>
    <div id="apptry"></div>
  </body>
</html>
