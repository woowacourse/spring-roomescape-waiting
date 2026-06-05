# ADR-0013: 대기 승격과 대기 취소 충돌 방어를 위해 명령 전용 비관적 락을 사용한다

## 상태

Proposed

## 맥락

ADR-0012에서는 예약 취소로 인한 대기 승격과 해당 대기자의 대기 취소가 동시에 발생할 때 생길 수 있는 유령 예약 시나리오를 핵심 방어 대상으로 정했다.

문제 시나리오는 다음과 같다.

```text
스레드 A: 대기자가 자신의 대기를 취소한다.
스레드 B: 같은 슬롯의 기존 예약이 취소되어 첫 번째 대기를 예약으로 승격한다.
```

기존 구현은 대기 취소에서만 `waiting.id` 단건 조회에 `FOR UPDATE`를 사용했고, 예약 취소 후 승격 대상 대기열 조회는 일반 조회를 사용했다. 또한 대기 취소 service 메서드에 트랜잭션이 없어 단건 조회 락이 검증과 삭제까지 유지된다고 보기 어려웠다.

따라서 ADR-0012의 목표를 실제 코드에서 만족시키려면 다음 두 흐름이 같은 waiting row를 기준으로 직렬화되어야 한다.

- 예약 취소가 승격 대상 대기열을 선택하는 흐름
- 사용자가 자신의 대기를 취소하는 흐름

## 결정

명령 유스케이스에서만 비관적 락을 사용한다.

### 1. 일반 조회와 락 조회를 분리한다

`WaitingRepository`에 일반 조회와 `FOR UPDATE` 조회를 구분하는 메서드를 둔다.

```java
Optional<Waiting> findById(long waitingId);
Optional<Waiting> findByIdForUpdate(long waitingId);

List<Waiting> findAllBySlotIdOrderById(long slotId);
List<Waiting> findAllBySlotIdOrderByIdForUpdate(long slotId);
```

일반 조회는 화면 조회, 순번 계산, 저장 응답 계산처럼 락이 필요 없는 흐름에서 사용한다.

락 조회는 데이터 변경을 동반하는 명령 유스케이스에서만 사용한다.

### 2. 예약 취소의 승격 대상 조회는 대기열 row를 잠근다

예약 취소 시 같은 슬롯의 대기 목록을 다음 쿼리로 조회한다.

```sql
SELECT id, member_id, slot_id
FROM waiting
WHERE slot_id = :slotId
ORDER BY id
FOR UPDATE
```

이 조회는 예약 삭제와 승격 예약 저장, 승격 대기 삭제가 끝날 때까지 같은 트랜잭션 안에서 유지된다.

### 3. 대기 취소는 트랜잭션 안에서 단건 row를 잠근다

대기 취소 service 메서드에 `@Transactional`을 추가하고, 대기 조회는 다음 쿼리를 사용한다.

```sql
SELECT id, member_id, slot_id
FROM waiting
WHERE id = :waitingId
FOR UPDATE
```

이 락은 소유자 검증과 대기 삭제까지 유지된다.

## 대안

### 대안 1: 기존 `findAllBySlotIdOrderById`를 전부 `FOR UPDATE`로 바꾼다

장점:

- 구현 변경량이 작다.
- 예약 취소 승격 흐름에서 락 누락 가능성이 줄어든다.

단점:

- 대기 신청 후 순번 계산처럼 락이 필요 없는 흐름까지 불필요하게 row lock을 잡는다.
- 조회용 메서드인지 명령용 메서드인지 코드에서 드러나지 않는다.
- ADR-0012의 “전체 시스템 엄격 락은 피한다”는 방향과 맞지 않는다.

### 대안 2: 슬롯 row를 잠근다

장점:

- 같은 슬롯에 대한 예약 취소, 대기 신청, 대기 취소를 슬롯 단위로 직렬화할 수 있다.
- 대기 row가 없는 경우도 슬롯 row를 기준으로 잠글 수 있다.

단점:

- 현재 ADR-0012의 핵심 방어 대상보다 락 범위가 넓다.
- 대기 신청까지 더 강하게 직렬화되어 처리량이 낮아질 수 있다.
- 슬롯 단위 락이 실제로 필요한지는 추가 성능/동시성 테스트 후 판단하는 편이 낫다.

### 대안 3: 낙관적 락을 사용한다

장점:

- 충돌이 적은 환경에서는 대기 없이 처리할 수 있다.
- 장기적으로 버전 기반 충돌 감지 정책을 명확히 둘 수 있다.

단점:

- 현재 테이블에 version 컬럼이 없다.
- 충돌 시 재시도 또는 실패 응답 정책이 필요하다.
- 이번 미션의 범위에서는 비관적 락보다 구현 비용이 크다.

## 근거

ADR-0012의 목표는 모든 동시성 문제를 막는 것이 아니라, 사용자가 대기를 취소했다고 인지했는데 예약으로 승격되는 유령 예약을 막는 것이다.

이 시나리오에서는 승격 대상 waiting row와 취소 대상 waiting row가 동일하다. 따라서 해당 waiting row를 양쪽 명령 흐름에서 `FOR UPDATE`로 잠그면 핵심 충돌을 직접 방어할 수 있다.

일반 조회와 락 조회를 분리한 이유는 락의 의도와 비용을 코드에 드러내기 위해서다. `findAllBySlotIdOrderById`를 그대로 락 쿼리로 바꾸면 대기 신청 후 순번 계산처럼 데이터 정합성 보호가 필요 없는 기능에서도 락을 잡는다. 이는 성능 비용을 늘리고, 어떤 유스케이스가 락을 요구하는지 읽기 어렵게 만든다.

## 결과

좋아지는 점:

- 예약 취소 승격과 대기 취소가 같은 waiting row 기준으로 직렬화된다.
- 대기 취소의 `FOR UPDATE` 락이 소유자 검증과 삭제까지 유지된다.
- 조회용 repository 메서드와 명령용 repository 메서드의 의도가 분리된다.
- ADR-0012의 핵심 방어 대상에 맞춰 락 범위를 제한할 수 있다.

감수해야 하는 점:

- repository port에 `ForUpdate` 메서드가 추가되어 DB 락 의도가 application port 이름에 드러난다.
- H2와 운영 DB의 `SELECT ... FOR UPDATE` 동작 차이를 주의해야 한다.
- 같은 슬롯에 대기가 없는 상태에서의 대기 생성/예약 취소 경합은 이번 결정의 핵심 방어 대상이 아니다.

후속 제약:

- 명령 흐름에서 락 조회를 사용할 때는 반드시 트랜잭션 경계 안에서 호출해야 한다.
- 단순 조회나 응답 계산 흐름에 `ForUpdate` 메서드를 사용하지 않는다.
- 동시성 테스트를 추가해 ADR-0012의 검증 방법을 코드로 고정해야 한다.
- 운영 DB가 H2가 아니라면 해당 DB의 `FOR UPDATE` 동작과 인덱스 사용 여부를 확인해야 한다.

## 검증 방법

현재 검증된 항목:

- `ReservationServiceTest`에서 예약 취소 시 승격 대상 대기열 조회가 `findAllBySlotIdOrderByIdForUpdate(...)`를 사용한다.
- `WaitingServiceTest`에서 대기 취소 시 `findByIdForUpdate(...)`를 사용한다.
- `JdbcWaitingRepositoryTest`에서 `FOR UPDATE` 조회 메서드가 대기 row를 정상 조회한다.
- `ReservationTransactionIntegrationTest`에서 승격 중 실패 시 기존 예약과 대기가 롤백되는지 확인한다.
- 전체 테스트는 `./gradlew test`로 통과한다.

추가해야 할 검증:

- 스레드 A가 대기 취소를 시도하고 스레드 B가 같은 슬롯의 예약 취소를 시도하는 race condition 테스트를 추가한다.
- 결과는 둘 중 하나여야 한다.
- 대기가 먼저 취소되면 승격 대상에서 제외된다.
- 승격이 먼저 진행되면 대기 취소는 이미 승격/삭제된 상태를 기준으로 처리된다.
- 사용자가 취소 완료로 인지한 대기가 예약으로 남는 상태는 발생하지 않아야 한다.

## 관련

- `docs/adr/ADR-0012-transaction-and-concurrency-strategy.md`
- `docs/adr/ADR-0009-waiting-promotion-transaction-boundary.md`
- `src/main/java/roomescape/reservation/application/ReservationService.java`
- `src/main/java/roomescape/waiting/application/WaitingService.java`
- `src/main/java/roomescape/waiting/application/port/out/WaitingRepository.java`
- `src/main/java/roomescape/waiting/adapter/out/persistence/JdbcWaitingRepository.java`
