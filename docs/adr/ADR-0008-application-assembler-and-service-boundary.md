# ADR-0008: 서비스 간 참조를 줄이기 위해 Application Assembler를 도입한다

## 상태

Proposed

## 맥락

`ReservationService`와 `WaitingService`는 예약/대기 생성 시 `SlotService.resolveSlot(...)`을 호출했다.

```text
ReservationService -> SlotService
WaitingService     -> SlotService
```

또한 `ThemeService`, `ReservationTimeService`는 테마/시간 삭제 가능 여부를 확인하기 위해 `SlotService`를 참조했다.

```text
ThemeService           -> SlotService.validateThemeDeletable(...)
ReservationTimeService -> SlotService.validateTimeDeletable(...)
```

이 구조는 동작은 하지만 서비스 간 의존이 늘어나고, `SlotService`가 슬롯 관리 유스케이스와 다른 유스케이스를 위한 도메인 조립/검증 책임을 함께 갖게 만든다.

참고 코드인 `네오/`는 `ReservationAssembler`가 원시 입력을 도메인으로 조립하고, `RoomEscape`가 여러 저장소와 도메인 규칙을 조율한다. 현재 프로젝트도 이 방향을 학습하되, 곧바로 `RoomEscape` 같은 큰 Facade를 도입하기보다 현재 문제를 직접 만드는 책임부터 분리하기로 했다.

## 결정

서비스 간 참조를 줄이기 위해 Application 계층에 Assembler와 Validator를 도입한다.

- `SlotAssembler`
  - `date`, `timeId`, `themeId`를 받아 `Slot` 도메인을 조립한다.
  - 기존 슬롯 조립과 새 슬롯 조립을 구분한다.
  - 기존 슬롯은 `SlotRepository`가 `ReservationTime`, `Theme`을 포함한 `Slot`으로 한 번에 복원한다.
  - 조립된 `Slot`에 과거 슬롯 검증을 위임한다.

- `ThemeAssembler`
  - 테마 생성 입력을 `Theme` 도메인으로 조립한다.

- `ReservationTimeAssembler`
  - 시간 생성 입력을 `ReservationTime` 도메인으로 조립한다.

- `SlotUsageValidator`
  - 시간/테마 삭제 시 슬롯에서 사용 중인지 확인한다.
  - 삭제 가능 여부 검증은 조립이 아니므로 Assembler가 아니라 Validator로 분리한다.

이에 따라 `ReservationService`, `WaitingService`, `ThemeService`, `ReservationTimeService`는 더 이상 `SlotService`를 참조하지 않는다.

## 대안

### 대안 1: 기존처럼 Service가 다른 Service를 참조한다

장점:

- 구현 변경이 적다.
- 중복 조회 로직이 줄어든다.
- 현재 규모에서는 빠르게 동작한다.

단점:

- 서비스 간 의존 그래프가 복잡해진다.
- 트랜잭션 경계가 흐려질 수 있다.
- `SlotService`가 슬롯 관리 외 책임까지 갖게 된다.
- 특정 서비스의 public 메서드가 다른 서비스의 유틸리티처럼 사용될 수 있다.

### 대안 2: `RoomEscape` Facade를 바로 도입한다

장점:

- 유스케이스 조율 중심이 명확해진다.
- 여러 저장소와 도메인 규칙을 한 흐름에서 다룰 수 있다.
- 서비스가 매우 얇아진다.

단점:

- 현재 구조에서 변경 범위가 크다.
- Facade가 어느 계층에 속하는지, 트랜잭션을 어디에 둘지 추가 결정이 필요하다.
- 충분히 작게 나누지 않으면 God Object가 될 수 있다.

### 대안 3: Assembler/Validator를 먼저 도입한다

장점:

- 현재 냄새인 `SlotService` 책임 과다를 직접 줄인다.
- 서비스 간 참조를 제거할 수 있다.
- `RoomEscape` Facade 도입 전에 작은 단위로 구조를 개선할 수 있다.
- DTO의 `toDomain()`을 제거하고 도메인 조립 책임을 Application 계층에 둘 수 있다.

단점:

- Application 계층의 컴포넌트 수가 늘어난다.
- Assembler가 단순 생성만 담당하면 과한 추상화가 될 수 있다.
- 향후 Facade 도입 시 Assembler와 Facade의 책임 경계를 다시 조정해야 할 수 있다.

## 근거

현재 문제는 전체 Facade 부재보다 `SlotService`가 여러 역할을 동시에 수행하는 데서 먼저 드러났다.

```text
SlotService
-> 슬롯 CRUD
-> Slot 도메인 조립
-> 과거 슬롯 검증 호출
-> 시간/테마 삭제 가능 여부 검증
```

따라서 `RoomEscape`를 바로 도입하기보다 `SlotAssembler`, `SlotUsageValidator`로 책임을 작게 분리하는 것이 현재 리팩토링 단계에 적합하다.

또한 DTO가 직접 도메인을 만드는 `toDomain()`은 요청 객체와 도메인 생성 책임을 섞는다. 도메인 조립이 저장소 조회를 필요로 하거나 다른 도메인 객체를 함께 구성해야 하는 순간 DTO에는 둘 수 없으므로, Application Assembler로 일관되게 이동한다.

## 결과

좋아지는 점:

- `ReservationService`, `WaitingService`가 `SlotService`를 참조하지 않는다.
- `ThemeService`, `ReservationTimeService`가 `SlotService`를 참조하지 않는다.
- `SlotService`는 슬롯 관리 유스케이스에 더 집중한다.
- `SlotAssembler`가 `Slot` 조립 책임을 명확히 가진다.
- 기존 슬롯 조립 시 `slotId` 조회 후 `ReservationTime`, `Theme`을 다시 조회하지 않는다.
- `ThemeSaveRequest`, `ReservationTimeSaveRequest`에서 `toDomain()`이 제거된다.

감수해야 하는 점:

- Application 계층에 Assembler/Validator 컴포넌트가 늘어난다.
- `SlotAssembler`는 새 슬롯 조립 시 여러 repository를 참조하므로, 도메인 조립 책임을 넘어서지 않도록 관리해야 한다.
- `ThemeAssembler`, `ReservationTimeAssembler`는 현재 단순하지만 이후 도메인 생성 검증이 추가될 때 의미가 커진다.

후속 제약:

- Service가 다른 Service를 참조하지 않는 방향을 유지한다.
- Assembler는 DTO가 아니라 원시 입력 또는 Command를 받아 도메인으로 조립한다.
- 저장/삭제/트랜잭션 조율은 Service에 남긴다.
- 여러 유스케이스를 하나로 묶는 흐름이 충분히 커지면 `RoomEscape` Facade 도입을 다시 검토한다.

## 검증 방법

- `ReservationService`, `WaitingService`, `ThemeService`, `ReservationTimeService`가 `SlotService`를 참조하지 않는지 확인한다.
- DTO의 `toDomain()`이 남아 있지 않은지 확인한다.
- `SlotAssembler`가 기존 슬롯과 새 슬롯을 올바르게 조립하는지 테스트한다.
- 전체 테스트를 실행한다.

```bash
./gradlew test
```

## 열린 질문

- `ThemeAssembler`, `ReservationTimeAssembler`가 단순 생성만 담당하는 현재 상태가 충분히 의미 있는지, 도메인 생성 검증 추가 후 다시 평가한다.
- `SlotUsageValidator`를 Application Validator로 유지할지, 향후 `SlotPolicy` 또는 `SlotReferences` 같은 도메인/정책 객체로 이동할지 결정해야 한다.
- `RoomEscape` Facade를 도입한다면 Assembler와 Facade의 책임 경계를 어떻게 나눌지 결정해야 한다.

## 관련

- `src/main/java/roomescape/slot/application/SlotAssembler.java`
- `src/main/java/roomescape/slot/application/SlotUsageValidator.java`
- `src/main/java/roomescape/theme/application/ThemeAssembler.java`
- `src/main/java/roomescape/reservationtime/application/ReservationTimeAssembler.java`
- `docs/adr/ADR-0003-domain-centered-hexagonal-architecture.md`
