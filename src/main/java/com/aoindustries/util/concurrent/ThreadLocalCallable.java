/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2016  AO Industries, Inc.
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
package com.aoindustries.util.concurrent;

import java.util.concurrent.Callable;

/**
 * Copies a ThreadLocal value from the current thread onto the thread
 * that runs the provided callable.
 *
 * @see  ThreadLocal
 */
public class ThreadLocalCallable<T> implements Callable<T> {

	private final Callable<T> task;
	private final ThreadLocal<?> threadLocal;
	private final Object value;

	public ThreadLocalCallable(Callable<T> task, ThreadLocal<?> threadLocal) {
		this.task = task;
		this.threadLocal = threadLocal;
		this.value = threadLocal.get();
	}

	@Override
	public T call() throws Exception {
		@SuppressWarnings("unchecked")
		ThreadLocal<Object> tl = (ThreadLocal<Object>)threadLocal;
		Object oldValue = tl.get();
		Object newValue = value;
		try {
			if(oldValue != newValue) tl.set(newValue);
			return task.call();
		} finally {
			if(oldValue != newValue) tl.set(oldValue);
		}
	}
}