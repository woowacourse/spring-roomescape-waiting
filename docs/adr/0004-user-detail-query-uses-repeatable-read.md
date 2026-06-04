# 0004. 사용자 예약/대기 통합 조회는 REPEATABLE_READ 격리 수준을 사용한다

- 상태: Accepted
- 날짜: 2026-06-04
- 관련 코드: `ReservationService.findReservationDetailsByUserName`, `ReservationDao.findAllByUserName`, `WaitingDao.findAllByUserName`

## Context

`findReservationDetailsByUserName(userName)`은 사용자 본인의 예약 + 예약 대기를 통합해 화면에 내려주는 메서드다. 흐름은 단순하다.

```java
List<Reservation> reservations = reservationDao.findAllByUserName(userName);
List<WaitingQueryResult> waitings = waitingDao.findAllByUserName(userName);
// 두 결과를 합쳐 반환
```

여기서 의문이 떠올랐다.

> 예약을 조회한 후 이제 예약 대기를 조회하기 전에 내 예약 대기가 예약으로 승격되어 버린다면? 예약 대기에서 내 예약 대기 목록은 사라지고, 예약 목록에서도 내 승격 예약이 없기 때문에 정합성 문제가 터진다.

기본 격리 수준 `READ_COMMITTED`에서는 SELECT가 *statement 단위 스냅샷*을 읽기 때문에, 두 DAO 호출 사이에 다른 트랜잭션(특히 ADR-0002·0003에서 도입한 자동 승격)이 commit하면 두 결과가 서로 다른 시점의 상태를 반영하게 된다.

```
시점       T1 (사용자 A의 조회)                  T2 (A의 W1이 promote됨)
─────────────────────────────────────────────────────────────────────────────
t0         BEGIN
t1         reservationDao.findAllByUserName(A)
           → A의 예약 결과: [ ] (없음)
t2                                                 다른 사용자가 같은 슬롯 cancel
                                                  → promote: A의 W1을 reservation으로
                                                  → COMMIT
t3         waitingDao.findAllByUserName(A)
           → A의 대기 결과: [ ] (W1 이미 승격되어 사라짐)
t4         반환: { reservations: [], waitings: [] }
─────────────────────────────────────────────────────────────────────────────
결과: A는 화면에서 "예약 0건, 대기 0건"으로 봄.
실제 DB 상태는 reservation 1건(승격된 새 row) + waiting 0건.
→ A 입장: "내 대기가 사라졌는데 예약도 안 보임. 내 데이터 어디 갔어?"
```

두 호출이 *물리적으로는* 한 트랜잭션 안이지만 *논리적 일관성*은 깨지는 race다.

## Decision

`findReservationDetailsByUserName`에 메서드 레벨 `@Transactional(isolation = Isolation.REPEATABLE_READ)`를 명시한다.

```java
@Transactional(isolation = Isolation.REPEATABLE_READ)
public ReservationDetailResults findReservationDetailsByUserName(String userName) {
    List<Reservation> reservations = reservationDao.findAllByUserName(userName);
    List<WaitingQueryResult> waitings = waitingDao.findAllByUserName(userName);
    // ...
}
```

클래스 레벨 `@Transactional(readOnly = true)`(기본 격리 수준 = `READ_COMMITTED`)를 메서드 레벨에서 격리 수준만 한 단계 올려 오버라이드한 형태다. `readOnly`는 그대로 유지된다.

## Rationale

`REPEATABLE_READ`는 트랜잭션 단위 스냅샷을 보장한다. 트랜잭션이 시작되는 순간 시점이 고정되고, 그 트랜잭션 안에서 호출되는 모든 SELECT는 같은 스냅샷을 본다. 다른 트랜잭션이 중간에 commit해도 우리 트랜잭션의 view는 영향받지 않는다.

위 race 시나리오에 적용하면:

```
t0   T1 BEGIN (스냅샷 시점 고정 — W1이 waiting에 있는 상태)
t1   T1: reservation 조회 → 스냅샷 시점 기준 결과
t2   T2: promote commit ← T1의 view에는 보이지 않음
t3   T1: waiting 조회 → 여전히 t0 스냅샷 기준 → W1 보임
t4   T1 반환: { reservations: [...], waitings: [W1] }
```

T1의 두 SELECT가 항상 같은 시점의 상태를 보므로 모순이 사라진다. 사용자 입장에서는 *조금 옛날 스냅샷*을 볼 수는 있어도 데이터가 모순된 상태로는 비치지 않는다.

### 비용 — 최신 변경에 대한 일관성

다른 트랜잭션이 commit한 *최신 변경*에 대해서는 우리 트랜잭션이 끝날 때까지 view가 늦게 갱신된다. 즉 사용자에게 살짝 옛 상태가 보일 수 있다.

다만 이 메서드의 트랜잭션은 DAO 두 호출, 수 밀리초로 매우 짧으므로 stale window가 사용자 경험에 큰 영향을 주지 않는다. 사용자가 다음 요청으로 새로고침하면 즉시 갱신된 스냅샷을 보게 된다.

핵심 trade-off:
- **모순 상태가 화면에 보임** (격리 수준 안 올림) → 사용자가 패닉
- **살짝 옛 상태가 보임** (REPEATABLE_READ) → 다음 요청에서 해소

후자가 받아들일 만한 비용이라고 판단했다.

### 미해결 — SERIALIZABLE까지 갈 필요는 없는가?

격리 수준 후보로 `READ_COMMITTED` / `REPEATABLE_READ` / `SERIALIZABLE` 세 가지가 있다. 본 결정 시점에는 `SERIALIZABLE`의 정확한 동작과 비용을 본인의 사고로 풀어내지 못했고, "더 강한 격리 수준은 동시성 비용이 크다"는 일반론에 의지해 `REPEATABLE_READ`로 멈췄다.

추후 학습 영역: SERIALIZABLE이 read에도 락을 거는지, phantom을 어떻게 다루는지, 본 메서드에 적용했을 때 다른 트랜잭션(특히 promote)을 어디까지 차단하는지를 정확히 검토하면, 본 결정의 근거가 더 단단해진다.

## Consequences

긍정:
- 사용자 화면에 모순 상태("내 대기·예약이 둘 다 사라진 듯 보임")가 비치지 않는다.
- 두 DAO 호출의 합성 결과가 항상 한 시점의 일관된 상태를 반영한다.

부정/주의:
- 다른 트랜잭션이 commit한 최신 변경이 우리 트랜잭션 동안 view에 반영되지 않는다. 트랜잭션이 짧아 실질 영향은 작지만, "최신성"을 약간 양보한 결정임을 인지한다.
- `REPEATABLE_READ`의 정확한 동작은 RDB마다 다르다. 본 결정은 H2 기준이며 다른 RDB로 옮기는 경우 동작 검증이 필요하다.
- 격리 수준 명시는 이 메서드 단위에 머문다. 향후 비슷한 "두 DAO 합성" 메서드가 추가되면 같은 처리를 명시적으로 해주어야 한다.

## Alternatives considered

1. **`READ_COMMITTED` 유지 + 한 SQL로 합치기.** `reservation`과 `waiting`을 `UNION ALL`로 단일 SQL에서 조회하면 한 statement 안이라 race가 사라진다. 다만 두 도메인 객체의 매핑이 한 RowMapper에 섞이며 가독성·유지보수성이 떨어진다. 현재의 "각 DAO가 자기 도메인을 반환" 컨벤션을 깬다.

2. **SERIALIZABLE.** race 제거 효과는 동일하나 일반적으로 동시성 비용이 더 크다고 알려져 있다. 정확한 비교는 위 "미해결" 항목에 남긴다.