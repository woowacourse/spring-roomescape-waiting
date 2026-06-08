# ADR-0005: 도메인 리팩토링은 예약/대기/슬롯 중심으로 우선 진행한다

## 상태

Proposed

## 맥락

현재 프로젝트에는 도메인으로 보기 애매하거나 책임 위치가 불명확한 코드가 여럿 존재한다.

대표적으로 다음 문제가 있다.

- DTO가 도메인 객체를 직접 생성하는 `toDomain()` 메서드를 가진다.
- `ReservationService`가 `ReservationDetailProjection`으로 소유자 검증을 수행한다.
- `ReservationTimeService`가 예약/대기 존재 여부를 `timeId` 집합으로 조합해 시간 상태를 판단한다.
- 예외 계층이 도메인, 애플리케이션, 웹 응답 성격을 함께 가진다.
- 일부 도메인 객체는 정적 팩토리 메서드가 생겼지만 생성자가 여전히 열려 있다.

다만 모든 문제를 한 번에 수정하면 사이클2 구현 전 리팩토링 범위가 과도하게 커진다. 지금의 핵심 목표는 예약/대기 전환 요구사항을 수용할 수 있도록 예약, 대기, 슬롯 주변의 도메인 책임을 먼저 세우는 것이다.

## 결정

리팩토링 우선순위를 예약/대기/슬롯 중심으로 제한한다.

지금 바로 수행하는 작업:

- `WaitingLine`이 같은 슬롯의 대기만 받을 수 있도록 검증한다.
- `Slot.create(...)`, `Slot.of(...)`를 추가해 새 슬롯 생성과 기존 슬롯 복원 의도를 구분한다.
- `SlotSaveRequest.toDomain()`을 제거하고 `SlotService`에서 `Slot.create(...)`를 사용한다.

보류하는 작업:

- 예외 계층 정리
- `ReservationService`의 Projection 기반 소유자 검증 정리
- `ReservationTimeService`의 시간 상태 판단 구조 변경
- `ThemeSaveRequest.toDomain()`, `ReservationTimeSaveRequest.toDomain()` 제거
- 생성자 접근 제한을 통한 정적 팩토리 강제

## 대안

### 대안 1: 발견한 모든 설계 문제를 한 번에 수정한다

장점:

- 전체 코드 일관성을 빠르게 높일 수 있다.
- DTO, 예외, Projection, 생성 정책을 한 번에 정리할 수 있다.

단점:

- 변경 범위가 커져 회귀 위험이 높다.
- 사이클2 구현 전에 리팩토링 자체가 목적화될 수 있다.
- 각 결정의 이유와 효과를 학습하기 어렵다.

### 대안 2: 예약/대기/슬롯 중심으로 작은 단위만 수정한다

장점:

- 사이클2의 대기 전환 흐름과 직접 연결되는 영역부터 개선할 수 있다.
- 테스트로 검증 가능한 작은 변경을 유지할 수 있다.
- 도메인 객체가 왜 존재하는지 하나씩 확인하며 학습할 수 있다.

단점:

- 한동안 코드 스타일과 책임 위치가 부분적으로 혼재된다.
- `Theme`, `ReservationTime` 등 다른 영역의 DTO 변환 문제는 남는다.
- 예외 계층 정리가 뒤로 밀린다.

## 근거

현재 가장 중요한 도메인 흐름은 예약, 대기, 슬롯이다.

사이클2 요구사항은 예약 취소와 대기 전환, 대기 순번 재정렬을 다룬다. 따라서 `WaitingLine`, `SlotOccupancy`, `Slot` 같은 객체의 의미를 먼저 명확히 하는 것이 가장 직접적인 리팩토링 효과를 낸다.

반면 예외 계층이나 Projection 기반 검증은 중요하지만 영향 범위가 넓다. 지금 다루면 도메인 설계 학습보다 구조 전환 작업이 커질 수 있다. 따라서 별도 단계로 분리한다.

## 결과

좋아지는 점:

- `WaitingLine`은 같은 슬롯의 대기열이라는 의미를 더 명확히 가진다.
- `SlotSaveRequest`는 더 이상 도메인 객체를 생성하지 않는다.
- `SlotService`가 슬롯 생성 유스케이스의 조립 책임을 갖는다.
- `Slot.create(...)`, `Slot.of(...)`로 생성 의도를 구분할 기반이 생긴다.

감수해야 하는 점:

- `ThemeSaveRequest`, `ReservationTimeSaveRequest`에는 아직 `toDomain()`이 남아 있다.
- 생성자가 public이라 정적 팩토리 사용이 강제되지는 않는다.
- 예외는 여전히 기존 `EscapeRoomException`, `ErrorCode` 체계를 사용한다.
- `ReservationService`에는 Projection 기반 소유자 검증이 남아 있다.

후속 제약:

- 남은 `toDomain()` 제거는 별도 커밋에서 진행한다.
- 예외 계층 정리는 도메인 패키지 이동과 함께 별도로 설계한다.
- 생성자 접근 제한은 Repository와 테스트 생성 코드까지 함께 정리할 때 진행한다.
- `ReservationTimeService`의 시간 상태 판단은 조회 최적화와 도메인 정책의 경계를 다시 논의한 뒤 수정한다.

## 검증 방법

- `WaitingLineTest`에서 서로 다른 슬롯의 대기열 생성 실패를 검증한다.
- `SlotServiceTest`, `SlotApiIntegrationTest` 등 기존 슬롯 생성 테스트가 통과하는지 확인한다.
- 전체 테스트를 실행한다.

```bash
./gradlew test
```

## 열린 질문

- `WaitingLine`의 슬롯 불일치 예외를 나중에 어떤 도메인 예외 코드로 표현할 것인가?
- `Slot`이 장기적으로 `timeId`, `themeId`만 가질지, `ReservationTime`, `Theme` 객체를 참조할지 결정해야 한다.
- `ReservationService`의 Projection 기반 검증을 언제 도메인 모델 기반으로 바꿀지 결정해야 한다.
- `Theme`과 `ReservationTime`의 생성 책임 정리를 어느 시점에 진행할지 결정해야 한다.

## 관련

- `src/main/java/roomescape/waiting/WaitingLine.java`
- `src/test/java/roomescape/waiting/WaitingLineTest.java`
- `src/main/java/roomescape/slot/Slot.java`
- `src/main/java/roomescape/slot/application/SlotService.java`
- `src/main/java/roomescape/slot/dto/request/SlotSaveRequest.java`
- `docs/adr/ADR-0004-waiting-line-domain.md`

