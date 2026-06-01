# 🚀 2단계: 데이터베이스 연동
## 요구사항
1단계 메모리 저장은 서버 재시작 시 예약 데이터가 모두 사라지는 상황이다. 이를 해결하기 위해 예약 CRUD를 H2 데이터베이스로 전환한다.

⚠️ 새로운 테스트 도구나 기법(Spring Boot Test, Mock, RestAssured 추가 활용 등)을 도입하지 않고 레벨1에서 학습했던 JUnit만 활용한 단위 테스트에 집중한다. 요구사항에서 RestAssured가 주어진 경우 그대로 사용하되, 그 위에 새 테스트 기법을 쌓지 않는다.

## 환경 설정
- build.gradle에 의존성 추가: spring-boot-starter-jdbc, h2
- application.properties에 H2 콘솔 활성화와 datasource URL을 설정한다:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.datasource.url=jdbc:h2:mem:database
```

## 테이블 스키마
resources/schema.sql:

```sql
CREATE TABLE reservation (
id      BIGINT       NOT NULL AUTO_INCREMENT,
name    VARCHAR(255) NOT NULL,
date    VARCHAR(255) NOT NULL,
time    VARCHAR(255) NOT NULL,
PRIMARY KEY (id)
);
```

## 구현 전환
- 1단계에서 만든 조회 · 추가 · 삭제 API를 모두 JdbcTemplate 기반으로 전환한다
- 기존의 List<Reservation>, AtomicLong은 제거한다
- 예약 추가 시 DB가 생성한 id를 응답에 담는다

## 요구사항 테스트
아래 테스트가 모두 통과하면 단계 2 완료.

```java
@Autowired
private JdbcTemplate jdbcTemplate;

@Test
void 데이터베이스_연동() {
try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
assertThat(connection).isNotNull();
assertThat(connection.getCatalog()).isEqualTo("DATABASE");
assertThat(connection.getMetaData().getTables(null, null, "RESERVATION", null).next()).isTrue();
} catch (SQLException e) {
throw new RuntimeException(e);
}
}

@Test
void DB_조회_API_전환() {
jdbcTemplate.update("INSERT INTO reservation (name, date, time) VALUES (?, ?, ?)", "브라운", "2023-08-05", "15:40");

    List<Reservation> reservations = RestAssured.given().log().all()
            .when().get("/reservations")
            .then().log().all()
            .statusCode(200).extract()
            .jsonPath().getList(".", Reservation.class);

    Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);

    assertThat(reservations.size()).isEqualTo(count);
}

@Test
void DB_추가_삭제_API_전환() {
Map<String, String> params = new HashMap<>();
params.put("name", "브라운");
params.put("date", "2023-08-05");
params.put("time", "10:00");

    RestAssured.given().log().all()
            .contentType(ContentType.JSON)
            .body(params)
            .when().post("/reservations")
            .then().log().all()
            .statusCode(200);

    Integer count = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
    assertThat(count).isEqualTo(1);

    RestAssured.given().log().all()
            .when().delete("/reservations/1")
            .then().log().all()
            .statusCode(200);

    Integer countAfterDelete = jdbcTemplate.queryForObject("SELECT count(1) from reservation", Integer.class);
    assertThat(countAfterDelete).isEqualTo(0);
}
```

## 힌트
막힐 때 참고. 처음부터 모두 읽을 필요는 없다.

### 어디서 시작할지 모르겠다면
- 데이터베이스 연동을 위해 gradle 의존성 추가 → schema 정의 → 데이터베이스 설정 순서로 진행한다.
- 학습 테스트를 이용해 사용법을 먼저 익히고, 미션에 적용하기 적절한 방법을 찾는다.

### JdbcTemplate · 식별자 발급
- Spring은 JdbcTemplate 객체를 제공하여 데이터베이스 접근을 돕는다.
- 예약 추가 시 DB가 생성한 id를 응답에 담기 위해 KeyHolder 또는 SimpleJdbcInsert를 활용할 수 있다.

### H2 참고
H2는 Java로 작성된 관계형 데이터베이스(RDBMS)로, 인메모리 모드에서 시스템 메모리에 데이터를 저장한다. 프로그램이나 DB 서버가 종료되면 데이터는 사라진다.
H2 콘솔(/h2-console)에서 데이터베이스 상태를 확인할 수 있다.

### SQL 쿼리 예시

```sql
-- SELECT (전체)
SELECT id, name, date, time FROM reservation;

-- SELECT (조건)
SELECT id, name, date, time FROM reservation WHERE id = 1;

-- INSERT
INSERT INTO reservation(name, date, time) VALUES (?, ?, ?);

-- DELETE
DELETE FROM reservation WHERE id = 1;
```

### 키워드
- JdbcTemplate
- KeyHolder, SimpleJdbcInsert
- SQL — SELECT, INSERT, DELETE
