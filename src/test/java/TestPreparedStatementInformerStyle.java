import java.sql.*;

public class TestPreparedStatementInformerStyle {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestPreparedStatementInformerStyle <jdbc_url>");
            System.exit(1);
        }
        String url = args[0];
        try {
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            Connection conn = DriverManager.getConnection(url);
            System.out.println("Connected.");

            String sql = "SELECT * FROM bronze_suspicious_applicants";
            System.out.println("Executing via PreparedStatement (Scroll Insensitive): " + sql);
            
            // Informer might use scroll insensitive/read only
            PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
