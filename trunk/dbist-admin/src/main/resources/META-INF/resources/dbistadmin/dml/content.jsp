<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="java.util.List"%>
<%@page import="org.dbist.dml.Query"%>
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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
		Object data = null;
		String method = request.getParameter("_method");
		String id = ValueUtils.toNull(request.getParameter("_selected_id"));
		try {
			if ("select".equals(method)) {
				data = dml.select(clazz, ValueUtils.isEmpty(id) ? request : StringUtils.commaDelimitedListToStringArray(id));
				if (data == null)
					throw new Exception("Couldn't find the data.");
			} else if ("insert".equals(method)) {
				data = dml.insert(clazz, request);
			} else if ("update".equals(method)) {
				data = dml.update(clazz, request);
			} else if ("upsert".equals(method)) {
				data = dml.upsert(clazz, request);
			} else if ("delete".equals(method)) {
				data = dml.delete(clazz, request);
				data = null;
			} else if ("deleteList".equals(method)) {
				// TODO
			}
		} catch (Exception e) {
			ex = e;
			Logger logger = LoggerFactory.getLogger(this.getClass());
			logger.error(e.getMessage(), e);
		}
		if (data != null && id == null) {
			StringBuffer idBuf = new StringBuffer();
			int i = 0;
			for (String fieldName : table.getPkFieldNames())
				idBuf.append(i++ == 0 ? "" : ",").append(ValueUtils.toString(table.getField(fieldName).get(data), ""));
			id = idBuf.toString();
		}

		if (ex != null) {
%>
<div class="scope errorScope">
	error message:
	<%=ex.getMessage()%></div>
<%
	}
%>
<div class="scope">
	<form name="dataForm" method="post" onsubmit="return dataForm._method.value != '' && (dataForm._method.value != 'delete' || confirm('Delete?'))">
		<input name="_method" type="hidden" value="" />
		<div class="titleScope">
			<%=clazz.getSimpleName()%>
			(<%=table.getName()%>) Data
			<div class="titleButtonScope">
				<input type="submit" value="Select" class="button" onmouseover="dataForm._method.value = 'select'" onmouseout="listForm._method.value = ''" /> <input
					type="submit" value="Insert" class="button" onmouseover="dataForm._method.value = 'insert'" onmouseout="listForm._method.value = ''" /> <input
					type="submit" value="Update" class="button" onmouseover="dataForm._method.value = 'update'" onmouseout="listForm._method.value = ''" /> <input
					type="submit" value="Upsert" class="button" onmouseover="dataForm._method.value = 'upsert'" onmouseout="listForm._method.value = ''" /> <input
					type="submit" value="Delete" class="button" onmouseover="dataForm._method.value = 'delete'" onmouseout="listForm._method.value = ''" /> <input
					type="submit" value="Clear" class="button" onmouseover="dataForm._method.value = 'clear'" onmouseout="listForm._method.value = ''" />
			</div>
		</div>
		<div class="scope dataScope">
			<table>
				<thead>
					<tr>
						<th>field (column)</th>
						<th>value</th>
					</tr>
				</thead>
				<tbody>
					<%
						for (String pkColumnName : table.getPkColumnNameList()) {
								Column column = table.getColumn(pkColumnName);
								String fieldName = column.getField().getName();
					%>
					<tr>
						<td class="label"><%=table.toFieldName(pkColumnName)%> (<%=pkColumnName%>) - PK</td>
						<td class="value"><input name="<%=fieldName%>" type="<%=column.isPassword() ? "password" : "text"%>" class="textInput"
							value="<%=data == null ? ValueUtils.toNotNull(request.getParameter(fieldName)) : ValueUtils.toString(column.getField().get(data))%>" /></td>
					</tr>
					<%
						}
					%>
					<%
						for (Column column : table.getColumnList()) {
								if (table.isPkColmnName(column.getName()))
									continue;
								String fieldName = column.getField().getName();
					%>
					<tr>
						<td class="label"><%=fieldName%> (<%=column.getName()%>)</td>
						<td class="value">
							<%
								if (column.isText()) {
							%><textarea name="<%=fieldName%>" class="textArea"><%=data == null ? ValueUtils.toNotNull(request.getParameter(fieldName)) : ValueUtils.toString(column.getField().get(
								data))%></textarea> <%
 	} else {
 %><input name="<%=fieldName%>" type="<%=column.isPassword() ? "password" : "text"%>" class="textInput"
							value="<%=data == null ? ValueUtils.toNotNull(request.getParameter(fieldName)) : ValueUtils.toString(column.getField().get(
								data))%>" />
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
		</div>
	</form>

	<%
		int pageIndex = ValueUtils.toInteger(ParameterUtils.get(request, "pageIndex"), 0);
			int pageSize = ValueUtils.toInteger(ParameterUtils.get(request, "pageSize"), 20);
			Query query = new Query();
			query.setPageIndex(pageIndex);
			query.setPageSize(pageSize);
			List<?> list = dml.selectList(clazz, query);
	%>
	<form name="listForm" method="post" onsubmit="return listForm._method.value != '' && (listForm._method.value != 'deleteList' || confirm('Delete?'))">
		<input name="_method" type="hidden" value="" /> <input name="_selected_id" type="hidden" value="" />
		<div class="titleScope">
			<%=clazz.getSimpleName()%>
			(<%=table.getName()%>) List
			<div class="titleButtonScope">
				<input type="button" value="Reload" class="button" onmouseover="listForm._method.value = 'reload'" onmouseout="listForm._method.value = ''" /> <input
					type="button" value="Delete" class="button" onmouseover="listForm._method.value = 'deleteList'" onmouseout="listForm._method.value = ''" />
			</div>
		</div>
		<div class="scope listScope">
			<table>
				<thead>
					<tr>
						<th class="shortFieldHeader">no</th>
						<th class="shortFieldHeader">!</th>
						<%
							for (String columnName : table.getTitleColumnNameList()) {
						%>
						<th class="titleFieldHeader"><%=table.toFieldName(columnName)%> (<%=columnName%>)</th>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnNameList()) {
						%>
						<th class="listedFieldHeader"><%=table.toFieldName(columnName)%> (<%=columnName%>)</th>
						<%
							}
						%>
					</tr>
				</thead>
				<tbody>
					<%
						if (list.isEmpty()) {
					%>
					<tr class="listRow">
						<td class="shortField"></td>
						<td class="shortField"><input type="checkbox" /></td>
						<%
							for (String columnName : table.getTitleColumnNameList()) {
						%>
						<td class="titleField"></td>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnNameList()) {
						%>
						<td class="shortField"></td>
						<%
							}
						%>
					</tr>
					<%
						} else {
								for (Object item : list) {
									StringBuffer idRefBuf = new StringBuffer();
									int i = 0;
									for (String fieldName : table.getPkFieldNames())
										idRefBuf.append(i++ == 0 ? "" : ",").append(ValueUtils.toString(table.getField(fieldName).get(item), ""));
									String idRef = idRefBuf.toString();
					%>
					<tr class="listRow<%=idRef.equals(id) ? " listRowSelected" : ""%>"
						onclick="listForm._method.value = 'select'; listForm._selected_id.value = '<%=idRef%>'; listForm.submit();">
						<td class="shortField"></td>
						<td class="shortField"><input type="checkbox" /></td>
						<%
							for (String columnName : table.getTitleColumnNameList()) {
						%>
						<td class="titleField"><%=ValueUtils.toString(table.getFieldByColumnName(columnName).get(item))%></td>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnNameList()) {
						%>
						<td class="listedField"><%=ValueUtils.toString(table.getFieldByColumnName(columnName).get(item))%></td>
						<%
							}
						%>
					</tr>
					<%
						}
							}
					%>
				</tbody>
			</table>
		</div>
	</form>
</div>
<%
	}
%>