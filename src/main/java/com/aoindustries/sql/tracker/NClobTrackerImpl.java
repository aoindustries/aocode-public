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
import com.aoindustries.sql.wrapper.NClobWrapperImpl;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.NClob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import static java.util.Collections.synchronizedMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tracks a {@link NClob} for unclosed or unfreed objects.
 *
 * @author  AO Industries, Inc.
 */
public class NClobTrackerImpl extends NClobWrapperImpl implements NClobTracker {

	public NClobTrackerImpl(ConnectionTrackerImpl connectionTracker, NClob wrapped) {
		super(connectionTracker, wrapped);
	}

	private final List<Runnable> onCloseHandlers = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void addOnClose(Runnable onCloseHandler) {
		onCloseHandlers.add(onCloseHandler);
	}

	private final Map<InputStream,InputStreamTracker> trackedInputStreams = synchronizedMap(new IdentityHashMap<>());
	private final Map<OutputStream,OutputStreamTracker> trackedOutputStreams = synchronizedMap(new IdentityHashMap<>());
	private final Map<Reader,ReaderTracker> trackedReaders = synchronizedMap(new IdentityHashMap<>());
	private final Map<Writer,WriterTracker> trackedWriters = synchronizedMap(new IdentityHashMap<>());

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<InputStream,InputStreamTracker> getTrackedInputStreams() {
		return trackedInputStreams;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<OutputStream,OutputStreamTracker> getTrackedOutputStreams() {
		return trackedOutputStreams;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Reader,ReaderTracker> getTrackedReaders() {
		return trackedReaders;
	}

	@Override
	@SuppressWarnings("ReturnOfCollectionOrArrayField") // No defensive copy
	final public Map<Writer,WriterTracker> getTrackedWriters() {
		return trackedWriters;
	}

	@Override
	protected InputStreamTracker wrapInputStream(InputStream in) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedInputStreams, in,
			() -> (InputStreamTracker)super.wrapInputStream(in),
			InputStreamTracker::getWrapped
		);
	}

	@Override
	protected OutputStreamTracker wrapOutputStream(OutputStream out) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedOutputStreams, out,
			() -> (OutputStreamTracker)super.wrapOutputStream(out),
			OutputStreamTracker::getWrapped
		);
	}

	@Override
	protected ReaderTracker wrapReader(Reader in) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedReaders, in,
			() -> (ReaderTracker)super.wrapReader(in),
			ReaderTracker::getWrapped
		);
	}

	@Override
	protected WriterTracker wrapWriter(Writer out) {
		return ConnectionTrackerImpl.getIfAbsent(
			trackedWriters, out,
			() -> (WriterTracker)super.wrapWriter(out),
			WriterTracker::getWrapped
		);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see  InputStreamTracker#close()
	 * @see  OutputStreamTracker#close()
	 * @see  ReaderTracker#close()
	 * @see  WriterTracker#close()
	 */
	@Override
	@SuppressWarnings({"UseSpecificCatch", "TooBroadCatch", "unchecked"})
	public void free() throws SQLException {
		Throwable t0 = ConnectionTrackerImpl.clearRunAndCatch(onCloseHandlers);
		// Close tracked objects
		t0 = ConnectionTrackerImpl.clearCloseAndCatch(t0,
			trackedInputStreams,
			trackedOutputStreams,
			trackedReaders,
			trackedWriters
		);
		try {
			super.free();
		} catch(Throwable t) {
			t0 = Throwables.addSuppressed(t0, t);
		}
		if(t0 != null) throw Throwables.wrap(t0, SQLException.class, SQLException::new);
	}
}