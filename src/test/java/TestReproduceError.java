import java.sql.*;
import java.util.Properties;

public class TestReproduceError {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestReproduceError <jdbc_url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            System.out.println("Connecting to: " + url);
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected.");

            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM bronze_suspicious_applicants";
            System.out.println("Executing: " + sql);
            
            ResultSet rs = stmt.executeQuery(sql);
            int count = 0;
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            
            while (rs.next()) {
                count++;
                if (count % 50 == 0) {
                    System.out.println("Fetched " + count + " rows...");
                }
            }
            System.out.println("Success! Total rows: " + count);

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
