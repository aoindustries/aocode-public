/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2014  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of aocode-public.
 *
 * aocode-public is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * aocode-public is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with aocode-public.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.messaging.http;

import com.aoindustries.lang.NotImplementedException;
import com.aoindustries.security.Identifier;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Server component for bi-directional messaging over HTTP.
 *
 * Requires Servlet 3.0 or newer.
 * Must use with asyncSupported=true.
 */
abstract public class HttpSocketServlet extends HttpServlet {

	//private static final Logger logger = Logger.getLogger(HttpSocketServlet.class.getName());

	private static final long serialVersionUID = 1L;

	private final HttpSocketContext socketContext = new HttpSocketContext() {
		@Override
		public void onClose(HttpSocket socket) {
			throw new NotImplementedException("TODO");
		}
	};

	/**
	 * Each socket is kept here, keyed on id.
	 */
	private final Map<Identifier,HttpSocket> sockets = new LinkedHashMap<Identifier,HttpSocket>();

	/**
	 * Last modified times must never be used.
	 */
	@Override
	final protected long getLastModified(HttpServletRequest request) {
		return -1;
	}

	/**
	 * Get requests must never be used.
	 */
	@Override
	final protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getParameter("action");
		if("connect".equals(action)) {
			throw new NotImplementedException("TODO");
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unexpected action: " + action);
		}
	}

	/**
	 * When the servlet is destroyed, all active connections are
	 * also closed.
	 */
	@Override
	public void destroy() {
		throw new NotImplementedException("TODO");
	}

	/**
	 * Handles a new connection.
	 * This can register any necessary listeners on the socket itself.
	 */
	abstract void onConnect(HttpSocket socket);
}