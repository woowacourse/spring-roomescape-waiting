# ADR-0010: 헥사고날 구조와 코드 컨벤션을 기준으로 남은 리팩토링을 진행한다

## 상태

Proposed

## 맥락

현재 프로젝트는 도메인 객체와 애플리케이션 서비스의 책임을 분리하고, 서비스 간 참조를 줄이기 위해 여러 차례 리팩토링을 진행했다.

예를 들어 `SlotOccupancy`, `WaitingLine`, `WaitingPromotionPolicy`, `SlotAssembler` 같은 객체가 도입되었고, 예약 취소와 대기 승격의 트랜잭션 경계도 별도 ADR로 정리했다.

하지만 아직 구조와 코드 스타일 측면에서 다음 문제가 남아 있다.

- 저장소 인터페이스가 `infrastructure` 패키지에 있어 애플리케이션 계층이 인프라 계층을 향하는 것처럼 보인다.
- `ReservationService`, `WaitingService`가 repository projection을 직접 알고 있어 포트와 어댑터의 경계가 흐리다.
- 패키지 이름이 `presentation`, `application`, `infrastructure`로 나뉘어 있지만, 헥사고날 아키텍처의 inbound adapter, use case, outbound port, persistence adapter 역할이 명확히 드러나지는 않는다.
- 도메인과 서비스에 비슷한 동작이 다른 방식으로 표현된다. 예를 들어 `Reservation`은 `validateOwnedBy(...)`를 사용하지만, `WaitingService`는 `waiting.isOwnedBy(...)`로 직접 검증한다.
- 조회, 검증, 응답 생성, 순번 계산 같은 코드가 서비스에 섞여 있어 읽는 사람이 유스케이스 흐름과 세부 구현을 함께 따라가야 한다.
- 테스트 일부가 저장소 호출 순서나 내부 구현에 강하게 묶여 있어 리팩토링 비용을 높인다.

따라서 다음 기능을 추가하기 전에, 헥사고날 아키텍처에 맞는 구조와 인터페이스 사용 방식, 네이밍 컨벤션, 중복 제거 기준을 명확히 정할 필요가 있다.

## 결정

남은 리팩토링은 다음 기준으로 진행한다.

첫째, 패키지 구조를 헥사고날 아키텍처의 역할이 드러나도록 점진적으로 정리한다.

추천 구조는 다음과 같다.

```text
roomescape
  reservation
    domain
    application
      port
        in
        out
    adapter
      in
        web
      out
        persistence
  waiting
    domain
    application
      port
        in
        out
    adapter
      in
        web
      out
        persistence
  slot
    domain
    application
      port
        out
    adapter
      out
        persistence
```

둘째, 애플리케이션 계층은 outbound port 인터페이스에 의존한다.

예를 들어 `ReservationRepository`, `WaitingRepository`, `SlotRepository` 같은 인터페이스는 `infrastructure`가 아니라 각 기능의 `application.port.out`으로 이동한다. JDBC 구현체와 SQL projection은 persistence adapter에 둔다.

셋째, inbound port는 유스케이스가 충분히 커졌을 때 도입한다.

현재는 `ReservationService`, `WaitingService`가 컨트롤러에서 직접 호출되는 구조를 유지할 수 있다. 다만 예약 생성, 예약 취소, 내 예약 조회, 대기 생성처럼 흐름이 커지는 기능은 `CreateReservationUseCase`, `CancelReservationUseCase`, `FindMyReservationsUseCase`, `CreateWaitingUseCase` 같은 인터페이스로 분리할 수 있게 준비한다.

넷째, 도메인과 서비스의 검증 메서드 네이밍을 통일한다.

- 단순 boolean 판단은 `is...`, `has...`, `can...`을 사용한다.
- 예외를 던지는 검증은 `validate...`를 사용한다.
- 서비스는 도메인에 이미 존재하는 검증 메서드를 우선 사용한다.
- 같은 규칙을 서비스와 도메인에 중복 구현하지 않는다.

다섯째, 중복된 기능과 과도한 서비스 내부 구현을 줄인다.

- `findMyReservations()`의 대기 순번 계산처럼 조회 후 슬롯별 추가 조회가 필요한 로직은 repository query 또는 전용 projection으로 이동한다.
- 반복되는 `findById(...).orElse(null)` 후 early return 패턴은 `find...OrNull`, `find...OrThrow`, `deleteIfExists` 등 유스케이스 의미가 드러나는 방식으로 통일한다.
- 응답 DTO 조립과 도메인 규칙 판단이 한 메서드에 섞이면 assembler, mapper, query projection 중 적절한 위치로 분리한다.

## 대안

### 대안 1: 현재 패키지 구조를 유지하고 필요한 부분만 정리한다

장점:

- 파일 이동이 적어 충돌 가능성이 낮다.
- 학습 중인 프로젝트에서 과한 구조 변경을 피할 수 있다.
- 기능 구현 속도가 빠르다.

단점:

- `application`이 `infrastructure` 인터페이스에 의존하는 모양이 계속 남는다.
- 헥사고날 아키텍처를 적용했다는 설명이 코드 구조로 드러나지 않는다.
- 시간이 지날수록 컨벤션이 섞인 상태가 고착될 수 있다.

### 대안 2: 전체 프로젝트를 한 번에 헥사고날 구조로 재배치한다

장점:

- 구조가 빠르게 명확해진다.
- 포트와 어댑터 경계를 한 번에 맞출 수 있다.
- 이후 기능 추가 시 기준이 분명하다.

단점:

- 변경 파일이 많아 리뷰가 어려워진다.
- 기능 변경 없이 패키지 이동만 많은 PR이 될 수 있다.
- 테스트와 import 수정 비용이 크다.
- 현재 코드의 중복과 네이밍 문제를 해결하지 못한 채 위치만 바꿀 위험이 있다.

### 대안 3: 기능 단위로 구조, 포트, 컨벤션을 함께 정리한다

장점:

- 변경 범위를 작게 유지할 수 있다.
- 패키지 이동과 실제 책임 정리를 함께 진행할 수 있다.
- 테스트를 기능 단위로 확인하기 쉽다.
- 리팩토링 결과가 단순 위치 이동이 아니라 코드 가독성 개선으로 이어진다.

단점:

- 전환 기간에는 기존 구조와 새 구조가 함께 존재한다.
- 패키지 기준을 문서와 리뷰로 계속 맞춰야 한다.
- 기능마다 어느 정도까지 옮길지 판단이 필요하다.

## 근거

현재 프로젝트의 목표는 단순히 디렉터리 이름을 바꾸는 것이 아니라, 도메인 규칙과 유스케이스 흐름을 읽기 쉬운 구조로 만드는 것이다.

따라서 한 번에 전체 구조를 바꾸기보다 기능 단위로 다음 순서를 따른다.

```text
테스트로 현재 동작 고정
-> 중복/네이밍/책임 정리
-> application port 추출
-> JDBC 구현을 persistence adapter로 이동
-> 필요할 때 inbound use case 인터페이스 도입
```

이 방식은 현재 프로젝트 규모에서 구현 비용을 통제하면서도 헥사고날 아키텍처의 핵심인 의존 방향을 개선할 수 있다.

또한 `ReservationService.findMyReservations(...)`처럼 읽기 전용 조회가 복잡한 경우, 도메인 객체로 모든 것을 옮기기보다 query projection을 활용하는 편이 더 단순할 수 있다. 핵심 규칙은 도메인으로, 조회 최적화와 화면 응답 구성은 adapter/query 쪽으로 나누는 기준을 둔다.

## 결과

좋아지는 점:

- 애플리케이션 계층이 인프라 구현 세부사항에서 멀어진다.
- 저장소 인터페이스의 위치만 봐도 의존 방향을 이해할 수 있다.
- 컨트롤러, 유스케이스, 포트, JDBC 구현체의 역할이 분명해진다.
- 도메인 검증 메서드의 사용 방식이 통일된다.
- 서비스 메서드가 유스케이스 흐름을 중심으로 짧아진다.
- 중복 조회와 중복 검증을 줄일 수 있다.

감수해야 하는 점:

- 패키지 이동으로 import 변경이 많이 발생할 수 있다.
- 전환 기간 동안 기존 `presentation/application/infrastructure` 구조와 새 `adapter/port` 구조가 함께 존재할 수 있다.
- 단순 CRUD에 inbound port를 과하게 도입하면 추상화 비용이 커질 수 있다.
- query projection을 활용하는 조회는 도메인 모델 중심 코드보다 SQL 의존이 더 강해진다.

후속 제약:

- 새 repository 인터페이스는 `infrastructure` 패키지에 만들지 않는다.
- 애플리케이션 서비스는 JDBC 구현체나 SQL projection에 직접 의존하지 않는다.
- 도메인 객체에 이미 있는 규칙은 서비스에서 다시 구현하지 않는다.
- 단순 파일 이동만 하는 리팩토링과 책임 변경 리팩토링은 가능하면 PR 또는 커밋을 분리한다.
- 테스트는 내부 호출 순서보다 외부 응답, 도메인 상태, DB 상태를 우선 검증한다.

## 검증 방법

구조 검증:

- `ReservationRepository`, `WaitingRepository`, `SlotRepository` 인터페이스가 `application.port.out` 아래에 있는지 확인한다.
- JDBC 구현체가 `adapter.out.persistence` 또는 그에 준하는 infrastructure adapter 아래에 있는지 확인한다.
- 컨트롤러가 inbound adapter 역할만 수행하고 도메인 규칙을 직접 판단하지 않는지 확인한다.

코드 컨벤션 검증:

- `isOwnedBy(...)`로 직접 예외를 만들던 코드를 `validateOwnedBy(...)` 사용으로 통일했는지 확인한다.
- 같은 예외 정책을 가진 검증 메서드가 서비스마다 다른 이름으로 흩어져 있지 않은지 확인한다.
- `find...OrThrow`, `find...OrNull`, `delete...IfExists` 같은 메서드 네이밍이 역할과 일치하는지 확인한다.

중복 제거 검증:

- `findMyReservations()`에서 슬롯별 대기열 추가 조회가 제거되었는지 확인한다.
- 대기 순번 계산이 query projection 또는 명시적인 도메인 객체 중 한 곳에만 존재하는지 확인한다.
- 응답 DTO 조립 로직이 서비스 유스케이스 흐름을 가리지 않는지 확인한다.

테스트:

```bash
./gradlew test
```

## 열린 질문

- inbound use case 인터페이스를 어느 시점부터 도입할지 결정해야 한다.
- query projection을 application port의 반환 타입으로 둘지, adapter 내부 DTO로 감춘 뒤 application 전용 result로 변환할지 결정해야 한다.
- 단순한 `ThemeService`, `ReservationTimeService`에도 동일한 헥사고날 구조를 엄격히 적용할지 결정해야 한다.
- Assembler, Mapper, QueryService의 책임 경계를 어디까지 나눌지 추가 사례를 보며 결정해야 한다.

## 관련

- `src/main/java/roomescape/reservation/application/ReservationService.java`
- `src/main/java/roomescape/waiting/application/WaitingService.java`
- `src/main/java/roomescape/reservation/infrastructure/ReservationRepository.java`
- `src/main/java/roomescape/waiting/infrastructure/WaitingRepository.java`
- `src/main/java/roomescape/slot/infrastructure/SlotRepository.java`
- `src/main/java/roomescape/slot/application/SlotAssembler.java`
- `src/main/java/roomescape/waiting/Waiting.java`
- `src/main/java/roomescape/reservation/Reservation.java`
- `docs/adr/ADR-0003-domain-centered-hexagonal-architecture.md`
- `docs/adr/ADR-0008-application-assembler-and-service-boundary.md`
