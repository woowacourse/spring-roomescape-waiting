# ADR-0007: Reservation은 Slot을 포함하고 명령 검증은 도메인 모델로 수행한다

## 상태

Proposed

## 맥락

기존 `Reservation`은 `slotId`만 가진 가벼운 객체였다.

```java
class Reservation {
    Long id;
    Long memberId;
    Long slotId;
}
```

이 구조에서는 예약이 본인 소유 여부 정도는 판단할 수 있지만, 예약 날짜와 시작 시간을 알 수 없기 때문에 과거 예약인지, 수정 가능한 예약인지 같은 규칙을 스스로 판단할 수 없다.

그 결과 `ReservationService`는 `ReservationDetailProjection`을 조회해 소유자 검증과 과거 예약 검증을 수행했다.

```text
ReservationDetailProjection
-> 소유자 검증
-> 과거 날짜/시간 검증
-> 응답 DTO 변환
```

즉, Projection이 조회 응답용 데이터이면서 명령 검증용 데이터 역할까지 겸하고 있었다. 이는 도메인이 가벼워지고 서비스와 Projection이 비즈니스 규칙을 대신 갖게 만드는 원인이었다.

참고 코드인 `네오/` 프로젝트는 `Reservation`이 `Schedule`, `ReservationTime`, `Theme` 같은 작은 도메인 객체를 포함하고, 각 객체 생성과 협력 과정에서 신뢰도를 높이는 구조를 사용한다. 이 방향을 현재 프로젝트에도 점진적으로 차용하기로 했다.

## 결정

`Reservation`이 `Slot`을 필드로 가지도록 변경한다.

`Slot`은 단순히 `timeId`, `themeId`를 보관하지 않고 `ReservationTime`, `Theme` 도메인 객체를 포함한다.

```text
Reservation
-> Slot
   -> ReservationTime
   -> Theme
```

명령 흐름의 예약 취소/수정 검증은 `ReservationDetailProjection`이 아니라 `Reservation` 도메인을 조회해서 수행한다.

조회 응답을 위한 `ReservationDetailProjection`은 아직 남긴다. 단, 명령 검증에는 사용하지 않는다.

## 대안

### 대안 1: 기존처럼 Reservation이 slotId만 가진다

장점:

- DB 구조와 가장 단순하게 맞는다.
- 저장과 응답 구현이 쉽다.
- 변경 범위가 작다.

단점:

- 예약이 자기 날짜와 시간을 모르므로 과거 예약 여부를 판단할 수 없다.
- 서비스가 Projection 또는 별도 조회 모델을 사용해 검증해야 한다.
- 도메인이 계속 가벼운 데이터 컨테이너로 남는다.

### 대안 2: Reservation이 SlotSnapshot 같은 명령용 조회 모델을 가진다

장점:

- 필요한 값만 담아 변경 범위를 줄일 수 있다.
- Projection보다 명령 검증 목적이 명확해진다.

단점:

- Snapshot이라는 이름과 개념이 도메인 언어로 자연스럽지 않을 수 있다.
- 결국 별도 조회 모델을 도메인처럼 사용하는 구조가 될 위험이 있다.

### 대안 3: Reservation이 Slot을 가지고, Slot이 ReservationTime과 Theme을 가진다

장점:

- 예약이 자기 슬롯 정보를 통해 날짜, 시간, 테마를 알 수 있다.
- Projection 없이 명령 검증을 도메인 모델 중심으로 수행할 수 있다.
- 도메인 객체가 작은 도메인 객체를 포함하며 신뢰도를 높이는 방향으로 갈 수 있다.
- 이후 `Reservation.isPast(...)`, `Reservation.changeSlot(...)` 같은 행위를 추가하기 쉽다.

단점:

- Repository 조회 SQL이 조인을 통해 도메인 객체를 복원해야 한다.
- 테스트에서 도메인 객체 생성이 다소 길어진다.
- `Slot`, `ReservationTime`, `Theme`의 책임도 함께 정리해야 의미가 커진다.

## 근거

이번 리팩토링의 목표는 도메인이 "왜 존재하는가"에 답할 수 있게 만드는 것이다.

`Reservation`이 `slotId`만 가지면 예약의 핵심인 날짜와 시간이 외부에 흩어진다. 그러면 과거 예약 검증, 수정 가능 여부, 같은 슬롯 판단 같은 규칙이 서비스와 Projection에 남게 된다.

반대로 `Reservation`이 `Slot`을 포함하면 예약은 자기 슬롯을 통해 날짜, 시간, 테마 정보를 알 수 있다. 아직 모든 검증을 도메인 내부로 옮긴 것은 아니지만, 명령 검증에서 Projection을 제거할 수 있는 기반이 생긴다.

## 결과

좋아지는 점:

- `ReservationService`의 취소/수정 명령 흐름에서 `ReservationDetailProjection` 의존이 제거된다.
- `ReservationRepository.findById(...)`는 조인 결과를 `Reservation -> Slot -> ReservationTime/Theme` 도메인 구조로 복원한다.
- `Slot`은 더 이상 단순 FK 묶음이 아니라 시간과 테마 도메인을 포함한다.
- `Reservation`이 향후 과거 여부, 변경 가능 여부, 슬롯 변경 같은 행위를 가질 수 있는 구조가 된다.

감수해야 하는 점:

- `ReservationDetailProjection`은 조회 응답용으로 아직 남아 있다.
- `Slot` 생성에는 `ReservationTime`, `Theme`이 필요해 테스트 데이터 생성이 길어진다.
- `Slot`과 `Reservation`이 아직 충분한 검증 행위를 갖지는 않는다.

후속 제약:

- `ReservationDetailProjection`은 조회 전용으로만 사용해야 한다.
- 이후 응답 조회도 도메인 기반으로 전환할지, query model로 분리할지 결정해야 한다.
- `Reservation` 내부로 과거 예약 검증을 옮기려면 현재 시간 값을 도메인 메서드 인자로 전달하는 방식이 필요하다.
- `Theme`, `ReservationTime`도 생성 검증과 정적 팩토리/VO 도입 여부를 별도 결정해야 한다.

## 검증 방법

- 예약 취소/수정 서비스 테스트가 `ReservationRepository.findById(...)` 기반으로 동작하는지 확인한다.
- JDBC 예약 저장소가 `Reservation -> Slot -> ReservationTime/Theme` 구조를 복원하는지 확인한다.
- 전체 테스트를 실행한다.

```bash
./gradlew test
```

## 열린 질문

- `ReservationDetailProjection`을 조회 전용 query result로 이름과 위치를 바꿀지 결정해야 한다.
- `Reservation.isPast(now)`, `Reservation.validateCancelableBy(memberId, now)` 같은 행위를 언제 도입할지 결정해야 한다.
- `Slot`이 `Theme` 전체를 가져야 하는지, 테마 식별자만 가져야 하는지 장기적으로 재검토할 수 있다.
- `ReservationTime`의 `StartTime` VO 도입 여부를 결정해야 한다.

## 관련

- `src/main/java/roomescape/reservation/Reservation.java`
- `src/main/java/roomescape/slot/Slot.java`
- `src/main/java/roomescape/reservation/application/ReservationService.java`
- `src/main/java/roomescape/reservation/infrastructure/JdbcReservationRepository.java`
- `src/main/java/roomescape/slot/infrastructure/JdbcSlotRepository.java`
- `docs/adr/ADR-0003-domain-centered-hexagonal-architecture.md`

