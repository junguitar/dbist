<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="org.springframework.util.ClassUtils"%>
<%@page import="org.springframework.util.StringUtils"%>
<%@page import="net.sf.common.util.ValueUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", request.getProtocol().equals("HTTP/1.1") ? "no-cache" : "no-store");

	Class<?> clazz = null;
	String className = null;
	String prefix = StringUtils.replace(ValueUtils.toNotNull(request.getParameter("prefix")).trim(), "/", ".");
	if (!ValueUtils.isEmpty(prefix)) {
		StringBuffer buf = new StringBuffer(prefix);
		String[] paths = request.getParameterValues("path");
		if (!ValueUtils.isEmpty(paths)) {
			for (String path : paths) {
				if (ValueUtils.isEmpty(path))
					break;
				buf.append(buf.toString().endsWith(".") ? "" : ".").append(path);
			}
		}
		try {
			clazz = ClassUtils.forName(buf.toString(), null);
		} catch (ClassNotFoundException e) {
		}
	}
%>
<%
	if (clazz == null) {
%>
<div class="scope">no content</div>
<%
	} else {
%>
<div class="scope">

	<div class="scope dataScope">
		<!--  -->
		<div class="titleScope">
			<%=clazz.getSimpleName()%>
			Data
			<div class="titleButtonScope">
				<input type="button" value="Select" class="button" /> <input
					type="button" value="Insert" class="button" /> <input
					type="button" value="Update" class="button" /> <input
					type="button" value="Upsert" class="button" /> <input
					type="button" value="Delete" class="button" /> <input
					type="button" value="Clear" class="button" />
			</div>
		</div>
		<table>
			<thead>
				<tr>
					<th>field</th>
					<th>value</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<th></th>
					<td></td>
				</tr>
			</tbody>
		</table>
	</div>

	<div class="scope listScope">
		<!--  -->
		<div class="titleScope">
			<%=clazz.getSimpleName()%>
			List
			<div class="titleButtonScope">
				<input type="button" value="New" class="button" /> <input
					type="button" value="Delete" class="button" />
			</div>
		</div>
		<table>
			<thead>
				<tr>
					<th>No</th>
					<th></th>
					<th>Title</th>
					<th>Updated At</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td></td>
					<td></td>
					<td></td>
				</tr>
			</tbody>
		</table>
	</div>

</div>
<%
	}
%>