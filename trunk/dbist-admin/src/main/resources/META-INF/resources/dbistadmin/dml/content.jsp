<?xml version="1.0" encoding="UTF-8" ?>
<%@page import="org.dbist.dml.Filters"%>
<%@page import="java.util.ArrayList"%>
<%@page import="org.dbist.dml.Filter"%>
<%@page import="org.dbist.dml.Page"%>
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
	String dmlName = ValueUtils.toNull(ParameterUtils.get(request, "_dml"));
	if (dmlName != null) {
		dml = beanUtils.get(dmlName, Dml.class);
		String by = ParameterUtils.get(request, "_by");
		if ("table".equals(by)) {
			String table = ParameterUtils.get(request, "_table");
			if (!ValueUtils.isEmpty(table)) {
				try {
					clazz = dml.getClass(table);
				} catch (Exception e) {
				}
			}
		} else {
			String prefix = StringUtils.replace(ValueUtils.toNotNull(ParameterUtils.get(request, "_prefix")).trim(), "/", ".");
			if (!ValueUtils.isEmpty(prefix)) {
				StringBuffer buf = new StringBuffer(prefix);
				String[] paths = ParameterUtils.getValues(request, "_path");
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
		String id = ValueUtils.toNull(request.getParameter("_selectedId"));
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
			} else if ("clear".equals(method)) {
				data = clazz.newInstance();
			} else if ("deleteList".equals(method)) {
				String[] listChecked = request.getParameterValues("_listChecked");
				Query query = new Query();
				if (!ValueUtils.isEmpty(listChecked)) {
					if (!listChecked[0].contains(",")) {
						query.addFilter((String) table.getPkFieldNames()[0], (Object) listChecked);
					} else {
						for (String idRef : listChecked) {
							query.setOperator("or");
							Filters filters = new Filters();
							query.addFilters(filters);
							int i = 0;
							for (String value : StringUtils.tokenizeToStringArray(idRef, ","))
								filters.addFilter(table.getPkFieldNames()[i++], value);
						}
					}
					dml.deleteList(clazz, query);
				}
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
			<a title="<%=clazz.getName()%>"><%=clazz.getSimpleName()%> (<%=table.getName()%>) Data</a>
			<div class="titleButtonScope">
				<input type="submit" value="Select" class="button" onmouseover="dataForm._method.value = 'select'" onmouseout="dataForm._method.value = ''" /> <input
					type="submit" value="Insert" class="button" onmouseover="dataForm._method.value = 'insert'" onmouseout="dataForm._method.value = ''" /> <input
					type="submit" value="Update" class="button" onmouseover="dataForm._method.value = 'update'" onmouseout="dataForm._method.value = ''" /> <input
					type="submit" value="Upsert" class="button" onmouseover="dataForm._method.value = 'upsert'" onmouseout="dataForm._method.value = ''" /> <input
					type="submit" value="Delete" class="button" onmouseover="dataForm._method.value = 'delete'" onmouseout="dataForm._method.value = ''" /> <input
					type="submit" value="Clear" class="button" onmouseover="dataForm._method.value = 'clear'" onmouseout="dataForm._method.value = ''" />
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
							value="<%=data == null ? ValueUtils.toNotNull(request.getParameter(fieldName)) : ValueUtils.toString(column.getField().get(data),
							"")%>" /></td>
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
							%><textarea name="<%=fieldName%>" class="textArea"><%=data == null ? ValueUtils.toNotNull(request.getParameter(fieldName)) : ValueUtils.toString(
								column.getField().get(data), "")%></textarea> <%
 	} else {
 %><input name="<%=fieldName%>" type="<%=column.isPassword() ? "password" : "text"%>" class="textInput"
							value="<%=data == null ? ValueUtils.toNotNull(request.getParameter(fieldName)) : ValueUtils.toString(
								column.getField().get(data), "")%>" />
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
		String className = clazz.getName();
			boolean useParam = className.equals(ParameterUtils.get(request, "_className"));
			String leftOperand = useParam ? ParameterUtils.get(request, "_leftOperand") : null;
			String operator = null;
			String rightOperand = "";
			String order = useParam ? ValueUtils.toNotNull(ParameterUtils.get(request, "_order")) : "";
			String orderDirection = useParam ? ValueUtils.toNotNull(ParameterUtils.get(request, "_orderDirection")) : "";
			int pageIndex = ValueUtils.toInteger(ParameterUtils.get(request, "_pageIndex"), 0);
			int pageSize = ValueUtils.toInteger(ParameterUtils.get(request, "_pageSize"), 10);
			Query query = new Query();
			if (!ValueUtils.isEmpty(leftOperand) && table.getField(leftOperand) != null) {
				operator = ParameterUtils.get(request, "_operator");
				rightOperand = ValueUtils.toNotNull(ParameterUtils.get(request, "_rightOperand"));
				if (ValueUtils.isEmpty(rightOperand)) {
					query.addFilter(new Filter(leftOperand, operator, rightOperand == null || rightOperand.isEmpty() ? null : rightOperand));
				} else {
					query.addFilter(new Filter(leftOperand, operator, ValueUtils.toList(StringUtils.tokenizeToStringArray(rightOperand, ","))
							.toArray()));
				}
			}
			if (!ValueUtils.isEmpty(order))
				query.addOrder(order, !"desc".equals(orderDirection));
			query.setPageIndex(pageIndex);
			query.setPageSize(pageSize);
			Page<?> _page = null;
			Exception exc = null;
			try {
				_page = dml.selectPage(clazz, query);
			} catch (Exception e) {
				exc = e;
				_page = new Page<Object>();
				_page.setList(new ArrayList(0));
			}
			int lastPageIndex = _page.getLastIndex();
			int pageGroupFromIndex = (pageIndex / pageSize) * pageSize;
			int pageGroupToIndex = Math.min(lastPageIndex, pageGroupFromIndex + pageSize - 1);
			int totalSize = _page.getTotalSize();
			List<?> list = _page.getList();

			if (exc != null) {
	%>
	<div class="scope errorScope">
		error message:
		<%=ex.getMessage()%></div>
	<%
		}
	%>
	<form name="listForm" method="post" onsubmit="return listForm._method.value != '' && (listForm._method.value != 'deleteList' || confirm('Delete?'))">
		<input name="_method" type="hidden" value="" /><input name="_className" type="hidden" value="<%=className%>" /><input name="_order" type="hidden"
			value="<%=order%>" /><input name="_orderDirection" type="hidden" value="<%=orderDirection%>" /><input name="_pageIndex" type="hidden"
			value="<%=pageIndex%>" /><input name="_selectedId" type="hidden" value="" />
		<div class="titleScope">
			<a title="<%=className%>"><%=clazz.getSimpleName()%> (<%=table.getName()%>) List</a>
			<div class="titleButtonScope">
				filter: <select name="_leftOperand">
					<option></option>
					<%
						for (Column column : table.getColumnList()) {
								String fieldName = column.getField().getName();
					%>
					<option value="<%=fieldName%>" <%=fieldName.equals(leftOperand) ? "selected=\"selected\"" : ""%>><%=fieldName%></option>
					<%
						}
					%>
				</select> <select name="_operator">
					<option value="=" <%="=".equals(operator) ? "selected=\"selected\"" : ""%>>=</option>
					<option value="like" <%="like".equals(operator) ? "selected=\"selected\"" : ""%>>like</option>
					<option value="!=" <%="!=".equals(operator) ? "selected=\"selected\"" : ""%>>!=</option>
					<option value="&lt;" <%="<".equals(operator) ? "selected=\"selected\"" : ""%>>&lt;</option>
					<option value="&lt;=" <%="<=".equals(operator) ? "selected=\"selected\"" : ""%>>&lt;=</option>
					<option value="&gt;" <%=">".equals(operator) ? "selected=\"selected\"" : ""%>>&gt;</option>
					<option value="&gt;=" <%=">=".equals(operator) ? "selected=\"selected\"" : ""%>>&gt;=</option>
				</select> <input name="_rightOperand" value="<%=rightOperand%>" onkeydown="listForm._method.value = 'reload'" /> &nbsp;&nbsp;pageSize: <select
					name="_pageSize"><option value="5" <%=pageSize == 5 ? "selected=\"selected\"" : ""%>>5</option>
					<option value="10" <%=pageSize == 10 ? "selected=\"selected\"" : ""%>>10</option>
					<option value="20" <%=pageSize == 20 ? "selected=\"selected\"" : ""%>>20</option>
					<option value="50" <%=pageSize == 50 ? "selected=\"selected\"" : ""%>>50</option>
					<option value="100" <%=pageSize == 100 ? "selected=\"selected\"" : ""%>>100</option>
				</select> <input type="submit" value="Reload" class="button" onmouseover="listForm._method.value = 'reload'" /> <input type="submit" value="Delete"
					class="button" onmouseover="listForm._method.value = 'deleteList'" onmouseout="listForm._method.value = ''" />
			</div>
		</div>
		<div class="scope listScope">
			<div class="pagination">
				page:
				<%
				if (pageGroupFromIndex != 0) {
						if (pageGroupFromIndex > pageSize) {
			%>
				<a onclick="listForm._pageIndex.value = '0'; listForm.submit();">&lt;1&gt;</a>
				<%
					}
				%>
				<a onclick="listForm._pageIndex.value = '<%=pageGroupFromIndex - 1%>'; listForm.submit();">&lt;<%=pageGroupFromIndex%>&gt;
				</a>
				<%
					}

						for (int i = pageGroupFromIndex; i < pageGroupToIndex + 1;) {
							if (i == pageIndex) {
				%>
				<b><%=++i%></b>
				<%
					} else {
				%>
				<a onclick="listForm._pageIndex.value = '<%=i%>'; listForm.submit();"><%=++i%></a>
				<%
					}
						}

						if (pageGroupToIndex < lastPageIndex) {
				%>
				<a onclick="listForm._pageIndex.value = '<%=pageGroupToIndex + 1%>'; listForm.submit();">&lt;<%=pageGroupToIndex + 2%>&gt;
				</a> <a onclick="listForm._pageIndex.value = '<%=lastPageIndex%>'; listForm.submit();">&lt;<%=lastPageIndex + 1%>&gt;
				</a>
				<%
					}
				%>
				<div class="totalSize">
					(total:
					<%=totalSize%>)
				</div>
			</div>
			<table>
				<thead>
					<tr class="listHeader">
						<th class="shortFieldHeader">no</th>
						<th class="shortFieldHeader">!</th>
						<%
							for (String columnName : table.getTitleColumnNameList()) {
									String fieldName = table.toFieldName(columnName);
									boolean orderField = order.equals(fieldName);
									String orderMark = orderField ? orderDirection.equals("asc") ? ">>" : "<<" : "";
						%>
						<th class="titleFieldHeader"
							onclick="listForm._order.value='<%=fieldName%>'; listForm._orderDirection.value='<%=orderField && orderDirection.equals("asc") ? "desc" : "asc"%>'; listForm.submit();">
							<%=fieldName%> <%=orderMark%></th>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnNameList()) {
									String fieldName = table.toFieldName(columnName);
									boolean orderField = order.equals(fieldName);
									String orderMark = orderField ? orderDirection.equals("asc") ? ">>" : "<<" : "";
						%>
						<th class="listedFieldHeader"
							onclick="listForm._order.value='<%=fieldName%>'; listForm._orderDirection.value='<%=orderField && orderDirection.equals("asc") ? "desc" : "asc"%>'; listForm.submit();">
							<%=fieldName%> <%=orderMark%></th>
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
								int no = pageIndex * pageSize;
								for (Object item : list) {
									StringBuffer idRefBuf = new StringBuffer();
									int i = 0;
									for (String fieldName : table.getPkFieldNames())
										idRefBuf.append(i++ == 0 ? "" : ",").append(ValueUtils.toString(table.getField(fieldName).get(item), ""));
									String idRef = idRefBuf.toString();
					%>
					<tr class="listRow<%=idRef.equals(id) ? " listRowSelected" : ""%>">
						<td class="shortField" onclick="listForm._method.value = 'select'; listForm._selectedId.value = '<%=idRef%>'; listForm.submit();"><%=++no%></td>
						<td class="shortField"><input name="_listChecked" type="checkbox" value="<%=idRef%>" /></td>
						<%
							for (String columnName : table.getTitleColumnNameList()) {
						%>
						<td class="titleField" onclick="listForm._method.value = 'select'; listForm._selectedId.value = '<%=idRef%>'; listForm.submit();"><%=ValueUtils.toString(table.getFieldByColumnName(columnName).get(item), "")%></td>
						<%
							}
						%>
						<%
							for (String columnName : table.getListedColumnNameList()) {
						%>
						<td class="listedField" onclick="listForm._method.value = 'select'; listForm._selectedId.value = '<%=idRef%>'; listForm.submit();"><%=ValueUtils.toString(table.getFieldByColumnName(columnName).get(item), "")%></td>
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