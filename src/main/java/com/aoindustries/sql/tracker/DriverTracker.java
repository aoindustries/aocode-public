/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2020  AO Industries, Inc.
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
package com.aoindustries.sql.tracker;

import com.aoindustries.sql.wrapper.DriverWrapper;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tracks a {@link Driver} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
// TODO: logger with package as root?
public class DriverTracker extends DriverWrapper implements IDriverTracker {

	private static final Logger logger = Logger.getLogger(DriverTracker.class.getName());

	public DriverTracker(Driver wrapped) {
		super(wrapped);
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	private final Map<Connection,ConnectionTracker> trackedConnections = synchronizedMap(new IdentityHashMap<>());

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Connection,ConnectionTracker> getTrackedConnections() {
		return trackedConnections;
	}

	@Override
	protected ConnectionTracker newConnectionWrapper(Connection connection) {
		return ConnectionTracker.newIfAbsent(trackedConnections, this, connection, ConnectionTracker::new);
	}

	@Override
	public void deregister() {
		Throwable t0 = ConnectionTracker.clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = ConnectionTracker.clearCloseAndCatch(t0, trackedConnections);
		if(t0 != null) {
			Logger l;
			try {
				l = getParentLogger();
			} catch(SQLFeatureNotSupportedException e) {
				l = logger;
			}
			l.log(Level.WARNING, "Errors during deregister closing connections", t0);
		}
	}
}
