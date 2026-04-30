import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DuckTest {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java DuckTest <attach_path>");
            System.exit(1);
        }
        String attachPath = args[0];
        try {
            Connection conn = DriverManager.getConnection("jdbc:duckdb:");
            Statement stmt = conn.createStatement();

            System.out.println("Connected.");

            stmt.execute("SELECT 1");
            System.out.println("Basic query OK.");

            //stmt.execute("INSTALL ducklake");
            stmt.execute("LOAD ducklake");
           // stmt.execute("INSTALL httpfs");
            stmt.execute("LOAD httpfs");
            System.out.println("DuckLake loaded.");

            stmt.execute("ATTACH '" + attachPath + "' AS mylake");
            stmt.execute("USE mylake");

            ResultSet rs = stmt.executeQuery("SELECT * FROM watermark_courses");
            while (rs.next()) {
                System.out.println("Course: " + rs.getString(1));
            }

            conn.close();
            System.out.println("Done.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}