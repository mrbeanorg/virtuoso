

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// ============================================================================
// DATABASE BASE CLASS
// Handles MySQL connection & auto-creates missing application tables
// ============================================================================
public class DatabaseApp {
    // 1. CHANGED TO PUBLIC: Now other separate files can access these configuration strings
    public static final String DB_URL = "jdbc:mysql://localhost:3306/virtuso";
    public static final String DB_USER = "root";
    public static final String DB_PASS = ""; 

    // Explicitly load the MySQL Driver class to force VS Code to hook into your JAR file
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ MySQL JDBC Driver missing! Please check 'Referenced Libraries' in VS Code.");
            e.printStackTrace();
        }
    }

    // 2. CHANGED TO PUBLIC: Allows components like LoginPanel to invoke your connection broker
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    /**
     * Initializes required tables automatically on application launch if they don't exist.
     */
    public static void initializePasswordTable() {
        // Table for storing saved vault passwords
        String createVaultTableSQL = "CREATE TABLE IF NOT EXISTS user_saved_passwords (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "logged_in_username VARCHAR(100) NOT NULL, " +
                "app_name VARCHAR(150) NOT NULL, " +
                "account_username VARCHAR(150) NOT NULL, " +
                "generated_password VARCHAR(255) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        // Table for user authentication (Login/Register)
        String createLoginTableSQL = "CREATE TABLE IF NOT EXISTS student_login (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(150) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        // Table for Algorithm Virtuoso sorting history
        String createHistoryTableSQL = "CREATE TABLE IF NOT EXISTS user_sort_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "algorithm_name VARCHAR(100) NOT NULL, " +
                "input_array TEXT NOT NULL, " +
                "sorted_array TEXT NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createVaultTableSQL);
            stmt.execute(createLoginTableSQL);
            stmt.execute(createHistoryTableSQL);

        } catch (SQLException e) {
            System.err.println("Database initialization notice: " + e.getMessage());
        }
    }
}