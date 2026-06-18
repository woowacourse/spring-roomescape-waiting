# 05 · 4단계 — 예약 대기 관리 (자동 승인 / 트랜잭션 경계)

> **설계 판단의 벽.** 어드민 대기 관리 + 예약 취소 시 자동 승인. 이 단계가 만드는 고유 관찰 = **flush 순서**.
> ⚠️ **내 코드 특수 상황**: `delete-before-insert` 승급 흐름 + `@Transactional`이 **이미 JDBC로 구현됨** → 1-2에서 JPA로 전환하며 flush 순서 버그도 이미 고침. 그래서 이 단계는 그 흐름의 **flush 순서를 정식 관찰 + 트랜잭션 경계 결정**이 핵심.
> ✅ **상태**: 자동 승인 채택. 코드 변경 0(승급·`flush()` 픽스는 1-2). 전체 green(134). **도달한 만큼을 본다** — 자동까지 완료.

---

## 들어가기 전 자기진단

**Q. 자동 승인 로직을 어디에 둘 것인가? (Service 메서드 / 별도 도메인 이벤트 / 기타) 첫 직감과 근거는?**
> (직감) Service의 `@Transactional` 메서드(`deleteByOwner`) 안. 도메인 이벤트로 분리하면 핸들러가 별도 트랜잭션(`@TransactionalEventListener(AFTER_COMMIT)`)이 되기 쉽고, 그러면 **취소만 커밋된 뒤 승급이 실패하면 빈 슬롯**이 남아 원자성이 깨진다 → 한 트랜잭션 유지. **(B)**

**Q. 트랜잭션 경계는 어디까지 굳혀야 한다고 보나?**
> (직감) 취소+승급을 **한 트랜잭션**. delete-before-insert(취소 DELETE → 같은 슬롯 승급 INSERT)가 한 경계 안이라야 read-your-own-writes로 안전. 단, 경계가 *원자성*은 줘도 *flush 순서*는 따로(③).

---

## 요구사항 (LMS)

- **어드민**: 대기 목록 조회·취소 / **승인 (택1)**: 수동 / 자동(취소 시 다음 대기를 예약으로 전환) → **자동 채택**.

## 전환 전 (JDBC) → 전환 후 (JPA)

- **전환 전**: 취소 시 같은 슬롯의 다음 `order_index` 대기를 찾아 예약으로 전환. JDBC가 DELETE를 **즉시 실행**해 delete-before-insert를 손으로 보장.
- **전환 후**(1-2): 같은 흐름을 엔티티 조작으로 —
```java
@Transactional
public void deleteByOwner(Long id, String name) {
    Reservation reservation = findByIdAndName(id, name);
    reservationPolicy.validateCancellable(reservation.dateTime());
    reservationRepository.deleteById(id);          // 취소 (어댑터에서 deleteById 후 flush())
    promoteFirstWaitingIfExists(reservation);      // 승급
}
// promote: findBySlot → save(Reservation.promote(first)) → waiting deleteById → reorder(updateOrderIndex)
```

---

## 본질 신호 — 자동 승인 시 만나야 할 것 (LMS)

### ① 트랜잭션 경계

- `deleteByOwner` **한 `@Transactional`** 안에 취소+승급 전부 → **원자성**(승급 실패 시 취소까지 롤백, all-or-nothing). 도메인 이벤트(AFTER_COMMIT)로 분리하면 취소만 커밋·승급 실패 시 빈 슬롯 → 일관성 붕괴. **한 경계 유지.**
- **핵심**: 트랜잭션 경계는 *원자성*을 주지만 ***flush 순서는 보장하지 않는다*** (③). **(B)**

### ② 동시성 (명시적 비결정)

- 같은 슬롯에 두 사용자가 동시에 취소·승급하면 `order_index` 재번호 경쟁 / 중복 승급 가능.
- **의도적 deferred**: 이번 사이클은 *JdbcTemplate→JPA 전환*이 범위. 락/격리는 **Level 3 동시성 아크**(레이스 재현 → 비관적 락 → 격리 수준 비교)로 분리해 백로그에 명시. 현재는 `UNIQUE(date,time_id,theme_id)`·`UNIQUE(...,order_index)`가 **최종 방어선**으로 최악(중복 예약)은 DB가 막음. "지금 안 하는 이유"를 결정으로 남김. **(B)**

### ③ flush 순서 ⭐ 차원 A (이 단계 고유 관찰)

```
시도 코드:  한 @Transactional에서 취소(reservation delete) + 승급(reservation insert + waiting delete + reorder)
예측:       delete-before-insert가 SQL에서 보장되나? JPA 기본 flush는 INSERT→UPDATE→DELETE인데?
실제:       아래 순서 (deleteByOwner 한 번)
왜 다른가:  액션큐 기본 순서(INSERT→UPDATE→DELETE)면 ②INSERT가 ①DELETE보다 먼저 → 같은 슬롯 UNIQUE 충돌
```
발행 순서:
```
SELECT reservation        -- findByIdAndName (R 로드)
SELECT reservation_time   -- LAZY time (validateCancellable → dateTime())
DELETE reservation        -- ① 취소 (deleteById + 어댑터의 flush())  ← 먼저!
SELECT waiting            -- findBySlot (같은 슬롯 대기들)
INSERT reservation        -- ② 승급 (Reservation.promote, IDENTITY save = 즉시)
DELETE waiting           -- ③ 승급된 대기 제거
UPDATE waiting           -- ④ 남은 대기 재번호(reorderAfterRemoval)
```
- **핵심 인사이트**: **트랜잭션 경계(원자성) ≠ flush 순서(쓰기 발행 순서).** 한 `@Transactional` 안이어도 순서는 **JPA 액션큐 규칙(INSERT→UPDATE→DELETE)**을 따른다. 내 의도 순서(delete-before-insert)는 **`deleteById` 후 명시적 `flush()`로만** 강제된다(1-2에서 `ConstraintViolation`으로 터지고 이 픽스로 해결한 그 현상).
- `@GeneratedValue(IDENTITY)`의 **즉시 INSERT**(1-3 ③)가 충돌을 악화 — INSERT를 flush까지 못 미루므로, 명시 flush로 DELETE를 앞세우지 않으면 곧장 부딪힌다.

---

## 확인 과제

**Q. 자동 승인 로직의 위치는?**
> **자동 승인 채택** — Service의 단일 `@Transactional` 메서드 `deleteByOwner`(취소 → `promoteFirstWaitingIfExists`) 안. 수동(승인 버튼) 아님.

**Q. 그 결정의 한계는? 트랜잭션 경계·동시성·일관성 중 가장 약한 것은?**
> **동시성**이 가장 약하다(의도적 deferred). 단일 사용자 기준 경계(원자성)·일관성(delete-before-insert flush)은 견고하나, **다중 사용자 동시 취소·승급**은 미보호 — `UNIQUE` 제약이 *최악(중복 예약)*만 막고, 재번호 경쟁·중복 승급 시도는 락 없이는 못 막는다. **(B)**

---

## 결정 기록 (인라인)

### 결정 — 자동 승인 위치 & 트랜잭션 경계

- **선택한 것**: 자동 승인 / Service 단일 `@Transactional`(`deleteByOwner`) 안 + `deleteById` 후 명시적 `flush()`로 delete-before-insert 강제.
- **비교한 대안**: Service 한 메서드(채택) / 도메인 이벤트(AFTER_COMMIT 분리 트랜잭션) / 분리 트랜잭션.
- **비교 기준**: 원자성(취소+승급 all-or-nothing) + flush 순서 통제. 도메인 이벤트 분리는 원자성이 깨지고(취소만 커밋), flush 순서 통제권도 흩어짐.
- **한계 / 가장 약한 축**: 동시성(다중 사용자).
- **명시적 비결정(동시성·락 deferred 근거)**: JPA 전환이 이번 사이클 범위. 락/격리는 Level 3 동시성 아크로 분리(레이스 재현→비관적 락→격리 비교)해 백로그에 명시. **(B)**

## 테스트 변경 사항 (차원 C)

- **코드 변경 0**(자동 승인·`flush()` 픽스는 1-2). 승급 **롤백 테스트**(`@MockitoSpyBean` fault injection on `WaitingRepository` *포트*)는 어댑터를 spy하므로 LAZY/`@EntityGraph` 전환에도 **무영향 → green(134)**. 이 테스트가 1-2에서 먼저 `ConstraintViolation`으로 깨졌다 `flush()`로 고쳐진 그 테스트(02 문서 참조).

## 피드백 채널 신호 (차원 C)

- **flush 순서를 SQL 로그가 직접 보여줌**: `DELETE(reservation) → INSERT(reservation) → DELETE(waiting) → UPDATE(waiting)`.
- **의외였던 것**: *트랜잭션 경계가 발행 순서를 정하지 않는다* — 순서는 액션큐가 정하고, 내 의도는 명시 flush로만 강제. JDBC에서 당연했던 "내가 쓴 순서대로 실행"이 JPA에선 보장이 아니었다.

## 막힌 지점 → 다음 정의

- 막힘: 동시 취소·승급의 **재번호 경쟁 / 중복 승급**.
- 다음 정의: Level 3 동시성 아크 — 레이스 재현 → 비관적 락(같은 슬롯 다중 행 전환) → 격리 수준 비교. (이번 미션 범위 밖, 백로그.)

## 얻은 인사이트

- (A) 자동 승인의 발행 순서는 `DELETE→INSERT→DELETE→UPDATE`이며, 이 delete-before-insert는 **트랜잭션 경계가 아니라 명시적 flush**가 만든다. (A)
- (B) **트랜잭션 경계(원자성) ≠ flush 순서(가시성 순서)** — 둘은 다른 축. 원자성은 `@Transactional`이, 순서는 flush가. (B)
- (B) 동시성은 명시적으로 *지금 다루지 않는다*고 적는 것이 결정 — UNIQUE가 최악만 방어함을 함께 기록. (B)

---

## 📌 단일 PR 본문 재료 (07로 합침)

- **도달 지점(한 줄)**: `step2`의 JdbcTemplate 기반 방탈출 예약/대기 시스템을 **헥사고날 구조를 유지한 채 JPA로 전환 완료**(1~4단계) — 엔티티 매핑·단방향 `@ManyToOne`·영속성 컨텍스트 6관찰·N+1 fetch join·순번 결정·자동 승인 flush 순서까지.
- **시작/작업 브랜치·범위**: 시작 `step2` → 작업 `step3-jpa`. 건드린 범위 = `adapter/persistence`(어댑터·JpaRepository) + `domain`(엔티티 4종에 매핑 어노테이션) + 설정(`create-drop`·로깅). **API 명세·도메인 정책·예외 계층은 불변**(재구현 금지 준수).
- **망설인 결정 1~2**: ① **fetch 전략** — EAGER 기본값을 1-2/1-3 내내 일부러 유지(관찰용)했다가 3-1에서 LAZY+`@EntityGraph`로 전환(기본값→문제→해결). ② **순번** — `order_index` 유지 vs JPQL rank, 둘 다 실행 후 유지로 후퇴(UNIQUE 방어선·승급 통합).
- **발행 SQL 발췌(가장 강한 예측-실제 갭)**: ⓐ `findByName`의 N+1(같은 EAGER인데 by-id는 join fetch, 쿼리는 연관별 보조 SELECT) → `@EntityGraph`로 8→2. ⓑ 자동 승인 flush 순서(DELETE-before-INSERT는 명시 flush로만). ⓒ IDENTITY persist=즉시 INSERT.
- **흔들린 한 장면**: LAZY 전환 직후 `ReservationServiceTest` 3건이 `LazyInitializationException` — 1-3 ⑥에서 "reads tx-free + LAZY면 터진다"고 예고한 그 좌표가 실제 read 경로에서 터지고, `@EntityGraph`로 닫은 순간. OSIV가 admin 경로만 가려준 것도 그때 드러남.

## 이 단계가 회수한 차원

- A: 자동 승인 flush 순서(DELETE→INSERT→DELETE→UPDATE) 실측 / 트랜잭션 경계≠flush 순서.
- B: 단일 `@Transactional`+명시 flush 결정 / 동시성 deferred 명시.
- C: 코드 변경 0·green(134) / flush 순서를 로그가 가르침 / 승급 롤백 테스트 유지.
