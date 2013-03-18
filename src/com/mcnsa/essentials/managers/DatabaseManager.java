package com.mcnsa.essentials.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.mcnsa.essentials.MCNSAEssentials;
import com.mcnsa.essentials.annotations.DatabaseTableInfo;
import com.mcnsa.essentials.exceptions.EssentialsCommandException;
import com.mcnsa.essentials.exceptions.EssentialsDatabaseException;

// http://zetcode.com/db/mysqljava/

public class DatabaseManager {	
	// our connection settings
	public static String url = "jdbc:mysql://localhost/mcnsa";
	public static String user = "mcnsa";
	public static String password = "mcnsa";
	
	// our connections
	private static Connection connection = null;
	private static PreparedStatement preparedStatement = null;
	private static ResultSet resultSet = null;
	
	private static HashMap<String, String> tableConstructions = new HashMap<String, String>();
	
	// our constructor
	public DatabaseManager() {
		try {
			// connect
			connect();
			
			// build our tables
			ensureTablesExist();
		}
		catch(Exception e) {
			// disconnect on error
			e.printStackTrace();
			MCNSAEssentials.error("Failed to initialize database connection! Using url <%s>, user <%s>, pass <%s>", url, user, password);
			MCNSAEssentials.warning("You won't be able to use any commands that utilize the database!");
			disconnect();
		}
	}
	
	public void disable() {
		disconnect();
	}
	
	// connection commands
	private void connect() throws SQLException, EssentialsDatabaseException {	
		connection = DriverManager.getConnection(url, user, password);
		preparedStatement = connection.prepareStatement("select version();");
		resultSet = preparedStatement.executeQuery();
		
		if(resultSet.next()) {
			MCNSAEssentials.log("&aDatabase connected! Database version: &f%s", resultSet.getString(1));
		}
		else {
			throw new EssentialsDatabaseException("Failed to retrieve database version!");
		}
	}
	private void disconnect() {
		try {
			if(resultSet != null) {
				resultSet.close();
			}
			
			if(preparedStatement != null) {
				preparedStatement.close();
			}
			
			if(connection != null) {
				connection.close();
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
			MCNSAEssentials.error("Failed to terminate database connection! (%s)", e.getMessage());
		}
	}
	
	// table construction commands
	public static void addTableConstruct(DatabaseTableInfo tableInfo) {
		String query = String.format("CREATE TABLE IF NOT EXISTS %s ( id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT UNIQUE", tableInfo.name());
		for(String field: tableInfo.fields()) {
			query += ", " + field + " NOT NULL";
		}
		query += " );";
		tableConstructions.put(tableInfo.name(), query);
	}
	private void ensureTablesExist() throws SQLException {
		for(String table: tableConstructions.keySet()) {
			try {
				preparedStatement = connection.prepareStatement(tableConstructions.get(table));
				preparedStatement.executeUpdate();
			}
			catch(SQLException e) {
				MCNSAEssentials.error("Failed to ensure table construction: (%s)! Skipping...", e.getMessage());
			}
			finally {
				preparedStatement.close();
			}
		}
	}
	
	// utility commands
	private static PreparedStatement prepareStatement(String query, Object... args) throws SQLException, EssentialsDatabaseException {
		// prepare our statement
		preparedStatement = connection.prepareStatement(query);
		
		// keep track of where in the statement to do stuff
		int i = 1;
		for(Object arg: args) {
			// now add to the prepared statement based on what data type we have
			if(arg.getClass().equals(String.class)) {
				preparedStatement.setString(i, (String)arg);
			}
			else if(arg.getClass().equals(int.class) || arg.getClass().equals(Integer.class)) {
				preparedStatement.setInt(i, (Integer)arg);
			}
			else if(arg.getClass().equals(boolean.class) || arg.getClass().equals(Boolean.class)) {
				preparedStatement.setBoolean(i, (Boolean)arg);
			}
			else if(arg.getClass().equals(float.class) || arg.getClass().equals(Float.class)) {
				preparedStatement.setFloat(i, (Float)arg);
			}
			else if(arg.getClass().equals(long.class) || arg.getClass().equals(Long.class)) {
				preparedStatement.setLong(i, (Long)arg);
			}
			else if(arg.getClass().equals(Date.class)) {
				preparedStatement.setDate(i, new java.sql.Date(((java.util.Date)arg).getTime()));
			}
			else if(arg.getClass().equals(Timestamp.class)) {
				preparedStatement.setTimestamp(i, (Timestamp)arg);
			}
			else {
				throw new EssentialsDatabaseException("Unknown SQL data type: %s", arg.getClass().getSimpleName());
			}
			
			// increment our index
			i++;
		}
		
		return preparedStatement;
	}
	
	// data access commands
	public static ArrayList<HashMap<String, Object>> accessQuery(String query, Object... args) throws EssentialsCommandException {
		try {
			// make sure we have a connection
			if(connection == null || connection.isClosed()) {
				throw new EssentialsDatabaseException("Not connected to a database!");
			}
			
			// prepare our statement
			preparedStatement = prepareStatement(query, args);
			
			// ok, now execute our query!
			ResultSet results = preparedStatement.executeQuery();
			
			// get the result set meta data so we can access column names
			ResultSetMetaData metaData = results.getMetaData();
			
			// build our returned results
			ArrayList<HashMap<String, Object>> ret = new ArrayList<HashMap<String, Object>>();
			while(results.next()) {
				HashMap<String, Object> row = new HashMap<String, Object>();
				for(int column = 1; column <= metaData.getColumnCount(); column++) {
					row.put(metaData.getColumnName(column), results.getObject(column));
				}
				ret.add(row);
			}
			
			return ret;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new EssentialsCommandException("Failed to prepare query: (%s)!", e.getMessage());
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				MCNSAEssentials.error("Failed to close prepared statement on query: (%s)!", e.getMessage());
			}
		}
	}
	
	public static int updateQuery(String query, Object... args) throws EssentialsCommandException {
		try {
			// make sure we have a connection
			if(connection == null || connection.isClosed()) {
				throw new EssentialsDatabaseException("Not connected to a database!");
			}
			
			// prepare our statement
			preparedStatement = prepareStatement(query, args);
			
			// ok, now execute our query!
			return preparedStatement.executeUpdate();
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new EssentialsCommandException("Failed to prepare query: (%s)!", e.getMessage());
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch(SQLException e) {
				e.printStackTrace();
				MCNSAEssentials.error("Failed to close prepared statement on query: (%s)!", e.getMessage());
			}
		}
	}
}
