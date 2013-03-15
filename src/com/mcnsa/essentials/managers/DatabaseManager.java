package com.mcnsa.essentials.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mcnsa.essentials.MCNSAEssentials;

// http://zetcode.com/db/mysqljava/

public class DatabaseManager {
	public void connect() {
		String url = "jdbc:mysql://localhost/mcnsa";
		String user = "mcnsa";
		String password = "mcnsa";
		
		Connection connection = null;
		Statement statement = null;
		ResultSet resultSet = null;
		
		try {
			connection = DriverManager.getConnection(url, user, password);
			statement = connection.createStatement();
			resultSet = statement.executeQuery("select version();");
			
			if(resultSet.next()) {
				MCNSAEssentials.log("Database version: " + resultSet.getString(1));
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(resultSet != null) {
					resultSet.close();
				}
				
				if(statement != null) {
					statement.close();
				}
				
				if(connection != null) {
					connection.close();
				}
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
