import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseApp {
    
    public static final String DB_URL = "jdbc:mysql://localhost:3306/virtuso"; // Update if using cloud DB
    public static final String DB_USER = "root"; 
    public static final String DB_PASS = "";     

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void initializeTables() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            
            // 1. Student Login Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS student_login (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(100) NOT NULL)"
            );

            // 2. User Sort History Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_sort_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "algorithm_name VARCHAR(50) NOT NULL, " +
                "input_array TEXT NOT NULL, " +
                "sorted_array TEXT NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // 3. User Quiz History Table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_quiz_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "actual_algorithm VARCHAR(50) NOT NULL, " +
                "user_guess VARCHAR(50) NOT NULL, " +
                "is_correct TINYINT(1) NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            // 4. NEW: User Knowledge Bank Table (For PDF Extracts)
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_knowledge_bank (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "question TEXT NOT NULL, " +
                "answer TEXT NOT NULL, " +
                "source_file VARCHAR(255), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"
            );

            System.out.println("Database connection established and active tables verified successfully.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed. Please make sure MySQL is running.");
            e.printStackTrace();
        }
    }
}