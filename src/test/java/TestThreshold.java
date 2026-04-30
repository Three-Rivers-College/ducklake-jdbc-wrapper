import java.sql.*;

public class TestThreshold {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestThreshold <jdbc_url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected.");

            for (int limit : new int[]{10, 50, 100, 150, 200}) {
                String sql = "SELECT * FROM bronze_suspicious_applicants LIMIT " + limit;
                System.out.println("Executing: " + sql);
                try {
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);
                    int count = 0;
                    while (rs.next()) count++;
                    System.out.println("Success! Fetched " + count + " rows.");
                } catch (Exception e) {
                    System.err.println("FAILED at LIMIT " + limit);
                    e.printStackTrace();
                    break;
                }
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
