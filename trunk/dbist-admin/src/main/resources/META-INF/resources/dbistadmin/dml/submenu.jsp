<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="net.sf.common.util.ReflectionUtils"%>
<%@page import="org.springframework.core.io.Resource"%>
<%@page import="java.io.FileNotFoundException"%>
<%@page import="org.springframework.util.StringUtils"%>
<%@page import="java.io.File"%>
<%@page import="org.springframework.util.ResourceUtils"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page import="net.sf.common.util.ValueUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%
	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", request.getProtocol().equals("HTTP/1.1") ? "no-cache" : "no-store");

	class Select {
		String value;
		List<String> optionList = new ArrayList<String>();
	}

	String prefix = ValueUtils.toNotNull(request.getParameter("prefix")).trim();
	List<Select> selectList = new ArrayList<Select>();
	if (!ValueUtils.isEmpty(prefix) && !prefix.contains("*")) {
		String[] paths = request.getParameterValues("path");
		int pathSize = ValueUtils.isEmpty(paths) ? 0 : paths.length;
		int i = 0;
		String location = "classpath*:" + StringUtils.replace(prefix, ".", "/")
				+ (prefix.endsWith("/") || prefix.endsWith(".") ? "**" : "/**");
		List<String> nameList = new ArrayList<String>();
		{
			String prefixRemove = StringUtils.replace(prefix, "/", ".") + (prefix.endsWith(".") ? "" : ".");
			List<String> list = ReflectionUtils.getClassNameList(location, null);
			for (String className : list)
				nameList.add(className.replaceFirst(prefixRemove, ""));
		}
		while (!ValueUtils.isEmpty(nameList)) {
			Select select = new Select();
			selectList.add(select);
			List<String> nextNameList = new ArrayList<String>();
			String path = pathSize > i ? paths[i++] : null;
			String pathDot = path + ".";
			for (String className : nameList) {
				if (className.contains(".")) {
					String name = className.substring(0, className.indexOf("."));
					if (!select.optionList.contains(name))
						select.optionList.add(name);
					if (className.startsWith(pathDot)) {
						select.value = path;
						nextNameList.add(className.substring(className.indexOf(".") + 1));
					}
				} else {
					select.optionList.add(className);
					if (className.equals(path))
						select.value = path;
				}
			}
			nameList = nextNameList;
		}
	}
%>
<form name="submenuForm">
	<table>
		<tr>
			<td><input name="menu" type="hidden"
				value="<%=ValueUtils.toNotNull(request.getParameter("menu"))%>" />
				class: <input id="dmlSubmenuPrefix" name="prefix" type="text"
				value="<%=prefix%>" /> <%
 	for (Select select : selectList) {
 %> <select name="path" onchange="submit()">
					<option value=""></option>
					<%
						for (String option : select.optionList) {
					%>
					<option value="<%=option%>"
						<%=option.equals(select.value) ? "selected=\"selected\"" : ""%>><%=option%></option>
					<%
						}
					%>
			</select> <%
 	}
 %> <input type="submit" value="OK" class="button" /></td>
		</tr>
	</table>
</form>