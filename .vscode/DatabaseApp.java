import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseApp {
    
    public static final String DB_URL = "jdbc:mysql://localhost:3306/virtuso";
    public static final String DB_USER = "root";
    public static final String DB_PASS = ""; 

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("⚠️ MySQL JDBC Driver missing! Please check 'Referenced Libraries' in VS Code.");
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void initializeTables() {
        String createLoginTableSQL = "CREATE TABLE IF NOT EXISTS student_login (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "email VARCHAR(150) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createHistoryTableSQL = "CREATE TABLE IF NOT EXISTS user_sort_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "algorithm_name VARCHAR(100) NOT NULL, " +
                "input_array TEXT NOT NULL, " +
                "sorted_array TEXT NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        String createQuizTableSQL = "CREATE TABLE IF NOT EXISTS user_quiz_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(100) NOT NULL, " +
                "actual_algorithm VARCHAR(100) NOT NULL, " +
                "user_guess VARCHAR(100) NOT NULL, " +
                "is_correct BOOLEAN NOT NULL, " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createLoginTableSQL);
            stmt.execute(createHistoryTableSQL);
            stmt.execute(createQuizTableSQL); 
            
            System.out.println("✅ Database connection successful. Schema verified.");

        } catch (SQLException e) {
            System.err.println("⚠️ Database initialization notice: " + e.getMessage());
        }
    }
}