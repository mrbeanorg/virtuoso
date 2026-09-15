import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseApp {
    
    public static final String DB_URL = "jdbc:http://localhost/phpmyadmin/index.php?route=/database/structure&db=virtuso";
    public static final String DB_USER = "root"; 
    public static final String DB_PASS = "";     

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void initializeTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            System.out.println("Database connection established successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Is XAMPP/MySQL running?");
            e.printStackTrace();
        }
    }
}