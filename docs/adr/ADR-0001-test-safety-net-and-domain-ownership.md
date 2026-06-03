# ADR-0001: 테스트 안전망을 보강한 뒤 소유자 검증을 도메인 행위로 이동한다

## 상태

Proposed

## 맥락

사이클2 요구사항을 구현하기 전에 기존 예약/대기 코드를 리팩토링하기로 했다.

현재 코드는 `ReservationService`, `WaitingService`에 비즈니스 규칙과 검증이 많이 모여 있다. 특히 대기 생성 가능 여부, 본인 대기 취소 가능 여부 같은 규칙이 서비스의 조건문으로 표현되어 있어 도메인 객체가 거의 행위를 갖지 않는다.

리팩토링은 외부 동작을 유지하면서 내부 구조를 개선해야 하므로, 먼저 변경 대상 주변의 현재 동작을 테스트로 고정할 필요가 있었다.

이번 변경에서는 대기 생성 정책 중 누락되어 있던 "예약된 슬롯에는 첫 번째 대기를 신청할 수 있다"는 테스트를 추가했고, 이후 가장 작은 도메인 행위인 소유자 검증을 `Waiting` 도메인으로 이동했다.

## 결정

리팩토링을 시작할 때 예측 가능한 모든 테스트를 한 번에 추가하지 않고, 변경하려는 범위 주변의 테스트를 작게 보강한다.

첫 번째 리팩토링 단위로는 대기 생성 정책 테스트를 보강한 뒤, `WaitingService`의 대기 소유자 검증을 `Waiting.isOwnedBy(...)` 도메인 메서드로 이동한다.

`Reservation`에도 동일한 도메인 언어를 맞추기 위해 `Reservation.isOwnedBy(...)`를 추가한다. 다만 현재 `ReservationService`는 `ReservationDetailProjection` 기반으로 소유자 검증을 수행하므로, `ReservationService` 적용은 별도 설계 주제로 미룬다.

## 대안

### 대안 1: 모든 예측 가능한 테스트를 먼저 작성한다

장점:

- 리팩토링 전 넓은 범위의 회귀 안전망을 확보할 수 있다.
- 사이클2 구현 전 요구사항 누락을 미리 발견할 수 있다.

단점:

- 아직 구조가 바뀌기 전이라 테스트가 현재 구현 세부사항에 과하게 묶일 수 있다.
- 테스트 작성 비용이 커져 리팩토링 진행 속도가 느려질 수 있다.
- 설계가 바뀌면 테스트 자체를 대량 수정해야 할 가능성이 있다.

### 대안 2: 변경 범위 주변의 테스트만 작게 추가한다

장점:

- 현재 리팩토링 목표와 직접 관련된 동작만 고정할 수 있다.
- 테스트가 과하게 많아지는 것을 막고, 작은 단위로 Red-Green-Refactor 흐름을 유지할 수 있다.
- 실패 원인을 좁은 범위에서 파악하기 쉽다.

단점:

- 아직 테스트로 고정되지 않은 기존 동작은 리팩토링 중 깨질 수 있다.
- 전체 요구사항 안전망은 여러 리팩토링 단계를 거치며 점진적으로 확보해야 한다.

### 대안 3: 테스트 보강 없이 바로 도메인 리팩토링을 진행한다

장점:

- 코드 변경을 빠르게 시작할 수 있다.

단점:

- 리팩토링 과정에서 기존 동작이 깨졌는지 판단하기 어렵다.
- 서비스에 숨어 있는 정책을 놓치기 쉽다.
- 이후 사이클2 구현 시 회귀 버그를 찾기 어려워진다.

## 근거

현재 프로젝트는 학습 목적의 리팩토링을 진행하고 있으며, 한 번에 큰 구조 변경을 하기보다 작은 단위로 의도를 확인하는 것이 중요하다.

대기 생성 정책은 사이클2의 대기 전환/순번 재정렬 흐름의 기반이다. 따라서 대기 생성 정책 중 누락된 케이스를 먼저 테스트로 보강하는 것이 적절하다.

소유자 검증은 저장소나 외부 의존성이 필요 없는 순수한 판단이다. `WaitingService`가 `Objects.equals(waiting.getMemberId(), memberId)`로 직접 비교하는 대신 `waiting.isOwnedBy(memberId)`를 호출하면 서비스는 유스케이스 흐름에 집중하고, 구체적인 소유자 판단은 도메인이 맡게 된다.

## 결과

좋아지는 점:

- 대기 생성 정책 중 "예약된 슬롯에는 첫 번째 대기를 신청할 수 있다"는 동작이 테스트로 명확해졌다.
- `Waiting`이 본인 소유 여부를 판단하는 행위를 갖게 되었다.
- `WaitingService`에서 직접 필드를 비교하는 코드가 줄었다.
- `Reservation`과 `Waiting`에 `create(...)`, `of(...)` 생성 메서드가 생겨 저장 전 생성과 기존 객체 복원 의도를 구분할 기반이 생겼다.

감수해야 하는 점:

- `ReservationService`는 아직 `ReservationDetailProjection`에 의존해 소유자 검증을 수행한다.
- `Reservation.isOwnedBy(...)`는 추가되었지만 아직 서비스 흐름에 적용되지 않았다.
- 생성자와 정적 팩토리 메서드가 함께 열려 있어 생성 정책이 완전히 강제되지는 않는다.

후속 제약:

- 이후 DTO의 `toDomain()` 제거 시 `create(...)`를 사용할지 결정해야 한다.
- JDBC 어댑터에서 도메인 복원 시 `of(...)`를 사용할 수 있도록 점진적으로 정리할 수 있다.
- `ReservationService`의 Projection 기반 소유자 검증은 별도 설계 결정이 필요하다.

## 검증 방법

- `WaitingServiceTest`에서 예약된 슬롯에 첫 번째 대기를 신청할 수 있는지 확인한다.
- `ReservationTest`, `WaitingTest`에서 `isOwnedBy(...)`가 같은 회원 id에 대해 `true`, 다른 회원 id에 대해 `false`를 반환하는지 확인한다.
- `WaitingServiceTest`에서 본인 대기가 아닌 경우 취소가 거부되는지 확인한다.
- 전체 테스트를 실행한다.

```bash
./gradlew test
```

## 열린 질문

- `ReservationService`의 소유자 검증을 도메인으로 옮기려면 `ReservationDetailProjection`을 계속 사용할지, 별도 조회 모델과 도메인 모델을 분리할지 결정해야 한다.
- `Reservation`과 `Waiting`의 생성자를 private으로 제한하고 정적 팩토리만 허용할지 결정해야 한다.
- `isOwnedBy(...)`의 인자를 `Long`으로 유지할지, 필수값 의미를 드러내기 위해 `long`으로 바꿀지 결정해야 한다.

## 관련

- `src/main/java/roomescape/waiting/Waiting.java`
- `src/main/java/roomescape/reservation/Reservation.java`
- `src/main/java/roomescape/waiting/application/WaitingService.java`
- `src/test/java/roomescape/waiting/WaitingServiceTest.java`
- `src/test/java/roomescape/waiting/WaitingTest.java`
- `src/test/java/roomescape/reservation/ReservationTest.java`
- `docs/refactoring-plan.md`

