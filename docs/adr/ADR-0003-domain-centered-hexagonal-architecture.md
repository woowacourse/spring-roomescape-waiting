# ADR-0003: 도메인 중심 설계는 참고하되 포트와 어댑터가 드러나는 헥사고날 구조를 따른다

## 상태

Proposed

## 맥락

리팩토링 과정에서 학습 참고 코드인 `네오/` 프로젝트를 확인했다. 참고 코드는 `Reservation`, `Schedule`, `Reservations`, `RoomEscape` 같은 도메인 객체가 풍부한 행위를 가지고 있고, 예외 처리도 도메인 예외와 웹 어댑터 매핑으로 깔끔하게 분리되어 있다.

특히 다음 부분은 현재 프로젝트가 개선하고 싶은 방향과 잘 맞는다.

- 서비스가 모든 규칙을 직접 판단하지 않고 도메인에 위임한다.
- 단일 객체가 판단할 수 있는 규칙은 객체 메서드로 표현한다.
- 여러 객체의 협력이 필요한 규칙은 도메인 Facade 또는 일급 컬렉션으로 조율한다.
- 도메인 예외는 HTTP 응답 형식을 모르고, 웹 어댑터가 변환한다.
- 시간 의존 값은 도메인 내부에서 직접 만들지 않고 외부에서 전달한다.

다만 참고 코드는 전형적인 헥사고날 아키텍처라기보다, `domain/application/adapter` 의존 방향과 도메인 응집을 합친 구조다. Repository 인터페이스도 도메인 패키지에 위치한다.

이번 프로젝트의 학습 목표에는 헥사고날 아키텍처의 정석적인 포트/어댑터 구조를 경험하는 것도 포함되어 있다. 따라서 참고 코드의 도메인 설계 감각은 배우되, 패키지와 의존성 구조는 포트와 어댑터가 명시적으로 드러나는 방향으로 잡을 필요가 있다.

## 결정

참고 코드의 도메인 중심 설계 원칙은 적극적으로 참고한다.

하지만 프로젝트 구조는 전형적인 헥사고날 아키텍처에 가깝게 다음 방향을 따른다.

```text
adapter.in.web
    -> application.port.in
    -> application.service
    -> domain

application.service
    -> application.port.out
adapter.out.persistence
    -> application.port.out 구현
```

도메인은 Spring, HTTP, JDBC, Repository 구현체를 알지 않는다.

Repository 인터페이스는 도메인 패키지가 아니라 `application.port.out`에 둔다. 이는 헥사고날 학습 목적상 outbound port를 명시적으로 표현하기 위함이다.

웹 컨트롤러는 inbound adapter로 이동하고, JDBC 저장소 구현체는 outbound persistence adapter로 이동한다.

## 대안

### 대안 1: 참고 코드 구조를 거의 그대로 따른다

장점:

- 도메인 중심 설계가 강하게 드러난다.
- `RoomEscape` 같은 도메인 Facade를 통해 여러 도메인 객체의 협력 규칙을 응집할 수 있다.
- 서비스가 매우 얇아진다.

단점:

- Repository 인터페이스가 도메인 패키지에 위치해 헥사고날의 `port.out` 개념이 명시적으로 드러나지 않는다.
- 현재 프로젝트에 예약 대기 흐름이 추가되어 참고 코드보다 도메인 관계가 더 복잡하다.
- 그대로 이식하면 학습보다 복사에 가까워질 위험이 있다.

### 대안 2: 기능별 패키지 안에서 현재 구조를 조금씩 정리한다

장점:

- 변경 범위가 작고 현재 코드와 가장 자연스럽게 이어진다.
- 테스트를 유지하며 점진적으로 개선하기 쉽다.

단점:

- 아키텍처 학습 목표인 포트/어댑터 구조가 충분히 드러나지 않는다.
- `application`이 `infrastructure` 패키지의 Repository 인터페이스에 의존하는 현재 모호성이 남는다.
- 장기적으로 계층 경계가 다시 흐려질 수 있다.

### 대안 3: 포트와 어댑터가 명시적인 헥사고날 구조로 전환한다

장점:

- inbound port, outbound port, adapter의 역할이 패키지로 드러난다.
- 애플리케이션 서비스가 도메인과 포트에만 의존하는 구조를 연습할 수 있다.
- JDBC, Web, 테스트 Fake 구현체를 바깥쪽 어댑터로 분리하기 쉽다.
- 이후 예외 변환도 웹 어댑터의 책임으로 명확히 둘 수 있다.

단점:

- 클래스와 패키지가 늘어난다.
- 미션 규모에 비해 구조가 과해질 수 있다.
- 잘못 적용하면 도메인 설계보다 계층 이동 작업에 집중하게 될 위험이 있다.

## 근거

이 리팩토링의 목적은 단순히 기능을 빠르게 구현하는 것이 아니라 학습이다.

참고 코드에서 가장 배워야 할 부분은 패키지 모양 자체가 아니라, 도메인 객체가 비즈니스 언어와 행위를 갖는 방식이다. 따라서 `Reservation`, `Waiting`, `SlotOccupancy`, `WaitingLine` 같은 도메인 객체를 먼저 풍부하게 만들고, 이후 포트/어댑터 구조로 이동하는 순서가 적절하다.

동시에 사용자는 헥사고날 아키텍처의 정석적 구조를 경험하고 싶어 한다. Repository 인터페이스를 도메인 패키지에 두는 방식도 DIP 관점에서는 가능하지만, 헥사고날 학습 목적에는 `application.port.out`이 더 명시적이다.

따라서 "도메인 설계는 참고 코드처럼 풍부하게, 아키텍처 경계는 포트/어댑터로 명시적으로"라는 방향을 선택한다.

## 결과

좋아지는 점:

- 이후 리팩토링 방향이 명확해진다.
- 도메인 중심 설계와 헥사고날 구조 학습을 동시에 진행할 수 있다.
- Repository, Controller, Service의 책임 위치를 판단할 기준이 생긴다.
- 예외 처리를 도메인 예외와 웹 응답 매핑으로 나눌 근거가 생긴다.

감수해야 하는 점:

- 패키지 이동과 인터페이스 분리가 늘어나며 단기 변경량이 커질 수 있다.
- `RoomEscape` 같은 도메인 Facade를 바로 도입하지 않기 때문에 참고 코드만큼 서비스가 즉시 얇아지지는 않는다.
- 한 번에 구조를 옮기지 않고 작은 단위로 진행해야 하므로 중간 상태가 한동안 혼재될 수 있다.

후속 제약:

- 새 Repository 인터페이스는 가능한 한 `application.port.out` 아래에 둔다.
- Controller는 장기적으로 `adapter.in.web`으로 이동한다.
- JDBC 구현체는 장기적으로 `adapter.out.persistence`로 이동한다.
- 도메인은 Spring annotation, HTTP 응답, JDBC 타입에 의존하지 않는다.
- 예외 처리는 장기적으로 도메인 예외와 웹 어댑터 매핑으로 분리한다.

## 검증 방법

- 도메인 패키지에서 `org.springframework`, `jakarta.servlet`, `JdbcTemplate` 의존이 생기지 않는지 확인한다.
- 애플리케이션 서비스가 JDBC 구현체가 아니라 outbound port 인터페이스에 의존하는지 확인한다.
- 컨트롤러가 장기적으로 use case input port에 의존하는지 확인한다.
- 도메인 단위 테스트가 Spring 없이 실행되는지 확인한다.
- 전체 테스트를 실행한다.

```bash
./gradlew test
```

## 열린 질문

- `RoomEscape` 같은 도메인 Facade를 우리 프로젝트에도 도입할지, 유스케이스 서비스와 일급 컬렉션 중심으로 갈지 결정해야 한다.
- `WaitingLine`은 어느 패키지의 도메인으로 둘지 결정해야 한다.
- 예외 계층을 `DomainException` / `ApplicationException` / Web Mapper로 나눌 시점을 결정해야 한다.
- 기존 기능별 패키지를 한 번에 옮길지, 예약/대기부터 점진적으로 옮길지 결정해야 한다.

## 관련

- `docs/refactoring-plan.md`
- `docs/adr/ADR-0001-test-safety-net-and-domain-ownership.md`
- `docs/adr/ADR-0002-slot-occupancy-domain.md`
- `네오/docs/domain-design.md` (참고용, Git 추적 제외)
- `네오/docs/adr/0012-package-structure.md` (참고용, Git 추적 제외)

