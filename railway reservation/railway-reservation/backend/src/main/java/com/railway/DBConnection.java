package com.railway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {

        String host = System.getenv("RAILWAY_DB_HOST");
        String port = System.getenv("RAILWAY_DB_PORT");
        String database = System.getenv("RAILWAY_DB_NAME");
        String user = System.getenv("RAILWAY_DB_USER");
        String password = System.getenv("RAILWAY_DB_PASSWORD");

        if (host == null || port == null || database == null
                || user == null || password == null) {
            throw new SQLException(
                    "Database environment variables are not configured."
            );
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/"
                + database + "?sslMode=REQUIRED";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found.", e);
        }

        return DriverManager.getConnection(url, user, password);
    }
}
