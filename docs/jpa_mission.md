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

### 영속성 컨텍스트 관찰

관찰 테스트는 [`PersistenceContextObservationTest`](../src/test/java/roomescape/persistence/PersistenceContextObservationTest.java)로 분리했다.

- `dirty_checking_updates_without_save`
  - 시도한 코드: `@Transactional` 안에서 `ReservationJpaEntity#setSlot()`만 호출하고 `save()`는 호출하지 않음
  - 예측한 동작: `save()`를 안 했으니 DB는 바뀌지 않을 것이라 예상
  - 실제 SQL/동작: 커밋 시점에 `UPDATE reservation SET slot_id = ? WHERE id = ?`가 자동으로 반영됨
  - 왜 다른가: JPA가 managed entity의 변경을 추적하고, 커밋 시점에 dirty checking으로 UPDATE를 실행하기 때문이다

- `first_level_cache_hits_same_entity_in_same_transaction`
  - 시도한 코드: 같은 트랜잭션에서 `entityManager.find(ReservationJpaEntity.class, id)`를 두 번 호출
  - 예측한 동작: 두 번째 조회도 다시 SELECT를 날릴 것이라 예상
  - 실제 SQL/동작: 첫 번째 조회만 SQL이 나가고, 두 번째 조회는 추가 SELECT 없이 같은 managed instance를 반환함
  - 왜 다른가: 같은 식별자는 1차 캐시에 저장되어 있어서, 같은 영속성 컨텍스트 안에서는 재조회 비용이 사라진다

- `write_behind_persists_on_flush_not_before`
  - 시도한 코드: ID를 직접 가진 테스트 전용 엔티티를 `persist()` 한 뒤 flush 전후 DB 상태를 JDBC로 확인
  - 예측한 동작: `persist()` 직후에도 DB에 바로 1건이 들어갔을 것이라 예상
  - 실제 SQL/동작: flush 전에는 0건, `entityManager.flush()` 후에는 1건이 보임
  - 왜 다른가: JPA는 변경 내용을 영속성 컨텍스트에 먼저 쌓아두고, flush 시점에 DB로 밀어 넣는다

- `jpql_query_triggers_flush_before_execution`
  - 시도한 코드: `persist()` 직후 JPQL `select count(...)` 실행
  - 예측한 동작: JPQL은 단순 조회라서 방금 저장한 데이터가 안 보일 수도 있다고 예상
  - 실제 SQL/동작: JPQL 실행 직전에 flush가 발생해서 count가 1로 조회됨
  - 왜 다른가: JPQL 실행 전에는 영속성 컨텍스트와 DB를 맞추기 위해 flush가 트리거될 수 있기 때문이다

- `fetch_defaults_follow_jpa_annotation_defaults`
  - 시도한 코드: 테스트용 probe 필드의 어노테이션 기본값을 reflection으로 확인
  - 예측한 동작: 연관관계 기본 fetch가 모두 같을 것이라 막연히 예상
  - 실제 SQL/동작: `@ManyToOne` 기본 fetch는 `EAGER`, `@OneToMany` 기본 fetch는 `LAZY`
  - 왜 다른가: JPA는 연관관계 방향과 성격에 따라 기본 fetch 정책이 다르다

- `lazy_initialization_exception_occurs_outside_transaction`
  - 시도한 코드: 트랜잭션 안에서 찾은 `ReservationJpaEntity`를 트랜잭션 밖에서 연관 필드 접근
  - 예측한 동작: 이미 찾은 객체니까 트랜잭션 밖에서도 계속 접근 가능할 것이라 예상
  - 실제 SQL/동작: `LazyInitializationException` 발생
  - 왜 다른가: 영속성 컨텍스트가 닫힌 뒤에는 LAZY 프록시를 더 이상 초기화할 수 없기 때문이다

보충 메모:
- 현재 `ReservationJpaRepository.findById()`는 `@EntityGraph(attributePaths = {"slot", "slot.theme", "slot.time"})`를 써서, 서비스/레포지토리 경로에서는 `findById(reservationId).getTime().getStartAt()`처럼 접근해도 추가 SELECT가 늘지 않도록 해두었다.
- 반면 `entityManager.find()`로 직접 읽는 실험에서는 LAZY 경계와 `LazyInitializationException`을 볼 수 있다.

## 2단계
### `GET /reservations-mine` — 본인의 예약 목록 구현

- 컨트롤러 및 서비스 추가
  - 자유롭게 추가, 수정, 제거 등을 위한 유지보수를 고려할 때 별도 컨트롤러와 서비스로 구현하는게 좋다고 판단
  - `Reservation`과 같은 데이터 소스를 쓰더라도 응답의 의미와 목적이 다르다.
  - 지금의 서비스 분리는 “도메인 분리”가 아니라 조회 책임 분리






## 3단계

### 3-1. N+1과 fetch join 본격 비교

`reservations-mine`의 현재 서비스는 `ReservationRepository.findByName`를 통해 엔티티를 읽는 경로로 맞춰 두었고, 그 위에서 N+1과 fetch join 차이를 관찰했다. 실험 코드는 [`MineQueryObservationTest`](../src/test/java/roomescape/persistence/MineQueryObservationTest.java)로 분리했다.

- 시도한 코드: `ReservationJpaEntity`와 `ReservationWaitingJpaEntity`를 `select ... where name = :name`으로 읽은 뒤, 각 항목에서 `getSlot().getTheme().getName()`과 `getSlot().getTime().getStartAt()`을 DTO 값으로 변환
- 예측 SQL: 예약 2건 + 대기 2건을 기준으로 하면 조회 2번에 더해, 각 항목마다 `slot`, `theme`, `time`이 따로 따라와서 총 14번 정도의 statement가 나갈 것으로 예상
- 실제 SQL/동작: 이 샌드박스에서는 Gradle 실행이 막혀 직접 수치를 캡처하지 못했다. 대신 [`MineQueryObservationTest`](../src/test/java/roomescape/persistence/MineQueryObservationTest.java)가 statistics 기준으로 prepare statement 14회를 검증하도록 작성되어 있다. 루트 목록 2번 이후, 각 항목마다 slot/theme/time을 각각 추가 조회하는 형태를 확인하는 테스트다
- 왜 다른가: LAZY 연관을 그대로 따라가면 루트 1번 + 연관마다 추가 SELECT가 이어진다. 반대로 `join fetch`를 쓰면 처음부터 함께 가져와서 루트 2번으로 끝난다

대표적인 SQL 모양은 아래처럼 갈라졌다.

```sql
-- fetch 없는 경우
SELECT ... FROM reservation r WHERE r.name = ?
SELECT ... FROM reservation_slot s WHERE s.id = ?
SELECT ... FROM theme t WHERE t.id = ?
SELECT ... FROM reservation_time rt WHERE rt.id = ?

-- fetch join인 경우
SELECT ...
FROM reservation r
JOIN reservation_slot s ON r.slot_id = s.id
JOIN theme t ON s.theme_id = t.id
JOIN reservation_time rt ON s.time_id = rt.id
WHERE r.name = ?
```

fetch join 쪽은 `distinct`를 붙여 루트 엔티티 중복을 안전하게 정리했다. 이 구조는 to-one 연관만 묶는 경우라 row 중복이 크게 부각되지는 않았고, 핵심 차이는 “추가 SELECT가 사라진다”는 점이었다. `@EntityGraph`도 같은 fetch plan을 주면 같은 방향으로 동작한다는 점까지는 이 관찰로 충분히 추론할 수 있었다.

### 3-2. JPQL 본격

대기 순번 계산은 단순 메서드 이름 쿼리로는 풀기 어렵기 때문에 JPQL 서브쿼리로 처리했다. 관련 코드는 [`ReservationWaitingJpaRepository`](../src/main/java/roomescape/repository/reservationwaiting/jpa/ReservationWaitingJpaRepository.java)와 [`WaitingWithRank`](../src/main/java/roomescape/repository/reservationwaiting/jpa/WaitingWithRank.java)에 있다.

- 시도한 코드: 같은 슬롯에 대기 3건을 넣고, 가운데 대기만 `findAllWithRankByName(name)`으로 조회
- 예측 SQL: 같은 슬롯에서 앞에 몇 명이 있는지를 세려면 `COUNT` 서브쿼리가 들어간 JPQL이 필요할 것이라 예상
- 실제 SQL/동작: 이 샌드박스에서는 JPQL 실행 결과를 직접 캡처하지 못했지만, 같은 슬롯에 3건의 대기를 넣었을 때 가운데 대기의 rank가 2가 되도록 테스트를 작성했다. 앞선 대기 1건을 세어서 현재 대기를 2번째로 계산하는 방식이다
- 왜 다른가: 이름/날짜/시간 같은 평면 조건만으로는 순번을 표현할 수 없다. 정렬 기준과 누적 개수가 함께 필요해서, JPQL 서브쿼리처럼 “데이터를 계산하는 질의”로 푸는 편이 자연스럽다

실제 JPQL은 다음처럼 정리했다.

```jpql
select new roomescape.repository.reservationwaiting.jpa.WaitingWithRank(
        w.id,
        w.slot.id,
        w.name,
        w.requestedAt,
        (select count(w2) + 1
         from ReservationWaitingJpaEntity w2
         where w2.slot = w.slot
           and (
               w2.requestedAt < w.requestedAt
               or (w2.requestedAt = w.requestedAt and w2.id < w.id)
           )
        )
)
from ReservationWaitingJpaEntity w
where w.name = :name
order by w.slot.date, w.slot.theme.id, w.slot.time.id, w.requestedAt, w.id
```

이 단계에서의 결론은 분명했다. 조회 결과를 조합하는 책임이 Java 쪽으로 넘어오면 N+1이 드러나고, 순번 계산처럼 파생값이 필요하면 JPQL이 자연스럽게 들어온다. 반대로 fetch join은 “조회할 연관을 미리 정해 두는 방식”이라 N+1을 줄이는 도구로 쓸 수 있었다.


## 4단계

### 예약 대기 관리 JPA 전환

4단계에서는 자동 승인 로직을 새로 만들기보다, 이미 동작하던 자동 승격 흐름을 JPA 엔티티/레포지토리 기준으로 유지하는 쪽에 초점을 맞췄다. 어드민은 [`ReservationWaitingAdminPageController`](../src/main/java/roomescape/controller/reservationwaiting/ReservationWaitingAdminPageController.java)에서 대기 목록을 조회하고 삭제할 수 있고, 승인은 예약 취소 시 [`ReservationService.cancel()`](../src/main/java/roomescape/service/reservation/ReservationService.java) 안에서 자동으로 이어진다.

- 트랜잭션 경계: 취소/승격은 `ReservationService`의 public 메서드에 붙은 `@Transactional` 안에서 한 번에 처리되도록 두었다. 예약 삭제, 승격 예약 저장, 대기 삭제를 한 서비스 흐름으로 묶어야 중간 상태가 외부에 노출되지 않는다.
- 동시성: 같은 슬롯에서 동시에 취소와 승격이 들어오면 중복 예약 위험이 있다. 그래서 저장 단계에서는 JPA unique constraint와 `PersistenceConflictException` 변환을 그대로 활용해 DB가 최종 충돌을 막도록 두었고, 서비스는 그 예외를 도메인 에러로 바꿔 돌려준다.
- flush 순서: `deleteReservation()` -> `savePromotedReservation()` -> `reservationWaitingRepository.delete()` 순서로 호출되도록 유지했다. 각 레포지토리 메서드가 `flush()`까지 수행하므로, 예약 슬롯을 먼저 비운 다음 승격 예약을 넣고 마지막에 대기를 지우는 순서가 실제 DB 반영 순서로도 드러난다.

관리자용 대기 목록은 `ReservationWaitingRepository.findAll()`을 JPA로 옮기고, `@EntityGraph`로 `slot`, `theme`, `time`을 함께 읽도록 맞췄다. 덕분에 목록 조회 시에도 `ReservationWaitingJpaEntity.toDomain()` 과정에서 연관 필드 때문에 추가 SELECT가 다시 늘어나는 상황을 피할 수 있었다.
