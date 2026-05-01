# DuckLake JDBC Wrapper

A thin JDBC driver wrapper for DuckDB that simplifies connection URL assembly and provides automated initialization for **BI tools** and **DuckLake environments**.

## Motivation
The standard DuckDB JDBC driver is excellent for general-purpose Java development. However, many BI tools do not support executing initialization scripts (e.g., `INSTALL`/`LOAD httpfs;`) upon connection. Additionally, these tools often allow connection details to be passed via standard JDBC properties rather than being embedded in a potentially sensitive plain-text connection URL.

This wrapper provides a "plug-and-play" experience for these environments by:
- **Automating Extension Loading**: Automatically installs and loads the `httpfs` and `ducklake` extensions.
- **Secure Property Mapping**: Maps standard JDBC connection properties (like `user`, `password`, `host`, etc.) directly into DuckDB `ATTACH` commands, keeping credentials out of the URL.
- **Simplifying URL Assembly**: Provides a consistent `jdbc:duck-lake:` prefix that translates into the appropriate DuckDB attachment logic.

## When to Use This Wrapper

- **Use this if**: You are connecting from a BI tool or platform—such as [Entrinsik Informer](https://www.entrinsik.com/platform/informer/)—that works best with standard JDBC property fields and you want to automate extension loading (`httpfs`, etc.) without manual SQL initialization.
- **Don't use this if**: You are writing a custom Java application where you can easily call `stmt.execute("INSTALL httpfs; LOAD httpfs;");` after connecting with the standard DuckDB driver.

## Features

- **Automatic Driver Registration**: Registers itself as a `java.sql.Driver`.
- **URL Assembly & Secure Property Handling**: Supports `jdbc:duck-lake:` prefix and automatically merges connection properties (like `host`, `port`, `user`, `password`, `dbname`) into the `ATTACH` command. This allows BI tools and applications to pass sensitive credentials securely via standard JDBC properties instead of embedding them in a plain-text connection URL.
- **Dynamic Dependency**: Designed to work with your existing DuckDB JDBC JAR.

## Usage

To use this wrapper, include both the **DuckLake JDBC Wrapper** JAR and the standard **DuckDB JDBC Driver** JAR in your tool or application's classpath or folder.

[Download the Wrapper JAR](https://github.com/Three-Rivers-College/ducklake-jdbc-wrapper/releases)

**Driver CLass:** `com.ducklake.jdbc.DuckDBWrapperDriver`

### Connection URL Examples

- **In-Memory (Default)**: `jdbc:duck-lake:`
- **Local File**: `jdbc:duck-lake:local_cache.db`
- **With Inline Parameters**: `jdbc:duck-lake:postgres?host=lakehouse.example.com&dbname=catalog`
- **Property-Driven (Secure)**: `jdbc:duck-lake:postgres` (Pass credentials via JDBC BI `Connection Properties`)

## Building

### Prerequisites

- Java 8 or higher
- Maven 3.6+ (Optional)
- [DuckDB JDBC Driver JAR](https://duckdb.org/install/?environment=java) (external)

### Option 1: Using Maven (Recommended)
To compile the project and generate the thin JAR file:

```bash
mvn clean package
```
The resulting JAR will be in `target/ducklake-jdbc-wrapper-1.0-SNAPSHOT.jar`.

### Option 2: Manual Build (Without Maven)
If you don't have Maven, you can build manually using the standard Java tools:

1. **Create an output directory**:
   ```bash
   mkdir out
   ```

2. **Compile the source**:
   (Ensure `duckdb_jdbc.jar` is in your current directory)
   ```bash
   javac -cp "duckdb_jdbc.jar" -d out src/main/java/com/ducklake/jdbc/DuckDBWrapperDriver.java
   ```

3. **Copy the Service Provider config**:
   ```bash
   mkdir -p out/META-INF/services
   cp src/main/resources/META-INF/services/java.sql.Driver out/META-INF/services/
   ```

4. **Create the JAR**:
   ```bash
   jar cvf ducklake-jdbc-wrapper.jar -C out .
   ```

## Testing

The tests accept connection details via command-line arguments.

### Compiling Tests
```bash
mvn test-compile
```

### Running Tests
Include the DuckDB driver in the classpath when running tests:

```bash
# Example
java -cp "target/ducklake-jdbc-wrapper-1.0-SNAPSHOT.jar:target/test-classes:path/to/duckdb_jdbc.jar" TestWrapper "ducklake:postgres:..."
```

## License

This project is licensed under the [MIT License](LICENSE).
