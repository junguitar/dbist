<?xml version="1.0" encoding="UTF-8" ?>
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
	class Resource {
		String nameSelected;
		List<File> fileList = new ArrayList<File>();
	}

	response.setDateHeader("Expires", 0);
	response.setHeader("Pragma", "no-cache");
	response.setHeader("Cache-Control", request.getProtocol().equals("HTTP/1.1") ? "no-cache" : "no-store");

	String prefix = ValueUtils.toNotNull(request.getParameter("prefix"));
	List<Resource> rscList = new ArrayList<Resource>();
	if (!ValueUtils.isEmpty(prefix)) {
		StringBuffer buf = new StringBuffer("classpath:").append(StringUtils.replace(prefix, ".", "/"));
		String[] paths = request.getParameterValues("path");
		int pathSize = ValueUtils.isEmpty(paths) ? 0 : paths.length;
		int i = 0;
		File dir = null;
		try {
			dir = ResourceUtils.getFile(buf.toString());
		} catch (FileNotFoundException e) {
		}
		while (dir != null && dir.isDirectory()) {
			File dirOld = dir;
			String path = pathSize > i ? paths[i++] : null;
			Resource rsc = new Resource();
			rscList.add(rsc);
			File[] files = dir.listFiles();
			for (File file : files) {
				String fileName = file.getName();
				if (!fileName.endsWith(".class") && !file.isDirectory())
					continue;
				rsc.fileList.add(file);
				if (fileName.equals(path)) {
					rsc.nameSelected = fileName;
					if (file.isDirectory())
						dir = file;
				}
			}
			if (ValueUtils.isEmpty(path) && rsc.fileList.size() == 1 && rsc.fileList.get(0).isDirectory()) {
				dir = rsc.fileList.get(0);
				rsc.nameSelected = dir.getName();
			} else if (dirOld.equals(dir))
				break;
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
 	for (Resource rsc : rscList) {
 %> <select name="path" onchange="submit()">
					<option value=""></option>
					<%
						for (File file : rsc.fileList) {
								String fileName = file.getName();
					%>
					<option value="<%=fileName%>"
						<%=fileName.equals(rsc.nameSelected) ? "selected=\"selected\"" : ""%>><%=fileName%></option>
					<%
						}
					%>
			</select> <%
 	}
 %> <input type="submit" value="OK" class="button" /></td>
		</tr>
	</table>
</form>