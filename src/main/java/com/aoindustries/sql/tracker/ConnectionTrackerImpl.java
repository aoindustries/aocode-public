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

import com.aoindustries.collections.IdentityKey;
import com.aoindustries.collections.wrapper.Converter;
import com.aoindustries.collections.wrapper.FunctionalConverter;
import com.aoindustries.collections.wrapper.MapWrapper;
import com.aoindustries.lang.AutoCloseables;
import com.aoindustries.lang.Runnables;
import com.aoindustries.lang.Throwables;
import com.aoindustries.sql.wrapper.ConnectionWrapperImpl;
import com.aoindustries.sql.wrapper.StatementWrapperImpl;
import com.aoindustries.util.concurrent.CallableE;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Tracks a {@link Connection} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class ConnectionTrackerImpl extends ConnectionWrapperImpl implements ConnectionTracker {

	public ConnectionTrackerImpl(DriverTracker driver, Connection wrapped) {
		super(driver, wrapped);
	}

	public ConnectionTrackerImpl(Connection wrapped) {
		super(wrapped);
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	private final Map<Array,ArrayTrackerImpl> trackedArrays = synchronizedMap(new IdentityHashMap<>());
	private final Map<Blob,BlobTrackerImpl> trackedBlobs = synchronizedMap(new IdentityHashMap<>());
	private final Map<CallableStatement,CallableStatementTrackerImpl> trackedCallableStatements = synchronizedMap(new IdentityHashMap<>());
	private final Map<Clob,ClobTrackerImpl> trackedClobs = synchronizedMap(new IdentityHashMap<>());
	private final Map<DatabaseMetaData,DatabaseMetaDataTrackerImpl> trackedDatabaseMetaDatas = synchronizedMap(new IdentityHashMap<>());
	private final Map<InputStream,InputStreamTracker> trackedInputStreams = synchronizedMap(new IdentityHashMap<>());
	private final Map<NClob,NClobTrackerImpl> trackedNClobs = synchronizedMap(new IdentityHashMap<>());
	private final Map<OutputStream,OutputStreamTracker> trackedOutputStreams = synchronizedMap(new IdentityHashMap<>());
	private final Map<ParameterMetaData,ParameterMetaDataTrackerImpl> trackedParameterMetaDatas = synchronizedMap(new IdentityHashMap<>());
	private final Map<PreparedStatement,PreparedStatementTrackerImpl> trackedPreparedStatements = synchronizedMap(new IdentityHashMap<>());
	private final Map<Reader,ReaderTracker> trackedReaders = synchronizedMap(new IdentityHashMap<>());
	private final Map<Ref,RefTrackerImpl> trackedRefs = synchronizedMap(new IdentityHashMap<>());
	private final Map<ResultSet,ResultSetTrackerImpl> trackedResultSets = synchronizedMap(new IdentityHashMap<>());
	private final Map<ResultSetMetaData,ResultSetMetaDataTrackerImpl> trackedResultSetMetaDatas = synchronizedMap(new IdentityHashMap<>());
	private final Map<RowId,RowIdTrackerImpl> trackedRowIds = synchronizedMap(new IdentityHashMap<>());
	private final Map<SQLData,SQLDataTrackerImpl> trackedSQLDatas = synchronizedMap(new IdentityHashMap<>());
	private final Map<SQLInput,SQLInputTrackerImpl> trackedSQLInputs = synchronizedMap(new IdentityHashMap<>());
	private final Map<SQLOutput,SQLOutputTrackerImpl> trackedSQLOutputs = synchronizedMap(new IdentityHashMap<>());
	private final Map<SQLXML,SQLXMLTrackerImpl> trackedSQLXMLs = synchronizedMap(new IdentityHashMap<>());

	/**
	 * Maintains ordering with {@link LinkedHashMap} while using {@link IdentityKey} as a {@linkplain MapWrapper key wrapper}.
	 */
	@SuppressWarnings("unchecked")
	private final Map<Savepoint,SavepointTrackerImpl> trackedSavepoints = synchronizedMap(
		MapWrapper.of(
			new LinkedHashMap<>(),
			new FunctionalConverter<>(
				Savepoint.class,
				(Class<IdentityKey<Savepoint>>)(Class)IdentityKey.class,
				IdentityKey::of,
				IdentityKey::getValue
			),
			Converter.identity()
		)
	);

	private final Map<Statement,StatementTrackerImpl> trackedStatements = synchronizedMap(new IdentityHashMap<>());
	private final Map<Struct,StructTrackerImpl> trackedStructs = synchronizedMap(new IdentityHashMap<>());
	private final Map<Writer,WriterTracker> trackedWriters = synchronizedMap(new IdentityHashMap<>());

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Array,ArrayTrackerImpl> getTrackedArrays() {
		return trackedArrays;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Blob,BlobTrackerImpl> getTrackedBlobs() {
		return trackedBlobs;
	}

	@Override
	final @SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	public Map<CallableStatement,CallableStatementTrackerImpl> getTrackedCallableStatements() {
		return trackedCallableStatements;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Clob,ClobTrackerImpl> getTrackedClobs() {
		return trackedClobs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<DatabaseMetaData,DatabaseMetaDataTrackerImpl> getTrackedDatabaseMetaDatas() {
		return trackedDatabaseMetaDatas;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<InputStream,InputStreamTracker> getTrackedInputStreams() {
		return trackedInputStreams;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<NClob,NClobTrackerImpl> getTrackedNClobs() {
		return trackedNClobs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<OutputStream,OutputStreamTracker> getTrackedOutputStreams() {
		return trackedOutputStreams;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ParameterMetaData,ParameterMetaDataTrackerImpl> getTrackedParameterMetaDatas() {
		return trackedParameterMetaDatas;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<PreparedStatement,PreparedStatementTrackerImpl> getTrackedPreparedStatements() {
		return trackedPreparedStatements;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Reader,ReaderTracker> getTrackedReaders() {
		return trackedReaders;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Ref,RefTrackerImpl> getTrackedRefs() {
		return trackedRefs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ResultSet,ResultSetTrackerImpl> getTrackedResultSets() {
		return trackedResultSets;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<ResultSetMetaData,ResultSetMetaDataTrackerImpl> getTrackedResultSetMetaDatas() {
		return trackedResultSetMetaDatas;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<RowId,RowIdTrackerImpl> getTrackedRowIds() {
		return trackedRowIds;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<SQLData,SQLDataTrackerImpl> getTrackedSQLDatas() {
		return trackedSQLDatas;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<SQLInput,SQLInputTrackerImpl> getTrackedSQLInputs() {
		return trackedSQLInputs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<SQLOutput,SQLOutputTrackerImpl> getTrackedSQLOutputs() {
		return trackedSQLOutputs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<SQLXML,SQLXMLTrackerImpl> getTrackedSQLXMLs() {
		return trackedSQLXMLs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Savepoint,SavepointTrackerImpl> getTrackedSavepoints() {
		return trackedSavepoints;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Statement,StatementTrackerImpl> getTrackedStatements() {
		return trackedStatements;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Struct,StructTrackerImpl> getTrackedStructs() {
		return trackedStructs;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Writer,WriterTracker> getTrackedWriters() {
		return trackedWriters;
	}

	static Throwable clearCloseAndCatch(Throwable t0, Map<?,? extends AutoCloseable> map) {
		List<AutoCloseable> closeMes;
		synchronized(map) {
			closeMes = new ArrayList<>(map.values());
			map.clear();
		}
		return AutoCloseables.closeAndCatch(t0, closeMes);
	}

	@SuppressWarnings("unchecked")
	static Throwable clearCloseAndCatch(Throwable t0, Map<?,? extends AutoCloseable> ... maps) {
		List<AutoCloseable> closeMes = null;
		for(Map<?,? extends AutoCloseable> map : maps) {
			synchronized(map) {
				if(closeMes == null) {
					closeMes = new ArrayList<>(map.values());
				} else {
					closeMes.clear();
					closeMes.addAll(map.values());
				}
				map.clear();
			}
			t0 = AutoCloseables.closeAndCatch(t0, closeMes);
		}
		return t0;
	}

	static Throwable clearCloseAndCatch(Map<?,? extends AutoCloseable> map) {
		return clearCloseAndCatch(null, map);
	}

	@SuppressWarnings("unchecked")
	static Throwable clearCloseAndCatch(Map<?,? extends AutoCloseable> ... maps) {
		return clearCloseAndCatch(null, maps);
	}

	private static void clear(Map<?,?> map) {
		map.clear();
	}

	static Throwable clearRunAndCatch(Throwable t0, Collection<? extends Runnable> runnables) {
		List<Runnable> runMes;
		synchronized(runnables) {
			runMes = new ArrayList<>(runnables);
			runnables.clear();
		}
		return Runnables.runAndCatch(t0, runMes);
	}

	static Throwable clearRunAndCatch(Collection<? extends Runnable> runnables) {
		return clearRunAndCatch(null, runnables);
	}

	/**
	 * Puts a value into the map when not null and not already in the map.
	 * When a new map entry is added, an onClose handler is registered that will remove that map entry on close.
	 *
	 * @param  map          The mapping of tracked objects
	 * @param  wrapped      The object to find already tracked or retrieve new tracker
	 * @param  getTracker   Retrieves new tracker when wrapped is not already in the map
	 * @param  keyFunction  Gets the map key to use for the tracker obtained from {@code getTracker}
	 * 
	 * @return  The value, either obtained from the map or retrieved
	 */
	static <K,V extends OnCloseHandler,E extends Throwable> V getIfAbsent(
		Map<K,V> map,
		K wrapped,
		CallableE<? extends V,? extends E> getTracker,
		Function<? super V,? extends K> keyFunction
	) throws E {
		if(wrapped != null) {
			V tracker;
			synchronized(map) {
				tracker = map.get(wrapped);
				if(tracker == null) {
					V gotTracker = getTracker.call();
					tracker = map.computeIfAbsent(
						keyFunction.apply(gotTracker),
						key -> {
							gotTracker.addOnClose(() -> map.remove(key, gotTracker));
							return gotTracker;
						}
					);
				} else {
					assert keyFunction.apply(tracker) == wrapped : "tracker from map does not track the expected object";
				}
			}
			return tracker;
		} else {
			return null;
		}
	}

	/**
	 * Puts a value into the map when not already in the map.
	 * When a new map entry is added, an onClose handler is registered that will remove that map entry on close.
	 *
	 * @param  map          The mapping of tracked objects
	 * @param  thisTracker  Passed as the first argument to {@code trackerGenerator}
	 * @param  wrapped      The object to find already tracked or generate a new tracker
	 * @param  newTracker   Generates new tracker when wrapped is not already in the map
	 * 
	 * @return  The value, either obtained from the map or new
	 */
	static <T,K,V extends OnCloseHandler> V newIfAbsent(
		Map<K,V> map,
		T thisTracker,
		K wrapped,
		BiFunction<? super T, ? super K, ? extends V> newTracker
	) {
		return map.computeIfAbsent(
			wrapped,
			k -> {
				assert k == wrapped;
				V tracker = newTracker.apply(thisTracker, k);
				tracker.addOnClose(() -> map.remove(k, tracker));
				return tracker;
			}
		);
	}

	/**
	 * @see  #newIfAbsent(java.util.Map, java.lang.Object, java.lang.Object, java.util.function.BiFunction)
	 */
	private <K,V extends OnCloseHandler> V newIfAbsent(
		Map<K,V> map,
		K wrapped,
		BiFunction<? super ConnectionTrackerImpl, ? super K, ? extends V> newTracker
	) {
		return newIfAbsent(map, this, wrapped, newTracker);
	}

	/**
	 * Closes / frees all tracked objects except savepoints, which are expected to be closed by a following
	 * {@link #rollback()}.
	 */
	@SuppressWarnings("unchecked")
	protected Throwable closeTracked(Throwable t0) {
		// TODO: Logging FINE with count, FINER with each object
		return clearCloseAndCatch(t0,
			// Streams
			trackedInputStreams,
			trackedOutputStreams,
			trackedReaders,
			trackedWriters,
			// Types
			trackedArrays,
			trackedBlobs,
			trackedClobs,
			trackedNClobs,
			trackedRefs,
			trackedRowIds,
			trackedSQLXMLs,
			trackedStructs,
			// SQLData
			trackedSQLDatas,
			trackedSQLInputs,
			trackedSQLOutputs,
			// Meta datas
			trackedDatabaseMetaDatas,
			trackedParameterMetaDatas,
			trackedResultSetMetaDatas,
			// Statements and results
			trackedResultSets,
			trackedCallableStatements,
			trackedPreparedStatements,
			trackedStatements
		);
	}

	/**
	 * Clears all tracking.
	 */
	protected void clearTracking() {
		clear(trackedArrays);
		clear(trackedBlobs);
		clear(trackedCallableStatements);
		clear(trackedClobs);
		clear(trackedDatabaseMetaDatas);
		clear(trackedInputStreams);
		clear(trackedNClobs);
		clear(trackedOutputStreams);
		clear(trackedParameterMetaDatas);
		clear(trackedPreparedStatements);
		clear(trackedReaders);
		clear(trackedRefs);
		clear(trackedResultSets);
		clear(trackedResultSetMetaDatas);
		clear(trackedRowIds);
		clear(trackedSQLDatas);
		clear(trackedSQLInputs);
		clear(trackedSQLOutputs);
		clear(trackedSQLXMLs);
		clear(trackedSavepoints);
		clear(trackedStatements);
		clear(trackedStructs);
		clear(trackedWriters);
	}

	/**
	 * Releases all tracked savepoints.
	 */
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	protected Throwable releaseAllTrackedSavepoints(Throwable t0) {
		List<SavepointTrackerImpl> savepoints;
		synchronized(trackedSavepoints) {
			savepoints = new ArrayList<>(trackedSavepoints.values());
			trackedSavepoints.clear();
		}
		for(SavepointTrackerImpl savepoint : savepoints) {
			try {
				savepoint.onRelease();
			} catch(Throwable t) {
				t0 = Throwables.addSuppressed(t0, t);
			}
		}
		return t0;
	}

	@Override
	protected ArrayTrackerImpl newArrayWrapper(StatementWrapperImpl stmtWrapper, Array array) {
		return newIfAbsent(trackedArrays, array, (conn, k) ->
			new ArrayTrackerImpl(conn, (StatementTrackerImpl)stmtWrapper, k));
	}

	@Override
	protected BlobTrackerImpl newBlobWrapper(Blob blob) {
		return newIfAbsent(trackedBlobs, blob, BlobTrackerImpl::new);
	}

	@Override
	protected CallableStatementTrackerImpl newCallableStatementWrapper(CallableStatement cstmt) {
		return newIfAbsent(trackedCallableStatements, cstmt, CallableStatementTrackerImpl::new);
	}

	@Override
	protected ClobTrackerImpl newClobWrapper(Clob clob) {
		return newIfAbsent(trackedClobs, clob, ClobTrackerImpl::new);
	}

	@Override
	protected DatabaseMetaDataTrackerImpl newDatabaseMetaDataWrapper(DatabaseMetaData metaData) {
		return newIfAbsent(trackedDatabaseMetaDatas, metaData, DatabaseMetaDataTrackerImpl::new);
	}

	@Override
	protected InputStreamTracker newInputStreamWrapper(InputStream in) {
		return newIfAbsent(trackedInputStreams, in, InputStreamTracker::new);
	}

	@Override
	protected NClobTrackerImpl newNClobWrapper(NClob nclob) {
		return newIfAbsent(trackedNClobs, nclob, NClobTrackerImpl::new);
	}

	@Override
	protected OutputStreamTracker newOutputStreamWrapper(OutputStream out) {
		return newIfAbsent(trackedOutputStreams, out, OutputStreamTracker::new);
	}

	@Override
	protected ParameterMetaDataTrackerImpl newParameterMetaDataWrapper(ParameterMetaData metaData) {
		return newIfAbsent(trackedParameterMetaDatas, metaData, ParameterMetaDataTrackerImpl::new);
	}

	@Override
	protected PreparedStatementTrackerImpl newPreparedStatementWrapper(PreparedStatement pstmt) {
		return newIfAbsent(trackedPreparedStatements, pstmt, PreparedStatementTrackerImpl::new);
	}

	@Override
	protected ReaderTracker newReaderWrapper(Reader in) {
		return newIfAbsent(trackedReaders, in, ReaderTracker::new);
	}

	@Override
	protected RefTrackerImpl newRefWrapper(Ref ref) {
		return newIfAbsent(trackedRefs, ref, RefTrackerImpl::new);
	}

	@Override
	protected ResultSetTrackerImpl newResultSetWrapper(StatementWrapperImpl stmtWrapper, ResultSet results) {
		return newIfAbsent(trackedResultSets, results, (conn, k) ->
			new ResultSetTrackerImpl(conn, (StatementTrackerImpl)stmtWrapper, k));
	}

	@Override
	protected ResultSetMetaDataTrackerImpl newResultSetMetaDataWrapper(ResultSetMetaData metaData) {
		return newIfAbsent(trackedResultSetMetaDatas, metaData, ResultSetMetaDataTrackerImpl::new);
	}

	@Override
	protected RowIdTrackerImpl newRowIdWrapper(RowId rowId) {
		return newIfAbsent(trackedRowIds, rowId, RowIdTrackerImpl::new);
	}

	@Override
	protected SQLDataTrackerImpl newSQLDataWrapper(SQLData sqlData) {
		return newIfAbsent(trackedSQLDatas, sqlData, SQLDataTrackerImpl::new);
	}

	@Override
	protected SQLInputTrackerImpl newSQLInputWrapper(SQLInput sqlInput) {
		return newIfAbsent(trackedSQLInputs, sqlInput, SQLInputTrackerImpl::new);
	}

	@Override
	protected SQLOutputTrackerImpl newSQLOutputWrapper(SQLOutput sqlOutput) {
		return newIfAbsent(trackedSQLOutputs, sqlOutput, SQLOutputTrackerImpl::new);
	}

	@Override
	protected SQLXMLTrackerImpl newSQLXMLWrapper(SQLXML sqlXml) {
		return newIfAbsent(trackedSQLXMLs, sqlXml, SQLXMLTrackerImpl::new);
	}

	@Override
	protected SavepointTrackerImpl newSavepointWrapper(Savepoint savepoint) {
		return newIfAbsent(trackedSavepoints, savepoint, SavepointTrackerImpl::new);
	}

	@Override
	protected StatementTrackerImpl newStatementWrapper(Statement stmt) {
		return newIfAbsent(trackedStatements, stmt, StatementTrackerImpl::new);
	}

	@Override
	protected StructTrackerImpl newStructWrapper(Struct struct) {
		return newIfAbsent(trackedStructs, struct, StructTrackerImpl::new);
	}

	@Override
	protected WriterTracker newWriterWrapper(Writer out) {
		return newIfAbsent(trackedWriters, out, WriterTracker::new);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  SavepointTrackerImpl#onRelease()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		Throwable t0 = null;
		if(autoCommit) {
			// Release tracked objects
			t0 = releaseAllTrackedSavepoints(t0);
		}
		try {
			super.setAutoCommit(autoCommit);
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  SavepointTrackerImpl#onRelease()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void commit() throws SQLException {
		Throwable t0 = null;
		// Release tracked objects
		t0 = releaseAllTrackedSavepoints(t0);
		try {
			super.commit();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  SavepointTrackerImpl#onRelease()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void rollback() throws SQLException {
		Throwable t0 = null;
		// Release tracked objects
		t0 = releaseAllTrackedSavepoints(t0);
		try {
			super.rollback();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation calls {@link #doClose()}.
	 * </p>
	 *
	 * @see  #closeTracked(java.lang.Throwable)
	 * @see  #doClose()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void close() throws SQLException {
		Throwable t0 = clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = closeTracked(t0);
		// Rollback any transaction in-progress and put back in auto-commit mode
		try {
			if(!isClosed() && !getAutoCommit()) {
				rollback();
				setAutoCommit(true);
			}
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		// Any savepoints not removed on rollback()
		t0 = clearCloseAndCatch(t0, trackedSavepoints);
		try {
			doClose();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  SavepointTrackerImpl#onRelease()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void rollback(Savepoint savepoint) throws SQLException {
		// Release tracked objects
		// Call onRelease for all that follow the given savepoint
		SavepointTrackerImpl savepointTracker = (SavepointTrackerImpl)wrapSavepoint(savepoint);
		List<SavepointTrackerImpl> toRelease = new ArrayList<>();
		synchronized(trackedSavepoints) {
			Iterator<SavepointTrackerImpl> iter = trackedSavepoints.values().iterator();
			boolean matched = false;
			while(iter.hasNext()) {
				SavepointTrackerImpl value = iter.next();
				if(matched) {
					iter.remove();
					toRelease.add(value);
				} else {
					matched = (value == savepointTracker);
				}
			}
		}
		Throwable t0 = null;
		for(int i = toRelease.size() - 1; i >= 0; i--) {
			SavepointTrackerImpl releaseMe = toRelease.get(i);
			try {
				releaseMe.onRelease();
			} catch(Throwable t) {
				t0 = Throwables.addSuppressed(t0, t);
			}
		}
		try {
			super.rollback(savepoint);
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  SavepointTrackerImpl#close()
	 * @see  SavepointTrackerImpl#onRelease()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		// Release tracked objects
		// Call onRelease for the given savepoint and all that follow
		SavepointTrackerImpl savepointTracker = (SavepointTrackerImpl)wrapSavepoint(savepoint);
		List<SavepointTrackerImpl> toRelease = new ArrayList<>();
		synchronized(trackedSavepoints) {
			Iterator<SavepointTrackerImpl> iter = trackedSavepoints.values().iterator();
			boolean matched = false;
			while(iter.hasNext()) {
				SavepointTrackerImpl value = iter.next();
				if(!matched) {
					matched = (value == savepointTracker);
				}
				if(matched) {
					iter.remove();
					toRelease.add(value);
				}
			}
			if(!matched) {
				toRelease.add(savepointTracker);
			}
		}
		Throwable t0 = null;
		for(int i = toRelease.size() - 1; i >= 0; i--) {
			SavepointTrackerImpl releaseMe = toRelease.get(i);
			try {
				releaseMe.onRelease();
			} catch(Throwable t) {
				t0 = Throwables.addSuppressed(t0, t);
			}
		}
		try {
			super.releaseSavepoint(savepoint);
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This default implementation calls {@link #doAbort(java.util.concurrent.Executor)}.
	 * </p>
	 *
	 * @see  #clearTracking()
	 * @see  #doAbort(java.util.concurrent.Executor)
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch"})
	public void abort(Executor executor) throws SQLException {
		Throwable t0 = clearRunAndCatch(onCloseHandlers);
		clearTracking();
		try {
			doAbort(executor);
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}

	/**
	 * Performs the actual close, called once all onClose handlers completed and all tracked objects closed.
	 * <p>
	 * This default implementation calls {@code super.close()}
	 * </p>
	 *
	 * @see  #close()
	 */
	protected void doClose() throws SQLException {
		super.close();
	}

	/**
	 * Performs the actual abort, called once all onClose handlers completed and all tracking cleared.
	 * <p>
	 * This default implementation calls {@code super.abort(executor)}
	 * </p>
	 *
	 * @see  #abort(java.util.concurrent.Executor)
	 */
	protected void doAbort(Executor executor) throws SQLException {
		super.abort(executor);
	}
}