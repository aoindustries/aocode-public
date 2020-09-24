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

import java.sql.Clob;
import java.sql.NClob;
import java.util.Map;

public interface ITrackedClobs {

	/**
	 * Gets all the clobs that have not yet been freed.
	 * This only contains {@link Clob}, please see other method for {@link NClob}.
	 *
	 * @return  The mapping from wrapped clob to tracker without any defensive copy.
	 *
	 * @see  ITrackedNClobs#getTrackedNClobs()
	 *
	 * @see  Clob#free()
	 */
	Map<Clob,? extends IClobTracker> getTrackedClobs();
}