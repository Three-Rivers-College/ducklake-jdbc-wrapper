import java.sql.*;

public class TestPreparedStatementVerbose {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestPreparedStatementVerbose <jdbc_url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected.");

            String sql = "SELECT * FROM bronze_suspicious_applicants";
            System.out.println("Executing via PreparedStatement: " + sql);
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            System.out.println("Columns: " + cols);

            int count = 0;
            while (rs.next()) {
                count++;
                for (int i = 1; i <= cols; i++) {
                    Object val = rs.getObject(i);
                }
                if (count % 50 == 0) System.out.println("Fetched " + count + "...");
            }
            System.out.println("Success! Total rows: " + count);

            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
