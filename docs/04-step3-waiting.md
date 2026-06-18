# 04 · 3단계 — 예약 대기 (N+1 / JPQL)

> **학습 절정.** 3-1 N+1·fetch join / 3-2 JPQL의 2중 관찰 구조.
> ⚠️ **내 코드 특수 상황**: `Waiting` 엔티티·`order_index`·중복 방지(UNIQUE)·name 기준 조회가 **이미 JDBC로 구현됨** → 1-2에서 JPA 전환 완료. 그래서 이 단계는 "기능 만들기"가 아니라 **fetch 전략으로 N+1 닫기 + order_index ↔ JPQL rank 재결정**이 핵심.
> ✅ **상태**: 3-1 코드 푸시(EAGER→LAZY + @EntityGraph), 3-2 결정(order_index 유지) 완료. 전체 green.

---

## 들어가기 전 자기진단

**Q. 예약 대기 도메인을 별도 엔티티로 만들지, 기존 `Reservation`에 status 컬럼만 추가할지 — 첫 직감과 근거는?**
> **이미 결정됨**: `Waiting` 별도 엔티티 + 분리 테이블. 근거 보강 — 분리 모델이라야 한 슬롯에 대해 **예약 UNIQUE(date,time_id,theme_id)** 와 **대기 UNIQUE(date,time_id,theme_id,name)·(…,order_index)** 가 *각각* 성립한다. 단일 테이블 + status ENUM이면 "예약은 슬롯당 1, 대기는 슬롯당 N(이름·순번 유일)"이라는 **서로 다른 유일성 규칙을 한 테이블에서 못 나눈다**. 분리는 제약의 표현력을 위한 것. **(B)**

---

## 요구사항 (LMS)

- `POST /waitings` — 예약 대기 요청 / `DELETE /waitings/{id}` — 취소
- `GET /reservations-mine` 응답에 대기 목록 포함 (status = "N번째 예약대기")
- 같은 테마·날짜·시간 중복 예약 방지 / (심화) 내 대기가 몇 번째인지

## 전환 전 (JDBC) → 전환 후 (JPA)  — 1-2에서 완료

- **전환 전**: `JdbcWaitingRepository` = save(INSERT+KeyHolder) / findBySlot·findByName(3중 조인 SELECT + RowMapper) / `updateOrderIndex`(bulk UPDATE).
- **전환 후**(1-2): `@Entity Waiting` + 단방향 `@ManyToOne`(time/theme) + `@Table` 복합 UNIQUE 2개 + `WaitingJpaRepository`(파생 쿼리 `findByDateAndTime_IdAndTheme_IdOrderByOrderIndexAsc`·`findByNameOrderByDateAscTime_StartAtAsc` + `@Modifying updateOrderIndex`). 상세는 `02-step1 §1-2`.
- 이 단계(3-1)에서 그 위에 **fetch 전략을 바꾼다**.

---

## 3-1. N+1과 fetch join 본격 비교 (관찰 과제 2) ⭐ 차원 A

### 관찰 시나리오

`reservations-mine`(예약 N + 대기 M)을 가져와 각 `getTheme()`·`getTime()`을 DTO 변환할 때 SQL이 몇 번 나가나? fetch join / `@EntityGraph`로 묶으면? row 중복은?

### 관찰 A — N+1 (전략 없음 = EAGER)

```
시도 코드:  @ManyToOne 기본(EAGER) + 파생 쿼리(findByName) + DTO 변환
예측:       목록 1 + (N+M)회 추가 SELECT
실제:       예약2 + 대기2 → distinct 8 SELECT
왜 다른가:  파생/HQL 쿼리는 EAGER 연관을 자동 join fetch 하지 않고 행마다 보조 SELECT
```
```
[예약 sub-path · tx1]  reservation(main, ORDER BY 위해 reservation_time 조인)
                       → theme (1회: 1차 캐시가 공유 theme dedup)
                       → reservation_time (t1), reservation_time (t2)   ← 행별
[대기 sub-path · tx2]  waiting(main) → theme (재로드: 새 tx) → reservation_time ×2
```
- **1차 캐시가 N+1을 부분 완화**: 같은 tx 안 공유 `theme`는 1회만(naive 1+2N 아님). 서로 다른 `time`은 행별.
- **reads tx-free의 청구서**: 예약·대기가 별도 tx-free 호출 → 캐시 비공유 → `theme` 재로드. **tx 경계가 캐시 범위를 가른다**(1-3 ②·결정과 직결).

### 중간 단계 — EAGER→LAZY 전환의 청구서 (1-3 ⑥ 예고 실현)

`@ManyToOne(fetch=LAZY)`로 바꾼 직후:
```
ReservationServiceTest 3건 FAILED — org.hibernate.LazyInitializationException
  (findMyReservationsAndWaitings는 @Transactional 아님 = reads tx-free)
```
- DTO 변환이 컨텍스트 닫힌 뒤 LAZY 연관을 건드려 예외 → **1-3 ⑥(getReference 프록시 tx 밖 접근)이 실제 read 경로에서 재현**. "reads를 tx-free로 둔다"는 결정이 LAZY와 충돌하는 좌표가 현실화.
- **`findAll`(admin)은 안 깨짐**: **OSIV(`spring.jpa.open-in-view` 기본 on)** 가 HTTP 요청 동안 영속성 컨텍스트를 뷰까지 열어 둬 LAZY를 늦게라도 로드. 단 **OSIV는 LazyInit만 가리고 N+1은 못 가린다**(서비스 직접 호출 테스트엔 OSIV 없음 → 노출). → "왜 어디는 깨지고 어디는 안 깨지나"의 답이 OSIV.

### 관찰 B — fetch join / @EntityGraph

```
시도 코드:  @EntityGraph(attributePaths={"time","theme"})
            on ReservationJpaRepository.findByName...·findAll / WaitingJpaRepository.findByName...
예측:       한 join 쿼리로 합쳐짐
실제:       mine 경로 8 → 2 SELECT (예약 fetch-join 1 + 대기 fetch-join 1)
왜 다른가:  @EntityGraph가 연관을 join fetch로 끌어와 보조 SELECT 제거. LazyInit도 해소.
```
발행 SQL(예약, 대기 동형):
```sql
select r1_0.id, r1_0.date, r1_0.name,
       t2_0.id, t2_0.description, t2_0.name, t2_0.thumbnail_url,   -- theme
       r1_0.time_id, t1_0.id, t1_0.start_at                        -- time
from reservation r1_0
join reservation_time t1_0 on t1_0.id=r1_0.time_id
join theme t2_0 on t2_0.id=r1_0.theme_id
where r1_0.name=? order by r1_0.date, t1_0.start_at
```
- **row 중복(distinct/Set) 처리는?** → **불필요**. `@ManyToOne`(to-one) fetch join은 1행→1연관이라 행이 늘지 않는다. row 중복(곱집합)은 **to-many 컬렉션 fetch join**의 문제(거기선 `distinct`/`Set` + 페이징 충돌까지). 단방향 to-one만 쓰는 이 모델은 그 함정을 안 만남.
- `optional=false`라 **INNER JOIN**으로 발행(optional이면 LEFT JOIN).

### 두 SQL 나란히 (N+1 ↔ fetch join)

| | 관찰 A (EAGER, 전략 없음) | 관찰 B (@EntityGraph) |
|---|---|---|
| mine SELECT 수 | **8** (예약 4 + 대기 4) | **2** (예약 1 + 대기 1) |
| 형태 | main + 연관별 보조 SELECT | 단일 INNER JOIN fetch |
| tx-free read | (EAGER라 동작) / LAZY면 LazyInit | 정상(연관 미리 로드) |
| row 중복 | 없음 | 없음(to-one) |

---

## 3-2. JPQL 본격 — N번째 대기 계산 ⭐ 차원 A·B

### 관찰 — order_index 읽기(A) vs JPQL 상관 서브쿼리 COUNT(B)

같은 슬롯에 대기 3명(철수=1, 영희=2, 모카=3) 심고 "모카의 순번"을 두 길로:
- **A. 저장된 `order_index` 읽기**: `findByName` 결과의 `getOrderIndex()` = **3**. (컬럼 SELECT 한 번.)
- **B. JPQL 상관 서브쿼리 COUNT**: 같은 슬롯에서 `id <` 인 대기 수 = **2**(앞에 철수·영희) → 3번째.

LMS 힌트(memberId)를 내 모델(name)에 맞춰 실행한 JPQL → **발행 SQL**:
```sql
select w1_0.id,
       (select count(w2_0.id)
        from waiting w2_0
        where w2_0.theme_id = w1_0.theme_id
          and w2_0.date     = w1_0.date
          and w2_0.time_id  = w1_0.time_id
          and w2_0.id       < w1_0.id)
from waiting w1_0
where w1_0.name = ?
```
→ JPQL `w2.theme = w.theme`가 FK 컬럼 `theme_id` 비교로, 상관 서브쿼리가 **행마다** 같은 슬롯의 선행 대기를 COUNT.

### 확인 과제

**Q. JPQL이 발행하는 SQL은?**
> 위 상관 서브쿼리 COUNT(메인 `waiting` 1행마다 `id<` 조건의 서브 SELECT). 엔티티 경로(`w2.theme`)가 FK(`theme_id`)로 컴파일됨.

**Q. 메서드 이름 쿼리로 왜 못 풀었나? (1줄)**
> **상관 서브쿼리 COUNT + 자기 자신과의 비교(`w2.id < w.id`, 같은 슬롯 조건)** 는 파생 메서드 이름 문법 밖 — 파생은 *단일 엔티티의 속성 조건·정렬*만 인코딩하고, **같은 테이블을 자기참조로 집계**하는 표현이 없다.

### 결정 기록 — order_index 유지 vs JPQL rank 계산

- **선택한 것**: `order_index` **유지** (비교 후 유지).
- **비교한 대안**: ⓐ materialized `order_index` 유지 / ⓑ 조회 시 JPQL COUNT 계산 / ⓒ 혼합.
- **비교 기준**:
  - **order_index 유지(채택)**: 읽기가 trivial(컬럼 SELECT) + **`UNIQUE(date,time_id,theme_id,order_index)` 가 슬롯당 위치 중복을 막는 DB 최종 방어선**(insert-uniqueness 경쟁 방어) + 승급(`promote`)·`Waitings` FIFO 정책과 이미 통합. **대가**: 부기(`reorderAfterRemoval`로 취소 시 재번호) + order_index 할당 동시성.
  - **JPQL rank**: **부기 0**(취소 시 *자가 치유* — 선행 `id` 수가 자동 감소) + order_index 컬럼/제약 불필요. **대가**: 행별 상관 서브쿼리 COUNT + 위치가 `id` 순서에서 *암묵* 파생 + **위치 UNIQUE 제약을 못 검**.
- **한계 / 다음 망가질 지점**: order_index 유지의 부기는 **동시 취소 시 재번호 경쟁**(같은 슬롯 다중 행 전환 → 비관적 락 후보, 백로그). JPQL rank로 가면 그 부기가 통째로 사라지지만 슬롯당 위치 UNIQUE 방어를 잃는다.
- **시도→후퇴 흔적**: JPQL rank를 **관찰·실행**(위 SQL 확보)했으나, ① `UNIQUE(order_index)`가 insert-uniqueness 경쟁의 마지막 방어선이고 ② 승급이 order_index에 의존하며 ③ green 모델을 미션 중 갈아엎는 리스크 때문에 **유지로 후퇴**. JPQL rank는 미션 후 "부기 제거" 리팩터링 **1순위 후보**로 기록. **(B)**

---

## 테스트 변경 사항 (차원 C)

- **3-1**: `@ManyToOne` EAGER→LAZY 직후 `ReservationServiceTest` 3건이 `LazyInitializationException`(reads tx-free의 mine 경로) → `@EntityGraph`(time/theme)로 해소. 최종 **전체 green(134)**.
- **3-2**: 코드 변경 0(order_index 유지). JPQL rank는 **관찰만**(임시 쿼리, 푸시 안 함) — 결정의 비교 자료로 SQL만 확보.

## 피드백 채널 신호 (차원 C)

- **N+1을 먼저 알려준 채널**: SQL 로그(mine 1회 8쿼리). "성능 문제"가 아니라 *구조가 만든 쿼리 수*가 눈에 보임.
- **예외가 가르친 것**: `LazyInitializationException`이 "reads tx-free + LAZY"의 충돌을 가르침.
- **의외**: **OSIV가 LazyInit은 가리지만 N+1은 못 가린다** — 같은 LAZY인데 HTTP(admin findAll)는 통과, 서비스 직접 호출(mine)은 예외. 차이의 정체가 OSIV.

## 막힌 지점 → 다음 정의

- 막힘: order_index 부기의 **동시 취소 재번호 경쟁**(같은 슬롯 다중 행 전환).
- 다음 정의: **4단계** 자동 승인 트랜잭션 경계(승급 flush 순서, 1-2에서 먼저 터진 그 지점) + (백로그) 비관적 락으로 재번호 경쟁 차단.

## 얻은 인사이트

- (A) N+1은 진입 경로(EAGER + 파생/HQL 쿼리)에서 나고, `@EntityGraph`(to-one) fetch join이 **단일 INNER JOIN·row 중복 없이** 닫는다(8→2). (A)
- (A) LAZY 전환은 reads tx-free와 충돌해 LazyInit을 낳고, **OSIV는 LazyInit만 가린다**(N+1은 잔존). (A)
- (B) `order_index`(저장) vs JPQL rank(조회 계산)는 **"부기+UNIQUE 방어 ↔ 무부기+자가치유"** 의 맞교환. 유지를 택한 건 UNIQUE 방어선·승급 통합·미션 안정성 때문. (B)
- (B) 파생 메서드 쿼리는 상관 서브쿼리 집계를 표현 못 함 → 거기서부터 JPQL의 영역. (B)
- (C) "관찰만 하고 채택하지 않은 대안(JPQL rank)"을 SQL과 함께 남기는 게 시도→후퇴의 정직한 기록. (C)

## 이 단계가 회수한 차원

- A: mine N+1 8→2(fetch join 실측) + LAZY/OSIV/LazyInit 관계 + JPQL 상관 서브쿼리 rank SQL.
- B: order_index 유지(비교 후) + fetch 전략(EAGER→LAZY+@EntityGraph) + 파생 쿼리 표현 한계.
- C: LazyInit→@EntityGraph 수정·green(134) / JPQL rank 관찰만 / 동시성 백로그 명시.
