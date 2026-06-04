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

문제는 **두 DAO 호출이 별개의 SELECT 두 번**이라는 점이다. 기본 격리 수준인 `READ_COMMITTED`에서는 SELECT가 *statement 단위 스냅샷*을 읽으므로, 두 SELECT 사이에 다른 트랜잭션이 commit하면 **두 결과가 서로 다른 시점의 상태**를 반영하게 된다.

특히 ADR-0002·0003에서 도입한 **예약 대기 자동 승격**이 이 사이에 끼어드는 경우가 위험하다.

### 실제로 발생할 수 있는 race

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

반대 방향의 race도 가능하다.

- t1에 W1이 보이고, t2에서 promote → t3에 새 reservation도 같이 보임 → **A 화면에 같은 슬롯의 대기와 예약이 동시에 보임**. A 입장: "내가 예약자이면서 동시에 대기자네?"

방향이 어떻든 본질은 같다: **두 SELECT가 서로 다른 시점의 스냅샷을 봐서 사용자 화면에 모순된 상태가 비친다.** 두 호출이 *물리적으로는* 한 트랜잭션 안이지만 *논리적 일관성*은 깨진다.

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

클래스 레벨 `@Transactional(readOnly = true)`(기본 격리 수준 = `READ_COMMITTED`)를 메서드 레벨에서 **격리 수준만 한 단계 올려** 오버라이드한 형태다. `readOnly`는 그대로 유지된다.

## Rationale

`REPEATABLE_READ`는 트랜잭션 단위 스냅샷을 보장한다. 즉 트랜잭션이 시작되는 순간 시점이 고정되고, 그 트랜잭션 안에서 호출되는 모든 SELECT는 **같은 스냅샷**을 본다. 다른 트랜잭션이 중간에 commit해도 우리 트랜잭션의 view는 영향받지 않는다.

위 race 시나리오에 적용하면:

```
t0   T1 BEGIN (스냅샷 시점 고정 — W1이 waiting에 있는 상태)
t1   T1: reservation 조회 → 스냅샷 시점 기준 결과
t2   T2: promote commit ← T1의 view에는 보이지 않음
t3   T1: waiting 조회 → 여전히 t0 스냅샷 기준 → W1 보임
t4   T1 반환: { reservations: [...], waitings: [W1] }
```

T1의 두 SELECT가 항상 같은 시점의 상태를 보므로 모순이 사라진다. 사용자 입장에서는 *조금 옛날 스냅샷*을 볼 수는 있어도 **데이터가 모순된 상태로는 절대 비치지 않는다**.

### 왜 READ_COMMITTED로 부족한가

`READ_COMMITTED`는 **statement-level 스냅샷**이라 같은 트랜잭션 안에서 호출되는 SELECT가 각각 다른 시점의 스냅샷을 본다. 두 DAO 호출 사이의 시간 차에서 race가 그대로 발생한다.

### 왜 SERIALIZABLE까지 안 가는가

`SERIALIZABLE`은 race를 막아주지만 **비용이 크다.**

| | REPEATABLE_READ (H2 MVCC) | SERIALIZABLE |
|---|---|---|
| 같은 row 두 번 읽기 일관성 | ✅ 보장 | ✅ 보장 |
| phantom (조회 중 새 row INSERT 보일 수 있음) | △ 일부 발생 가능 | ✅ 막음 |
| 일반 SELECT가 다른 트랜잭션의 write 차단 | ❌ 차단 안 함 | ✅ 차단함 |
| 동시성/성능 | 보통 | 낮음 |

`SERIALIZABLE`은 *우리의 read가 다른 트랜잭션의 write까지 차단*하기 시작한다. 즉 사용자가 자기 데이터를 조회하는 동안 다른 사용자의 promote/취소가 우리 트랜잭션이 commit할 때까지 대기하게 된다. 시스템 전체 동시성이 깎인다.

우리가 막고 싶은 race는 "*내 트랜잭션 안의 두 SELECT 일관성*"이고, 이건 트랜잭션 단위 스냅샷만으로 정확히 해결된다. phantom은 우리 케이스에서 모순을 만들지 않는다 — 조회 중 새 waiting이 들어와 보일 수 있다는 정도이며, 정합성이 아니라 *최신성*의 문제다.

> **격리 수준은 "딱 필요한 만큼"만 올린다. 자기 데이터 조회 메서드에 시스템 전체 동시성 비용을 깎을 가치는 없다.**

### 핵심 멘탈 모델

> **"내부 일관성을 얻기 위해 외부 최신성을 살짝 포기한다."**
>
> 우리 트랜잭션 *내부*에서는 일관성이 강화되고(두 SELECT가 같은 시점), 우리 트랜잭션 *외부*의 변경(다른 트랜잭션의 commit)은 늦게 본다. 이게 REPEATABLE_READ의 본질이다.

## Consequences

긍정:
- **사용자 화면에 모순 상태가 비치지 않는다.** "내 대기·예약이 동시에 사라진 것처럼 보임" / "같은 슬롯에 예약자이면서 대기자로 보임" 같은 race가 제거된다.
- **두 DAO 호출의 합성 결과가 항상 한 시점의 일관된 상태**를 반영한다. 호출 측은 결과 합산을 안심하고 가공할 수 있다.
- **SERIALIZABLE 대비 동시성 비용이 거의 없다.** 일반 SELECT는 다른 트랜잭션의 write를 차단하지 않는다 (ADR-0003의 "일반 조회는 X-lock 영향 안 받음" 보장이 이 메서드에도 그대로 적용된다).

부정/주의:
- **Stale read를 살짝 허용한다.** 트랜잭션 시작 후 다른 트랜잭션이 commit한 변경은 우리 트랜잭션이 끝날 때까지 우리 view에 보이지 않는다. 트랜잭션이 매우 짧으므로(DAO 두 호출, 수 밀리초) 사용자가 다음 요청에서 즉시 갱신된 상태를 보게 되어 실질 영향은 무시할 수 있다. **"내부 일관성"과 "외부 최신성"의 trade-off 중 전자를 선택한 결과**.
- **MVCC 버전 보존 비용**이 약간 발생한다. DB가 트랜잭션 동안 옛 버전 데이터를 유지해야 하나, 트랜잭션 길이가 짧아 무시 가능한 수준.
- **이식성에 주의가 필요하다.** `REPEATABLE_READ`의 정확한 동작은 RDB마다 다르다. 특히 phantom 처리, lock 동작은 H2 / MySQL / PostgreSQL이 모두 미묘하게 다르다. 다른 RDB로 옮기는 경우 동작 검증이 필요하다.
- **격리 수준 명시는 메서드 단위에 머문다.** 클래스 레벨의 `@Transactional(readOnly = true)`는 그대로 두고 이 메서드만 격리 수준을 올리는 형태라, 다른 조회 메서드는 영향받지 않는다. 향후 비슷한 "두 DAO 합성" 메서드가 추가되면 같은 처리를 명시적으로 해주어야 한다.

## Alternatives considered

1. **READ_COMMITTED 유지 + 한 SQL로 합치기.** `reservation`과 `waiting`을 `UNION ALL`로 단일 SQL에서 조회. 한 statement 안이라 statement-level 스냅샷으로도 race가 없어진다. 다만 두 도메인 객체의 매핑이 한 SQL/RowMapper에 섞이며 가독성·유지보수성이 떨어진다. 또한 두 테이블의 컬럼 형태가 달라 DTO 구조가 강제로 평탄화된다. 현재의 "각 DAO가 자기 도메인을 반환" 컨벤션을 깬다.

2. **SERIALIZABLE.** race 제거는 동일하나 다른 트랜잭션의 write까지 차단해 시스템 전체 동시성을 깎는다. 단순 자기 데이터 조회용 메서드에 들이기엔 비용이 크다.

3. **격리 수준은 그대로 두고 애플리케이션에서 결과를 한 번 더 정합화.** 예: reservation 조회 후 waiting 조회, 둘 사이에 변화가 감지되면 retry. 구현이 복잡하고 retry 정책 자체가 또 race를 만든다. DB가 한 줄로 처리하는 일을 애플리케이션이 다시 만들 이유가 없다.