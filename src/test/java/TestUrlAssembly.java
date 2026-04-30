import com.ducklake.jdbc.DuckDBWrapperDriver;
import java.sql.DriverManager;
import java.util.Properties;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TestUrlAssembly {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java TestUrlAssembly <password> [host] [dbname]");
            System.exit(1);
        }
        String password = args[0];
        String host = (args.length > 1) ? args[1] : "lakehouse.example.com";
        String dbname = (args.length > 2) ? args[2] : "ducklake_catalog";

        try {
            DuckDBWrapperDriver driver = new DuckDBWrapperDriver();
            
            // Simulate Informer-like properties
            Properties info = new Properties();
            info.setProperty("host", host);
            info.setProperty("port", "5432");
            info.setProperty("dbname", dbname);
            info.setProperty("user", "ducklake");
            info.setProperty("password", password);
            
            // We'll capture stdout to see what the driver prints
            PrintStream oldOut = System.out;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            System.setOut(new PrintStream(baos));
            
            try {
                // We use a dummy URL that won't actually trigger a full DB connection 
                // if we just want to see the string, but here we call the driver directly.
                // Note: This will attempt to connect to :memory: duckdb
                driver.connect("jdbc:ducklake:", info);
            } catch (Exception e) {
                // It might fail on the actual ATTACH because we don't have the extension,
                // but it should have printed the ATTACH string by then.
            }
            
            System.setOut(oldOut);
            String output = baos.toString();
            System.out.println("--- Driver Output ---");
            System.out.println(output);
            System.out.println("---------------------");
            
            if (output.contains("Attaching catalog:")) {
                String line = output.substring(output.indexOf("Attaching catalog:"));
                line = line.split("\n")[0];
                System.out.println("VERIFIED ASSEMBLED PATH: " + line);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
