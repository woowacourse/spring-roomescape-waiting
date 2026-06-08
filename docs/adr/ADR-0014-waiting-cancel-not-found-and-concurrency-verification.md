# ADR-0014: 없는 대기 취소는 실패시키고 대기 승격 충돌은 양방향 동시성 테스트로 검증한다

## 상태

Proposed

## 맥락

ADR-0012와 ADR-0013에서는 예약 취소로 인한 대기 승격과 사용자의 대기 취소가 동시에 발생할 때 생기는 유령 예약 시나리오를 핵심 방어 대상으로 정했다.

핵심 시나리오는 다음과 같다.

```text
사용자는 자신의 대기를 취소한다.
동시에 같은 슬롯의 기존 예약이 취소되어 해당 대기가 승격 대상이 된다.
사용자는 대기가 취소되었다고 인지했지만, 시스템에는 예약이 생길 수 있다.
```

ADR-0013에서는 이 문제를 막기 위해 예약 취소의 승격 대상 대기열 조회와 대기 취소의 단건 대기 조회에 `SELECT ... FOR UPDATE`를 사용하기로 했다. 하지만 이후 검토 과정에서 다음 정책이 불명확했다.

```text
예약 취소가 먼저 waiting row를 락으로 잡고 승격을 완료한 뒤,
뒤늦게 들어온 대기 취소는 어떤 응답을 받아야 하는가?
```

기존 대기 취소 흐름은 조회한 waiting row가 없으면 성공으로 처리했다.

```java
Waiting waiting = waitingRepository.findByIdForUpdate(waitingId)
        .orElse(null);
if (waiting == null) {
    return;
}
```

이 정책에서는 이미 승격되어 삭제된 대기와 원래부터 없던 대기를 구분하지 않는다. 따라서 뒤늦은 대기 취소 요청도 성공으로 응답될 수 있다. 이는 사용자가 "대기 취소가 성공했다"고 이해할 여지를 남긴다.

이번 결정은 대기 취소의 실패 정책과, ADR-0013에서 남겨둔 동시성 검증 방법을 명확히 하기 위한 것이다.

## 결정

### 1. 없는 대기 취소는 실패로 처리한다

대기 취소 요청에서 `findByIdForUpdate(waitingId)` 결과가 없으면 성공 처리하지 않고 `WAITING_NOT_FOUND` 예외를 발생시킨다.

```java
Waiting waiting = waitingRepository.findByIdForUpdate(waitingId)
        .orElseThrow(() -> new EscapeRoomException(ErrorCode.WAITING_NOT_FOUND, waitingId));
```

HTTP 응답은 `404 Not Found`로 처리한다.

```text
error.code = WAITING_404
```

이 정책에 따라 이미 승격되어 waiting row가 삭제된 뒤늦은 대기 취소도 실패한다.

### 2. 예약 취소와 대기 취소의 충돌은 먼저 waiting row를 잡은 쪽이 이긴다

동시성 정책은 다음 두 방향으로 정의한다.

```text
예약 취소가 먼저 waiting row를 락으로 잡은 경우
-> 해당 대기는 예약으로 승격된다.
-> waiting row는 삭제된다.
-> 뒤늦은 대기 취소는 waiting을 찾지 못해 404로 실패한다.
```

```text
대기 취소가 먼저 waiting row를 락으로 잡은 경우
-> 해당 대기는 삭제된다.
-> 예약 취소는 삭제된 waiting을 승격하지 않는다.
-> 대기가 더 없다면 기존 예약만 삭제되고 승격 예약은 생기지 않는다.
```

### 3. 동시성 테스트는 양방향 순서를 모두 검증한다

ADR-0013의 후속 검증으로 다음 두 테스트를 둔다.

```java
reservation_cancel_first_promotes_waiting_and_late_waiting_cancel_fails
```

이 테스트는 예약 취소가 먼저 승격 대상 waiting row를 락으로 잡으면 대기 취소가 락 해제 전까지 완료되지 않고, 승격 완료 후 404로 실패하는지 검증한다.

```java
waiting_cancel_first_deletes_waiting_and_reservation_cancel_does_not_promote_that_waiting
```

이 테스트는 대기 취소가 먼저 waiting row를 락으로 잡으면 예약 취소가 해당 waiting을 승격하지 않는지 검증한다.

## 대안

### 대안 1: 없는 대기 취소를 계속 성공으로 처리한다

장점:

- DELETE 요청을 멱등적으로 처리할 수 있다.
- 이미 삭제된 리소스에 대한 재시도 요청이 클라이언트 입장에서 단순하다.
- 기존 정책과 테스트 변경이 적다.

단점:

- 원래부터 없는 대기와 승격으로 인해 삭제된 대기를 구분하지 못한다.
- 뒤늦은 대기 취소 요청이 성공으로 응답되면 사용자는 대기 취소가 반영되었다고 이해할 수 있다.
- 예약 취소가 먼저 승격을 완료한 상황에서 대기 취소 성공 응답과 실제 예약 생성 결과가 충돌한다.

### 대안 2: 대기를 hard delete하지 않고 상태로 관리한다

예를 들어 waiting row에 `WAITING`, `CANCELED`, `PROMOTED` 상태를 둔다.

장점:

- 대기가 취소되었는지, 승격되었는지 이력을 명확히 남길 수 있다.
- 뒤늦은 대기 취소에서 `PROMOTED` 상태를 보고 더 구체적인 실패 응답을 줄 수 있다.
- 운영 감사나 사용자 문의 대응에 유리하다.

단점:

- 현재 요구사항의 "대기 취소는 hard delete" 정책과 충돌한다.
- 대기 순번 계산에서 취소/승격 상태를 제외하는 조건이 추가된다.
- 단순한 대기열 모델에 상태 전이 복잡도가 생긴다.

### 대안 3: 슬롯 단위 락으로 예약/대기 명령 전체를 직렬화한다

예약 생성, 예약 취소, 대기 신청, 대기 취소를 모두 slot row 기준으로 직렬화한다.

장점:

- 같은 슬롯의 상태 전이가 하나의 기준으로 정렬된다.
- waiting row가 없는 경우까지 포함해 더 넓은 동시성 문제를 제어할 수 있다.
- 같은 예약 취소 요청이 동시에 두 번 들어오는 문제도 별도 설계로 다루기 쉬워진다.

단점:

- ADR-0012와 ADR-0013에서 정한 핵심 방어 대상보다 락 범위가 넓다.
- 대기 신청이나 단순한 상태 변경까지 직렬화되어 처리량이 줄 수 있다.
- 현재 프로젝트 규모에서는 구현 비용과 테스트 범위가 커진다.

## 근거

이번 프로젝트에서 우선 방어하기로 한 문제는 "사용자가 대기를 취소했다고 인지했는데 예약이 생기는 상황"이다. 따라서 뒤늦은 대기 취소를 성공으로 응답하는 정책은 이 목표와 맞지 않는다.

예약 취소가 먼저 waiting row를 락으로 잡고 승격을 완료했다면, 그 시점 이후 같은 waiting id에 대한 취소 요청은 더 이상 취소할 대기가 없다. 이 요청은 성공이 아니라 실패로 표현하는 편이 실제 상태와 일치한다.

반대로 대기 취소가 먼저 waiting row를 락으로 잡았다면 예약 취소는 해당 row의 삭제가 끝난 뒤 대기열을 다시 판단해야 한다. 이 경우 삭제된 waiting은 승격 대상이 아니다.

즉 `SELECT ... FOR UPDATE`의 목적은 두 명령 중 하나를 무조건 우선시키는 것이 아니라, 같은 waiting row를 동시에 처리하지 못하게 하고 먼저 완료된 상태 전이를 기준으로 뒤쪽 명령이 판단하게 만드는 것이다.

## 결과

좋아지는 점:

- 대기 취소 성공 응답과 실제 시스템 상태가 충돌하지 않는다.
- 이미 승격되어 삭제된 대기에 대한 취소 요청은 명확히 실패한다.
- 예약 취소 선점, 대기 취소 선점 두 방향의 동시성 정책이 테스트로 고정된다.
- ADR-0013의 "동시성 테스트를 추가해야 한다"는 후속 과제가 구체화된다.

감수해야 하는 점:

- DELETE 요청의 멱등 성공 정책을 포기한다.
- 이미 삭제된 대기 취소 재시도는 404로 응답된다.
- 기존 API 문서나 테스트에서 "없는 대기 취소는 성공"이라고 설명한 부분은 수정되어야 한다.

후속 제약:

- 대기 취소에서 `findByIdForUpdate(...)`는 반드시 트랜잭션 안에서 호출되어야 한다.
- 승격된 대기는 hard delete되므로, 현재 구조에서는 "원래부터 없던 대기"와 "승격되어 삭제된 대기"를 응답에서 구분하지 않는다.
- 구분이 필요해지면 상태 컬럼이나 이력 테이블 도입을 별도 ADR로 검토해야 한다.
- 같은 예약 취소 요청이 동시에 두 번 들어오는 문제는 이번 결정의 직접 대상이 아니다. 필요하다면 reservation row 락 또는 slot row 락을 별도 결정으로 다룬다.

## 검증 방법

단위 테스트:

- `WaitingServiceTest`에서 없는 대기 취소 시 `WAITING_NOT_FOUND` 예외가 발생하는지 확인한다.
- `ReservationServiceTest`에서 예약 취소 시 승격 대상 대기열 조회가 `findAllBySlotIdOrderByIdForUpdate(...)`를 사용하는지 확인한다.
- `WaitingServiceTest`에서 대기 취소 시 `findByIdForUpdate(...)`를 사용하는지 확인한다.

API 통합 테스트:

- `WaitingApiIntegrationTest`에서 없는 대기 취소 요청이 `404`와 `WAITING_404`를 반환하는지 확인한다.

동시성 통합 테스트:

- `ReservationTransactionIntegrationTest`에서 예약 취소가 먼저 waiting row를 잡은 경우를 검증한다.
  - 예약 취소는 `204`를 반환한다.
  - 뒤늦은 대기 취소는 `404`를 반환한다.
  - 승격 예약이 생성된다.
  - waiting row는 삭제된다.

- `ReservationTransactionIntegrationTest`에서 대기 취소가 먼저 waiting row를 잡은 경우를 검증한다.
  - 예약 취소는 대기 취소 트랜잭션이 끝나기 전까지 완료되지 않는다.
  - 해당 waiting은 삭제된다.
  - 해당 waiting 사용자의 승격 예약은 생성되지 않는다.
  - 기존 예약은 삭제된다.

실행 명령:

```bash
./gradlew test --tests 'roomescape.reservation.domain.ReservationTransactionIntegrationTest'
./gradlew test
```

## 열린 질문

- 같은 reservation id에 대한 취소 요청이 동시에 두 번 들어오는 경우도 명시적으로 방어할 것인가?
- 예약 생성, 예약 취소, 대기 신청, 대기 취소를 모두 slot 단위로 직렬화할 필요가 있는가?
- 운영 DB가 H2가 아닐 경우, 해당 DB에서 `SELECT ... FOR UPDATE`와 `ORDER BY` 조합의 락 범위가 기대와 일치하는지 확인할 필요가 있는가?
- 승격된 대기와 원래부터 없던 대기를 사용자 응답에서 구분해야 하는 요구가 생길 수 있는가?

## 관련

- `docs/adr/ADR-0012-transaction-and-concurrency-strategy.md`
- `docs/adr/ADR-0013-pessimistic-lock-for-waiting-promotion.md`
- `src/main/java/roomescape/waiting/application/WaitingService.java`
- `src/main/java/roomescape/exception/ErrorCode.java`
- `src/test/java/roomescape/waiting/WaitingServiceTest.java`
- `src/test/java/roomescape/waiting/WaitingApiIntegrationTest.java`
- `src/test/java/roomescape/reservation/ReservationTransactionIntegrationTest.java`
