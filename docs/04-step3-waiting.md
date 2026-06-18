# 04 · 3단계 — 예약 대기 (N+1 / JPQL)

> **학습 절정.** 3-1 N+1·fetch join / 3-2 JPQL의 2중 관찰 구조.
> ⚠️ **내 코드 특수 상황**: `Waiting` 엔티티·`order_index`·중복 방지(UNIQUE)·name 기준 조회가 **이미 JDBC로 구현됨.** 그래서 이 단계는 "기능 만들기"가 아니라 *
*전환 + N+1 관찰 + order_index ↔ JPQL rank 재결정**이 핵심.
> 커밋: `[3단계] ...` /

---

## 들어가기 전 자기진단

**Q. 예약 대기 도메인을 별도 엔티티로 만들지, 기존 `Reservation`에 status 컬럼만 추가할지 — 첫 직감과 근거는?**
> 🖊️ **이미 결정됨**: `Waiting` 별도 엔티티 + 분리 테이블. 근거 재확인 — 분리 모델이라야 `UNIQUE(date, time_id, theme_id, name)`가
> reservation/waiting 각각 성립(단일 ENUM 테이블론 불가). _(보강 작성)_  **(B)**

---

## 요구사항 (LMS)

- `POST /waitings` — 예약 대기 요청
- `DELETE /waitings/{id}` — 예약 대기 취소
- `GET /reservations-mine` 응답에 대기 목록 포함 (status = "N번째 예약대기")
- 같은 테마·날짜·시간 중복 예약 방지
- (심화) 내 대기가 몇 번째인지 표시

## 전환 전 (JDBC)

> 🖊️ `JdbcWaitingRepository`(save/findBySlot/findByName 3중 조인 + `updateOrderIndex`) 발췌 _(붙여넣기)_

## 전환 후 (JPA)

> 🖊️ `@Entity Waiting` + `@ManyToOne` + repository _(작성)_

---

## 3-1. N+1과 fetch join 본격 비교 (관찰 과제 2) ⭐ 차원 A

### 관찰 시나리오 (LMS)

- `reservations-mine`에서 예약 N + 대기 M을 가져온 뒤 각 `getTheme().getName()`·`getTime().getStartAt()`을 DTO 변환할 때 **SQL이 몇 번 나가나?
  **
- 같은 작업을 fetch join / `@EntityGraph`로 묶으면 SQL이 어떻게 달라지나?
- join 합쳐지면 row 중복은 어떻게 처리되나?

### 관찰 A — N+1 (전략 없음)

```
시도 코드:  연관 무명시 + DTO 변환
예측:       목록 1 + (N+M)회 추가 SELECT?
실제:       (발행 SQL 붙여넣기)
왜 다른가:
```

### 관찰 B — fetch join / @EntityGraph

```
시도 코드:
예측:       한 join 쿼리로 합쳐짐?
실제:
왜 다른가:  row 중복(distinct/Set) 처리는?
```

> 🖊️ **두 SQL을 나란히 붙이는 것**이 N+1↔fetch join 차이를 가장 정직하게 보여준다. (LMS)

---

## 3-2. JPQL 본격 — N번째 대기 계산 ⭐ 차원 A·B

> ⚠️ **재결정 포인트**: 내 코드는 순번을 `order_index`(materialized)로 저장·유지(`updateOrderIndex` bookkeeping) 중. LMS 힌트는 **조회 시 JPQL
서브쿼리 COUNT로 매번 계산**. 같은 "순번" 문제를 *저장 유지* vs *조회 계산*으로 푸는 두 길 — 무엇을 택하든(혹은 갈아탔다 되돌리든) 그 판단이 차원 B 1급 재료.

### LMS 힌트 (서브쿼리 COUNT)

```sql
SELECT new...WaitingWithRank(
               w,
               (SELECT COUNT(w2)
                FROM Waiting w2
                WHERE w2.theme = w.theme
                  AND w2.date = w.date
                  AND w2.time = w.time
                  AND w2.id < w.id))
FROM Waiting w
WHERE w.memberId = :memberId
```

### 확인 과제

**Q. JPQL이 발행하는 SQL은?**
> 🖊️ (실제 발행 SQL 붙여넣기) _(작성)_

**Q. 메서드 이름 쿼리로 왜 못 풀었나? (1줄)**
> 🖊️ (예: 상관 서브쿼리 CONT + 자기 조인 조건은 파생 메서드 문법 밖) _(작성)_

### 결정 기록 — order_index 유지 vs JPQL rank 계산

- 선택한 것:
- 비교한 대안: (materialized order_index 유지 / 조회 시 JPQL 계산 / 혼합)
- 비교 기준: (쓰기 단순성·정합 vs 읽기 비용·bookkeeping 제거)
- 한계 / 다음 망가질 지점:
- 시도→후퇴 흔적:  **(B)**

---

## 테스트 변경 사항 (차원 C)

- 깨진 테스트(waiting slice·중복방지·순번) / 원인 / 수정:

## 피드백 채널 신호 (차원 C)

- N+1을 먼저 알려준 채널(SQL 로그) / 의외였던 추가 쿼리:

## 막힌 지점 → 다음 정의
-

## 얻은 인사이트

- _(A/B/C 태그)_

## 이 단계가 회수한 차원

- A: / B: / C:
