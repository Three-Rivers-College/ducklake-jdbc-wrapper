import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestWrapper {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestWrapper <attach_path>");
            System.exit(1);
        }
        String attachPath = args[0];
        try {
            // Register our driver explicitly just in case
            Class.forName("com.ducklake.jdbc.DuckDBWrapperDriver");
            
            // Use the wrapper URL
            Connection conn = DriverManager.getConnection("jdbc:duck-lake:");
            Statement stmt = conn.createStatement();

            System.out.println("Connected via Wrapper.");

            // We don't manually load here, the wrapper should have done it.
            // Let's try to ATTACH directly.
            
            System.out.println("Attempting ATTACH...");
            stmt.execute("ATTACH '" + attachPath + "' AS mylake");
            stmt.execute("USE mylake");

            ResultSet rs = stmt.executeQuery("SELECT * FROM watermark_courses LIMIT 1");
            if (rs.next()) {
                System.out.println("Success! Course: " + rs.getString(1));
            }

            conn.close();
            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
