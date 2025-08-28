import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // keep your credentials here (same as you already used)
    private static final String URL = "jdbc:mysql://localhost:3306/bank_db";
    private static final String USER = "root";
    private static final String PASSWORD = "Nandu00@5143";

    // Reusable method for other classes
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // ensure driver is loaded (you already had this)
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // keep a small test main (optional) - you ran this successfully already
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("✅ Database connected successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }
}
