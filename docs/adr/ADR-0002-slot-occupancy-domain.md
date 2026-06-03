# ADR-0002: 슬롯의 예약/대기 가능 상태를 SlotOccupancy 도메인으로 표현한다

## 상태

Proposed

## 맥락

현재 대기 생성 흐름에서는 슬롯에 예약 또는 대기가 존재하는지 서비스가 직접 조회하고, boolean 조건문으로 대기 가능 여부를 판단한다.

기존 `WaitingService`의 판단은 다음과 같았다.

```java
if (!reservationRepository.existsBySlotId(slotId)
        && !waitingRepository.existsBySlotId(slotId)) {
    throw new EscapeRoomException(ErrorCode.WAITING_TARGET_BAD_REQUEST);
}
```

이 조건문은 "예약도 대기도 없는 슬롯에는 대기를 신청할 수 없다"는 정책을 표현하지만, 정책의 이름이 코드에 직접 드러나지 않는다.

또한 같은 슬롯 상태는 예약 생성/수정에서도 반대 방향으로 사용된다.

```text
예약 가능: 예약 없음 && 대기 없음
대기 가능: 예약 있음 || 대기 있음
```

따라서 예약/대기 존재 여부 조합을 서비스 조건문에 두기보다, 슬롯의 점유 상태를 표현하는 도메인 객체로 분리할 필요가 있었다.

## 결정

`SlotOccupancy` 도메인 객체를 추가한다.

`SlotOccupancy`는 슬롯에 예약이 있는지, 대기가 있는지를 값으로 받아 다음 정책을 판단한다.

- `isReservable()`: 예약과 대기가 모두 없으면 예약 가능하다.
- `isWaitable()`: 예약 또는 대기가 있으면 대기 가능하다.

저장소 조회는 여전히 애플리케이션 서비스가 수행한다. 도메인은 Repository를 알지 않고, 서비스가 전달한 상태 값으로만 판단한다.

이번 단계에서는 `WaitingService.validateWaitingTargetExists(...)`에만 먼저 적용한다. `ReservationService` 적용은 다음 단계에서 별도로 진행한다.

## 대안

### 대안 1: 서비스 조건문을 유지한다

장점:

- 새 객체를 만들 필요가 없다.
- 현재 코드 변경량이 가장 적다.

단점:

- 예약 가능/대기 가능 정책이 서비스 조건문에 숨어 있다.
- 예약 서비스와 대기 서비스에서 같은 슬롯 상태 판단이 중복될 가능성이 크다.
- 정책 변경 시 여러 서비스 조건문을 함께 수정해야 한다.

### 대안 2: 서비스 private 메서드로 이름만 부여한다

예시:

```java
private boolean isWaitableSlot(long slotId) {
    return reservationRepository.existsBySlotId(slotId)
            || waitingRepository.existsBySlotId(slotId);
}
```

장점:

- 조건문보다 의미가 명확해진다.
- 새 도메인 객체를 만들지 않아 구현 비용이 낮다.

단점:

- 여전히 서비스가 DB 조회와 정책 판단을 모두 담당한다.
- 예약 가능 판단과 대기 가능 판단을 한 도메인 개념으로 묶지 못한다.
- 다른 서비스에서 재사용하기 어렵다.

### 대안 3: `SlotOccupancy` 도메인 객체를 둔다

장점:

- 슬롯 상태 정책이 `isReservable()`, `isWaitable()`이라는 도메인 언어로 드러난다.
- 서비스는 필요한 데이터를 조회하고, 판단은 도메인에 위임할 수 있다.
- 예약 생성/수정 흐름에서도 같은 객체를 재사용할 수 있다.
- 단위 테스트로 슬롯 상태 정책을 독립적으로 검증할 수 있다.

단점:

- 현재는 boolean 두 개를 감싸는 작은 객체라 과해 보일 수 있다.
- 슬롯 상태 정책이 복잡해지지 않는다면 객체의 이점이 제한적일 수 있다.
- 모든 요청에 억지로 적용하면 불필요한 추상화가 될 수 있다.

## 근거

이 프로젝트의 리팩토링 목표 중 하나는 빈약한 도메인을 개선하는 것이다.

슬롯이 예약 가능한지, 대기 가능한지는 Spring, HTTP, JDBC와 무관한 방탈출 예약 도메인의 정책이다. 따라서 서비스 조건문보다 도메인 객체가 표현하는 것이 적절하다.

다만 도메인이 저장소를 직접 조회하면 헥사고날 아키텍처의 의존 방향을 깨뜨릴 수 있다. 그래서 `SlotOccupancy`는 Repository를 가지지 않고, 서비스가 조회한 결과를 생성자 입력으로만 받는다.

현재 변경 범위는 `WaitingService`로 제한했다. 이는 새 도메인 객체가 실제로 의미 있는지 작은 범위에서 검증하고, 이후 `ReservationService` 적용 여부를 별도로 판단하기 위함이다.

## 결과

좋아지는 점:

- 대기 가능 여부 판단이 `slotOccupancy.isWaitable()`로 명확해졌다.
- 슬롯 상태 정책을 `SlotOccupancyTest`에서 독립적으로 검증할 수 있게 되었다.
- 이후 예약 생성/수정 흐름에서도 `isReservable()`을 사용할 수 있는 기반이 생겼다.

감수해야 하는 점:

- `WaitingService`는 여전히 예약 존재 여부와 대기 존재 여부를 각각 조회한다.
- `ReservationService`에는 아직 기존 조건문이 남아 있다.
- `SlotOccupancy`가 현재는 작은 값 객체이므로, 남용하면 불필요한 추상화가 될 수 있다.

후속 제약:

- `SlotOccupancy`는 예약/대기 가능 상태 판단이 필요한 유스케이스에만 적용한다.
- 대기 취소나 내 예약 조회처럼 슬롯 가능 상태 판단이 필요 없는 요청에는 적용하지 않는다.
- 사이클2의 예약 취소 후 대기 전환은 `SlotOccupancy`보다 `WaitingLine` 같은 대기열 도메인이 더 적합할 수 있다.

## 검증 방법

- `SlotOccupancyTest`에서 예약 가능/대기 가능 정책을 검증한다.
- `WaitingServiceTest`에서 기존 대기 생성 정책이 유지되는지 검증한다.
- 전체 테스트를 실행한다.

```bash
./gradlew test
```

## 열린 질문

- `ReservationService.save(...)`와 `ReservationService.updateInternal(...)`에도 `SlotOccupancy.isReservable()`을 적용할지 결정해야 한다.
- `ReservationTimeService`의 `TimeSlotStatus` 변환에도 같은 정책을 재사용할지 결정해야 한다.
- `SlotOccupancy`를 현재 `roomescape.slot` 패키지에 둘지, 헥사고날 전환 시 `roomescape.slot.domain`으로 이동할지 결정해야 한다.

## 관련

- `src/main/java/roomescape/slot/SlotOccupancy.java`
- `src/test/java/roomescape/slot/SlotOccupancyTest.java`
- `src/main/java/roomescape/waiting/application/WaitingService.java`
- `src/test/java/roomescape/waiting/WaitingServiceTest.java`
- `docs/refactoring-plan.md`

