import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseApp {
    
    // Database credentials based on your virtuso.sql dump
    public static final String DB_URL = "jdbc:mysql://localhost:3306/virtuso";
    public static final String DB_USER = "root"; // Default local user
    public static final String DB_PASS = "";     // Default local password

    // Used by LoginPanel and RegisterPanel (e.g., db.getConnection())
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    // Used by MainApp.java on startup
    public static void initializeTables() {
        // Since you are importing virtuso.sql manually via phpMyAdmin, 
        // this can remain a simple console check or be left empty.
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("Database connection established successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Is XAMPP/MySQL running?");
            e.printStackTrace();
        }
    }
}