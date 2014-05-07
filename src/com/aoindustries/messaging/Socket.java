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
package com.aoindustries.messaging;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;

/**
 * One established connection.
 */
public interface Socket extends Closeable {

	/**
	 * Gets the time this connection was established.
	 */
	long getConnectTime();

	/**
	 * Gets the time this connection closed or null if still connected.
	 */
	Long getCloseTime();

	/**
	 * Gets the local address at connection time.  This value will not change.
	 */
	InetSocketAddress getConnectSocketAddress();

	/**
	 * Gets the most recently seen local address.  This value may change.
	 */
	InetSocketAddress getLocalSocketAddress();

	/**
	 * Gets the remote address at connection time.  This value will not change.
	 */
	InetSocketAddress getConnectRemoteSocketAddress();

	/**
	 * Gets the most recently seen remote address.  This value may change.
	 */
	InetSocketAddress getRemoteSocketAddress();

	@Override
	void close() throws IOException;

	boolean isClosed();

	/**
	 * Adds a listener.
	 *
	 * @throws IllegalStateException  If the listener has already been added
	 */
	void addSocketListener(SocketListener listener) throws IllegalStateException;

	/**
	 * Removes a listener.
	 *
	 * @return true if the listener was found
	 */
	boolean removeSocketListener(SocketListener listener);

	/**
	 * Sends a single message.  This will never block.
	 */
	void sendMessage(Message message) throws ClosedChannelException;

	/**
	 * Sends a set of messages.  This will never block.
	 * If messages is empty, the request is ignored.
	 */
	void sendMessages(Iterable<? extends Message> messages) throws ClosedChannelException;
}