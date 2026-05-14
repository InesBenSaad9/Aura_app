package tn.esprit.aura.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/aura_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    private static DBConnection instance;
    private Connection connection;

    private DBConnection() {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connection established to DB: " + URL);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        } else {
            try {
                if (instance.getConnection() == null || instance.getConnection().isClosed()) {
                    instance = new DBConnection();
                }
            } catch (SQLException e) {
                instance = new DBConnection();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
