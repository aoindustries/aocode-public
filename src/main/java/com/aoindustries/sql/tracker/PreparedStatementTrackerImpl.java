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

import com.aoindustries.lang.Throwables;
import com.aoindustries.sql.wrapper.PreparedStatementWrapperImpl;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks a {@link PreparedStatement} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class PreparedStatementTrackerImpl extends PreparedStatementWrapperImpl implements PreparedStatementTracker {

	public PreparedStatementTrackerImpl(ConnectionTrackerImpl connectionTracker, PreparedStatement wrapped) {
		super(connectionTracker, wrapped);
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	// Statement
	private final Map<ResultSet,ResultSetTrackerImpl> trackedResultSets = synchronizedMap(new IdentityHashMap<>());
	// PreparedStatement
	private final Map<ParameterMetaData,ParameterMetaDataTrackerImpl> trackedParameterMetaDatas = synchronizedMap(new IdentityHashMap<>());
	private final Map<ResultSetMetaData,ResultSetMetaDataTrackerImpl> trackedResultSetMetaDatas = synchronizedMap(new IdentityHashMap<>());

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ResultSet,ResultSetTrackerImpl> getTrackedResultSets() {
		return trackedResultSets;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ParameterMetaData,ParameterMetaDataTrackerImpl> getTrackedParameterMetaDatas() {
		return trackedParameterMetaDatas;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ResultSetMetaData,ResultSetMetaDataTrackerImpl> getTrackedResultSetMetaDatas() {
		return trackedResultSetMetaDatas;
	}

	@Override
	protected ResultSetTrackerImpl wrapResultSet(ResultSet results) throws SQLException {
		return ConnectionTrackerImpl.getIfAbsent(trackedResultSets, results,
			() -> (ResultSetTrackerImpl)super.wrapResultSet(results),
			ResultSetTrackerImpl::getWrapped
		);
	}

	@Override
	protected ParameterMetaDataTrackerImpl wrapParameterMetaData(ParameterMetaData metaData) {
		return ConnectionTrackerImpl.getIfAbsent(trackedParameterMetaDatas, metaData,
			() -> (ParameterMetaDataTrackerImpl)super.wrapParameterMetaData(metaData),
			ParameterMetaDataTrackerImpl::getWrapped
		);
	}

	@Override
	protected ResultSetMetaDataTrackerImpl wrapResultSetMetaData(ResultSetMetaData metaData) {
		return ConnectionTrackerImpl.getIfAbsent(trackedResultSetMetaDatas, metaData,
			() -> (ResultSetMetaDataTrackerImpl)super.wrapResultSetMetaData(metaData),
			ResultSetMetaDataTrackerImpl::getWrapped
		);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  ResultSetTrackerImpl#close()
	 *
	 * @see  ParameterMetaDataTrackerImpl#close()
	 * @see  ResultSetMetaDataTrackerImpl#close()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch", "unchecked"})
	public void close() throws SQLException {
		Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0,
			// Statement
			trackedResultSets,
			// PreparedStatement
			trackedParameterMetaDatas,
			trackedResultSetMetaDatas
		);
		try {
			super.close();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}
}