# 내 SQL 로거 라이브러리 만들기 — Spring Boot Starter로 배포하기

---

## 결론: Spring Boot Auto-configuration + JitPack으로 배포하면 남들이 의존성 한 줄로 쓸 수 있다

p6spy를 그냥 쓰는 것과 내 라이브러리를 만드는 것의 차이는 **Auto-configuration**이다.
Spring Boot Starter 구조를 만들면 사용자는 `build.gradle`에 한 줄만 추가하면 설정 없이 동작한다.

```groovy
// 사용자 입장에서 이것만 추가하면 끝
dependencies {
    implementation 'com.github.나의찬:my-sql-logger:1.0.0'
}
```

---

## 1. P6Spy의 단점 — 내가 개선할 것들

| 단점 | 원인 | 내 라이브러리에서 개선 방향 |
|---|---|---|
| `spy.properties` 별도 파일 필요 | 자체 설정 시스템 | `application.properties`로 통합 |
| 드라이버 클래스명, URL 직접 변경 필요 | JDBC Driver Proxy 방식 | DataSource BeanPostProcessor 방식 (변경 불필요) |
| 무거움 — 안 쓰는 기능 많음 | monolithic 설계 | 핵심 기능만 경량으로 |
| 로그 포맷 커스터마이징이 불편 | 고정된 포맷터 | 인터페이스로 포맷터 교체 가능하게 |
| JSON 로그 포맷 없음 | 미지원 | JSON 포맷 기본 제공 |
| 느린 쿼리만 필터링 불편 | 설정 복잡 | `sql-logger.slow-query-threshold-ms` 한 줄 |
| Spring Boot Auto-configuration 없음 | 오래된 라이브러리 | 완전한 Auto-configuration 지원 |

---

## 2. 선행 학습 — 라이브러리를 만들기 전에 알아야 하는 것

### 2-1. Spring Boot Auto-configuration 동작 원리

Spring Boot는 애플리케이션 시작 시 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 파일을 읽어서 자동으로 빈을 등록한다.

```
[Spring Boot 시작]
    → classpath 스캔
    → META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 발견
    → 여기 적힌 클래스들을 @Configuration으로 처리
    → 조건(@ConditionalOn...)에 맞는 빈만 등록
```

### 2-2. BeanPostProcessor — 이미 만들어진 빈을 가로채는 방법

Auto-configuration에서 DataSource를 감싸는 가장 깔끔한 방법이다.
Spring이 `DataSource` 빈을 만들고 나면 `BeanPostProcessor`의 `postProcessAfterInitialization`이 호출된다.
이 시점에 `ProxyDataSource`로 교체하면 JdbcTemplate 등 다른 빈들은 Proxy를 주입받게 된다.

```java
@Bean
public static BeanPostProcessor dataSourceProxyBeanPostProcessor(...) {
    return new BeanPostProcessor() {
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            if (bean instanceof DataSource) {
                return new ProxyDataSource((DataSource) bean, ...);
            }
            return bean; // DataSource가 아니면 그냥 통과
        }
    };
}
```

**왜 `static`인가**: `BeanPostProcessor`는 Spring 컨테이너 초기화 초반에 필요하다. `static`이 아니면 `@Configuration` 클래스 자체가 아직 완전히 초기화되지 않아 오동작할 수 있다.

### 2-3. @ConditionalOn... — 조건부 빈 등록

| 어노테이션 | 조건 |
|---|---|
| `@ConditionalOnClass(DataSource.class)` | DataSource 클래스가 classpath에 있을 때만 |
| `@ConditionalOnProperty("sql-logger.enabled")` | 프로퍼티 값이 true일 때만 |
| `@ConditionalOnMissingBean` | 같은 타입의 빈이 없을 때만 (사용자가 직접 설정하면 내 것은 등록 안 함) |

### 2-4. @ConfigurationProperties — 설정값 바인딩

`application.properties`의 값들을 객체로 자동 바인딩한다.

```properties
sql-logger.enabled=true
sql-logger.slow-query-threshold-ms=100
sql-logger.format=json
```

```java
@ConfigurationProperties(prefix = "sql-logger")
public class SqlLoggerProperties {
    private boolean enabled = true;
    private long slowQueryThresholdMs = -1; // -1이면 모든 쿼리 출력
    private Format format = Format.SINGLE_LINE;
    public enum Format { SINGLE_LINE, MULTI_LINE, JSON }
}
```

---

## 3. 라이브러리 구조 — 어떻게 만들 것인가

### 디렉토리 구조

```
my-sql-logger/                              ← GitHub 저장소 이름
├── build.gradle
├── settings.gradle
├── src/
│   └── main/
│       ├── java/
│       │   └── io/github/나의찬/sqllogger/
│       │       ├── proxy/
│       │       │   ├── ProxyDataSource.java
│       │       │   ├── ProxyConnection.java
│       │       │   └── ProxyPreparedStatement.java
│       │       ├── formatter/
│       │       │   ├── SqlLogFormatter.java          ← 인터페이스
│       │       │   ├── SingleLineSqlLogFormatter.java
│       │       │   ├── MultiLineSqlLogFormatter.java
│       │       │   └── JsonSqlLogFormatter.java
│       │       ├── SqlLoggerProperties.java
│       │       └── SqlLoggerAutoConfiguration.java
│       └── resources/
│           └── META-INF/
│               └── spring/
│                   └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── README.md
```

---

## 4. 핵심 구현

### 4-1. SqlLoggerProperties

```java
package io.github.나의찬.sqllogger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sql-logger")
public class SqlLoggerProperties {

    /** 전체 on/off */
    private boolean enabled = true;

    /** -1이면 모든 쿼리, 100이면 100ms 초과 쿼리만 출력 */
    private long slowQueryThresholdMs = -1;

    /** 로그 포맷 */
    private Format format = Format.SINGLE_LINE;

    /** 실행 시간 출력 여부 */
    private boolean showExecutionTime = true;

    /** 파라미터 치환 여부 */
    private boolean showParameters = true;

    public enum Format { SINGLE_LINE, MULTI_LINE, JSON }

    // getter / setter
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(long ms) { this.slowQueryThresholdMs = ms; }
    public Format getFormat() { return format; }
    public void setFormat(Format format) { this.format = format; }
    public boolean isShowExecutionTime() { return showExecutionTime; }
    public void setShowExecutionTime(boolean show) { this.showExecutionTime = show; }
    public boolean isShowParameters() { return showParameters; }
    public void setShowParameters(boolean show) { this.showParameters = show; }
}
```

### 4-2. SqlLogFormatter — 사용자가 교체 가능한 인터페이스

```java
package io.github.나의찬.sqllogger.formatter;

public interface SqlLogFormatter {
    /**
     * @param sql           실행된 SQL (파라미터 치환 완료)
     * @param elapsedMs     실행 시간 (ms), 실패 시 -1
     */
    String format(String sql, long elapsedMs);
}
```

```java
// 기본 구현 1 — 한 줄 출력
public class SingleLineSqlLogFormatter implements SqlLogFormatter {
    @Override
    public String format(String sql, long elapsedMs) {
        if (elapsedMs < 0) return "[SQL-ERROR] " + sql;
        return String.format("[SQL] %dms | %s", elapsedMs, sql);
    }
}

// 기본 구현 2 — 여러 줄 출력 (긴 쿼리 가독성)
public class MultiLineSqlLogFormatter implements SqlLogFormatter {
    @Override
    public String format(String sql, long elapsedMs) {
        return String.format("""
                [SQL]
                time  : %dms
                query : %s
                """, elapsedMs, sql);
    }
}

// 기본 구현 3 — JSON 포맷 (로그 수집 시스템 연동)
public class JsonSqlLogFormatter implements SqlLogFormatter {
    @Override
    public String format(String sql, long elapsedMs) {
        return String.format(
            "{\"type\":\"SQL\",\"elapsed_ms\":%d,\"query\":\"%s\"}",
            elapsedMs, sql.replace("\"", "\\\"").replace("\n", "\\n")
        );
    }
}
```

### 4-3. ProxyPreparedStatement — 포맷터를 주입받도록 수정

```java
package io.github.나의찬.sqllogger.proxy;

import io.github.나의찬.sqllogger.SqlLoggerProperties;
import io.github.나의찬.sqllogger.formatter.SqlLogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.TreeMap;

public class ProxyPreparedStatement implements PreparedStatement {

    private static final Logger log = LoggerFactory.getLogger("SQL");

    private final PreparedStatement delegate;
    private final String originalSql;
    private final SqlLoggerProperties properties;
    private final SqlLogFormatter formatter;
    private final TreeMap<Integer, String> parameters = new TreeMap<>();

    public ProxyPreparedStatement(PreparedStatement delegate, String sql,
                                  SqlLoggerProperties properties, SqlLogFormatter formatter) {
        this.delegate = delegate;
        this.originalSql = sql;
        this.properties = properties;
        this.formatter = formatter;
    }

    @Override
    public void setString(int i, String x) throws SQLException {
        if (properties.isShowParameters()) parameters.put(i, "'" + x + "'");
        delegate.setString(i, x);
    }

    @Override
    public void setLong(int i, long x) throws SQLException {
        if (properties.isShowParameters()) parameters.put(i, String.valueOf(x));
        delegate.setLong(i, x);
    }

    @Override
    public void setInt(int i, int x) throws SQLException {
        if (properties.isShowParameters()) parameters.put(i, String.valueOf(x));
        delegate.setInt(i, x);
    }

    @Override
    public void setBoolean(int i, boolean x) throws SQLException {
        if (properties.isShowParameters()) parameters.put(i, String.valueOf(x));
        delegate.setBoolean(i, x);
    }

    @Override
    public void setNull(int i, int sqlType) throws SQLException {
        if (properties.isShowParameters()) parameters.put(i, "NULL");
        delegate.setNull(i, sqlType);
    }

    @Override
    public void setObject(int i, Object x) throws SQLException {
        if (properties.isShowParameters()) {
            if (x == null) parameters.put(i, "NULL");
            else if (x instanceof String) parameters.put(i, "'" + x + "'");
            else parameters.put(i, String.valueOf(x));
        }
        delegate.setObject(i, x);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long start = System.currentTimeMillis();
        try {
            ResultSet rs = delegate.executeQuery();
            printLog(System.currentTimeMillis() - start);
            return rs;
        } catch (SQLException e) {
            printLog(-1);
            throw e;
        }
    }

    @Override
    public int executeUpdate() throws SQLException {
        long start = System.currentTimeMillis();
        try {
            int result = delegate.executeUpdate();
            printLog(System.currentTimeMillis() - start);
            return result;
        } catch (SQLException e) {
            printLog(-1);
            throw e;
        }
    }

    @Override
    public boolean execute() throws SQLException {
        long start = System.currentTimeMillis();
        try {
            boolean result = delegate.execute();
            printLog(System.currentTimeMillis() - start);
            return result;
        } catch (SQLException e) {
            printLog(-1);
            throw e;
        }
    }

    private void printLog(long elapsedMs) {
        // 느린 쿼리 필터링
        if (properties.getSlowQueryThresholdMs() >= 0 && elapsedMs >= 0
                && elapsedMs < properties.getSlowQueryThresholdMs()) {
            return; // 임계값 미만이면 출력 안 함
        }
        String completeSql = fillParameters(originalSql);
        String message = formatter.format(completeSql, elapsedMs);
        if (elapsedMs < 0) log.warn(message);
        else log.info(message);
    }

    private String fillParameters(String sql) {
        if (!properties.isShowParameters() || parameters.isEmpty()) return sql;
        StringBuilder result = new StringBuilder(sql);
        for (String value : parameters.values()) {
            int idx = result.indexOf("?");
            if (idx == -1) break;
            result.replace(idx, idx + 1, value);
        }
        return result.toString();
    }

    // 나머지 메서드는 이전 문서의 delegate 위임과 동일 (생략)
    // ...
}
```

### 4-4. SqlLoggerAutoConfiguration — 핵심

```java
package io.github.나의찬.sqllogger;

import io.github.나의찬.sqllogger.formatter.*;
import io.github.나의찬.sqllogger.proxy.ProxyDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnClass(DataSource.class)
@ConditionalOnProperty(prefix = "sql-logger", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SqlLoggerProperties.class)
public class SqlLoggerAutoConfiguration {

    /**
     * 사용자가 직접 포맷터 빈을 등록하면 그것을 쓰고, 아니면 기본값 사용
     */
    @Bean
    @ConditionalOnMissingBean(SqlLogFormatter.class)
    public SqlLogFormatter sqlLogFormatter(SqlLoggerProperties properties) {
        return switch (properties.getFormat()) {
            case MULTI_LINE -> new MultiLineSqlLogFormatter();
            case JSON -> new JsonSqlLogFormatter();
            default -> new SingleLineSqlLogFormatter();
        };
    }

    /**
     * BeanPostProcessor로 DataSource를 가로채서 Proxy로 교체
     * static이어야 하는 이유: BeanPostProcessor는 컨테이너 초기화 초반에 등록되어야 함
     */
    @Bean
    public static BeanPostProcessor sqlLoggerDataSourcePostProcessor(
            SqlLoggerProperties properties,
            SqlLogFormatter formatter) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof DataSource dataSource
                        && !(bean instanceof ProxyDataSource)) {
                    return new ProxyDataSource(dataSource, properties, formatter);
                }
                return bean;
            }
        };
    }
}
```

### 4-5. META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

이 파일이 없으면 Auto-configuration이 동작하지 않는다.

```
io.github.나의찬.sqllogger.SqlLoggerAutoConfiguration
```

---

## 5. build.gradle — 라이브러리용 설정

```groovy
plugins {
    id 'java-library'                          // 라이브러리임을 선언
    id 'maven-publish'                         // 배포용
}

group = 'io.github.나의찬'
version = '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_17
    withSourcesJar()                           // sources.jar도 같이 배포
    withJavadocJar()                           // javadoc.jar도 같이 배포
}

dependencies {
    // compileOnly — 라이브러리를 사용하는 프로젝트가 이미 가지고 있을 것들
    compileOnly 'org.springframework.boot:spring-boot-autoconfigure:3.4.4'
    compileOnly 'org.springframework.boot:spring-boot-starter-jdbc:3.4.4'
    compileOnly 'org.slf4j:slf4j-api:2.0.9'

    // annotationProcessor — 컴파일 시에만 필요
    annotationProcessor 'org.springframework.boot:spring-boot-configuration-processor:3.4.4'

    testImplementation 'org.springframework.boot:spring-boot-starter-test:3.4.4'
    testImplementation 'com.h2database:h2:2.3.232'
}

publishing {
    publications {
        mavenJava(MavenPublication) {
            from components.java
            pom {
                name = 'My SQL Logger'
                description = 'SQL query logger with actual parameter substitution for Spring Boot'
                url = 'https://github.com/나의찬/my-sql-logger'
                licenses {
                    license {
                        name = 'MIT License'
                        url = 'https://opensource.org/licenses/MIT'
                    }
                }
            }
        }
    }
}
```

---

## 6. 배포 방법 3가지

### 6-1. 로컬 Maven 저장소 (팀 내 공유)

```bash
./gradlew publishToMavenLocal
```

사용하는 쪽 `build.gradle`:
```groovy
repositories {
    mavenLocal()  // 로컬 저장소 추가
    mavenCentral()
}
dependencies {
    implementation 'io.github.나의찬:my-sql-logger:1.0.0'
}
```

### 6-2. JitPack — GitHub 저장소로 바로 배포 (가장 간단)

1. GitHub에 `my-sql-logger` 저장소 생성
2. 코드 push 후 GitHub에서 Release 태그 생성 (예: `v1.0.0`)
3. [jitpack.io](https://jitpack.io) 에서 저장소 URL 입력하면 자동 빌드

사용하는 쪽 `build.gradle`:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
    mavenCentral()
}
dependencies {
    implementation 'com.github.나의찬:my-sql-logger:v1.0.0'
}
```

JitPack은 GitHub Release 태그를 버전으로 인식한다. 별도 서버 설정이 필요 없다.

### 6-3. Maven Central — 공식 배포 (가장 표준적)

1. [central.sonatype.com](https://central.sonatype.com) 계정 생성
2. 네임스페이스 소유권 증명 (GitHub Actions로 검증)
3. GPG 서명 설정
4. `./gradlew publishToMavenCentral`

```groovy
// build.gradle에 추가
plugins {
    id 'com.gradleup.nmcp' version '0.0.8'  // Maven Central 배포 플러그인
}

nmcp {
    publishAllPublicationsToCentralPortal {
        username = System.getenv("MAVEN_CENTRAL_USERNAME")
        password = System.getenv("MAVEN_CENTRAL_PASSWORD")
        publishingType = "AUTOMATIC"
    }
}
```

사용하는 쪽 `build.gradle`:
```groovy
repositories {
    mavenCentral()  // 기본 저장소이므로 추가 설정 불필요
}
dependencies {
    implementation 'io.github.나의찬:my-sql-logger:1.0.0'
}
```

---

## 7. 사용자 입장에서 최종 사용법

```groovy
// build.gradle
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.나의찬:my-sql-logger:v1.0.0'
}
```

```properties
# application.properties — 설정이 없어도 기본값으로 동작
sql-logger.enabled=true
sql-logger.format=multi_line
sql-logger.slow-query-threshold-ms=100  # 100ms 초과 쿼리만 출력
sql-logger.show-execution-time=true
sql-logger.show-parameters=true
```

```java
// 포맷을 완전히 커스터마이징하고 싶으면 빈 등록
@Bean
public SqlLogFormatter myFormatter() {
    return (sql, elapsedMs) -> "[MY-SQL] " + sql + " (" + elapsedMs + "ms)";
}
```

---

## 8. 구현 순서 요약

```
1. GitHub에 my-sql-logger 저장소 생성

2. 핵심 구현
   - SqlLoggerProperties (설정값 바인딩)
   - SqlLogFormatter 인터페이스 + 3가지 구현체
   - ProxyPreparedStatement (파라미터 기록 + SQL 완성)
   - ProxyConnection (prepareStatement 가로채기)
   - ProxyDataSource (getConnection 가로채기)

3. Auto-configuration
   - SqlLoggerAutoConfiguration (@AutoConfiguration + BeanPostProcessor)
   - META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

4. 테스트
   - Spring Boot Test로 DataSource 빈이 ProxyDataSource로 감싸졌는지 검증
   - 실제 JdbcTemplate 호출 시 로그가 출력되는지 검증

5. 배포
   - GitHub Release 태그 생성
   - JitPack에서 빌드 확인
   - README.md에 사용법 작성
```
