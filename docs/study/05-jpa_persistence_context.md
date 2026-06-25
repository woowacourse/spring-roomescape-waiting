# JPA 영속성 컨텍스트 핵심 동작 정리

> 프로젝트: spring-roomescape-waiting (JPA 마이그레이션 이후 관찰 기록)

---

## 영속성 컨텍스트란

`@Transactional` 범위 안에서 JPA가 엔티티를 관리하는 1차 저장소.
DB와 애플리케이션 사이의 버퍼 역할을 하며, 트랜잭션이 닫히면 함께 소멸된다.

```
@Transactional 범위 안
┌─────────────────────────────┐
│  영속성 컨텍스트 (1차 캐시)     │
│  ┌──────────────────────┐  │
│  │ Session#1  id=1      │  │
│  │ Waiting#1  id=1      │  │
│  └──────────────────────┘  │
│          ↕ flush           │
│     H2 DATABASE            │
└─────────────────────────────┘

@Transactional 범위 밖
┌─────────────────────────────┐
│  영속성 컨텍스트 (닫힘)         │
│  ┌──────────────────────┐  │
│  │ TimeSlot 프록시 (미초기화)│  │
│  │  → getStartAt() 호출 시 │  │
│  │  LazyInitializationEx │  │
│  └──────────────────────┘  │
└─────────────────────────────┘
```

---

## ① 더티 체킹 (Dirty Checking)

**현상**: `save()` 없이 필드만 수정해도 트랜잭션 커밋 시 UPDATE 자동 발행

**원리**: 엔티티를 로드할 때 Hibernate가 그 시점의 필드값을 **스냅샷**으로 저장한다.
flush/commit 시 스냅샷과 현재 상태를 비교해 변경이 있으면 UPDATE를 자동 발행한다.

```java
// save() 호출 없음
Session managed = em.find(Session.class, 1L);
managed.

setDate(LocalDate.now().

plusDays(99));  // 필드만 변경
        em.

flush();  // → UPDATE session SET date=? WHERE id=?
```

**콘솔 출력**:

```sql
update session
set date     = ?,
    theme_id = ?,
    time_id  = ?
where id = ?
-- save() 호출 없었지만 flush 시 자동 발행
```

---

## ② 1차 캐시

**현상**: 같은 트랜잭션에서 `findById` 두 번 호출 → SELECT 1회만 발행, 동일 인스턴스 반환

```java
Session a = repo.findById(1L);  // SELECT 발행
Session b = repo.findById(1L);  // SQL 없음 (캐시 적중)

assert a ==b;  // true — 동일 인스턴스
```

**콘솔 출력**:

```sql
select s1_0.id, s1_0.date, s1_0.theme_id, s1_0.time_id
from session s1_0
where s1_0.id = 1
-- 두 번째 호출은 SQL 미발행
```

**주의**: 같은 트랜잭션(영속성 컨텍스트) 안에서만 유효.
`em.clear()` 또는 트랜잭션 경계를 넘으면 캐시 소멸.

---

## ③ 쓰기 지연 (Write-Behind)

**현상**: `save()` 시점에 INSERT가 즉시 발행되지 않고 flush/commit까지 지연될 수 있음

**전략별 차이**:

| 전략              | INSERT 발행 시점   | 이유                        |
|-----------------|----------------|---------------------------|
| `IDENTITY` (현재) | `save()` 즉시    | DB가 id를 생성해야 하므로 지연 불가    |
| `SEQUENCE`      | flush/commit 시 | 시퀀스로 id를 미리 채번해 버퍼에 적재 가능 |

```java
// IDENTITY 전략 (현재 프로젝트)
Reservation saved = repo.save(Reservation.transientOf("브라운", session));
// ↑ 여기서 즉시 INSERT 발행 (id가 필요하므로)
System.out.

println(saved.getId());  // null 아님

// SEQUENCE 전략이었다면
// save() 시점 → 버퍼에 적재 (INSERT 미발행)
// em.flush() 또는 commit 시점 → INSERT 일괄 발행
```

---

## ④ flush 시점

**현상**: `flush()`를 명시 호출하지 않아도 특정 시점에 자동으로 영속성 컨텍스트 → DB 동기화

**자동 flush 발생 조건 3가지**:

1. **JPQL / Native Query 실행 직전** (FlushMode.AUTO 기본값)
2. **트랜잭션 커밋 시** (`@Transactional` 메서드 정상 종료)
3. **`em.flush()` 명시 호출** (커밋 없이 DB에만 반영, 롤백 가능)

```java
em.persist(waiting);  // 1차 캐시에만 존재

// JPQL 실행 직전 자동 flush
Long count = em.createQuery("SELECT COUNT(w) FROM Waiting w", Long.class)
        .getSingleResult();
// → persist한 데이터가 COUNT에 반영됨
```

**콘솔 출력**:

```sql
insert into waiting (name, session_id)
values (?, ?) ← 자동 flush
select count(w1_0.id)
from waiting w1_0 ← JPQL
```

---

## ⑤ fetch 기본값

**현상**: `@ManyToOne(fetch = LAZY)` 명시 → Session 로드 시 TimeSlot은 프록시로 대기

**JPA 표준 기본값**:

- `@ManyToOne`, `@OneToOne` → **EAGER** (기본)
- `@OneToMany`, `@ManyToMany` → **LAZY** (기본)

**현재 프로젝트**: 명시적으로 `FetchType.LAZY` 오버라이드

```java
// Session.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "time_id")
private TimeSlot timeSlot;  // 로드 시 프록시 객체
```

**콘솔 출력**:

```sql
-- sessionRepo.findById(1L) 호출 시
select s1_0.id, s1_0.date, s1_0.theme_id, s1_0.time_id
from session s1_0
where s1_0.id = 1
-- time_slot JOIN 없음 ← LAZY 적용

-- session.getTimeSlot().getStartAt() 접근 시점에
select t1_0.id, t1_0.start_at
from time_slot t1_0
where t1_0.id = ?
-- 이때서야 별도 SELECT 발행
```

---

## ⑥ LazyInitializationException

**현상**: 영속성 컨텍스트(트랜잭션)가 닫힌 후 LAZY 프록시에 접근 → 예외 발생

```java
// @Transactional 서비스에서 Session 반환
Session session = sessionService.findById(1L);
// → 메서드 반환 시 트랜잭션 종료, 영속성 컨텍스트 닫힘

// 컨트롤러에서 LAZY 필드 접근
session.

getTimeSlot().

getStartAt();
// → LazyInitializationException: could not initialize proxy - no Session
```

**발생 흐름**:

```
1. @Transactional 서비스에서 Session 로드
   → timeSlot은 미초기화 프록시 (SELECT 미발행)

2. 메서드 반환 → 트랜잭션 종료
   → 영속성 컨텍스트 닫힘

3. 컨트롤러에서 session.getTimeSlot().getStartAt()
   → 프록시 초기화 시도 → no Session
   → LazyInitializationException
```

**주의 — 1차 캐시로 인한 함정**:

같은 EntityManager에서 TimeSlot을 저장한 뒤 Session을 조회하면,
TimeSlot이 1차 캐시에 남아 **프록시 대신 실제 객체**를 반환한다.
이 경우 EM을 닫아도 예외가 발생하지 않아 문제를 놓치기 쉽다.

예외를 재현하려면 별도 EntityManager(별도 트랜잭션)로 조회해야 프록시가 생성된다.

**실제 서비스에서 발생하는 시나리오**:

1. `spring.jpa.open-in-view=false` 설정 (기본값 true → OSIV가 컨트롤러까지 세션 연장)
2. `@Transactional` 없는 컨트롤러에서 반환된 엔티티의 LAZY 필드를 JSON 직렬화

---

## 메서드 이름 쿼리 vs JPQL 선택 기준

### 메서드 이름 쿼리 (네임쿼리)

Spring Data JPA가 메서드명을 파싱해 쿼리를 자동 생성한다.
연관 엔티티 필드까지 경로 탐색(property traversal)을 지원한다.

```java
// Waiting.session (Session) → Session.id (Long) 경로 탐색
List<Waiting> findBySessionId(Long sessionId);
// → SELECT w FROM Waiting w WHERE w.session.id = :sessionId

// 정렬 포함
List<Waiting> findBySessionOrderByIdAsc(Session session);
// → SELECT w FROM Waiting w WHERE w.session = :session ORDER BY w.id ASC
```

### 네임쿼리의 한계

| 한계            | 설명                                    |
|---------------|---------------------------------------|
| 집계/그룹핑 불가     | `GROUP BY`, `HAVING`, `COUNT` 등 표현 불가 |
| 서브쿼리 불가       | `WHERE id IN (SELECT ...)` 불가         |
| JOIN FETCH 불가 | N+1 문제를 쿼리 레벨에서 해결 불가                 |
| 가독성 한계        | 조건이 3개 이상이면 메서드명이 파국                  |

### JPQL이 필요한 시점

```java
// N+1 해결 — JOIN FETCH
@Query("SELECT w FROM Waiting w JOIN FETCH w.session WHERE w.session.id = :id")
List<Waiting> findWithSession(@Param("id") Long id);

// 집계
@Query("SELECT COUNT(w) FROM Waiting w WHERE w.session = :session AND w.id <= :id")
int countBefore(@Param("session") Session session, @Param("id") Long id);

// 서브쿼리 (WHERE/HAVING 절만 가능, FROM 절 불가)
@Query("SELECT w FROM Waiting w WHERE w.session.id IN " +
        "(SELECT s.id FROM Session s WHERE s.date > :date)")
List<Waiting> findAfterDate(@Param("date") LocalDate date);
```

### JPQL의 한계 → 네이티브 쿼리

```java
// FROM 절 서브쿼리, ROW_NUMBER(), 윈도우 함수 등은 JPQL 불가
// → @Query(nativeQuery = true) 또는 @Formula 사용
@Formula("(SELECT COUNT(*) FROM waiting w2 WHERE w2.session_id = session_id AND w2.id <= id)")
private Integer waitingNumber;
```

### 선택 기준 요약

| 상황                  | 선택                 |
|---------------------|--------------------|
| 단순 조건 1-2개 + 정렬     | 네임쿼리               |
| 조건 3개 이상 / 복잡한 이름   | JPQL               |
| N+1 해결 (JOIN FETCH) | JPQL               |
| 집계, 그룹핑             | JPQL               |
| 서브쿼리 (WHERE/HAVING) | JPQL               |
| 윈도우 함수, FROM 서브쿼리   | 네이티브 쿼리 / @Formula |
