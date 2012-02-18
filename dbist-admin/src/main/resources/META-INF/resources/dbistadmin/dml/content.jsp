<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="org.dbist.admin.ParameterUtils"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="java.util.HashSet"%>
<%@page import="java.util.Set"%>
<%@page import="org.dbist.metadata.Column"%>
<%@page import="org.dbist.metadata.Table"%>
<%@page import="net.sf.common.util.BeanUtils"%>
<%@page import="org.dbist.dml.Dml"%>
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

	String contextName = StringUtils.replace(ValueUtils.toString(request.getContextPath(), "dbist"), "/", "");
	BeanUtils beanUtils = BeanUtils.getInstance(contextName);

	Dml dml = null;
	Class<?> clazz = null;
	String dmlName = ValueUtils.toNull(ParameterUtils.get(request, "dml"));
	if (dmlName != null) {
		dml = beanUtils.get(dmlName, Dml.class);
		String prefix = StringUtils.replace(ValueUtils.toNotNull(ParameterUtils.get(request, "prefix")).trim(), "/", ".");
		if (!ValueUtils.isEmpty(prefix)) {
			StringBuffer buf = new StringBuffer(prefix);
			String[] paths = ParameterUtils.getValues(request, "path");
			if (!ValueUtils.isEmpty(paths)) {
				for (String path : paths) {
					if (ValueUtils.isEmpty(path))
						break;
					buf.append(buf.toString().endsWith(".") ? "" : ".").append(path);
				}
			}
			try {
				clazz = ClassUtils.forName(buf.toString(), null);
			} catch (Exception e) {
			}
		}
	}

	Exception ex = null;
	Table table = null;
	try {
		table = dml == null || clazz == null ? null : dml.getTable(clazz);
	} catch (Exception e) {
		ex = e;
		Logger logger = LoggerFactory.getLogger(this.getClass());
		logger.error(e.getMessage(), e);
	}
	if (table == null) {
%>
<div class="scope<%=ex == null ? "" : " errorScope"%>"><%=ex == null ? "no content" : "error message: " + ex.getMessage()%></div>
<%
	} else {
%>
<div class="scope">

	<div class="scope dataScope">
		<!--  -->
		<div class="titleScope">
			<%=clazz.getSimpleName()%>
			(<%=table.getName()%>) Data
			<div class="titleButtonScope">
				<input type="button" value="Select" class="button" /> <input
					type="button" value="Insert" class="button" /> <input
					type="button" value="Update" class="button" /> <input
					type="button" value="Upsert" class="button" /> <input
					type="button" value="Delete" class="button" /> <input
					type="button" value="Clear" class="button" />
			</div>
		</div>
		<form>
			<table>
				<thead>
					<tr>
						<th>field (column)</th>
						<th>value</th>
					</tr>
				</thead>
				<tbody>
					<%
						for (String pkColumnName : table.getPkColumnName()) {
					%>
					<tr>
						<td class="label"><%=table.toFieldName(pkColumnName)%> (<%=pkColumnName%>)
							- PK</td>
						<td class="value"><input
							name="<%=table.getColumn(pkColumnName).getField().getName()%>"
							class="textInput" /></td>
					</tr>
					<%
						}
					%>
					<%
						for (Column column : table.getColumn()) {
								if (table.isPkColmnName(column.getName()))
									continue;
					%>
					<tr>
						<td class="label"><%=column.getField().getName()%> (<%=column.getName()%>)</td>
						<td class="value">
							<%
								if (column.isText()) {
							%><textarea name="<%=column.getField().getName()%>"
								class="textArea"></textarea> <%
 	} else {
 %><input name="<%=column.getField().getName()%>" class="textInput" />
							<%
								}
							%>
						</td>
					</tr>
					<%
						}
					%>
				</tbody>
			</table>
		</form>
	</div>

	<div class="scope listScope">
		<form>
			<!--  -->
			<div class="titleScope">
				<%=clazz.getSimpleName()%>
				(<%=table.getName()%>) List
				<div class="titleButtonScope">
					<input type="button" value="Reload" class="button" /> <input
						type="button" value="Delete" class="button" />
				</div>
			</div>
			<table>
				<thead>
					<tr>
						<th class="shortFieldHeader">no</th>
						<th class="shortFieldHeader">!</th>
						<%
							for (String columnName : table.getTitleColumnName()) {
						%>
						<th class="titleFieldHeader"><%=table.toFieldName(columnName)%>
							(<%=columnName%>)</th>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnName()) {
						%>
						<th class="shortFieldHeader"><%=table.toFieldName(columnName)%>
							(<%=columnName%>)</th>
						<%
							}
						%>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td class="shortField"></td>
						<td class="shortField"><input type="checkbox" /></td>
						<%
							for (String columnName : table.getTitleColumnName()) {
						%>
						<td class="titleField"></td>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnName()) {
						%>
						<td class="shortField"></td>
						<%
							}
						%>
					</tr>
				</tbody>
			</table>
		</form>
	</div>
</div>
<%
	}
%>