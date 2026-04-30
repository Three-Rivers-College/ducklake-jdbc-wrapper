import java.sql.*;

public class TestLimitFail {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestLimitFail <jdbc_url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected.");

            int highLimit = 1000;
            String sql = "SELECT * FROM bronze_suspicious_applicants LIMIT " + highLimit;
            
            System.out.println("Testing Statement with LIMIT " + highLimit);
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                int count = 0;
                while (rs.next()) count++;
                System.out.println("Statement Success: Fetched " + count + " rows.");
            } catch (Exception e) {
                System.err.println("Statement FAILED:");
                e.printStackTrace();
            }

            System.out.println("\nTesting PreparedStatement with LIMIT " + highLimit);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                int count = 0;
                while (rs.next()) count++;
                System.out.println("PreparedStatement Success: Fetched " + count + " rows.");
            } catch (Exception e) {
                System.err.println("PreparedStatement FAILED:");
                e.printStackTrace();
            }

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
