# JPA Mission

## 1단계

### 테마

- 테마 전체 조회
```sql
SELECT * FROM theme
```

- 테마 단건 조회
```sql
SELECT * FROM theme WHERE id = ?
```

- 테마 저장
```sql
INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)
```

- 테마 삭제
```sql
DELETE FROM theme WHERE id = ?
```

- 테마 이름 존재 여부 확인
```sql
SELECT EXISTS (SELECT 1 FROM theme WHERE name = ?)
```

- 최근 기간 기준 인기 테마 조회
```sql
SELECT t.id,
       t.name,
       t.description,
       t.thumbnail_url
FROM reservation r
INNER JOIN reservation_slot s ON r.slot_id = s.id
INNER JOIN theme t ON s.theme_id = t.id
WHERE s.date >= ? AND s.date <= ?
GROUP BY t.id, t.name, t.description, t.thumbnail_url
ORDER BY COUNT(*) DESC, t.id ASC
LIMIT ?
```

### 예약 시간

- 예약 시간 전체 조회
```sql
SELECT rt.id,
       rt.start_at
FROM reservation_time AS rt
ORDER BY rt.start_at
```

- 예약 시간 단건 조회
```sql
SELECT rt.id,
       rt.start_at
FROM reservation_time AS rt
WHERE rt.id = ?
```

- 예약 시간 저장
```sql
INSERT INTO reservation_time (start_at) VALUES (?)
```

- 예약 시간 삭제
```sql
DELETE FROM reservation_time WHERE id = ?
```

- 시작 시간 존재 여부 확인
```sql
SELECT EXISTS (SELECT 1 FROM reservation_time WHERE start_at = ?)
```

### 예약 슬롯

- 슬롯 전체 조회
```sql
SELECT  rs.id,
        rs.date,
        rs.time_id,
        rt.start_at,
        rs.theme_id,
        t.name AS theme_name,
        t.description,
        t.thumbnail_url
FROM    reservation_slot AS rs
INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
INNER JOIN theme AS t ON rs.theme_id = t.id
```

- 슬롯 단건 조회
```sql
SELECT  rs.id,
        rs.date,
        rs.time_id,
        rt.start_at,
        rs.theme_id,
        t.name AS theme_name,
        t.description,
        t.thumbnail_url
FROM    reservation_slot AS rs
INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
INNER JOIN theme AS t ON rs.theme_id = t.id
WHERE rs.id = ?
```

- 날짜와 테마로 슬롯 조회
```sql
SELECT  rs.id,
        rs.date,
        rs.time_id,
        rt.start_at,
        rs.theme_id,
        t.name AS theme_name,
        t.description,
        t.thumbnail_url
FROM    reservation_slot AS rs
INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
INNER JOIN theme AS t ON rs.theme_id = t.id
WHERE rs.date = ? AND rs.theme_id = ?
```

- 날짜, 테마, 시간으로 슬롯 조회
```sql
SELECT  rs.id,
        rs.date,
        rs.time_id,
        rt.start_at,
        rs.theme_id,
        t.name AS theme_name,
        t.description,
        t.thumbnail_url
FROM    reservation_slot AS rs
INNER JOIN reservation_time AS rt ON rs.time_id = rt.id
INNER JOIN theme AS t ON rs.theme_id = t.id
WHERE rs.date = ? AND rs.time_id = ? AND rs.theme_id = ?
```

- 슬롯 저장
```sql
INSERT INTO reservation_slot (date, theme_id, time_id) VALUES (?, ?, ?)
```

### 예약

- 예약 전체 조회
```sql
SELECT r.id,
       r.name AS reservation_name,
       r.slot_id,
       s.date,
       r.created_at,
       rt.id AS time_id,
       rt.start_at,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url
FROM reservation AS r
INNER JOIN reservation_slot AS s ON r.slot_id = s.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
INNER JOIN theme AS t ON s.theme_id = t.id
```

- 예약 단건 조회
```sql
SELECT r.id,
       r.name AS reservation_name,
       r.slot_id,
       s.date,
       r.created_at,
       rt.id AS time_id,
       rt.start_at,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url
FROM reservation AS r
INNER JOIN reservation_slot AS s ON r.slot_id = s.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
INNER JOIN theme AS t ON s.theme_id = t.id
WHERE r.id = ?
```

- 슬롯으로 예약 조회
```sql
SELECT r.id,
       r.name AS reservation_name,
       r.slot_id,
       s.date,
       r.created_at,
       rt.id AS time_id,
       rt.start_at,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url
FROM reservation AS r
INNER JOIN reservation_slot AS s ON r.slot_id = s.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
INNER JOIN theme AS t ON s.theme_id = t.id
WHERE s.date = ? AND s.theme_id = ? AND s.time_id = ?
```

- 날짜와 테마로 예약 조회
```sql
SELECT r.id,
       r.name AS reservation_name,
       r.slot_id,
       s.date,
       r.created_at,
       rt.id AS time_id,
       rt.start_at,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url
FROM reservation AS r
INNER JOIN reservation_slot AS s ON r.slot_id = s.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
INNER JOIN theme AS t ON s.theme_id = t.id
WHERE s.date = ? AND s.theme_id = ?
```

- 같은 시간 예약 존재 여부 확인
```sql
SELECT COUNT(1)
FROM reservation AS r
INNER JOIN reservation_slot AS s ON r.slot_id = s.id
WHERE s.time_id = ?
```

- 예약 저장
```sql
INSERT INTO reservation (name, slot_id, created_at) VALUES (?, ?, ?)
```

- 예약 수정
```sql
UPDATE reservation
SET slot_id = ?
WHERE id = ?
```

- 예약 삭제
```sql
DELETE FROM reservation WHERE id = ?
```

### 예약 대기

- 예약 대기 저장
```sql
INSERT INTO reservation_waiting (slot_id, name, requested_at) VALUES (?, ?, ?)
```

- 대기 단건 조회
```sql
SELECT rw.id,
       rw.name AS waiting_name,
       rw.requested_at,
       rw.slot_id,
       s.date,
       rt.id AS time_id,
       rt.start_at,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url
FROM reservation_waiting AS rw
INNER JOIN reservation_slot AS s ON rw.slot_id = s.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
INNER JOIN theme AS t ON s.theme_id = t.id
WHERE rw.id = ?
```

- 슬롯별 대기열 조회
```sql
SELECT rw.id,
       rw.name AS waiting_name,
       rw.requested_at,
       rw.slot_id,
       s.date,
       rt.id AS time_id,
       rt.start_at,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url
FROM reservation_waiting AS rw
INNER JOIN reservation_slot AS s ON rw.slot_id = s.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
INNER JOIN theme AS t ON s.theme_id = t.id
WHERE rw.slot_id = ?
```

- 예약 대기 삭제
```sql
DELETE FROM reservation_waiting WHERE id = ?
```

### 히스토리

- 이름 기준 예약/대기 내역 조회
```sql
SELECT 'RESERVATION' AS status,
       r.id AS reservation_id,
       NULL AS waiting_id,
       r.name AS history_name,
       s.date,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url,
       rt.id AS time_id,
       rt.start_at,
       CAST(NULL AS TIMESTAMP) AS requested_at
FROM reservation AS r
INNER JOIN reservation_slot AS s ON r.slot_id = s.id
INNER JOIN theme AS t ON s.theme_id = t.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
WHERE r.name = ?

UNION ALL

SELECT 'WAITING' AS status,
       r.id AS reservation_id,
       rw.id AS waiting_id,
       rw.name AS history_name,
       s.date,
       t.id AS theme_id,
       t.name AS theme_name,
       t.description,
       t.thumbnail_url,
       rt.id AS time_id,
       rt.start_at,
       rw.requested_at
FROM reservation_waiting AS rw
INNER JOIN reservation_slot AS s ON rw.slot_id = s.id
INNER JOIN reservation AS r ON r.slot_id = s.id
INNER JOIN theme AS t ON s.theme_id = t.id
INNER JOIN reservation_time AS rt ON s.time_id = rt.id
WHERE rw.name = ?
ORDER BY date, start_at, status
```

- 예약별 대기 순서 조회
```sql
SELECT r.id AS reservation_id,
       rw.slot_id,
       rw.id AS waiting_id,
       rw.requested_at
FROM reservation_waiting AS rw
INNER JOIN reservation AS r ON rw.slot_id = r.slot_id
WHERE r.id IN (?, ?, ...)
```

### 1단계 관찰 메모

JPA 전환 후에는 시작 시점에 엔티티 정의를 기준으로 DDL이 다시 생성되고, `create-drop` 설정에서는 재시작 시 데이터가 남지 않는다. 컬럼명과 제약은 대부분 엔티티 어노테이션으로 맞출 수 있지만, `created_at` 같은 기본값은 별도 컬럼 정의가 필요했다. 즉, 엔티티만으로 스키마를 모두 통제하는지, 아니면 실제 테이블 정의를 함께 신경 써야 하는지를 비교해 볼 수 있는 단계다.

`schema.sql`은 Hibernate가 생성한 DDL과 충돌해서 제거했고, 초기 데이터는 `data.sql` 기준으로 유지했다.

연관관계는 현재 흐름에 필요한 만큼만 단방향으로 남겼다. 양방향과 `cascade`까지 붙여보는 방향도 검토했지만, 저장/조회 책임이 repository adapter와 서비스 계층에 이미 분리되어 있어 역참조와 소유권 전파가 오히려 복잡도를 늘린다고 판단해 채택하지 않았다.

## 2단계

## 3단계

### 영속성 컨텍스트 관찰

관찰 테스트는 [`PersistenceContextObservationTest`](../src/test/java/roomescape/persistence/PersistenceContextObservationTest.java)로 분리했다.

- `dirty_checking_updates_without_save`
  - 시도한 코드: `@Transactional` 안에서 `ReservationJpaEntity#setSlot()`만 호출하고 `save()`는 호출하지 않음
  - 실제 동작: 커밋 시점에 `UPDATE reservation SET slot_id = ? WHERE id = ?`가 자동으로 반영됨
  - 의미: managed entity는 변경 감지가 되어 별도 저장 호출 없이도 반영된다

- `first_level_cache_hits_same_entity_in_same_transaction`
  - 시도한 코드: 같은 트랜잭션에서 `entityManager.find(ReservationJpaEntity.class, id)`를 두 번 호출
  - 실제 동작: 두 번째 조회는 추가 SELECT 없이 같은 managed instance를 반환함
  - 의미: 1차 캐시가 같은 식별자의 재조회 비용을 제거한다

- `write_behind_persists_on_flush_not_before`
  - 시도한 코드: ID를 직접 가진 테스트 전용 엔티티를 `persist()` 한 뒤 flush 전후 DB 상태를 JDBC로 확인
  - 실제 동작: flush 전에는 0건, flush 후에는 1건이 보임
  - 의미: INSERT는 영속성 컨텍스트에 먼저 쌓이고 flush 시점에 DB로 나간다

- `jpql_query_triggers_flush_before_execution`
  - 시도한 코드: `persist()` 직후 JPQL `select count(...)` 실행
  - 실제 동작: JPQL 실행 직전에 flush가 발생해서 count가 1로 조회됨
  - 의미: JPQL은 자동 flush 트리거가 될 수 있다

- `fetch_defaults_follow_jpa_annotation_defaults`
  - 시도한 코드: 테스트용 probe 필드의 어노테이션 기본값을 reflection으로 확인
  - 실제 동작: `@ManyToOne` 기본 fetch는 `EAGER`, `@OneToMany` 기본 fetch는 `LAZY`
  - 의미: 연관관계 타입에 따라 기본 fetch 전략이 다르다

- `lazy_initialization_exception_occurs_outside_transaction`
  - 시도한 코드: 트랜잭션 안에서 찾은 `ReservationJpaEntity`를 트랜잭션 밖에서 연관 필드 접근
  - 실제 동작: `LazyInitializationException` 발생
  - 의미: 영속성 컨텍스트가 닫힌 뒤 LAZY 프록시는 초기화할 수 없다

보충 메모:
- 현재 `ReservationJpaRepository.findById()`는 `@EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})`를 써서, 서비스/레포지토리 경로에서는 `findById(reservationId).getTime().getStartAt()`처럼 접근해도 추가 SELECT가 늘지 않도록 해두었다.
- 반면 `entityManager.find()`로 직접 읽는 실험에서는 LAZY 경계와 `LazyInitializationException`을 볼 수 있다.

## 4단계
