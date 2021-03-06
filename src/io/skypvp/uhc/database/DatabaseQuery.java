package io.skypvp.uhc.database;

import java.sql.Connection;
import java.sql.ResultSet;

public class DatabaseQuery {

	private final Connection connection;
	private final ResultSet rs;

	public DatabaseQuery(Connection conn, ResultSet rs) {
		this.connection = conn;
		this.rs = rs;
	}

	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * Fetches the {@link ResultSet} associated with this query.
	 * @return {@link ResultSet} or null if obtained ResultSet was empty.
	 */
	
	public ResultSet getResultSet() {
		return this.rs;
	}

}
