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
import com.aoindustries.messaging.AbstractSocket;
import com.aoindustries.messaging.Message;
import com.aoindustries.security.Identifier;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;

/**
 * One established connection over HTTP(S).
 */
public class HttpSocket extends AbstractSocket {

	protected HttpSocket(
		HttpSocketContext socketContext,
		Identifier id,
		long connectTime,
		InetSocketAddress localSocketAddress,
		InetSocketAddress remoteSocketAddress,
	) {
		super(socketContext, id, connectTime, localSocketAddress, remoteSocketAddress);
	}

	@Override
	public void close() throws IOException {
		super.close();
		throw new NotImplementedException("TODO");
	}

	@Override
	public void sendMessages(Collection<? extends Message> messages) throws ClosedChannelException {
		throw new NotImplementedException("TODO");
	}
}
