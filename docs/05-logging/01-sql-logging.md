# 실제 SQL 로깅 구현 — P6Spy처럼 파라미터가 치환된 쿼리 보기

---

## 결론: JDBC 계층에 Proxy를 끼워 넣으면 된다

Spring은 `DataSource → Connection → PreparedStatement` 순으로 JDBC를 사용한다.
이 체인의 `PreparedStatement` 단계에서 `setString(1, "브라운")` 같은 파라미터 바인딩이 일어난다.
Proxy 패턴으로 이 단계를 가로채면 `?`가 실제 값으로 치환된 완전한 SQL을 볼 수 있다.

```
[Application]
    ↓
[JdbcTemplate]
    ↓
[DataSource] ← 여기에 Proxy를 끼운다
    ↓
[Connection Proxy] ← PreparedStatement를 만들 때 Proxy로 감싼다
    ↓
[PreparedStatement Proxy] ← setXxx() 호출을 가로채서 파라미터를 기록한다
    ↓
[실제 JDBC Driver (H2 / MySQL)]
```

---

## 1. 선행 학습 — 이것들을 먼저 이해해야 한다

### 1-1. JDBC 아키텍처 4계층

```
DataSource          → Connection 풀을 관리하는 팩토리
  └── Connection    → DB와의 세션 하나
       └── PreparedStatement → SQL 하나 + 파라미터 바인딩
            └── ResultSet    → 조회 결과
```

`JdbcTemplate`은 이 4계층을 편하게 쓰기 위한 래퍼일 뿐이다.
내부에서는 결국 `DataSource.getConnection()` → `conn.prepareStatement(sql)` → `ps.setString(1, value)` → `ps.executeQuery()`를 호출한다.

### 1-2. Proxy 패턴

```java
// 원본 인터페이스
interface Foo {
    void doSomething();
}

// Proxy — 원본을 들고 있으면서 앞뒤에 행동을 추가
class ProxyFoo implements Foo {
    private final Foo real;  // 진짜 구현체를 위임

    @Override
    public void doSomething() {
        System.out.println("before");  // 가로채기
        real.doSomething();            // 실제 동작 위임
        System.out.println("after");   // 가로채기
    }
}
```

`PreparedStatement`는 인터페이스다. 이것을 Proxy로 감싸면 모든 `setXxx()` 호출을 가로챌 수 있다.

### 1-3. Decorator 패턴 vs Proxy 패턴

| | Decorator | Proxy |
|---|---|---|
| 목적 | 기능 추가 | 접근 제어 / 관찰 |
| 구현 | 동일 | 동일 (인터페이스 구현 + 위임) |
| SQL 로깅에서 | 로그 출력 기능 추가 → Decorator 역할 | JDBC 호출 가로채기 → Proxy 역할 |

실제로 SQL 로깅 구현은 두 패턴이 혼합된 형태다.

### 1-4. Spring의 DataSource 주입 방식

Spring Boot는 `application.properties`의 설정을 읽어 `HikariDataSource`를 빈으로 등록한다.
`JdbcTemplate`은 이 `DataSource` 빈을 주입받는다.

```
application.properties
    → HikariDataSource (자동 등록)
        → JdbcTemplate 이 주입받음
```

우리가 할 일은 `HikariDataSource`를 직접 쓰는 대신, 그 앞에 `ProxyDataSource`를 끼워서 `JdbcTemplate`이 Proxy를 쓰도록 바꾸는 것이다.

```
HikariDataSource
    → ProxyDataSource (우리가 만든 것, HikariDataSource를 감쌈)
        → JdbcTemplate 이 주입받음
```

---

## 2. P6Spy가 실제로 하는 일

P6Spy는 JDBC 드라이버 자체를 Proxy로 감싸는 방식이다.

```
application.properties에서:
  spring.datasource.driver-class-name=com.p6spy.engine.spy.P6SpyDriver  ← Proxy 드라이버 등록
  spring.datasource.url=jdbc:p6spy:h2:mem:database                      ← URL 앞에 p6spy: 추가

P6SpyDriver.connect() 호출
    → 진짜 H2Driver.connect() 를 내부에서 호출
    → Connection을 ProxyConnection으로 감싸서 반환
        → prepareStatement() 호출 시 ProxyPreparedStatement 반환
            → setString() 등 파라미터 바인딩을 가로채서 기록
            → execute() 시 기록한 파라미터로 SQL 완성 후 로그 출력
```

P6Spy를 직접 쓰면 이 모든 것이 자동이다. 하지만 내부 원리를 이해하기 위해 직접 구현해보는 것이 목표다.

---

## 3. 직접 구현 — 3개 클래스만 만들면 된다

### 구현할 것

```
ProxyDataSource.java       — DataSource 인터페이스 구현, HikariDataSource를 감쌈
ProxyConnection.java       — Connection 인터페이스 구현, 실제 Connection을 감쌈
ProxyPreparedStatement.java — PreparedStatement 구현, 파라미터 기록 + SQL 완성
```

### 3-1. ProxyPreparedStatement — 핵심

파라미터를 기록하고, 실행 시점에 `?`를 실제 값으로 치환해서 출력한다.

`PreparedStatement`는 메서드가 약 40개다. 모두 구현하면 코드가 방대해지므로,
자주 쓰는 타입만 오버라이드하고 나머지는 `delegate`에 위임한다.

```java
package roomescape.global.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.TreeMap;

public class ProxyPreparedStatement implements PreparedStatement {

    private static final Logger log = LoggerFactory.getLogger("SQL");

    private final PreparedStatement delegate;
    private final String originalSql;
    // TreeMap → 파라미터 인덱스 순서대로 유지
    private final TreeMap<Integer, String> parameters = new TreeMap<>();

    public ProxyPreparedStatement(PreparedStatement delegate, String originalSql) {
        this.delegate = delegate;
        this.originalSql = originalSql;
    }

    // ── 파라미터 바인딩 가로채기 ─────────────────────────────────────────────

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        parameters.put(parameterIndex, "NULL");
        delegate.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        parameters.put(parameterIndex, String.valueOf(x));
        delegate.setBoolean(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        parameters.put(parameterIndex, String.valueOf(x));
        delegate.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        parameters.put(parameterIndex, String.valueOf(x));
        delegate.setLong(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        parameters.put(parameterIndex, "'" + x + "'");
        delegate.setString(parameterIndex, x);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        if (x == null) {
            parameters.put(parameterIndex, "NULL");
        } else if (x instanceof String) {
            parameters.put(parameterIndex, "'" + x + "'");
        } else {
            parameters.put(parameterIndex, String.valueOf(x));
        }
        delegate.setObject(parameterIndex, x);
    }

    // ── 실행 시점 가로채기 + 로그 출력 ──────────────────────────────────────

    @Override
    public boolean execute() throws SQLException {
        long start = System.nanoTime();
        try {
            boolean result = delegate.execute();
            printLog(System.nanoTime() - start);
            return result;
        } catch (SQLException e) {
            printLog(-1);
            throw e;
        }
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long start = System.nanoTime();
        try {
            ResultSet result = delegate.executeQuery();
            printLog(System.nanoTime() - start);
            return result;
        } catch (SQLException e) {
            printLog(-1);
            throw e;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        long start = System.nanoTime();
        try {
            int result = delegate.executeUpdate();
            printLog(System.nanoTime() - start);
            return result;
        } catch (SQLException e) {
            printLog(-1);
            throw e;
        }
    }

    // ── SQL 완성 ─────────────────────────────────────────────────────────────

    private void printLog(long elapsedNanos) {
        String completeSql = fillParameters(originalSql, parameters);
        if (elapsedNanos < 0) {
            log.warn("[SQL-ERROR] {}", completeSql);
        } else {
            log.info("[SQL] {}ms | {}", elapsedNanos / 1_000_000, completeSql);
        }
    }

    // ? 를 실제 값으로 순서대로 치환
    private String fillParameters(String sql, TreeMap<Integer, String> params) {
        StringBuilder result = new StringBuilder(sql);
        for (String value : params.values()) {
            int idx = result.indexOf("?");
            if (idx == -1) break;
            result.replace(idx, idx + 1, value);
        }
        return result.toString();
    }

    // ── 나머지 메서드는 전부 delegate에 위임 ─────────────────────────────────

    @Override public void clearParameters() throws SQLException { delegate.clearParameters(); }
    @Override public ResultSetMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
    @Override public ParameterMetaData getParameterMetaData() throws SQLException { return delegate.getParameterMetaData(); }
    @Override public void addBatch() throws SQLException { delegate.addBatch(); }
    @Override public int[] executeBatch() throws SQLException { return delegate.executeBatch(); }
    @Override public void close() throws SQLException { delegate.close(); }
    @Override public int getMaxFieldSize() throws SQLException { return delegate.getMaxFieldSize(); }
    @Override public void setMaxFieldSize(int max) throws SQLException { delegate.setMaxFieldSize(max); }
    @Override public int getMaxRows() throws SQLException { return delegate.getMaxRows(); }
    @Override public void setMaxRows(int max) throws SQLException { delegate.setMaxRows(max); }
    @Override public void setEscapeProcessing(boolean enable) throws SQLException { delegate.setEscapeProcessing(enable); }
    @Override public int getQueryTimeout() throws SQLException { return delegate.getQueryTimeout(); }
    @Override public void setQueryTimeout(int seconds) throws SQLException { delegate.setQueryTimeout(seconds); }
    @Override public void cancel() throws SQLException { delegate.cancel(); }
    @Override public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
    @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
    @Override public void setCursorName(String name) throws SQLException { delegate.setCursorName(name); }
    @Override public ResultSet getResultSet() throws SQLException { return delegate.getResultSet(); }
    @Override public int getUpdateCount() throws SQLException { return delegate.getUpdateCount(); }
    @Override public boolean getMoreResults() throws SQLException { return delegate.getMoreResults(); }
    @Override public void setFetchDirection(int direction) throws SQLException { delegate.setFetchDirection(direction); }
    @Override public int getFetchDirection() throws SQLException { return delegate.getFetchDirection(); }
    @Override public void setFetchSize(int rows) throws SQLException { delegate.setFetchSize(rows); }
    @Override public int getFetchSize() throws SQLException { return delegate.getFetchSize(); }
    @Override public int getResultSetConcurrency() throws SQLException { return delegate.getResultSetConcurrency(); }
    @Override public int getResultSetType() throws SQLException { return delegate.getResultSetType(); }
    @Override public void addBatch(String sql) throws SQLException { delegate.addBatch(sql); }
    @Override public void clearBatch() throws SQLException { delegate.clearBatch(); }
    @Override public Connection getConnection() throws SQLException { return delegate.getConnection(); }
    @Override public boolean getMoreResults(int current) throws SQLException { return delegate.getMoreResults(current); }
    @Override public ResultSet getGeneratedKeys() throws SQLException { return delegate.getGeneratedKeys(); }
    @Override public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException { return delegate.executeUpdate(sql, autoGeneratedKeys); }
    @Override public int executeUpdate(String sql, int[] columnIndexes) throws SQLException { return delegate.executeUpdate(sql, columnIndexes); }
    @Override public int executeUpdate(String sql, String[] columnNames) throws SQLException { return delegate.executeUpdate(sql, columnNames); }
    @Override public boolean execute(String sql, int autoGeneratedKeys) throws SQLException { return delegate.execute(sql, autoGeneratedKeys); }
    @Override public boolean execute(String sql, int[] columnIndexes) throws SQLException { return delegate.execute(sql, columnIndexes); }
    @Override public boolean execute(String sql, String[] columnNames) throws SQLException { return delegate.execute(sql, columnNames); }
    @Override public int getResultSetHoldability() throws SQLException { return delegate.getResultSetHoldability(); }
    @Override public boolean isClosed() throws SQLException { return delegate.isClosed(); }
    @Override public void setPoolable(boolean poolable) throws SQLException { delegate.setPoolable(poolable); }
    @Override public boolean isPoolable() throws SQLException { return delegate.isPoolable(); }
    @Override public void closeOnCompletion() throws SQLException { delegate.closeOnCompletion(); }
    @Override public boolean isCloseOnCompletion() throws SQLException { return delegate.isCloseOnCompletion(); }
    @Override public boolean execute(String sql) throws SQLException { return delegate.execute(sql); }
    @Override public ResultSet executeQuery(String sql) throws SQLException { return delegate.executeQuery(sql); }
    @Override public int executeUpdate(String sql) throws SQLException { return delegate.executeUpdate(sql); }
    @Override public void setBytes(int i, byte[] x) throws SQLException { delegate.setBytes(i, x); }
    @Override public void setDate(int i, java.sql.Date x) throws SQLException { delegate.setDate(i, x); }
    @Override public void setTime(int i, Time x) throws SQLException { delegate.setTime(i, x); }
    @Override public void setTimestamp(int i, Timestamp x) throws SQLException { delegate.setTimestamp(i, x); }
    @Override public void setByte(int i, byte x) throws SQLException { delegate.setByte(i, x); }
    @Override public void setShort(int i, short x) throws SQLException { delegate.setShort(i, x); }
    @Override public void setFloat(int i, float x) throws SQLException { delegate.setFloat(i, x); }
    @Override public void setDouble(int i, double x) throws SQLException { delegate.setDouble(i, x); }
    @Override public void setBigDecimal(int i, java.math.BigDecimal x) throws SQLException { delegate.setBigDecimal(i, x); }
    @Override public void setAsciiStream(int i, java.io.InputStream x, int len) throws SQLException { delegate.setAsciiStream(i, x, len); }
    @Override public void setUnicodeStream(int i, java.io.InputStream x, int len) throws SQLException { delegate.setUnicodeStream(i, x, len); }
    @Override public void setBinaryStream(int i, java.io.InputStream x, int len) throws SQLException { delegate.setBinaryStream(i, x, len); }
    @Override public void setObject(int i, Object x, int t) throws SQLException { delegate.setObject(i, x, t); }
    @Override public void setCharacterStream(int i, java.io.Reader r, int len) throws SQLException { delegate.setCharacterStream(i, r, len); }
    @Override public void setRef(int i, Ref x) throws SQLException { delegate.setRef(i, x); }
    @Override public void setBlob(int i, Blob x) throws SQLException { delegate.setBlob(i, x); }
    @Override public void setClob(int i, Clob x) throws SQLException { delegate.setClob(i, x); }
    @Override public void setArray(int i, java.sql.Array x) throws SQLException { delegate.setArray(i, x); }
    @Override public void setDate(int i, java.sql.Date x, java.util.Calendar cal) throws SQLException { delegate.setDate(i, x, cal); }
    @Override public void setTime(int i, Time x, java.util.Calendar cal) throws SQLException { delegate.setTime(i, x, cal); }
    @Override public void setTimestamp(int i, Timestamp x, java.util.Calendar cal) throws SQLException { delegate.setTimestamp(i, x, cal); }
    @Override public void setNull(int i, int t, String tn) throws SQLException { delegate.setNull(i, t, tn); }
    @Override public void setURL(int i, java.net.URL x) throws SQLException { delegate.setURL(i, x); }
    @Override public void setRowId(int i, RowId x) throws SQLException { delegate.setRowId(i, x); }
    @Override public void setNString(int i, String v) throws SQLException { delegate.setNString(i, v); }
    @Override public void setNCharacterStream(int i, java.io.Reader v, long len) throws SQLException { delegate.setNCharacterStream(i, v, len); }
    @Override public void setNClob(int i, NClob v) throws SQLException { delegate.setNClob(i, v); }
    @Override public void setClob(int i, java.io.Reader r, long len) throws SQLException { delegate.setClob(i, r, len); }
    @Override public void setBlob(int i, java.io.InputStream in, long len) throws SQLException { delegate.setBlob(i, in, len); }
    @Override public void setNClob(int i, java.io.Reader r, long len) throws SQLException { delegate.setNClob(i, r, len); }
    @Override public void setSQLXML(int i, SQLXML x) throws SQLException { delegate.setSQLXML(i, x); }
    @Override public void setObject(int i, Object x, int t, int s) throws SQLException { delegate.setObject(i, x, t, s); }
    @Override public void setAsciiStream(int i, java.io.InputStream x, long len) throws SQLException { delegate.setAsciiStream(i, x, len); }
    @Override public void setBinaryStream(int i, java.io.InputStream x, long len) throws SQLException { delegate.setBinaryStream(i, x, len); }
    @Override public void setCharacterStream(int i, java.io.Reader r, long len) throws SQLException { delegate.setCharacterStream(i, r, len); }
    @Override public void setAsciiStream(int i, java.io.InputStream x) throws SQLException { delegate.setAsciiStream(i, x); }
    @Override public void setBinaryStream(int i, java.io.InputStream x) throws SQLException { delegate.setBinaryStream(i, x); }
    @Override public void setCharacterStream(int i, java.io.Reader r) throws SQLException { delegate.setCharacterStream(i, r); }
    @Override public void setNCharacterStream(int i, java.io.Reader v) throws SQLException { delegate.setNCharacterStream(i, v); }
    @Override public void setClob(int i, java.io.Reader r) throws SQLException { delegate.setClob(i, r); }
    @Override public void setBlob(int i, java.io.InputStream in) throws SQLException { delegate.setBlob(i, in); }
    @Override public void setNClob(int i, java.io.Reader r) throws SQLException { delegate.setNClob(i, r); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
}
```

### 3-2. ProxyConnection

`prepareStatement()` 호출 시 `ProxyPreparedStatement`를 반환한다.

```java
package roomescape.global.logging;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ProxyConnection implements Connection {

    private final Connection delegate;

    public ProxyConnection(Connection delegate) {
        this.delegate = delegate;
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new ProxyPreparedStatement(delegate.prepareStatement(sql), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return new ProxyPreparedStatement(delegate.prepareStatement(sql, autoGeneratedKeys), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new ProxyPreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new ProxyPreparedStatement(delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return new ProxyPreparedStatement(delegate.prepareStatement(sql, columnIndexes), sql);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return new ProxyPreparedStatement(delegate.prepareStatement(sql, columnNames), sql);
    }

    // 나머지는 모두 위임
    @Override public Statement createStatement() throws SQLException { return delegate.createStatement(); }
    @Override public CallableStatement prepareCall(String sql) throws SQLException { return delegate.prepareCall(sql); }
    @Override public String nativeSQL(String sql) throws SQLException { return delegate.nativeSQL(sql); }
    @Override public void setAutoCommit(boolean autoCommit) throws SQLException { delegate.setAutoCommit(autoCommit); }
    @Override public boolean getAutoCommit() throws SQLException { return delegate.getAutoCommit(); }
    @Override public void commit() throws SQLException { delegate.commit(); }
    @Override public void rollback() throws SQLException { delegate.rollback(); }
    @Override public void close() throws SQLException { delegate.close(); }
    @Override public boolean isClosed() throws SQLException { return delegate.isClosed(); }
    @Override public DatabaseMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
    @Override public void setReadOnly(boolean readOnly) throws SQLException { delegate.setReadOnly(readOnly); }
    @Override public boolean isReadOnly() throws SQLException { return delegate.isReadOnly(); }
    @Override public void setCatalog(String catalog) throws SQLException { delegate.setCatalog(catalog); }
    @Override public String getCatalog() throws SQLException { return delegate.getCatalog(); }
    @Override public void setTransactionIsolation(int level) throws SQLException { delegate.setTransactionIsolation(level); }
    @Override public int getTransactionIsolation() throws SQLException { return delegate.getTransactionIsolation(); }
    @Override public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
    @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
    @Override public Statement createStatement(int t, int c) throws SQLException { return delegate.createStatement(t, c); }
    @Override public CallableStatement prepareCall(String s, int t, int c) throws SQLException { return delegate.prepareCall(s, t, c); }
    @Override public Map<String, Class<?>> getTypeMap() throws SQLException { return delegate.getTypeMap(); }
    @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException { delegate.setTypeMap(map); }
    @Override public void setHoldability(int h) throws SQLException { delegate.setHoldability(h); }
    @Override public int getHoldability() throws SQLException { return delegate.getHoldability(); }
    @Override public Savepoint setSavepoint() throws SQLException { return delegate.setSavepoint(); }
    @Override public Savepoint setSavepoint(String name) throws SQLException { return delegate.setSavepoint(name); }
    @Override public void rollback(Savepoint savepoint) throws SQLException { delegate.rollback(savepoint); }
    @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException { delegate.releaseSavepoint(savepoint); }
    @Override public Statement createStatement(int t, int c, int h) throws SQLException { return delegate.createStatement(t, c, h); }
    @Override public CallableStatement prepareCall(String s, int t, int c, int h) throws SQLException { return delegate.prepareCall(s, t, c, h); }
    @Override public Clob createClob() throws SQLException { return delegate.createClob(); }
    @Override public Blob createBlob() throws SQLException { return delegate.createBlob(); }
    @Override public NClob createNClob() throws SQLException { return delegate.createNClob(); }
    @Override public SQLXML createSQLXML() throws SQLException { return delegate.createSQLXML(); }
    @Override public boolean isValid(int timeout) throws SQLException { return delegate.isValid(timeout); }
    @Override public void setClientInfo(String name, String value) throws SQLClientInfoException { delegate.setClientInfo(name, value); }
    @Override public void setClientInfo(Properties properties) throws SQLClientInfoException { delegate.setClientInfo(properties); }
    @Override public String getClientInfo(String name) throws SQLException { return delegate.getClientInfo(name); }
    @Override public Properties getClientInfo() throws SQLException { return delegate.getClientInfo(); }
    @Override public Array createArrayOf(String t, Object[] e) throws SQLException { return delegate.createArrayOf(t, e); }
    @Override public Struct createStruct(String t, Object[] a) throws SQLException { return delegate.createStruct(t, a); }
    @Override public void setSchema(String schema) throws SQLException { delegate.setSchema(schema); }
    @Override public String getSchema() throws SQLException { return delegate.getSchema(); }
    @Override public void abort(Executor executor) throws SQLException { delegate.abort(executor); }
    @Override public void setNetworkTimeout(Executor executor, int ms) throws SQLException { delegate.setNetworkTimeout(executor, ms); }
    @Override public int getNetworkTimeout() throws SQLException { return delegate.getNetworkTimeout(); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
}
```

### 3-3. ProxyDataSource

`getConnection()` 호출 시 `ProxyConnection`으로 감싸서 반환한다.

```java
package roomescape.global.logging;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class ProxyDataSource implements DataSource {

    private final DataSource delegate;

    public ProxyDataSource(DataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return new ProxyConnection(delegate.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return new ProxyConnection(delegate.getConnection(username, password));
    }

    @Override public PrintWriter getLogWriter() throws SQLException { return delegate.getLogWriter(); }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { delegate.setLogWriter(out); }
    @Override public void setLoginTimeout(int seconds) throws SQLException { delegate.setLoginTimeout(seconds); }
    @Override public int getLoginTimeout() throws SQLException { return delegate.getLoginTimeout(); }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return delegate.getParentLogger(); }
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
}
```

### 3-4. DataSourceConfig — Spring 빈으로 등록

로컬/개발 환경에서만 활성화하도록 프로파일을 건다.

```java
package roomescape.global.logging;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("!test-no-log")  // 이 프로파일이 아닐 때만 적용
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource proxyDataSource(DataSourceProperties properties) {
        // HikariCP가 자동으로 만들어주는 DataSource를 직접 생성
        HikariDataSource hikari = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        return new ProxyDataSource(hikari);
    }
}
```

---

## 4. 로그 출력 예시

위 구현을 적용하면 다음과 같은 로그가 출력된다.

```
# 기존 Spring 기본 로깅 (파라미터 없음)
DEBUG o.s.jdbc.core.JdbcTemplate - Executing prepared SQL statement [SELECT * FROM reservation WHERE id = ?]

# 직접 구현한 ProxyPreparedStatement 로그 (파라미터 치환됨)
INFO  SQL - 2ms | SELECT * FROM reservation WHERE id = 42

INFO  SQL - 1ms | INSERT INTO reservation (name, theme_slot_id, status) VALUES ('브라운', 1, 'CONFIRMED')

INFO  SQL - 3ms | UPDATE theme_slot SET is_reserved = true WHERE theme_id = 1 AND date = '2026-07-01' AND time_id = 2
```

---

## 5. application.properties 로그 레벨 설정

```properties
# SQL 로거 레벨 설정
logging.level.SQL=INFO

# Spring 기본 JDBC 로그는 끄거나 유지 (중복 방지)
logging.level.org.springframework.jdbc=WARN
```

---

## 6. p6spy 라이브러리를 바로 쓰는 방법

직접 구현 대신 p6spy를 쓰면 위 모든 구현이 필요 없다.

### build.gradle

```groovy
dependencies {
    implementation 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.9.0'
}
```

### application.properties

```properties
# p6spy는 자동 설정되므로 별도 코드 불필요
decorator.datasource.p6spy.enable-logging=true
decorator.datasource.p6spy.multiline=true
```

### spy.properties (src/main/resources에 생성)

```properties
# 로그 포맷 커스터마이징
logMessageFormat=com.p6spy.engine.spy.appender.MultiLineFormat
appender=com.p6spy.engine.spy.appender.Slf4JLogger
logfile=spy.log

# 실행 시간이 이 값(ms) 이상인 쿼리만 로그 (느린 쿼리 탐지)
executionThreshold=100

# 특정 카테고리만 출력 (commit, rollback, statement)
filter=true
include=statement
```

### 출력 예시

```
2026-06-03 10:15:23 | 2ms | statement |
SELECT r.id AS r_id, r.name, r.status
FROM reservation r
    INNER JOIN theme_slot ts ON r.theme_slot_id = ts.id
WHERE r.id = 42;
```

---

## 7. 직접 구현 vs p6spy 비교

| | 직접 구현 | p6spy |
|---|---|---|
| 코드 | 약 300줄 | 의존성 추가 + 설정 파일 |
| 학습 효과 | JDBC 아키텍처, Proxy 패턴 완전 이해 | 없음 |
| 유지보수 | 직접 해야 함 | 라이브러리가 관리 |
| 커스터마이징 | 자유로움 | 설정 파일 범위 내 |
| 운영 환경 제어 | 프로파일로 직접 제어 | `decorator.datasource.p6spy.enable-logging=false` |
| 추천 용도 | 학습 목적 | 실제 프로젝트 사용 |

---

## 정리

```
학습 순서:
  1. JDBC 4계층 (DataSource → Connection → PreparedStatement → ResultSet)
  2. Proxy / Decorator 패턴
  3. Spring의 DataSource 빈 주입 방식

구현 순서:
  1. ProxyPreparedStatement — setXxx() 가로채서 파라미터 기록, execute() 시 SQL 완성
  2. ProxyConnection       — prepareStatement() 시 ProxyPreparedStatement 반환
  3. ProxyDataSource       — getConnection() 시 ProxyConnection 반환
  4. DataSourceConfig      — @Primary 빈으로 등록해서 JdbcTemplate이 사용하게

실제 사용:
  → 학습이 끝났으면 p6spy 라이브러리로 교체 (의존성 한 줄 + 설정 파일)
```
