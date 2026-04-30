import java.sql.*;

public class TestPreparedStatementFetchSize {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestPreparedStatementFetchSize <jdbc_url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected.");

            String sql = "SELECT * FROM bronze_suspicious_applicants";
            System.out.println("Executing via PreparedStatement (FetchSize 1): " + sql);
            
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setFetchSize(1); // Informer often sets fetch size
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
            }
            System.out.println("Success! Total rows: " + count);

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
