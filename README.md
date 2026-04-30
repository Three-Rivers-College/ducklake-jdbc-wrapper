# DuckLake JDBC Wrapper

A thin JDBC driver wrapper for DuckDB that simplifies connection URL assembly and provides custom httpfs extension  initialization logic for DuckLake environments.

## Motivation
Connecting to DuckLake environments requires specific attachment logic and property handling that the default DuckDB JDBC driver doesn't always provide out-of-the-box. For example with [Entrinsik Informer](https://www.entrinsik.com/platform/informer/) running Windows, the httpfs command does not load, making it unable to connect to a remote or [Frozen Ducklacke](https://ducklake.select/2025/10/24/frozen-ducklake/).

This wrapper automates the `LOAD` of extensions and simplifies URL assembly, making it "plug-and-play" for tools that expect a standard JDBC connection string.

By keeping the DuckDB driver as an external dependency, users can easily update the underlying DuckDB version without needing to rebuild this wrapper.

## Features

- **Automatic Driver Registration**: Registers itself as a `java.sql.Driver`.
- **URL Assembly & Secure Property Handling**: Supports `jdbc:duck-lake:` prefix and automatically merges connection properties (like `host`, `port`, `user`, `password`, `dbname`) into the `ATTACH` command. This allows BI tools and applications to pass sensitive credentials securely via standard JDBC properties instead of embedding them in a plain-text connection URL.
- **Dynamic Dependency**: Designed to work with your existing DuckDB JDBC JAR.

## Prerequisites

- Java 8 or higher
- Maven 3.6+ (Optional)
- [DuckDB JDBC Driver JAR](https://duckdb.org/install/?environment=java) (external)

## Building

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

## Usage

When running your application, include both this wrapper and the DuckDB JDBC driver in your classpath:

```bash
java -cp "ducklake-jdbc-wrapper.jar:duckdb_jdbc.jar" YourApp
```

### Connection URL Examples

- **In-Memory (Default)**: `jdbc:duck-lake:`
- **Local File**: `jdbc:duck-lake:local_cache.db`
- **With Inline Parameters**: `jdbc:duck-lake:postgres?host=lakehouse.example.com&dbname=catalog`
- **Property-Driven (Secure)**: `jdbc:duck-lake:postgres` (Pass credentials via JDBC `Connection Properties`)

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
