package com.ducklake.jdbc;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class DuckDBWrapperDriver implements Driver {
    private static final String PREFIX1 = "jdbc:ducklake:";
    private static final String PREFIX2 = "jdbc:duck-lake:";
    private static final String[] PROVIDER_PREFIXES = {"postgres", "mysql", "sqlite", "motherduck", "ducklake"};
    private final org.duckdb.DuckDBDriver driver = new org.duckdb.DuckDBDriver();

    static {
        try {
            DuckDBWrapperDriver instance = new DuckDBWrapperDriver();
            DriverManager.registerDriver(instance);
        } catch (SQLException e) {
            System.err.println("[DuckDB-Wrapper] FAILED to register: " + e.getMessage());
        }
    }

    public static DuckDBWrapperDriver getInstance() {
        return new DuckDBWrapperDriver();
    }

    public DuckDBWrapperDriver() {}

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        
        String suffix;
        if (url.startsWith(PREFIX2)) {
            suffix = url.substring(PREFIX2.length());
        } else {
            suffix = url.substring(PREFIX1.length());
        }
        
        String dbFile = "";
        if (!suffix.isEmpty()) {
            int qMark = suffix.indexOf("?");
            int colon = suffix.indexOf(":");
            int end = suffix.length();
            if (qMark != -1) end = Math.min(end, qMark);
            if (colon != -1) end = Math.min(end, colon);
            
            String candidate = suffix.substring(0, end).trim();
            if (looksLikeDatabaseFile(candidate, suffix)) {
                dbFile = candidate;
            }
        }
        
        if (dbFile.isEmpty()) {
            dbFile = ":memory:";
        }
        String realUrl = "jdbc:duckdb:" + dbFile;
        
        Properties duckdbProps = new Properties();
        if (info != null) {
            String[] allowed = {"read_only", "temp_directory", "access_mode", "threads", "max_memory"};
            for (String key : allowed) {
                String val = info.getProperty(key);
                if (val != null) duckdbProps.setProperty(key, val);
            }
        }

        String attachPath = buildAttachPath(suffix, info);

        Connection conn = null;
        try {
            conn = driver.connect(realUrl, duckdbProps);
        } catch (SQLException e) {
            System.err.println("[DuckDB-Wrapper] Internal driver.connect FAILED: " + e.getMessage());
            throw e;
        }
        
        if (conn == null) {
            return null;
        }
        
        try (Statement stmt = conn.createStatement()) {
            loadExtension(stmt, "ducklake");
            loadExtension(stmt, "httpfs");
            
            if (!attachPath.isEmpty()) {
                stmt.execute("ATTACH '" + attachPath + "' AS mylake;");
                stmt.execute("USE mylake;");
            }
        } catch (SQLException e) {
            String maskedPath = attachPath.replaceAll("password=[^ ]+", "password=********");
            System.err.println("[DuckDB-Wrapper] ERROR during initialization: " + e.getMessage());
            System.err.println("[DuckDB-Wrapper] ATTEMPTED SQL: " + (attachPath.isEmpty() ? "NONE" : "ATTACH '" + maskedPath + "' AS mylake;"));
            try {
                conn.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
        return conn;
    }

    private void loadExtension(Statement stmt, String name) throws SQLException {
        try {
            stmt.execute("LOAD " + name + ";");
        } catch (SQLException e) {
            // If LOAD fails, try INSTALL then LOAD
            stmt.execute("INSTALL " + name + ";");
            stmt.execute("LOAD " + name + ";");
        }
    }

    private String buildAttachPath(String suffix, Properties info) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String base = "";
        String query = "";

        if (suffix.contains("?")) {
            int qMark = suffix.indexOf("?");
            base = suffix.substring(0, qMark);
            query = suffix.substring(qMark + 1);
        } else if (suffix.contains(":")) {
            int firstColon = suffix.indexOf(":");
            base = suffix.substring(0, firstColon);
            query = suffix.substring(firstColon + 1);
        } else {
            base = suffix;
        }

        // Clean up base
        if (base.isEmpty() || base.endsWith(".db")) {
            base = "postgres";
        }
        if (base.endsWith(":")) {
            base = base.substring(0, base.length() - 1);
        }

        // 1. Parse from query variable (from ? or :)
        if (!query.isEmpty()) {
            // If it came from :, it might have spaces or key=val pairs
            // If it came from ?, it might have &
            String[] parts = query.split("[&]");
            for (String p : parts) {
                if (!p.trim().isEmpty()) {
                    if (sb.length() > 0) sb.append(" ");
                    sb.append(p.trim());
                }
            }
        }

        // 2. Add from Properties (Informer fields)
        if (info != null) {
            for (String key : info.stringPropertyNames()) {
                // Filter out common JDBC-internal properties
                if (isJdbcInternalProperty(key)) continue;

                String val = info.getProperty(key);
                if (val != null && !val.isEmpty()) {
                    // Only add if not already provided in the URL query string
                    String keyEq = key + "=";
                    if (sb.indexOf(keyEq) == -1) {
                        if (sb.length() > 0) sb.append(" ");
                        sb.append(key).append("=").append(val);
                    }
                }
            }
        }

        if (sb.length() == 0 && base.equals("postgres")) {
            return ""; // Nothing to attach
        }

        return "ducklake:" + base + ":" + sb.toString();
    }
    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url != null && (url.startsWith(PREFIX1) || url.startsWith(PREFIX2));
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() { return driver.getMajorVersion(); }
    @Override
    public int getMinorVersion() { return driver.getMinorVersion(); }
    @Override
    public boolean jdbcCompliant() { return driver.jdbcCompliant(); }
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException { return driver.getParentLogger(); }

    private boolean isJdbcInternalProperty(String key) {
        String k = key.toLowerCase();
        return k.equals("logintimeout") || k.equals("connecttimeout") || 
               k.equals("remarksreporting") || k.equals("useunicode") || 
               k.equals("characterencoding") || k.equals("allowmultiqueries") ||
               k.startsWith("jdbc.");
    }

    private boolean looksLikeDatabaseFile(String candidate, String suffix) {
        if (candidate.isEmpty()) {
            return false;
        }

        String normalized = candidate.toLowerCase();
        for (String provider : PROVIDER_PREFIXES) {
            if (normalized.equals(provider) && suffix.regionMatches(true, candidate.length(), ":", 0, 1)) {
                return false;
            }
        }

        return candidate.endsWith(".db")
            || candidate.endsWith(".duckdb")
            || candidate.contains("\\")
            || candidate.contains("/")
            || candidate.equals(":memory:");
    }
}
