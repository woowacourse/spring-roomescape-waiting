# ADR-0009: 예약 취소와 대기 승격을 하나의 트랜잭션으로 묶는다

## 상태

Proposed

## 맥락

사이클2에서는 예약 취소 시 대기 1번을 자동으로 예약으로 전환하는 방식을 선택했다.

이 흐름은 단순한 예약 삭제가 아니라 여러 데이터 변경을 함께 수행한다.

```text
예약 삭제
-> 첫 번째 대기자를 예약으로 저장
-> 승격된 대기를 삭제
```

이 중 일부만 성공하면 데이터 일관성이 깨진다.

예를 들어 예약 삭제 후 대기 승격 중 예외가 발생했는데 예약 삭제만 커밋되면, 슬롯은 비어 있고 대기자는 그대로 남는 상태가 된다. 반대로 예약 저장은 성공했지만 대기 삭제가 실패하면 같은 사용자가 예약과 대기를 동시에 가지는 상태가 될 수 있다.

추가 요구사항도 "토론에서 정한 트랜잭션 경계에 맞춰 함께 일어나야 하는 데이터 변경을 묶고, 중간 실패 시 데이터 일관성이 유지되는지 테스트로 확인"하라고 요구한다.

따라서 예약 취소와 대기 승격의 트랜잭션 경계를 명확히 정하고, 실제 DB 상태로 롤백을 검증할 필요가 있다.

## 결정

예약 취소와 자동 대기 승격은 하나의 트랜잭션으로 처리한다.

트랜잭션 경계는 예약 취소 유스케이스의 public service 메서드에 둔다.

```java
@Transactional
public void deleteById(long reservationId)

@Transactional
public void deleteByIdForUser(long reservationId, long memberId)
```

현재 흐름은 다음 순서를 따른다.

```text
취소 대상 Reservation 조회
-> 사용자 요청이면 소유자 검증
-> 과거 예약 여부 검증
-> 같은 슬롯의 WaitingLine 조회
-> Reservation 삭제
-> WaitingLine.first()가 있으면 WaitingPromotionPolicy로 Reservation 생성
-> 승격 예약 저장
-> 승격된 Waiting 삭제
```

대기 순번은 별도 컬럼으로 갱신하지 않는다.

승격된 대기가 삭제되면 남은 대기 목록을 `WaitingLine`으로 다시 해석할 때 순번이 자연스럽게 재계산된다.

트랜잭션 검증은 Mockito 단위 테스트가 아니라 Spring 통합 테스트에서 실제 DB 상태를 조회해 확인한다.

## 대안

### 대안 1: 예약 삭제만 트랜잭션으로 처리하고 승격은 별도 처리한다

장점:

- 예약 삭제 흐름이 단순하다.
- 승격 실패가 예약 취소 실패로 이어지지 않는다.
- 나중에 비동기 처리로 전환하기 쉽다.

단점:

- 예약 취소 후 승격 실패 시 슬롯이 비어 있는 중간 상태가 생긴다.
- 사용자 입장에서는 대기 1번이 자동 승격되지 않는 순간이 생긴다.
- 사이클2에서 요구하는 "함께 일어나야 하는 데이터 변경"을 하나의 경계로 설명하기 어렵다.

### 대안 2: 예약 삭제, 예약 생성, 대기 삭제를 하나의 트랜잭션으로 묶는다

장점:

- 예약 취소와 대기 승격이 원자적으로 처리된다.
- 중간 실패 시 기존 예약과 대기 상태가 유지된다.
- API 통합 테스트와 트랜잭션 통합 테스트로 요구사항을 명확히 검증할 수 있다.

단점:

- 예약 취소 유스케이스가 단순 삭제보다 복잡해진다.
- 승격 실패가 예약 취소 실패로 이어진다.
- 같은 슬롯의 대기열을 조회하고 승격하는 동안 동시성 문제가 남아 있을 수 있다.

### 대안 3: 승격을 이벤트 또는 비동기로 처리한다

장점:

- 예약 취소 응답을 빠르게 줄 수 있다.
- 승격 실패 재시도, 알림 발송 같은 후속 기능을 확장하기 쉽다.
- 대규모 서비스에서는 처리량 측면에서 유리할 수 있다.

단점:

- 현재 프로젝트 규모에 비해 구현 복잡도가 크다.
- eventual consistency를 사용자가 이해할 수 있는 화면/API 설계가 필요하다.
- 사이클2의 트랜잭션 일관성 학습 목표와는 거리가 있다.

## 근거

현재 프로젝트의 목표는 자동 승격 정책을 안정적으로 구현하고, 도메인 흐름과 트랜잭션 경계를 학습하는 것이다.

예약 취소와 대기 승격은 같은 슬롯 상태를 바꾸는 하나의 유스케이스다. 사용자는 예약 취소 이후 대기 순번이 즉시 재정렬되고, 대기 1번이 예약으로 전환되기를 기대한다.

따라서 현재 단계에서는 이벤트나 비동기보다 하나의 트랜잭션으로 묶는 방식이 요구사항과 학습 목표에 더 적합하다.

또한 mock 기반 `ReservationServiceTest`는 메서드 호출 순서나 "대기 삭제를 호출하지 않는다" 같은 흐름은 검증할 수 있지만, 실제 롤백은 검증할 수 없다. 트랜잭션은 Spring proxy와 실제 DB 변경이 있어야 의미가 있으므로 `ReservationTransactionIntegrationTest`에서 `JdbcTemplate`으로 DB 상태를 직접 확인한다.

## 결과

좋아지는 점:

- 예약 삭제, 승격 예약 저장, 승격 대기 삭제가 원자적으로 처리된다.
- 승격 중 실패해도 기존 예약과 대기가 유지된다.
- 남은 대기 순번은 `WaitingLine` 조회 시 재계산되므로 별도 순번 업데이트 로직이 필요 없다.
- 사이클2의 트랜잭션 요구사항을 테스트로 설명할 수 있다.

감수해야 하는 점:

- 승격 과정에서 예외가 발생하면 예약 취소 요청 자체가 실패한다.
- 예약 취소 service 메서드는 대기 승격까지 조율하므로 책임이 커졌다.
- 동시 요청이 들어오는 경우 같은 슬롯 대기열을 안전하게 직렬화하는 문제는 아직 해결하지 않았다.

후속 제약:

- 예약 취소 흐름에서 repository 변경 순서를 바꿀 때 트랜잭션 테스트를 유지해야 한다.
- `ReservationService.deleteReservation(...)`을 리팩토링하더라도 같은 트랜잭션 경계를 깨면 안 된다.
- 동시성 처리를 도입한다면 슬롯 단위 락 또는 대기열 조회 락을 별도 ADR로 결정해야 한다.

## 검증 방법

API 요구사항 검증:

- `ReservationApiIntegrationTest`
  - 예약 취소 시 첫 번째 대기가 `RESERVED` 상태로 조회된다.
  - 두 번째 대기는 `WAITING` 상태로 남고 `waitingOrder = 1`로 재정렬된다.

트랜잭션 검증:

- `ReservationTransactionIntegrationTest`
  - `WaitingPromotionPolicy.promote(...)`에서 예외를 발생시킨다.
  - 예약 취소 API는 실패한다.
  - 기존 예약 `reservation.id = 1`이 DB에 남아 있다.
  - 기존 대기 `member_id = 2`, `slot_id = 1`이 DB에 남아 있다.
  - 승격 예약 `member_id = 2`, `slot_id = 1`은 생성되지 않는다.

전체 테스트:

```bash
./gradlew test
```

## 열린 질문

- 같은 슬롯에 대한 예약 취소, 대기 취소, 대기 생성이 동시에 들어올 때 어떤 락 전략을 사용할지 결정해야 한다.
- 예약 취소 API가 승격 실패 시 `500`을 그대로 응답하는 것이 적절한지, 도메인 예외로 감싸 별도 에러 코드를 줄지 결정해야 한다.
- 관리자 예약 취소와 사용자 예약 취소에서 자동 승격 실패를 동일하게 처리할지 유지해야 한다.
- 승격 대상 사용자가 같은 시간대의 다른 예약을 이미 가지고 있는 경우를 막을지 결정해야 한다.

## 관련

- `src/main/java/roomescape/reservation/application/ReservationService.java`
- `src/main/java/roomescape/waiting/WaitingLine.java`
- `src/main/java/roomescape/waiting/WaitingPromotionPolicy.java`
- `src/test/java/roomescape/reservation/ReservationApiIntegrationTest.java`
- `src/test/java/roomescape/reservation/ReservationTransactionIntegrationTest.java`
- `docs/adr/ADR-0006-automatic-waiting-promotion.md`
