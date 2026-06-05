# ADR-0011: 예약/대기 조회 흐름과 예약 수정 기능 제거 결정을 기록한다

## 상태

Proposed

## 맥락

`ReservationService.findMyReservations(...)`는 사용자의 예약 목록과 대기 목록을 함께 조회해 하나의 응답으로 반환한다.

초기 구현은 동작은 맞았지만 코드만 읽었을 때 다음 흐름을 바로 알기 어려웠다.

```text
내 예약 목록 조회
-> 내 대기 목록 조회
-> 대기 순번 계산
-> 예약 응답과 대기 응답 병합
```

또한 대기 순번 계산을 위해 내 대기 목록을 조회한 뒤 각 슬롯의 대기열을 다시 조회하는 구조가 있었다. 이 구조는 내 대기가 여러 슬롯에 걸쳐 있을수록 슬롯 수만큼 추가 쿼리가 발생한다.

다만 대기 순번은 단순 조회 정렬이 아니라 "대기열에서 신청 순서 기준으로 몇 번째인가"라는 정책이다. 따라서 SQL의 `ORDER BY`나 `ROW_NUMBER()`로 응답 값을 직접 만들기보다, 필요한 `Waiting` 목록을 가져온 뒤 `WaitingLine`, `WaitingLines`가 순번 정책을 담당하는 방향이 현재 프로젝트의 도메인 중심 리팩토링 방향과 더 잘 맞는다.

예약 수정 기능도 검토했다. 현재 대기 자동 승격 정책에서는 예약 취소 시 같은 슬롯의 첫 번째 대기가 예약으로 승격된다. 그런데 예약 수정은 기존 슬롯을 비우면서도 기존 슬롯의 대기를 승격하지 않는다. 이 기능을 유지하려면 "예약 변경 시 기존 슬롯의 대기를 자동 승격할지", "대기가 있는 예약은 변경을 막을지" 같은 추가 정책 결정이 필요하다.

현재 미션의 핵심은 예약 생성, 예약 취소, 대기 신청, 대기 자동 승격이다. 예약 수정은 이 흐름을 복잡하게 만들지만 필수 요구사항으로 보지 않았다.

## 결정

### 1. 내 예약 조회 흐름을 서비스 메서드에 드러낸다

`ReservationService.findMyReservations(...)`는 다음 흐름이 코드 표면에 보이도록 유지한다.

```java
public List<ReservationDetailFindResponse> findMyReservations(long memberId) {
    List<ReservationDetailFindResponse> reservations = findMyReservationResponses(memberId);
    List<ReservationDetailFindResponse> waitings = findMyWaitingResponses(memberId);

    return mergeMyReservations(reservations, waitings);
}
```

응답 DTO는 단일 projection을 response로 변환하는 책임만 가진다.

예약 목록과 대기 목록을 병합하는 유스케이스 흐름은 DTO의 `merge(...)` 같은 정적 메서드에 숨기지 않는다.

### 2. 대기 순번은 repository가 아니라 `WaitingLine`, `WaitingLines`가 계산한다

Repository는 여러 슬롯의 `Waiting` 목록을 한 번에 조회하는 책임만 가진다.

```java
List<Waiting> findAllBySlotIds(List<Long> slotIds);
```

이 메서드는 `waitingOrder` 정책을 표현하지 않는다. 따라서 메서드명에 `OrderBy...`를 넣지 않고, SQL에도 순번 계산용 `ORDER BY`를 두지 않는다.

대기 순번 정책은 다음 객체가 담당한다.

- `WaitingLine`: 한 슬롯의 대기열에서 특정 대기의 순번을 계산한다.
- `WaitingLines`: 여러 슬롯의 대기 목록을 슬롯별 `WaitingLine`으로 묶고, 특정 대기의 순번 계산을 위임한다.

### 3. 예약 삭제/취소 흐름은 단계 이름으로 표현한다

예약 삭제 API는 실제 도메인 행위상 "예약 취소"다. 따라서 서비스 내부 흐름은 다음 단계로 표현한다.

```text
findReservationIfExists
-> cancelReservationByUser 또는 cancelReservation
-> validateCancelable
-> findWaitingLineFor
-> deleteReservationOnly
-> promoteFirstWaitingIfExists
```

없는 예약 삭제 요청은 기존 정책대로 성공 처리한다.

### 4. 예약 수정 기능은 제거한다

예약 수정 API와 관련 코드를 제거한다.

제거 대상:

- 사용자/관리자 예약 `PATCH` endpoint
- `ReservationService.update...`
- `ReservationUpdateRequest`
- `ReservationRepository.updateSlotById(...)`
- `ReservationRepository.existsBySlotIdAndIdNot(...)`
- update 전용 에러 코드
- update 관련 테스트
- 클라이언트 예약 수정 폼, 모달, PATCH 호출

예약 변경이 필요하다면 현재 단계에서는 "기존 예약 취소 후 새 예약 생성"으로 다룬다.

## 대안

### 대안 1: 기존 구조를 유지한다

장점:

- 변경 범위가 작다.
- 기존 테스트를 거의 수정하지 않아도 된다.
- 서비스 코드가 한 곳에 모여 있어 빠르게 추적할 수 있다.

단점:

- `findMyReservations(...)`의 실제 흐름이 DTO `merge(...)`와 private 메서드 안에 숨는다.
- 대기 순번 계산을 위한 슬롯별 추가 조회가 계속 발생한다.
- 서비스가 `Map<Long, WaitingLine>` 같은 내부 자료구조를 직접 다룬다.
- 예약 수정 기능이 대기 자동 승격 정책과 불일치한 채 남는다.

### 대안 2: 대기 순번을 SQL에서 계산한다

장점:

- 조회 쿼리 한 번으로 응답에 필요한 순번을 만들 수 있다.
- application 코드가 줄어든다.
- DB가 정렬과 순번 계산을 직접 처리한다.

단점:

- 대기 순번 정책이 SQL에 묻힌다.
- `WaitingLine` 도메인 객체의 존재 이유가 약해진다.
- 순번 정책을 테스트하려면 repository 또는 통합 테스트에 더 의존하게 된다.

### 대안 3: 여러 슬롯의 대기 목록을 한 번에 조회하고 application에서 순번을 계산한다

장점:

- 슬롯별 추가 조회를 줄일 수 있다.
- repository는 데이터 조회만 담당한다.
- 대기 순번 정책은 `WaitingLine`, `WaitingLines`에서 테스트할 수 있다.
- 서비스는 `WaitingLines.orderOf(...)`만 호출하므로 내부 자료구조를 알 필요가 없다.

단점:

- SQL에서 직접 순번을 계산하는 방식보다 application 코드가 조금 늘어난다.
- `WaitingLines`라는 일급 컬렉션을 추가로 관리해야 한다.

### 대안 4: 예약 수정 기능을 유지한다

장점:

- 사용자는 예약을 취소하지 않고 날짜/시간을 바꿀 수 있다.
- 기존 클라이언트 UI를 유지할 수 있다.

단점:

- 기존 슬롯에 대기가 있을 때 자동 승격 여부를 별도로 결정해야 한다.
- 예약 취소와 예약 수정의 슬롯 비우기 정책이 달라질 수 있다.
- 정책을 제대로 맞추려면 수정 기능이 단순 update가 아니라 복합 유스케이스가 된다.

### 대안 5: 예약 수정 기능을 제거한다

장점:

- 예약 취소와 대기 자동 승격 정책을 단순하게 유지할 수 있다.
- 기능 표면이 줄어 테스트와 유지보수 비용이 줄어든다.
- 미션 핵심 요구사항에 집중할 수 있다.

단점:

- 사용자는 예약 변경을 취소 후 재예약으로 처리해야 한다.
- 클라이언트에서 수정 UI를 제거해야 한다.

## 근거

현재 프로젝트는 네오 코드의 기준처럼 "코드만 읽어도 유스케이스 흐름이 드러나는 구조"와 "도메인 정책을 도메인 객체에 둔다"는 방향으로 리팩토링하고 있다.

`findMyReservations(...)`는 단순 조회처럼 보이지만 실제로는 예약과 대기라는 서로 다른 상태를 하나의 응답으로 합치는 유스케이스다. 따라서 이 흐름을 DTO 정적 메서드나 `Map` 조립 로직에 숨기지 않고 service 메서드에 드러내는 것이 읽기 쉽다.

대기 순번은 `waiting.id` 기준으로 계산되는 정책이다. repository가 순서 있는 결과를 반환해도, 그 순서를 그대로 믿어 순번을 결정하면 정책 위치가 흐려진다. 따라서 repository는 필요한 목록을 제공하고, 순번 계산은 `WaitingLine`, `WaitingLines`가 맡는다.

예약 수정은 현재 대기 자동 승격 정책과 맞물리면 별도의 정책 결정이 필요한 기능이다. 필수 요구사항이 아니라면 제거하는 편이 현재 구조의 일관성과 학습 목표에 더 적합하다.

## 결과

좋아지는 점:

- `findMyReservations(...)`의 흐름이 코드 표면에 드러난다.
- 대기 순번 정책이 SQL이나 repository 메서드명에 묻히지 않는다.
- `WaitingLines`가 여러 슬롯 대기열 묶음이라는 개념을 표현한다.
- service가 `Map<Long, WaitingLine>` 같은 자료구조 세부사항을 직접 다루지 않는다.
- 예약 수정 기능 제거로 대기 자동 승격 정책과 충돌할 가능성이 줄어든다.

감수해야 하는 점:

- `WaitingLines`라는 도메인 객체가 추가된다.
- 예약 수정 UI/API가 사라진다.
- 예약 변경 요구가 다시 생기면 별도 정책 ADR을 작성해야 한다.

후속 제약:

- `waitingOrder`를 계산하기 위해 SQL `ROW_NUMBER()`나 repository `OrderBy...` 메서드에 의존하지 않는다.
- DTO에 유스케이스 흐름을 숨기는 `merge(...)` 같은 정적 메서드를 추가하지 않는다.
- 예약 수정 기능을 다시 도입하려면 기존 슬롯 대기 승격 여부를 먼저 결정한다.

## 검증 방법

코드 리뷰 체크포인트:

- `ReservationService.findMyReservations(...)`가 예약 조회, 대기 조회, 병합 흐름을 드러내는지 확인한다.
- `WaitingRepository.findAllBySlotIds(...)`가 순번 정책을 표현하지 않는지 확인한다.
- `WaitingLine`, `WaitingLines` 테스트가 대기 순번 계산을 검증하는지 확인한다.
- 예약 `PATCH` endpoint, `ReservationUpdateRequest`, update repository 메서드가 남아 있지 않은지 확인한다.

검색 검증:

```bash
rg "ReservationUpdate|PatchMapping|PATCH|updateSlotById|existsBySlotIdAndIdNot"
```

전체 테스트:

```bash
./gradlew test
```

## 열린 질문

- 예약 변경 요구가 다시 생기면 "취소 후 재예약"으로 충분한지, 별도 예약 변경 유스케이스가 필요한지 결정해야 한다.
- `findMyReservations(...)`를 `FindMyReservationsUseCase` 또는 별도 QueryService로 분리할지 결정해야 한다.
- `WaitingLines`를 현재 `waiting` 도메인 패키지에 유지할지, 향후 패키지 구조 전환 시 `domain/waiting`으로 이동할지 결정해야 한다.

## 관련

- `src/main/java/roomescape/reservation/application/ReservationService.java`
- `src/main/java/roomescape/waiting/WaitingLine.java`
- `src/main/java/roomescape/waiting/WaitingLines.java`
- `src/main/java/roomescape/waiting/infrastructure/WaitingRepository.java`
- `src/main/java/roomescape/reservation/presentation/UserReservationController.java`
- `src/main/java/roomescape/reservation/presentation/ManagerReservationController.java`
- `client/common.js`
- `client/admin.js`
- `docs/adr/ADR-0010-hexagonal-structure-and-code-convention-refactoring.md`
