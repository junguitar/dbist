<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="net.sf.common.util.ValueUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", request.getProtocol().equals("HTTP/1.1") ? "no-cache" : "no-store");
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<%
	String menu = ValueUtils.toString(request.getParameter("menu"), "home");
	String submenuUrl = menu + "/submenu.jsp";
	String contentUrl = menu + "/content.jsp";
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Dbist - Administrator</title>
<link type="text/css" rel="stylesheet" media="screen"
	href="dbistadmin.css" />
</head>

<body>

	<div id="wrap">

		<div id="header">
			<div id="logoTitle">
				<a href="index.jsp">Dbist - Administrator</a>
			</div>
			<div id="menu">
				<a href="index.jsp?menu=home"
					<%=menu.equals("home") ? " class=\"menuSelected\"" : ""%>>Home</a>
				<a href="index.jsp?menu=dml"
					<%=menu.equals("dml") ? " class=\"menuSelected\"" : ""%>>Dml</a>
			</div>
		</div>

		<div id="container">
			<div id="submenu">
				<jsp:include page="<%=submenuUrl%>" />
			</div>
			<div id="content">
				<jsp:include page="<%=contentUrl%>" />
			</div>
		</div>

		<div id="footer">© Dbist.org</div>

	</div>

</body>

</html>