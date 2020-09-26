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
package com.aoindustries.sql.wrapper;

import java.sql.Connection;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Wraps a {@link Connection}.
 *
 * @author  AO Industries, Inc.
 */
public interface IConnectionWrapper extends IWrapper, Connection {

	/**
	 * Gets the connection that is wrapped.
	 */
	@Override
	Connection getWrapped();

	/**
	 * @deprecated  Please use {@link #getWrapped()}
	 */
	@Deprecated
	default Connection getWrappedConnection() {
		return getWrapped();
	}

	@Override
	IStatementWrapper createStatement() throws SQLException;

	@Override
	IPreparedStatementWrapper prepareStatement(String sql) throws SQLException;

	@Override
	ICallableStatementWrapper prepareCall(String sql) throws SQLException;

	@Override
	default String nativeSQL(String sql) throws SQLException {
		return getWrapped().nativeSQL(sql);
	}

	@Override
	default void setAutoCommit(boolean autoCommit) throws SQLException {
		getWrapped().setAutoCommit(autoCommit);
	}

	@Override
	default boolean getAutoCommit() throws SQLException {
		return getWrapped().getAutoCommit();
	}

	@Override
	default void commit() throws SQLException {
		getWrapped().commit();
	}

	@Override
	default void rollback() throws SQLException {
		getWrapped().rollback();
	}

	/**
	 * Performs any clean-up, then calls {@code getWrapped().close()}.
	 */
	@Override
	void close() throws SQLException;

	@Override
	default boolean isClosed() throws SQLException {
		return getWrapped().isClosed();
	}

	@Override
	IDatabaseMetaDataWrapper getMetaData() throws SQLException;

	@Override
	default void setReadOnly(boolean readOnly) throws SQLException {
		getWrapped().setReadOnly(readOnly);
	}

	@Override
	default boolean isReadOnly() throws SQLException {
		return getWrapped().isReadOnly();
	}

	@Override
	default void setCatalog(String catalog) throws SQLException {
		getWrapped().setCatalog(catalog);
	}

	@Override
	default String getCatalog() throws SQLException {
		return getWrapped().getCatalog();
	}

	@Override
	default void setTransactionIsolation(int level) throws SQLException {
		getWrapped().setTransactionIsolation(level);
	}

	@Override
	default int getTransactionIsolation() throws SQLException {
		return getWrapped().getTransactionIsolation();
	}

	@Override
	default SQLWarning getWarnings() throws SQLException {
		return getWrapped().getWarnings();
	}

	@Override
	default void clearWarnings() throws SQLException {
		getWrapped().clearWarnings();
	}

	@Override
	IStatementWrapper createStatement(int resultSetType, int resultSetConcurrency) throws SQLException;

	@Override
	IPreparedStatementWrapper prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException;

	@Override
	ICallableStatementWrapper prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException;

	@Override
	default Map<String,Class<?>> getTypeMap() throws SQLException {
		// TODO: How can we wrap SQLData on UDT maps?
		return getWrapped().getTypeMap();
	}

	@Override
	default void setTypeMap(Map<String,Class<?>> map) throws SQLException {
		// TODO: How can we wrap SQLData on UDT maps?
		getWrapped().setTypeMap(map);
	}

	@Override
	default void setHoldability(int holdability) throws SQLException {
		getWrapped().setHoldability(holdability);
	}

	@Override
	default int getHoldability() throws SQLException {
		return getWrapped().getHoldability();
	}

	@Override
	ISavepointWrapper setSavepoint() throws SQLException;

	@Override
	ISavepointWrapper setSavepoint(String name) throws SQLException;

	@Override
	void rollback(Savepoint savepoint) throws SQLException;

	@Override
	void releaseSavepoint(Savepoint savepoint) throws SQLException;

	@Override
	IStatementWrapper createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException;

	@Override
	IPreparedStatementWrapper prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException;

	@Override
	ICallableStatementWrapper prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException;

	@Override
	IPreparedStatementWrapper prepareStatement(String sql, int autoGeneratedKeys) throws SQLException;

	@Override
	IPreparedStatementWrapper prepareStatement(String sql, int columnIndexes[]) throws SQLException;

	@Override
	IPreparedStatementWrapper prepareStatement(String sql, String columnNames[]) throws SQLException;

	@Override
	IClobWrapper createClob() throws SQLException;

	@Override
	IBlobWrapper createBlob() throws SQLException;

	@Override
	INClobWrapper createNClob() throws SQLException;

	@Override
	ISQLXMLWrapper createSQLXML() throws SQLException;

	@Override
	default boolean isValid(int timeout) throws SQLException {
		return getWrapped().isValid(timeout);
	}

	@Override
	default void setClientInfo(String name, String value) throws SQLClientInfoException {
		getWrapped().setClientInfo(name, value);
	}

	@Override
	default void setClientInfo(Properties properties) throws SQLClientInfoException {
		getWrapped().setClientInfo(properties);
	}

	@Override
	default String getClientInfo(String name) throws SQLException {
		return getWrapped().getClientInfo(name);
	}

	@Override
	default Properties getClientInfo() throws SQLException {
		return getWrapped().getClientInfo();
	}

	@Override
	IArrayWrapper createArrayOf(String typeName, Object[] elements) throws SQLException;

	@Override
	IStructWrapper createStruct(String typeName, Object[] attributes) throws SQLException;

	@Override
	default int getNetworkTimeout() throws SQLException {
		return getWrapped().getNetworkTimeout();
	}

	@Override
	default void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		getWrapped().setNetworkTimeout(executor, milliseconds);
	}

	@Override
	default void setSchema(String schema) throws SQLException {
		getWrapped().setSchema(schema);
	}

	@Override
	default String getSchema() throws SQLException {
		return getWrapped().getSchema();
	}

	/**
	 * Performs any clean-up, then calls {@code getWrapped().abort(executor)}.
	 */
	@Override
	void abort(Executor executor) throws SQLException;
}
