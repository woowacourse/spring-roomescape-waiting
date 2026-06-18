# 01 · 사전학습 — 개념 & 코드 다시 읽기

> 미션 시작 전 가설. **"이게 답이었어?"를 미션 끝에 비교할 기준점.** 여기 적힌 SQL/동작 예측은 1단계 이후 실제 발행 SQL로 검증한다.
> "미션1" = 현재 `step2` 브랜치에 누적된 JDBC 방탈출 코드 전체. / 출발점(사전설문)은 `00-overview §내 출발점` 참고.

---

## 학습 테스트 (참조 매뉴얼 — 막힐 때만)

- `spring-data-jpa-1` — Entity·Repository·기본 CRUD → **1단계 매핑(1-1)**
- `spring-data-jpa-2` — 연관관계·JPQL → **1단계 연관관계(1-2) + 3단계 JPQL(3-2)**

처음부터 끝까지 따라하지 않고, 막히는 지점에서 참조 매뉴얼처럼 편다.

---

## 1. 이번 사이클에 답할 4질문 (가설)

### Q1. JPA는 무엇을 자동화하고, 그 대가로 무엇을 감추는가

**자동화하는 것**: SQL 문자열 생성과 객체↔행 매핑(RowMapper·KeyHolder·SimpleJdbcInsert 보일러플레이트 소멸), 연관 데이터 접근을 SQL join이 아니라 객체 참조로(
`reservation.getTime().getStartAt()`), 그리고 영속성 컨텍스트가 주는 세 가지 — 변경 감지(save 없이 필드 수정만으로 UPDATE), 1차 캐시(같은 트랜잭션 내 동일 id 재조회
시 SELECT 생략), 쓰기 지연(INSERT/UPDATE를 모아 flush 시점 일괄).

**그 대가로 감추는 것**: 한마디로 **"내가 쓴 SQL = 나가는 SQL"이라는 JDBC의 투명성**. ① 언제 어떤 SQL이 나가는지가 코드 표면에서 사라져 의도와 다른 쿼리(N+1)가 나가도 모르고, ②
flush 시점이 암묵적이라 "내가 부른 적 없는 SQL"이 발생하며, ③ LAZY 프록시는 컴파일 타임엔 안 보이다 트랜잭션 밖 접근에서 터지고, ④ 영속성 컨텍스트의 생명주기(트랜잭션 경계)가 코드에 명시되지
않는다. 정리하면 **투명성을 자동화와 맞바꾼 것**이고, 이 미션은 그 맞바꾼 비용을 발행 SQL로 다시 눈에 보이게 만드는 작업.

### Q2. 영속성 컨텍스트가 켜져 있다는 걸 어느 시점에 의식해야 하는가

의식해야 할 시점: **`@Transactional` 경계**(여기서 컨텍스트가 열리고 종료 시 flush+닫힘), **entity 필드를 수정하는 순간**(save 없이도 commit 때 UPDATE → 의도치
않은 UPDATE 방지), **같은 트랜잭션에서 같은 걸 두 번 조회할 때**(두 번째는 캐시라 DB 최신값이 아닐 수 있음), **JPQL/native 실행 직전**(쓰기 지연분 강제 flush), **트랜잭션이
끝난 뒤 LAZY 필드 접근**(LazyInit).

한 문장으로 압축하면 — **"지금 이 entity가 영속(managed)인가 준영속(detached)인가."** 이게 dirty checking이 먹히느냐, LAZY가 초기화되느냐를 가른다. 내
`delete-before-insert` 승급 흐름에서 read-your-own-writes를 트랜잭션 안에서 보장받는 게 정확히 이 의식이고(Cycle 2에서 이미 만진 감각), JPA에서 그 감각이 어디로
옮겨붙는지 비교하면 좋다. reads를 트랜잭션-free로 두는 결정을 했다면 컨텍스트가 더 짧게 열렸다 닫혀 LAZY 접근 가능 구간이 좁아지는 부작용도 함께 본다.

### Q3. SQL join을 객체 그래프로 옮기면 부담이 어디로 옮겨가는가

JDBC에선 join을 SQL에 박아 한 번에 가져왔고, 부담은 **"SQL을 직접 짜는 손의 수고"**였지만 쿼리 횟수는 명시적·예측 가능했다. 객체 그래프로 옮기면 코드(`getTheme().getName()`)
는 깨끗해지는 대신 부담이 **보이지 않는 곳으로** 이동한다.

목록 N개를 가져온 뒤 각 연관 객체에 접근하면 추가 SELECT가 N번 나가는 **N+1**이 생기고, 부담은 "쿼리 작성"에서 **"fetch 전략 결정"**(EAGER/LAZY, fetch join,
`@EntityGraph`)으로 바뀐다. 다시 fetch join으로 합치면 **row 중복(카테시안) 처리** 부담이 새로 붙는다. 즉 join의 부담이 사라진 게 아니라 **"쿼리를 쓰는 손" → "쿼리가 언제
몇 번 나가는지 추적하는 눈 + 전략 결정"**으로 형태가 바뀐 것. 미션 3-1이 내 `reservations-mine`(예약 N + 대기 M, 각각 theme·time 참조)에서 이걸 정확히 재현한다.

### Q4. 어노테이션 한 줄이 만드는 실제 SQL을 추적할 수 있는가

yes/no보다 **현재 위치를 정직하게 찍는 답**. 추적 도구는 `show-sql`·`format_sql`·`org.hibernate.SQL` 로거(+`BasicBinder`로 바인딩 파라미터)가 기본.

지금 떠오르는 것 / 안 떠오르는 것: **단일 entity CRUD는 떠오른다** — `@GeneratedValue(IDENTITY)`면 id를 알아야 하니 INSERT가 즉시 발행돼 쓰기 지연이 부분 무력화,
`@Column(nullable/length)`는 DDL에 반영. 반면 **연관관계 + flush 순서 + cascade가 얽히면 예측이 흔들린다** — `@ManyToOne` 기본 EAGER가 join이냐 별도
select냐, 양방향에서 어느 쪽이 FK UPDATE를 치느냐, cascade 전파 시 SQL 순서 — 관찰해야 보이는 영역.

→ 시작 시점의 정직한 자기 위치: **"단일 CRUD는 추적 가능, 연관·flush 순서·cascade는 아직 예측 불가 → 이 갭을 메우는 게 이번 미션의 차원 A."**

---

## 2. 사전학습 키워드 (정의 + 내 코드)

### 패러다임 차이

**ORM (Object-Relational Mapping)**

- 정의: 객체(클래스)와 관계형 테이블을 자동 매핑해, SQL 대신 객체 조작으로 영속성을 다루는 기술. JPA는 표준 명세, Hibernate는 구현체.
- 내 코드: `RowMapper`를 손수 짜서 `ResultSet` ↔ 객체를 변환하던 자리를 ORM이 자동화한다.

**객체-관계 임피던스 불일치**

- 정의: 객체 모델(상속·연관·참조 방향)과 관계형 모델(테이블·FK·집합)의 표현 방식이 근본적으로 달라 생기는 간극.
- 내 코드: `reservation.getTime()`(객체 참조)과 `time_id`(FK 컬럼)가 같은 관계를 다르게 표현하는 것이 이 불일치의 실물.

**영속성 (Persistence)**

- 정의: 객체 상태를 프로그램 종료 후에도 유지되도록 DB 같은 영구 저장소에 보관하는 것.
- 내 코드: 예약을 insert해 DB에 남기는 모든 동작이 영속화. JPA는 이걸 객체 생명주기로 추상화한다.

### 핵심 개념

**영속성 컨텍스트**

- 정의: entity를 보관·관리하는 1차 메모리 공간. 트랜잭션 범위에서 살아 있고, 1차 캐시·dirty checking·쓰기 지연이 전부 여기서 일어난다.
- 내 코드: JDBC엔 없던 개념. 미션 1-3 관찰 과제가 이 공간의 동작을 직접 들여다보는 자리.

**1차 캐시**

- 정의: 영속성 컨텍스트 안에 entity를 id 기준으로 저장해, 같은 트랜잭션에서 동일 id 재조회 시 DB를 안 치고 캐시에서 반환.
- 내 코드: JDBC면 `findById` 두 번 = SELECT 두 번. JPA는 두 번째 SELECT가 생략된다(= 비교 관찰 포인트).

**dirty checking (변경 감지)**

- 정의: 영속 상태 entity의 필드를 수정하면, 별도 update 호출 없이 commit/flush 시점에 스냅샷과 비교해 UPDATE 자동 발행.
- 내 코드: `update reservation set ...`을 직접 짜서 `JdbcTemplate.update(...)` 호출하던 자리가 통째로 사라진다.

**쓰기 지연 (Transactional Write-behind)**

- 정의: `save` 시 INSERT/UPDATE를 즉시 보내지 않고 쓰기 지연 저장소에 모았다가 flush 시점에 일괄 발행.
- 내 코드: JDBC는 `update()` 즉시 발행이었지만 JPA는 시점이 뒤로 밀린다(단, IDENTITY 키는 예외 — 즉시 INSERT).

**flush**

- 정의: 영속성 컨텍스트의 변경분을 DB에 동기화(SQL 발행). 트랜잭션 commit·JPQL 실행 직전·명시적 호출 시 발생.
- 내 코드: "내가 부른 적 없는 SQL이 언제 나가는가"의 답. flush ≠ commit(트랜잭션 종료)을 구분하는 게 핵심.

### 매핑

**@Entity**

- 정의: 클래스를 JPA가 관리하는 영속 객체로 지정. 기본 생성자 필요, 테이블과 매핑된다.
- 내 코드: `Reservation`·`Theme`·`ReservationTime`·`Waiting` 도메인 클래스에 부여. (member는 step2에 없음 — 예약자는 `name VARCHAR`)

**@Id**

- 정의: entity의 기본 키(PK) 필드를 지정.
- 내 코드: 각 테이블의 `id` 컬럼. JDBC에선 RowMapper에서 `rs.getLong("id")`로 읽던 자리.

**@GeneratedValue**

- 정의: PK 생성 전략 지정. `IDENTITY`는 DB의 auto_increment에 위임(INSERT 후 키 확인).
- 내 코드: `KeyHolder`/`SimpleJdbcInsert`로 생성 키를 받아오던 코드가 이걸로 대체.

**@Column**

- 정의: 필드↔컬럼 매핑 세부(name, nullable, length, unique 등) 지정. 미지정 시 필드명 규칙 자동 매핑.
- 내 코드: SQL/DDL 문자열에 박혀 있던 컬럼 제약이 entity 어노테이션으로 옮겨오는 자리.

**@Table**

- 정의: entity가 매핑될 테이블 이름·제약(uniqueConstraints 등) 지정. 미지정 시 클래스명 기준.
- 내 코드: 분리 모델의 `UNIQUE(date, time_id, theme_id, name)` 복합 유니크를 여기에 선언 가능.

### 연관관계

**@ManyToOne**

- 정의: N:1 관계 매핑. 여러 entity가 하나를 참조(FK 보유 쪽). 기본 fetch는 EAGER.
- 내 코드: `Reservation` → `Theme`/`ReservationTime` 참조(member 없음). FK 컬럼 + join이 이걸로 표현된다.

**@OneToMany**

- 정의: 1:N 관계 매핑. 하나가 여러 entity를 컬렉션으로 보유. 기본 fetch는 LAZY.
- 내 코드: 단방향 시작이라 거의 안 씀. `Theme`가 `List<Reservation>`을 들면 등장(필요 전엔 안 만듦).

**단/양방향**

- 정의: 한쪽만 참조를 들면 단방향, 양쪽이 서로 참조 필드를 들면 양방향. 양방향은 그래프 탐색이 양쪽으로 가능.
- 내 코드: 미션 1-2가 "단방향 시작, 필요 생기면 양방향". 시도→후퇴 사이클 기록이 차원 B의 도달점.

**연관관계 주인 (Owning side)**

- 정의: 양방향에서 FK를 실제 관리(INSERT/UPDATE)하는 쪽. 보통 `@JoinColumn`을 가진 N쪽, 반대편은 `mappedBy`로 읽기 전용.
- 내 코드: 양방향을 안 쓰면 등장 안 함. 양방향 시도 시 "누가 FK를 치는가"를 정해야 하는 지점.

**cascade (영속성 전이)**

- 정의: 부모 entity의 영속 작업(persist/remove 등)을 연관 자식에게 전파. `CascadeType`으로 범위 지정.
- 내 코드: 미션이 "필요해질 때까지 적용 금지" 명시. 적용 시 PR에 근거를 적어야 하는 신중 영역.

**orphanRemoval (고아 객체 제거)**

- 정의: 부모 컬렉션에서 자식 참조가 끊기면 그 자식 row를 DELETE. cascade REMOVE보다 좁은 "연관 끊김 = 삭제".
- 내 코드: 대기 취소·승급으로 `Waiting`이 컬렉션에서 빠질 때 자동 삭제 후보지만, 미션은 보수적 미적용 권장.

### 페치

**EAGER (즉시 로딩)**

- 정의: entity 조회 시 연관 entity를 즉시 함께 로딩(주로 join 또는 추가 select). `@ManyToOne` 기본값.
- 내 코드: `findById(reservation)` 한 번에 time·theme까지 끌려온다. 편하지만 불필요한 조회·N+1의 원인이 되기도.

**LAZY (지연 로딩)**

- 정의: 연관 entity를 실제 접근 시점까지 로딩을 미루고 프록시로 대체. `@OneToMany` 기본값.
- 내 코드: 트랜잭션 밖에서 LAZY 필드 접근 시 `LazyInitializationException` — 미션 1-3에서 만나는 경계 신호.

**fetch join**

- 정의: JPQL에서 연관 entity를 한 번의 join 쿼리로 함께 조회. N+1을 한 방에 해소하는 대표 수단.
- 내 코드: `reservations-mine`에서 예약·대기 + theme·time을 묶어 가져올 때. row 중복(distinct) 처리 부담이 새로 생긴다.

**@EntityGraph**

- 정의: 어떤 연관을 함께 로딩할지 어노테이션으로 선언해, JPQL 없이 fetch join 효과를 내는 방법.
- 내 코드: fetch join을 `@Query` 없이 repository 메서드 위 선언으로 처리. 미션 3-1에서 fetch join과 비교.

### 쿼리

**JPQL**

- 정의: 테이블이 아니라 entity·필드를 대상으로 작성하는 객체지향 쿼리. JPA가 실제 SQL로 번역해 발행.
- 내 코드: 대기 N번째 순번 계산처럼 메서드 이름 쿼리로 못 푸는 복잡 조회. 미션 3-2의 서브쿼리 COUNT.

**Native Query**

- 정의: DB 방언 그대로의 실제 SQL을 직접 작성해 실행. JPQL로 표현 못 하는 DB 고유 기능·튜닝에 사용.
- 내 코드: JDBC 시절 직접 쓰던 SQL과 가장 가깝다. 마지막 탈출구로 남지만 객체 추상화 이점은 포기.

---

## 3. 미션1 코드 다시 읽기 (step2 실측)

### ① JdbcTemplate이 다루는 SQL은 몇 종류인가

- **INSERT** — 4곳 모두. `KeyHolder`/`GeneratedKeyHolder`로 생성 키를 받아오는 패턴(`save`).
- **SELECT (단일 테이블)** — `ReservationTime`·`Theme`의 `findAll`/`findById`.
- **SELECT + 3중 조인** — `Reservation`의 `findAll`/`findById`/`findByName`, `Waiting`도 동일하게 `reservation_time` + `theme`
  조인. 조회 핵심 경로는 거의 다 조인.
- **SELECT EXISTS** — `Reservation`에만 5개(`existsByDateAndTimeAndTheme`/`existsBySlotAndName`/`existsByTimeId`/
  `existsByThemeId`/`...ExcludingId`).
- **UPDATE** — `reservation`의 date/time_id(예약 변경), `waiting`의 order_index(순번 재배치). 2종.
- **DELETE** — 4곳 모두 `deleteById`.
- **부가** — `ReservationTime.findAvailable`의 `NOT IN(서브쿼리)`, `Theme.findPopularBetween`의
  `JOIN + COUNT + GROUP BY + LIMIT` 집계.

→ **CRUD 4종이 기본, SELECT가 단일/조인/EXISTS/집계로 분화.**

### ② 도메인↔테이블 1:1인가

**테이블↔도메인 클래스는 1:1** (테이블 4개 = 클래스 4개). 단 단서 셋:

- 조회 결과↔객체는 **1:N 테이블 조립** — `Reservation` 한 객체를 만들 때 RowMapper가 `reservation` row + 조인된 `reservation_time`·`theme` 컬럼을
  읽어 `ReservationTime.withId(...)`·`Theme.withId(...)`를 손으로 조립. 클래스↔테이블은 1:1이지만 "하나의 객체 그래프"는 3개 테이블에서 온다.
- **`name`은 객체가 아니다** — 예약자가 `Member` 같은 별도 도메인/테이블이 아니라 `VARCHAR` 컬럼. 이 자리만 매핑이 "값(String)" 수준.
- `waiting.order_index`도 별도 객체가 아닌 `Waiting`의 한 필드(materialized 파생값)로 1:1 매핑.

### ③ join을 박았나, 두 번 조회했나

**전부 join을 SQL에 직접 박음.** `time_id`로 `ReservationTimeRepository.findById`를 따로 부르는 "두 번 조회"가 아니라,
`INNER JOIN reservation_time ... INNER JOIN theme ...`를 한 쿼리에 넣고 RowMapper에서 조립. Reservation·Waiting·Theme(인기) 모두 동일.

→ JPA 전환의 핵심 대비점: 나는 **"한 쿼리 조인"을 명시적으로 선택**해뒀는데, JPA로 가면 `@ManyToOne`이 그 조인을 감추며 **기본 N+1 위험**으로 전환. 한 쿼리 조인을 유지하려면
fetch join/`@EntityGraph`로 *다시 명시* 필요(미션 3-1).

### ④ 객체 그래프로 옮기면 사라질 코드

- **RowMapper 전체** — `ReservationTime.withId(...)`/`Theme.withId(...)` 손수 조립이 `@ManyToOne` 매핑으로 대체.
  `getTime().getStartAt()`이 그냥 됨.
- **컬럼 별칭 곡예** — `r.id AS reservation_id`, `th.name AS theme_name` 같은 조인 컬럼 충돌 회피가 통째로 사라짐.
- **INSERT 보일러플레이트** — `PreparedStatement` + `GeneratedKeyHolder` + `ps.setXXX`가 `save()` + `@GeneratedValue(IDENTITY)`
  로.
- **명시적 UPDATE SQL** — `updateDateAndTime`·`updateOrderIndex`가 dirty checking으로 대체(트랜잭션 안 필드 수정).
- **EXISTS 쿼리들** — `existsBy...`는 메서드 이름 쿼리로 자동 생성 가능.

**함정**: `INNER JOIN` 문자열은 사라져도 조인의 부담은 fetch 전략 결정으로 이동. `order_index`의 `updateOrderIndex`는 dirty checking으로 사라지지만 *
*FIFO 순번 재계산 로직**(bookkeeping)은 남거나 → **JPQL rank 계산으로 갈아탈지**의 설계 재결정(미션 3-2).

---

> 🖊️ 이 문서가 02~05단계의 "전환 전" 기준선. 단계 진입 시 여기 ③④를 펴서 시작.
