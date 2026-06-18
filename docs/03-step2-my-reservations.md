# 03 · 2단계 — 내 예약 목록 조회

> **쿼리 메서드 도입의 벽** 단계. 단일 API라 1단계보다 가볍다. (관찰표 없음 — 이 단계가 만드는 건 "쿼리 작성 방식 결정"이 핵심)
> ✅ **상태**: 기능은 1-2에서 `findByName` 파생 쿼리를 만들 때 **이미 충족**됨(`GET /reservations-mine` 동작). 그래서 2단계는 *코드 추가*가 아니라 **결정 명시 + mine 경로 N+1 관찰 + 기록**이 본체. "이미 됐다"를 정직히 적는 것이 이 단계의 신호.

---

## 들어가기 전 자기진단

**Q. 내 예약 목록 조회를 풀 때, 메서드 이름 쿼리(`findByMemberId`)와 JPQL(`@Query`) 중 어느 쪽이 먼저 떠오르나? 그 직감의 근거는?**
> (직감) **메서드 이름 쿼리가 먼저**. 조건이 `name` 하나 + 정렬뿐이라 `findByNameOrderBy...`로 끝난다. JPQL은 fetch join·집계·다중 엔티티 결합처럼 *표현력*이 필요할 때 꺼내는 카드. 근거: 메서드 이름 쿼리는 "조건이 단순할 때 가장 적은 코드", JPQL은 "이름으로 못 적는 걸 적을 때".

---

## 요구사항 (LMS)

- `GET /reservations-mine` — 인증된 사용자의 예약(+대기) 목록

> ⚠️ **내 코드 특수 상황**: step2엔 member가 없고, "내 예약"은 `ReservationRepository.findByNameOrderByDateAscTimeAsc(name)` 즉 **이름 기준**으로 풀려 있음. 그래서 이 단계의 진짜 결정은 두 갈래(아래 결정 1).

---

## 현황 — 기능은 1-2에서 이미 동작 (전환 전/후 통합)

`GET /reservations-mine?name=` 흐름:
```
UserReservationController.myList(name)
  → ReservationService.findMyReservationsAndWaitings(name)   // @Transactional 아님(reads tx-free)
      → reservationRepository.findByNameOrderByDateAscTimeAsc(name)   // 예약
      → waitingRepository.findByName(name)                            // 대기
      → 두 리스트를 date, time.startAt 으로 in-memory 병합 정렬
  → MyReservationResponse.from(...)  // status=RESERVED/WAITING, waitingOrder(대기만)
```

**전환 전 (JDBC)**: `findByName...`은 reservation ⨝ reservation_time ⨝ theme 3중 조인 SELECT + RowMapper 손조립(1-2 "전환 전"과 동형). → **1-2에서 이미 파생 쿼리로 대체**됨.

**전환 후 (JPA)**: 메서드 이름 파생 쿼리로 동작.
```java
// ReservationJpaRepository
List<Reservation> findByNameOrderByDateAscTime_StartAtAsc(String name);
// WaitingJpaRepository
List<Waiting> findByNameOrderByDateAscTime_StartAtAsc(String name);
```
- `Time_StartAt` 밑줄 경로로 `time.startAt` 정렬을 메서드 이름에 인코딩.
- 예약·대기를 각각 조회 후, 서비스에서 `Comparator.comparing(date).thenComparing(time.startAt)`로 **in-memory 병합**(두 엔티티를 한 쿼리로 합칠 수 없어서).

---

## 발행 SQL 관찰 — mine 경로의 N+1 (예약 2 + 대기 2, name="모카")

mine 한 번 호출에 **distinct 8 SELECT**(show-sql·SQL 로거 이중 로깅이라 로그엔 16):
```
[예약 sub-path · tx1]
  ① reservation(main)          -- ORDER BY time.start_at 위해 reservation_time 조인, SELECT은 예약 컬럼 + FK id만
  ② theme                      -- 1회만! 두 예약이 같은 theme → 1차 캐시가 dedup
  ③ reservation_time (t1)      -- 행별 보조 SELECT
  ④ reservation_time (t2)
[대기 sub-path · tx2]
  ⑤ waiting(main)              -- 동형(reservation_time 조인 + name 조건 + 정렬)
  ⑥ theme                      -- 재로드! tx1과 다른 tx라 1차 캐시 비공유
  ⑦ reservation_time (t1)
  ⑧ reservation_time (t2)
```
main 쿼리 형태(예약·대기 동형):
```sql
select w1_0.id, w1_0.date, w1_0.name, w1_0.order_index, w1_0.theme_id, w1_0.time_id
from waiting w1_0 join reservation_time t1_0 on t1_0.id=w1_0.time_id
where w1_0.name=? order by w1_0.date, t1_0.start_at
```

**관찰 포인트 (이 단계의 핵심 A)**
- **N+1 실물**: EAGER `@ManyToOne` + 파생 쿼리 → main + 연관별 보조 SELECT. 1-2의 단건 N+1이 mine에선 예약·대기 **두 갈래**로 나타남.
- **1차 캐시의 N+1 완화**: 두 예약이 공유하는 `theme`는 같은 tx 안에서 **1회만** 로드(②). 즉 naive `1 + 2N`이 아니라 *공유 연관은 dedup*. 반면 서로 다른 `time`(t1·t2)은 행별로 각각.
- **reads 트랜잭션-free의 청구서**: 예약·대기가 **별도의 tx-free 호출**이라 1차 캐시를 공유 못 함 → `theme`가 tx2에서 **재로드**(⑥). **트랜잭션 경계가 캐시 범위를 가른다**는 것이 mine 경로에서 눈에 보임 — 1-3 ②(1차 캐시)·1-3 결정(reads tx-free)의 직접적 귀결.
- **정렬이 두 군데**: 각 sub-path 내부는 DB `ORDER BY`(date, time.start_at), 예약+대기를 합칠 때는 in-memory 병합 정렬. 두 엔티티를 한 쿼리로 못 합치니 최종 정렬은 메모리.

> fetch 전략은 3-1에서 본격 관찰. 여기선 **N+1이 실재함**까지만 확정(3-1 fetch join이면 sub-path당 1쿼리 → 총 2쿼리로 축소).

---

## 확인 과제

**Q. 메서드 이름 쿼리·JPQL 중 어느 것을 썼나?**
> **메서드 이름 파생 쿼리**(`findByNameOrderByDateAscTime_StartAtAsc`). 조건이 `name` 단일 + 정렬뿐이라 이름 쿼리로 충분, JPQL을 꺼낼 이유가 없었다.

**Q. 그 결정의 한계는?**
> ① **조건이 늘면 메서드 이름 폭발** — 이미 `existsByDateAndTime_IdAndTheme_IdAndIdNot`처럼 이름이 길어지는 징후. ② **fetch join을 표현 못 함** → 위 N+1을 못 막음. 3-1에서 `@Query`(fetch join) 또는 `@EntityGraph`로 보강 필요. ③ **두 엔티티(예약·대기)를 한 쿼리로 못 합침** → in-memory 병합에 의존.

---

## 결정 기록 (인라인)

### 결정 1 — name 유지 vs member 도입

- **선택한 것**: 예약자 `name`(String) **유지**.
- **비교한 대안**: `Member` 엔티티 + FK 도입 → `findByMemberId`(LMS 명세에 더 가까움).
- **비교 기준**: 0단계 **"처음부터 다시 구현 금지"** — 이 미션의 목적은 JdbcTemplate 베이스라인과 1:1로 비교하며 JPA를 배우는 것이고, member 도입은 도메인 재설계라 **비교 축 자체를 흐린다**. 게다가 1-2·1-3을 모두 name 기준으로 일관되게 진행해 옴.
- **한계 / 다음 망가질 지점**: `name`이 사실상 식별자라 **동명이인 구분 불가**. 실서비스라면 member가 필수. 이 한계는 "학습용 베이스라인 보존"과 의식적으로 맞바꾼 것 — 미션 종료 후 member 도입이 자연스러운 다음 리팩터링. **(B)**

### 결정 2 — 메서드 이름 쿼리 vs JPQL

- **선택한 것**: 메서드 이름 파생 쿼리.
- **비교한 대안**: `@Query` JPQL.
- **비교 기준**: 단일 조건 + 정렬엔 파생 쿼리가 **최소 코드**. JPQL은 fetch join·집계처럼 이름으로 못 적는 게 필요할 때.
- **한계**: **fetch join 불가 → N+1**(위 관찰). 3-1에서 JPQL fetch join 또는 `@EntityGraph`로 승격 예정. **(B)**

## 테스트 변경 사항 (차원 C)

- **코드 변경 0 → 깨진 테스트 0**. 1-2의 **134개 green** 그대로 유지. `/reservations-mine` 인수 테스트가 기존에 경로를 이미 커버(예약+대기 혼합 응답·status·waitingOrder 검증).

## 피드백 채널 신호 (차원 C)

- **SQL 로그가 보여준 것**: mine 1회에 8쿼리(N+1) / 1차 캐시가 공유 theme를 dedup / **tx 경계가 캐시 범위를 가름**(예약 tx와 대기 tx가 theme를 따로 로드). "성능 문제"가 아니라 *구조가 만든 쿼리 수*를 눈으로 확인.

## 막힌 지점 → 다음 정의

- 막힘: 예약·대기 각 sub-path의 N+1.
- 다음 정의: **3-1**에서 fetch join / `@EntityGraph`로 sub-path당 1쿼리화 + (fetch join 시) row 중복(distinct) 처리 비교. mine은 3-1 N+1 재현의 **입구**.

## 얻은 인사이트

- (A) mine N+1은 1차 캐시로 *공유 연관*이 dedup되지만, **tx 경계 밖에선 재로드** — 캐시 범위 = 트랜잭션 범위. reads tx-free 결정이 여기서 비용으로 드러남. (A)
- (B) `name` 유지 = 학습 베이스라인 보존 ↔ 동명이인 불가의 맞교환 / 메서드 쿼리 = 최소 코드 ↔ fetch join 불가의 맞교환. 두 결정 모두 "지금의 단순함"을 산 것. (B)
- (C) 코드를 한 줄도 안 바꿔도, "이미 충족됨 + 그 한계(N+1)"를 정직히 기록하는 것이 이 단계가 만드는 신호. (C)

## 이 단계가 회수한 차원

- A: mine 경로 N+1 실측 + 1차 캐시 dedup + tx 경계가 캐시 범위를 가름.
- B: name 유지 / 메서드 쿼리 결정과 각각의 한계(동명이인·fetch join 불가).
- C: 코드 변경 0·134 green / N+1을 3-1로 넘기는 좌표 명시.
