package org.dbist.admin;

import javax.servlet.http.HttpServletRequest;

public class ParameterUtils {

	public static String get(HttpServletRequest request, String name) {
		String sessionAttrName = request.getServletPath() + "." + name;
		String value = null;
		try {
			value = request.getParameter(name);
			if (value != null)
				return value;
			value = (String) request.getAttribute(name);
			if (value != null)
				return value;
		} finally {
			if (value != null)
				request.getSession().setAttribute(sessionAttrName, value);
		}
		return (String) request.getSession().getAttribute(sessionAttrName);
	}

	public static String[] getValues(HttpServletRequest request, String name) {
		String sessionAttrName = request.getServletPath() + "." + name;
		String[] value = null;
		try {
			value = request.getParameterValues(name);
			if (value != null)
				return value;
			value = (String[]) request.getAttribute(name);
			if (value != null)
				return value;
		} finally {
			if (value != null)
				request.getSession().setAttribute(sessionAttrName, value);
		}
		return (String[]) request.getSession().getAttribute(sessionAttrName);
	}

}
