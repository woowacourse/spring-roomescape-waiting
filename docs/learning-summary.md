# 방탈출 예약 미션 학습 정리

> 5주간의 PR 리뷰 피드백을 통해 배운 것들을 주제별로 정리한 문서.

---

## 1. 도메인 설계

### 생성자와 팩토리 메서드

**처음에 한 실수**: 도메인 객체 생성을 아무 검증 없이 허용했다. `Reservation`을 서비스에서 `new Reservation(name, date, time, theme)` 하면 과거 날짜도, null도 다 들어갔다.

**배운 것**: 도메인 제약은 생성 시점에 강제해야 한다.

```java
// 생성자 안에서 private validate() 호출
public Reservation(String name, LocalDate date, ReservationTime time, Theme theme) {
    validate(name, date, time, theme);
    ...
}

private void validate(String name, LocalDate date, ReservationTime time, Theme theme) {
    if (date == null) throw new InvalidInputException("날짜는 필수입니다.");
    ...
}
```

**추가로 배운 것**: validate를 처음에 `static` 메서드로 만들었다. 외부에서 직접 호출할 수 있어서 생성자를 우회하는 경로가 생긴다. `private` 인스턴스 메서드로 바꿔야 한다.

---

### 신규 생성 vs DB 복원 분리

서비스에서 `Reservation`을 새로 만들 때와 DB에서 꺼내올 때를 같은 경로로 처리하면 문제가 생긴다. DB에 저장된 과거 예약을 불러올 때도 날짜 검증을 통과해야 했다.

**처음 시도**: `restore()` / `create()` 정적 팩토리로 분리. DB 복원 시 `restore()`는 검증 건너뜀.

**더 나은 해결책**: `createdAt`을 외부에서 주입받으면 `date >= createdAt` 조건 하나로 신규/복원 둘 다 처리할 수 있다. 생성 경로를 하나로 통일하는 게 더 단순하다.

---

### 도메인의 비즈니스 규칙 책임

처음에는 과거 날짜 검증, 취소 가능 여부 판단을 서비스가 하고 있었다.

```java
// 서비스가 직접 날짜를 비교하는 코드 — 나쁜 패턴
if (reservation.getDate().isBefore(now.toLocalDate())) {
    throw new PastReservationException();
}
```

**배운 것**: 비즈니스 규칙이 도메인의 데이터에만 의존한다면 도메인이 판단해야 한다.

```java
// 도메인 메서드
public void validateCancellable(LocalDateTime now) {
    if (date.isBefore(now.toLocalDate())) throw new PastReservationException();
}

public Reservation withUpdated(LocalDate newDate, ReservationTime newTime, LocalDateTime now) {
    validateCancellable(now);
    return new Reservation(id, name, newDate, newTime, theme, createdAt);
}
```

서비스는 DB 조회가 필요한 충돌 체크(중복 예약 여부 등)만 담당하고, 나머지는 도메인에 위임한다.

---

### 상태 기반 설계 (ReservationStatus)

Week 4에서 `reservation`과 `reservation_waiting`을 별도 테이블로 관리했다. 문제점:
- 같은 데이터(날짜, 시간, 테마, 이름)를 두 테이블에 중복 저장
- 쿼리가 `NOT IN (SELECT reservation_id FROM reservation_waiting)` 형태로 복잡해짐
- 도메인 간 ID 모호성 발생

**Week 5에서 배운 것**: 상태 컬럼 하나로 통합하는 게 훨씬 낫다.

```sql
ALTER TABLE reservation ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED';
```

```java
public enum ReservationStatus {
    CONFIRMED, WAITING
}
```

쿼리는 `WHERE status = 'CONFIRMED'`로 단순해지고, 도메인 클래스도 하나로 통일된다.

---

## 2. 레이어 책임 분리

### DTO는 Controller 레이어에서만

처음에 서비스가 Request DTO를 직접 받고, Response DTO를 직접 만들었다.

**배운 것**: DTO는 Controller 레이어에서만 다뤄야 한다.

```
Controller: Request DTO → Domain 변환 → Service 호출
Service: Domain 객체만 사용
Controller: Service 결과(Domain) → Response DTO 변환
```

이렇게 하면 서비스가 HTTP 관심사에서 완전히 독립된다.

---

### DAO의 예외 변환 책임

처음에 `ReservationDao.save()`에서 `DuplicateKeyException`을 잡아 `ReservationConflictException`으로 변환했다.

**문제점**: DAO(infrastructure)가 service 레이어의 비즈니스 예외를 알고 있어야 한다. 역방향 의존이다.

**배운 것**: 예외 변환은 서비스에서 한다.

```java
// Service에서 DuplicateKeyException을 비즈니스 예외로 변환
try {
    reservationDao.save(reservation);
} catch (DuplicateKeyException e) {
    throw new ReservationConflictException();
}
```

---

### 서비스 간 의존 방향

`ReservationWaitingService`가 `ReservationDao`, `ReservationTimeDao`, `ThemeDao`를 직접 의존하고 있었다. 이러면 `ReservationWaitingService`가 Reservation 도메인의 내부 구조를 너무 많이 알게 된다.

**배운 것**: 서비스는 같은 레이어의 다른 서비스에 의존할 수 있고, 그 서비스가 DAO를 감싸도록 위임해야 한다.

```
ReservationWaitingService → ReservationWaitingDao (자기 도메인 DAO)
ReservationWaitingService → ReservationService (위임 메서드 호출)
```

---

### DAO 조립 책임

초기에 `ReservationDao.save()`가 예약을 저장한 뒤 JOIN 쿼리로 전체 객체를 조립해 반환하는 구조였다.

**배운 것**: DAO는 id만 반환하고, 객체 조립은 서비스가 담당한다.

```java
// Service
long id = reservationDao.save(reservation);
ReservationTime time = reservationTimeDao.findById(timeId);
return new Reservation(id, name, date, time, theme);
```

---

### 단일 트랜잭션 원칙

페이지네이션 구현 시 컨트롤러에서 `findAll(page, size)`와 `countAll()`을 따로 호출하고 있었다.

**문제점**: 두 쿼리 사이에 데이터 변경이 생기면 count와 실제 데이터가 불일치한다.

**배운 것**: 함께 일관된 상태를 봐야 하는 쿼리들은 서비스의 `@Transactional` 메서드 하나로 묶어야 한다.

---

## 3. 예외 처리

### 커스텀 예외 사용 기준

초기에는 모든 비즈니스 오류에 `IllegalArgumentException`을 사용했다. 문제점:
- HTTP 상태 코드를 세분화할 수 없다
- 예외 이름만 봐서는 어떤 비즈니스 규칙 위반인지 알 수 없다
- 예상치 못한 프로그래밍 오류와 의도된 비즈니스 실패를 구분할 수 없다

**배운 기준**:
- 도메인 규칙 위반 → 커스텀 예외
- 예상치 못한 프로그래밍 오류 → `IllegalArgumentException` (이 경우 메시지 노출 안 함, 경고 로그)

---

### HTTP 상태 코드 매핑

| 상황 | 코드 |
|------|------|
| 존재하지 않는 리소스 조회/수정/삭제 | 404 |
| 중복 생성, 사용 중인 리소스 삭제 | 409 |
| 과거 날짜 예약 | 422 |
| 입력값 형식 오류 | 400 |

처음에 모든 오류를 400으로 반환했었다. 클라이언트 입장에서 "요청 형식이 잘못됐나, 비즈니스 규칙에 걸렸나"를 구분할 방법이 없다.

---

### 예외 위치

예외 클래스가 `exception` 패키지에 한데 모여 있었다.

**배운 것**: 예외는 발생 위치 근처에 두어야 한다.
- `PastReservationException` → `domain` 패키지 (Reservation 도메인 규칙)
- `ReservationConflictException` → `service` 패키지 (ReservationService에서만 발생)

---

### 예외 계층

`ReservationNotFoundException`, `ReservationTimeNotFoundException`, `ThemeNotFoundException`이 각각 독립적으로 있었다.

**배운 것**: 공통 부모 예외 `NotFoundException`을 두면 `GlobalExceptionHandler`에서 하나로 통합할 수 있다.

```java
public class NotFoundException extends RuntimeException { ... }
public class ReservationNotFoundException extends NotFoundException { ... }
```

---

### delete() 멱등성

삭제 대상이 없으면 `NotFoundException`을 던지고 있었다.

**배운 것**: DELETE는 "해당 리소스가 없는 상태를 달성한다"는 목적으로 보면, 이미 없는 상태도 성공이다. 멱등하게 처리하는 게 REST 원칙에 맞다. (단, update()는 orElseThrow로 처리 — 수정 의도를 명확히 전달해야 하므로)

---

## 4. 데이터베이스 설계

### 컬럼 타입

- `date` 컬럼을 `VARCHAR(255)`로 하고 있었다 → `DATE` 타입으로
- `start_at`도 `VARCHAR(255)` → `TIME` 타입으로
- `thumbnail_url`을 `VARCHAR(255)` → `TEXT` 또는 `VARCHAR(2048)`로

적절한 타입을 쓰면 DB가 형식 검증을 대신 해주고, 인덱스 효율도 올라간다.

---

### 제약 조건으로 2중 방어

애플리케이션 레벨에서 중복 예약을 검증하더라도 동시 요청 등 레이스 컨디션이 있을 수 있다.

```sql
ALTER TABLE reservation_time ADD UNIQUE (start_at);
ALTER TABLE reservation ADD UNIQUE (date, time_id, theme_id);
```

DB UNIQUE 제약은 "마지막 방어선"이다. 애플리케이션 검증 + DB 제약을 함께 쓰는 것이 바람직하다.

---

### FK 참조 설계

Week 4에서 `reservation_waiting` 테이블이 `reservation` 테이블의 `(name, date, time_id, theme_id)`를 중복 저장하고 있었다.

**배운 것**: 연관된 데이터는 FK로 참조한다.

```sql
-- 이전: 중복 컬럼
reservation_waiting(id, name, date, time_id, theme_id, ...)

-- 이후: FK 참조
reservation_waiting(id, reservation_id REFERENCES reservation(id), ...)
```

---

## 5. 테스트

### 토토로지컬(Tautological) 테스트 제거

Mock의 반환값을 그대로 검증하는 테스트가 있었다.

```java
// 의미 없는 테스트 — mock이 뭘 반환하든 항상 통과
when(reservationDao.findAll()).thenReturn(List.of(reservation));
List<Reservation> result = reservationService.findAll();
assertThat(result).isEqualTo(List.of(reservation)); // mock 출력 그대로 검증
```

**배운 것**: 서비스 테스트에서 의미 있는 검증은 "서비스가 특정 조건에서 어떤 동작을 하는가"다. 단순 위임만 하는 메서드에는 굳이 테스트가 필요 없다.

---

### 하드코딩 의존 제거

```java
// 나쁜 패턴 — data.sql 내용에 종속
assertThat(result).hasSize(13);

// 좋은 패턴
assertThat(result).isNotEmpty();
// 또는 직접 삽입하고 그 수를 검증
```

테스트 픽스처가 바뀌면 전혀 관계없는 테스트가 깨진다.

---

### DAO 테스트에서 Spring 컨텍스트 제거

DAO 테스트에 `@SpringBootTest`가 붙어있었다. 웹 컨텍스트, 빈 초기화 등 불필요한 것들이 다 올라온다.

**배운 것**: DAO 테스트는 `EmbeddedDatabaseBuilder`로 H2 직접 세팅하면 컨텍스트 없이 빠르게 실행된다.

```java
DataSource dataSource = new EmbeddedDatabaseBuilder()
    .setType(EmbeddedDatabaseType.H2)
    .addScript("classpath:schema.sql")
    .build();
JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
reservationDao = new ReservationDao(jdbcTemplate);
```

---

### 컨트롤러 테스트 환경

```java
// 나쁜 패턴
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext  // 매 테스트마다 컨텍스트 재시작 → 매우 느림

// 좋은 패턴
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")  // 컨텍스트 재사용, DB만 초기화
```

`@DirtiesContext`는 가장 마지막 수단이다. 먼저 `@Sql`로 DB 상태를 초기화하는 방법을 찾아야 한다.

---

### GlobalExceptionHandler 테스트

예외 핸들러를 테스트하지 않아서, 핸들러가 잘못 설정돼도 다른 테스트에서는 발견이 안 됐다.

**배운 것**: `@WebMvcTest` + 인라인 더미 컨트롤러로 각 예외별 HTTP status를 독립적으로 검증한다.

```java
@WebMvcTest
class GlobalExceptionHandlerTest {
    @RestController
    static class TestController {
        @GetMapping("/test")
        public void throwIllegalArgument() {
            throw new IllegalArgumentException("test");
        }
    }
    // 각 예외 → HTTP status 검증
}
```

---

### 테스트 메서드명과 실제 동작 일치

```java
// 이름이 400인데 실제 반환은 404
@Test
void 존재하지_않는_시간으로_예약하면_400() { ... }
```

이름과 실제 검증이 다르면 리뷰어가 혼란스럽고, 나중에 로직이 바뀌었을 때 테스트 이름을 믿고 잘못된 판단을 할 수 있다.

---

## 6. 동시성 제어

### 레이스 컨디션 발생 시나리오

예약 취소 API(`DELETE /reservations/{id}`)를 동시에 두 요청이 호출하면:

1. VU1: `id=20` 예약 조회 (CONFIRMED)
2. VU2: `id=20` 예약 조회 (CONFIRMED)
3. VU1: 삭제 + waiter1 → CONFIRMED 승인
4. VU2: 삭제 + waiter2 → CONFIRMED 승인 ← **중복 승인 버그**

결과: 같은 슬롯에 CONFIRMED가 2개 생김.

---

### SELECT FOR UPDATE

```java
// 일반 조회 — 레이스 컨디션 발생
Optional<Reservation> findById(long id);

// 비관적 락 — 트랜잭션 직렬화
Optional<Reservation> findByIdForUpdate(long id);  // SELECT ... FOR UPDATE
```

`SELECT ... FOR UPDATE`는 해당 행에 락을 걸어, 한 트랜잭션이 처리를 마칠 때까지 다른 트랜잭션이 같은 행을 읽거나 수정할 수 없게 한다. 결과적으로 취소 요청이 직렬화된다.

---

### H2 vs MySQL 동시성 차이

H2는 내부적으로 트랜잭션을 직렬화하기 때문에 `SELECT FOR UPDATE` 없이도 레이스 컨디션이 재현되지 않는다. 동시성 테스트는 반드시 MySQL(또는 실제 운영 DB)에서 해야 한다.

---

### k6로 동시성 검증

k6를 사용해 VU 2개가 동시에 같은 API를 호출하는 부하 테스트를 했다.

```javascript
export const options = { vus: 2, iterations: 2 };

export default function () {
  const res = http.del('http://localhost:8080/reservations/1');
  console.log(`VU ${__VU}: status=${res.status}`);
}
```

**락 없음**: VU1=204, VU2=204 → DB에 CONFIRMED 2개  
**락 있음**: VU1=204, VU2=404 → DB에 CONFIRMED 1개 (정상)

---

## 7. API 설계

### List 응답 래핑

```java
// 나쁜 패턴 — 배열 직접 반환
return ResponseEntity.ok(reservationService.findAll());

// 좋은 패턴 — 래퍼 객체로 감싸기
return ResponseEntity.ok(new ReservationsResponse(reservationService.findAll()));
```

배열을 직접 반환하면 나중에 `totalCount`, `page` 등 메타데이터를 추가할 때 API 스펙이 깨진다. 처음부터 객체로 감싸는 것이 확장에 유리하다.

---

### 엔드포인트 설계

```
// 나쁜 패턴 — 쿼리 파라미터로 동작을 분기
GET /themes?condition=popular&size=10

// 좋은 패턴 — 별도 경로로 의미를 명확히
GET /themes/popular?size=10
```

`condition=popular`는 "popular라는 조건"이 아니라 "인기 테마 목록"이라는 별도 리소스로 봐야 한다.

---

### 응답 일관성

컨트롤러에서 일부는 `@ResponseStatus + return POJO`, 일부는 `ResponseEntity`를 반환하고 있었다. 하나로 통일하는 것이 낫다. (팀/프로젝트 기준에 따라 결정)

---

## 8. Spring 설정

### Jackson 전역 설정

`LocalTime`을 JSON으로 직렬화할 때 `@JsonFormat(pattern = "HH:mm")`을 각 DTO마다 붙이고 있었다.

**배운 것**: `JacksonConfig`에서 전역 설정하면 한 곳만 관리한다.

```java
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder.serializerByType(LocalTime.class, new LocalTimeSerializer(...));
    }
}
```

---

### Clock 주입으로 시간 테스트 가능하게

서비스에서 `LocalDateTime.now()`를 직접 호출하면 테스트에서 "과거 날짜 예약 거부" 케이스를 만들 수 없다.

**배운 것**: `Clock`을 빈으로 등록하고 주입받으면 테스트에서 `Clock.fixed()`로 고정 시간을 사용할 수 있다.

```java
// 설정
@Bean
public Clock clock() {
    return Clock.system(ZoneId.of("Asia/Seoul"));
}

// 서비스
private final Clock clock;
LocalDateTime now = LocalDateTime.now(clock);

// 테스트
Clock clock = Clock.fixed(Instant.parse("2020-01-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
```

`Clock.systemDefaultZone()` 대신 `Clock.system(ZoneId.of("Asia/Seoul"))`을 쓰는 이유: 서버 환경에 따라 기본 타임존이 달라지지 않도록 명시적으로 고정한다.

---

### 트랜잭션 원칙

- 데이터를 변경하는 메서드: `@Transactional`
- 조회만 하는 메서드: `@Transactional(readOnly = true)` — 불필요한 쓰기 잠금 방지, JPA 사용 시 dirty checking 비활성화

---

## 9. 코드 품질

### 타입 일관성

- 기본형 vs 래퍼 타입: null이 들어올 수 없는 자리에는 `long` (기본형) 사용. 예: `@PathVariable long id`, `dao.delete(long id)`
- DTO의 날짜/시간 타입: `String`으로 내려보내는 것보다 `LocalDate`/`LocalTime`을 유지하고 Jackson이 직렬화하도록 맡기는 것이 더 타입 안전하다.

---

### null 방어

`JdbcTemplate.queryForObject()`는 결과가 없으면 `null`을 반환할 수 있다. auto-unboxing 시 NPE가 발생한다.

```java
// 나쁜 패턴
long count = jdbcTemplate.queryForObject("SELECT COUNT(*) ...", Long.class);

// 좋은 패턴
long count = Objects.requireNonNullElse(
    jdbcTemplate.queryForObject("SELECT COUNT(*) ...", Long.class), 0L
);
```

반면 `JdbcTemplate.query()`는 결과가 없으면 빈 리스트를 반환하므로 try-catch가 불필요하다.

---

### 변수명

- `list`, `found`, `result` 같은 제네릭한 이름은 피한다.
- 동작을 담은 변수를 만드는 것보다 `stream().filter().findFirst().ifPresent(...)` 처럼 인라인으로 처리하는 게 더 의도가 명확할 때가 있다.

---

### 패키지 명명

Java 패키지명은 소문자만 써야 한다.

```
// 잘못된 패턴
roomescape.reservationTime

// 올바른 패턴
roomescape.reservationtime
```

---

### 의존성 관리

- Lombok은 getter, setter를 자동 생성해주지만 도메인 검증이 필요한 생성자와 맞지 않는다. 이 프로젝트에서는 제거했다.
- DTO의 `toDomain()`처럼 사용되지 않는 메서드는 즉시 제거한다.

---

## 10. 기타

### git 관리

- `.DS_Store`가 이미 git에 추적되고 있으면 `.gitignore` 추가만으로는 부족하다. `git rm --cached .DS_Store`로 인덱스에서 제거해야 한다.
- EOF 개행 문자: 파일 끝에 개행이 있어야 한다. (POSIX 표준)

---

## 돌아보며

5주간 받은 피드백을 요약하면 두 가지 핵심 원칙이 반복됐다.

**1. 책임의 적절한 위치**  
비즈니스 규칙은 도메인이, HTTP 관심사는 컨트롤러가, 예외 변환은 서비스가 담당한다. 각 레이어가 자기 역할 밖의 일을 하면 의존성이 꼬이고 테스트하기 어려워진다.

**2. 단순함 우선**  
두 테이블로 관리하던 것을 status 컬럼 하나로 해결했고, `restore()`/`create()` 두 팩토리를 `createdAt` 주입으로 통일했다. 처음부터 복잡한 구조를 만들기보다, 문제가 생겼을 때 단순한 해결책을 먼저 찾는 것이 중요하다.