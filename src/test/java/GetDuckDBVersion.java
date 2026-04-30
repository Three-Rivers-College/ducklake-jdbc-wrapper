import java.sql.*;

public class GetDuckDBVersion {
    public static void main(String[] args) {
        try {
            Class.forName("org.duckdb.DuckDBDriver");
            Connection conn = DriverManager.getConnection("jdbc:duckdb:");
            DatabaseMetaData dbmd = conn.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());
            System.out.println("Database Product Version: " + dbmd.getDatabaseProductVersion());
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
