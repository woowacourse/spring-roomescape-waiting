# 0001. `/themes/{id}/schedule`의 `isReservable`은 예약·대기 존재 여부를 함께 반영한다

- 상태: Accepted
- 날짜: 2026-06-02
- 관련 코드: `ThemeDao.findTimeStatusBy`, `ReservationService.validateNoWaiting`, `static/js/user.js`

## Context

Step1에서 예약 대기 기능을 도입하면서 `ReservationService`에 다음 가드를 추가했다.

```java
private void validateNoWaiting(ReservationCommand command) {
    if (waitingDao.existsBySlot(command.date(), command.timeId(), command.themeId())) {
        throw new ConflictException("이미 예약 대기자가 있는 시간입니다, 예약 대기로 신청해주세요.");
    }
}
```

대기자가 줄을 서 있는 슬롯에 다른 사용자가 새 예약을 생성/변경해 끼어드는 것을 막기 위함이다.
Step1 정책상 확정 예약이 취소되어도 **자동 승격은 하지 않는다** — 대기자는 그대로 남아있고, 그 슬롯에 대한 새 예약은 위 가드로 차단된다.

문제: `/themes/{id}/schedule`이 `reservation` 테이블만 LEFT JOIN해서 `isReserved`를 계산했다.

```sql
LEFT JOIN reservation r ON t.id = r.time_id AND r.theme_id = ? AND r.date = ?
-- isReserved = (r.id IS NOT NULL)
```

그래서 "확정 예약 취소됨 + 대기만 남음" 상태의 슬롯은 `isReserved=false`로 응답되었다.
프론트(`user.js`)는 이 값을 그대로 분기에 사용한다.

```js
statusEl.textContent = s.isReserved ? "대기 가능" : "예약 가능";
// ...
if (isReserved) { POST /waitings } else { POST /reservations }
```

결과적으로 사용자는 "예약 가능" 칩을 보고 `/reservations`를 호출하지만, 서버는 `validateNoWaiting`에서 409로 거절한다 — UI와 API 동작이 어긋났다.

## Decision

`/themes/{id}/schedule`의 슬롯 상태는 **"해당 슬롯이 직접 예약 가능한가"** 를 의미하도록 한다.
같은 슬롯에 `reservation` 또는 `waiting`이 하나라도 있으면 예약 불가로 보고, 둘 다 없을 때만 예약 가능으로 응답한다.

`ThemeDao.findTimeStatusBy`의 쿼리를 EXISTS 두 개의 OR로 변경한다.

```sql
CASE
    WHEN EXISTS (SELECT 1 FROM reservation r
                 WHERE r.time_id = t.id AND r.theme_id = ? AND r.date = ?)
      OR EXISTS (SELECT 1 FROM waiting w
                 WHERE w.time_id = t.id AND w.theme_id = ? AND w.date = ?)
    THEN FALSE ELSE TRUE
END AS reservable
```

동시에, 의미 변경을 명시적으로 드러내기 위해 boolean 필드를 **`isReserved` → `isReservable`** 로 리네이밍한다(상태 표현 → capability 표현). 변경 범위:

- DAO: `TimeQueryResult.isReservable`, SQL alias `reservable`, 매퍼 `getBoolean("reservable")`
- Service/Controller DTO: `ReservationTimeDetailResult.isReservable`, `ReservationTimeDetailResponse.isReservable`
- Frontend(`user.js`): `state.selectedTimeIsReservable`, `chip.dataset.reservable`, 칩/버튼/에러 메시지/POST 분기 모두 극성 반전 (`isReservable=true → /reservations, false → /waitings`)

```js
statusEl.textContent = s.isReservable ? "예약 가능" : "대기 가능";
// ...
if (isReservable) { POST /reservations } else { POST /waitings }
```

## Consequences

긍정:
- UI의 칩 상태("예약 가능" / "대기 가능")와 `/reservations` POST 결과가 일치한다.
- "확정 예약 취소 + 대기 잔존" 슬롯이 더 이상 "예약 가능"으로 잘못 보이지 않는다.
- 필드명이 의미("예약 가능 여부")와 일치해 향후 신규 사용처에서 오해의 여지가 줄어든다.
- 극성이 뒤집힌 덕분에 누락된 사용처는 컴파일 또는 런타임에서 즉시 드러난다.

부정/주의:
- 응답 스키마가 변경되어 외부 클라이언트가 있다면 동시 갱신 필요(Step1 시점엔 동봉 프론트가 유일한 소비자).
- "대기까지 마감"처럼 상태가 세 갈래 이상으로 늘어나면 boolean으론 부족하다. 그 시점에 `SlotStatus` enum(AVAILABLE / WAITING_ONLY / FULL) 도입을 고려한다 — 본 ADR의 후속.

## Alternatives considered

1. **상태를 enum으로 분리** (`AVAILABLE` / `WAITING` / `BLOCKED`).
   더 표현력 있지만, Step1에서는 클라이언트가 구분해 그릴 화면 요구가 없고, 응답/프론트/타입까지 한꺼번에 손대야 함. 과한 변경.
2. **자동 승격 도입**으로 "대기만 남는 슬롯" 상태 자체를 없애기.
   Step1 범위 밖이며 별도 의사결정이 필요. 본 이슈와 분리.
3. **프론트에서 `/waitings` 존재를 별도 조회**해 `isReserved`와 합성.
   요청 수 증가, 두 호출 사이 race, 클라이언트 책임 증가. 서버 한 쿼리에서 정합하게 내려주는 편이 낫다.
4. **SQL만 OR로 바꾸고 필드명은 `isReserved` 유지**.
   변경 면적은 가장 작지만 "예약됨"이라는 이름이 "예약 불가"를 가리키게 되어 의미가 어긋난다. capability 의미를 이름에 드러내는 편이 장기적으로 낫다고 판단해 리네이밍을 함께 수행.