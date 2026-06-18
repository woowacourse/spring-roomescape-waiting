# 테스트 전략 문서 기반 현행 테스트 코드 분석

## 수정이 필요한 부분

---

### 1. [API 계층] `@DirtiesContext` → truncate 방식으로 교체

**파일:** `PolicyAcceptanceTest.java:29`

```java
// 현재 - Spring Context를 테스트마다 완전히 파괴 후 재생성 → 극히 느림
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
```

전략 문서: *"롤백에 의존하지 말고, 각 테스트 전후로 테이블을 truncate하는 방식이 더 안전합니다."*

`@DirtiesContext`는 ApplicationContext를 매 테스트마다 재시작하는 방식이라 가장 비싼 선택지입니다.
`@BeforeEach`에서 테이블 truncate로 교체해야 합니다.

---

### 2. [API 계층] 엣지 케이스가 통합 테스트에 집중

**파일:** `PolicyAcceptanceTest.java` 전체

전략 문서: *"경계값·예외·분기 같은 엣지 케이스는 하위 계층에서 이미 검증했어야 하고, 여기서는 시나리오가 계층을 가로질러 연결되는지만 확인합니다."*

현재 `PolicyAcceptanceTest`에 있는 테스트를 분류하면:

| 테스트 | 이동해야 할 계층 |
|---|---|
| 빈 이름 → 400, 테마슬롯 ID 없음 → 400 | Controller (`@WebMvcTest`) |
| 과거 날짜 → 422, 중복 예약 → 409 | Service (Fake로 이미 검증 가능) |
| 존재하지 않는 예약 → 404 | Service |
| 예약 있는 시간 삭제 → 422 | Service |

API 테스트에는 **"예약 생성 → 조회 → 취소"** 같은 happy path 흐름만 남아야 합니다.

---

### 3. [Controller 계층] `@WebMvcTest` 테스트 자체가 없음

전략 문서: *"controller에 변환·검증·예외 매핑이 한 줄이라도 있으면 그건 자체 로직이므로 `@WebMvcTest`로 검증한다."*

`@Valid` 검증(이름 빈값 → 400, themeSlotId 누락 → 400)이 현재 `PolicyAcceptanceTest`(통합 테스트)에 있습니다.
이건 Controller 계층 책임이므로 `@WebMvcTest` 테스트로 내려야 합니다.

---

### 4. [Repository 계층] `JdbcTimeRepositoryTest` — 수동 DDL 실행

**파일:** `JdbcTimeRepositoryTest.java:27-35`

```java
// 현재 - 수동으로 DDL 실행
private void executeSchema() {
    jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS time ...");
    jdbcTemplate.execute("TRUNCATE TABLE time");
}
```

`JdbcReservationRepositoryTest`는 `@Sql({"/schema.sql", "/test-data.sql"})`를 사용해 일관성이 있는데,
`JdbcTimeRepositoryTest`만 수동 DDL 방식을 쓰고 있습니다. `@Sql`로 통일해야 합니다.

---

### 5. [Service 계층] 단순 위임 메서드 테스트

**파일:** `ReservationServiceTest.java`, `ThemeServiceTest.java`, `TimeServiceTest.java`

전략 문서 규칙 4: *"단순 포워딩(위임)만 하는 메서드는 테스트하지 않는다."*

아래 테스트들은 서비스가 Repository에 그대로 위임하는 것만 확인하므로 삭제 대상입니다:

| 테스트 메서드 | 이유 |
|---|---|
| `ReservationServiceTest.allReservations()` | `repo.findAll()` 위임만 함 |
| `ReservationServiceTest.findReservation()` | `repo.findById()` 위임만 함 |
| `ReservationServiceTest.removeReservation()` | `repo.deleteById()` 위임만 함 (검증 로직 없음) |
| `ThemeServiceTest.allTimes()` | 동일 |
| `ThemeServiceTest.findTime()` | 동일 |
| `ThemeServiceTest.removeTime()` | 동일 |
| `TimeServiceTest.allTimes()`, `findTime()`, `removeTime()` | 동일 |

---

## 우선순위 정리

```
1순위 (당장 바꿔야): @DirtiesContext → truncate
2순위 (구조적 개선): PolicyAcceptanceTest 엣지 케이스 → 하위 계층으로 이동
3순위 (빈 계층 채우기): @WebMvcTest Controller 테스트 추가
4순위 (일관성): JdbcTimeRepositoryTest → @Sql 방식으로 통일
5순위 (청소): 단순 위임 테스트 제거
```
