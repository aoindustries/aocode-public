/*
 * aocode-public - Reusable Java library of general tools with minimal external dependencies.
 * Copyright (C) 2013  AO Industries, Inc.
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
package com.aoindustries.util.i18n;

import com.aoindustries.encoding.MediaEncoder;
import java.io.IOException;

/**
 * The result of a bundle lookup, with possible prefix and suffix values that
 * represent markup to be added for any in-context translation editors.
 *
 * @author  AO Industries, Inc.
 */
public interface BundleLookupResult {

	/**
	 * Gets the result unaltered.
	 */
	@Override
	String toString();

	/**
	 * Appends the prefix to the given out.
	 */
	void appendPrefixTo(Appendable out) throws IOException;

	/**
	 * Appends the prefix to the given out using the given encoder.
	 */
	void appendPrefixTo(MediaEncoder encoder, Appendable out) throws IOException;

	/**
	 * Gets the resulting value from the lookup.
	 */
	String getResult();

	/**
	 * Appends the suffix to the given out.
	 */
	void appendSuffixTo(Appendable out) throws IOException;

	/**
	 * Appends the suffix to the given out using the given encoder.
	 */
	void appendSuffixTo(MediaEncoder encoder, Appendable out) throws IOException;
}
