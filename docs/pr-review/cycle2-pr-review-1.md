# Cycle 2 - PR Review 1 수정 사항

PR: https://github.com/woowacourse/spring-roomescape-waiting/pull/511

---

## ✅ 리팩토링 할 것 목록

### 동시성 판단 기준

- [ ] **1. 예약 취소 중 대기 신청/대기 취소가 동시에 일어나는 상황을 어떻게 다룰지 정리한다.**
  - 받은 리뷰: 동시성 처리에서 UNIQUE 제약으로 다룬 범위 외에도 예약 취소와 대기 승급 중 같은 슬롯의 대기 줄이 변하는 상황을 확인하라.
  - 판단 기준: 현재 구현은 예약 생성 중복과 같은 이름 대기 중복처럼 DB 제약으로 명확히 막을 수 있는 상황만 해결했다. 예약 취소 중 대기 줄 변동은 트랜잭션만으로 완전히 직렬화되지 않는다.
  - 해결 방향: 이번 단계에서 비관적 락을 도입할지, 정책적으로 허용/재시도 안내할지 결정한다. 도입한다면 `slot` row를 기준으로 잠그고 `slot -> reservation -> reservation_waiting` 순서로 접근하도록 정리한다. 도입하지 않는다면 PR 본문과 문서에 남은 위험으로 명시한다.

### 대기 줄 도메인

- [x] **2. `ReservationWaitingLine.sequenceOf()`가 1부터 시작하는 순번을 반환하는 책임이 맞는지 검토한다.**
  - 받은 리뷰: `ReservationWaitingLine.sequenceOf()`에서 `index + 1`을 반환하는 것이 표현 계층의 약속인지 신경써라. 찾지 못했을 때 예외를 던지는 방식도 함께 고민하라.
  - 판단 기준: 대기 순번이 도메인에서 쓰이는 개념인가? 화면 표시만을 위한 번호인가? 도메인적 순서라면 `ReservationWaitingLine`이 관리할 수 있다. 표시만을 위한 번호라면 응답 조립 계층에서 변환하는 편이 낫다.
  - 해결 방향: `ReservationWaitingLine`은 0-based 위치를 `OptionalInt`로 반환하는 `indexOf`만 제공하도록 변경. 히스토리 응답에 필요한 1-based 순번 변환은 `MyWaitingLines.sequenceOf()`에서 처리

- [x] **3. `ReservationWaitingLine`이 `ReservationWaitingOrder`를 따로 보관해야 하는지 검토한다.**
  - 받은 리뷰: `ReservationWaitingLine`이 어떤 검증을 가질 수 있는지 고민하라. `ReservationWaitingLine`에서 `ReservationWaitingOrder`를 별도로 둔 이유를 설명하라. `ReservationWaiting`을 필드로 가질 수는 없는지 검토하라.
  - 판단 기준: 대기 줄의 검증은 개별 대기 이름이나 시각 검증이 아니라 "하나의 줄에 속한 대기들이 같은 예약/슬롯을 기다리는가", "순번 계산이 가능한 저장된 대기들인가" 같은 컬렉션 불변식이어야 한다. 현재 구조는 `ReservationWaitingLine`을 실제 예약 대기 줄에도 사용하고, 히스토리 조회의 순번 계산에도 재사용한다. 이 때문에 실제 도메인 객체인 `ReservationWaiting` 대신 최소 정보만 가진 `ReservationWaitingOrder`가 생겼고, `name`이 필요한 경우와 필요 없는 경우가 섞이면서 책임이 흐려졌다. 재사용을 위해 도메인 대기 줄과 조회 보조 계산 책임을 하나의 객체에 몰아넣은 구조로 판단한다.
  - 해결 내용: `ReservationWaitingLine`은 실제 `ReservationWaiting` 목록을 보관하는 도메인 객체로 좁혔다. 히스토리 조회에서 `MyWaitingOrder`를 기반으로 표시 순번을 계산하는 책임은 `MyWaitingLines`로 분리했다. 또한 대기 줄 생성 시 저장되지 않은 대기가 포함되거나, 서로 다른 슬롯의 대기가 하나의 줄로 묶이면 예외를 던지도록 컬렉션 불변식을 추가했다. 이를 통해 `ReservationWaitingLine`이 도메인 대기 줄의 검증을 직접 담당하도록 만들고, `ReservationWaitingOrder`와 `name == null` 허용 구조를 제거했다.
  - 리뷰 답변: 처음에는 대기 순번 계산 규칙을 재사용하려고 `ReservationWaitingLine` 내부에 `ReservationWaitingOrder` record를 두었습니다. 히스토리 조회에서는 `ReservationWaiting` 전체 객체가 아니라 `waitingId`, `requestedAt` 같은 일부 값만 조회하고 있어서, `ReservationWaiting`을 필드로 사용하면 히스토리 순번 계산에 재사용하기 어렵다고 생각하여 해당 방식으로 구현하였습니다. 리뷰를 받고 재확인해보니 이 재사용을 하기 위해 실제 대기 줄과 히스토리 조회 순번 계산 책임이 섞인 상태였던 것 같습니다. 또한 `ReservationWaitingLine`이 검증을 가진다면 조회용 순번 계산이 아니라 같은 슬롯을 기다리는 대기들인지, 순번 계산 가능한 저장된 대기들인지 같은 컬렉션 불변식을 다루는 편이 더 자연스럽다고 판단했습니다. 그래서 `ReservationWaitingLine`은 `ReservationWaiting` 목록을 다루는 도메인 객체로 책임을 좁히고, 저장된 대기와 동일 슬롯 검증을 추가했습니다. `MyWaitingOrder` 기반 순번 계산은 `MyWaitingLines` 쪽으로 분리했습니다.

- [x] **4. `ReservationWaitingOrder.name`이 `null`이 될 수 있는 구조를 제거한다.**
  - 받은 리뷰: `ReservationWaitingOrder.name`이 `null`이 될 수 있는 구조인지 확인하라.
  - 판단 기준: 이 문제는 `ReservationWaitingLine`을 히스토리 순번 계산에 재사용하면서 생긴 파생 문제다.
  - 해결 내용: 3번에서 `ReservationWaitingLine`과 `MyWaitingLines`의 책임을 분리하면서 `ReservationWaitingOrder`를 제거했으므로 함께 해결했다.

- [x] **5. 대기 순서 규칙을 SQL과 도메인 중 한 곳으로 모은다.**
  - 받은 리뷰: `findFirstBySlot`의 `ORDER BY requested_at, id`와 `ReservationWaitingLine`의 정렬 규칙이 중복되어 있다. 대기 순서 규칙의 주인을 한 곳으로 모아라.
  - 판단 기준: 대기 순서가 도메인 규칙이라면 한 곳에서 관리되어야 한다. 같은 정렬 기준이 SQL과 도메인에 중복되면 변경 시 불일치가 생길 수 있다.
  - 해결 내용: `ReservationWaitingRepository.findFirstBySlot()`을 제거하고, Repository는 `findLineBySlot()`으로 해당 슬롯의 대기 목록만 조회하도록 정리했다. 첫 번째 대기 선택은 `ReservationWaitingLine.first()`에서 처리하도록 변경해 요청 시각과 ID 기준 정렬 규칙을 도메인 객체에 모았다.
  - 리뷰 답변: 말씀해주신 것처럼 `findFirstBySlot`의 SQL 정렬과 `ReservationWaitingLine`의 정렬 기준이 중복되어 있었습니다. 대기 순서는 조회 최적화보다 도메인 규칙에 가까운 개념이라고 판단해서, Repository에서는 슬롯의 대기 목록만 조회하고 첫 번째 대기 선택은 `ReservationWaitingLine.first()`가 담당하도록 변경했습니다. 이를 통해 대기 순서 기준이 바뀌더라도 `ReservationWaitingLine`만 확인하면 되도록 정리했습니다.

### 슬롯 도메인과 테이블 승격

- [ ] **6. `ReservationSlot.createNew`, `ReservationSlot.of`의 의미를 생성자 또는 명확한 이름으로 정리한다.**
  - 받은 리뷰: `ReservationSlot.of`가 어떤 의미인지 명확히 하라. 정적 메서드로 나누기보다 생성자를 다르게 호출하는 방식도 검토
  - 판단 기준: 현재 `createNew`는 아직 저장되지 않은 슬롯 후보, `of`는 DB 식별자가 있는 슬롯을 의미한다. 하지만 이름만으로 이 차이가 충분히 드러나지 않는다.
  - 해결 방향: public 생성자와 private 공통 생성자를 사용하는 방식으로 바꾸거나, `newUnsavedSlot`, `existingSlot`처럼 의도를 드러내는 이름으로 변경한다. 도메인 객체 생성 경로가 리뷰어에게 바로 읽히는지 기준으로 판단한다.

- [ ] **7. `ReservationSlot`을 DB row로 둘 만큼 실체가 있는 개념인지 다시 정리한다.**
  - 받은 리뷰: `ReservationSlot`이 예약/대기처럼 사용자 요청에 따라 생성되는 데이터인지 확인하라. DB row로 쌓일 만큼 가치 있는 개념인지 근거를 정리하라.
  - 판단 기준: 테이블로 승격할 도메인 개념은 식별성과 생명주기, 독립적인 정책을 가져야 한다. 단순히 `date/theme/time` 조합을 줄이기 위한 객체라면 값 객체나 조회 조건으로 충분할 수 있다.
  - 해결 방향: 슬롯을 테이블로 유지한다면 "관리자가 열어둔 예약 가능 슬롯"이라는 생명주기를 명확히 하고, 사용자 요청으로 슬롯이 생성되지 않음을 코드와 문서에 드러낸다. 그렇지 않다면 `reservation_slot` 테이블을 제거하고 `date/theme/time` 조합으로 되돌리는 선택지도 비교한다.

- [ ] **8. `findExistingSlot()` 흐름이 슬롯의 존재 의미를 잘 드러내는지 검토한다.**
  - 받은 리뷰: 슬롯 개념이 DB에 들어오면서 혼란이 생긴다. `reservationSlotRepository.save(...)`가 실제로 호출되는지, 슬롯이 언제 생성되는지 드러내라.
  - 판단 기준: 현재 사용자 예약/대기 요청에서는 슬롯을 새로 저장하지 않고, 이미 존재하는 슬롯만 찾아 사용한다. 그렇다면 `createNew`로 후보 객체를 만든 뒤 `findExistingSlot`으로 조회하는 흐름이 "생성"처럼 보이지 않아야 한다.
  - 해결 방향: `ReservationSlot.createNew`가 조회용 후보 생성처럼 읽히는 문제를 해결한다. 예를 들어 `ReservationSlot.condition(...)` 또는 별도 조회 조건 객체를 두는 방식을 검토한다.

### 서비스 책임과 작업 단위

- [x] **9. 예약 취소 시 대기 승급 로직을 `ReservationService` private 메서드에 두는 것이 맞는지 검토한다.**
  - 받은 리뷰: 예약 취소 시 대기자가 승급되는 로직이 누구의 책임인지 정리하라. 다른 응용 계층이 등장했을 때 private 메서드가 복사되지 않도록 책임 위치를 고민하라.
  - 판단 기준: 예약 취소와 대기 승급은 단순 서비스 보조 로직이 아니라 같은 슬롯의 상태 전이다. 여러 유스케이스에서 재사용될 수 있다면 별도 도메인 서비스나 작업 단위 객체로 분리하는 편이 낫다.
  - 해결 내용: `ReservationCancellationService`를 추가해 "예약 취소 후 첫 대기 승급" 흐름을 별도 유스케이스로 분리했다. `ReservationService`는 삭제 대상 조회, 이름 검증, 과거 예약 검증처럼 요청 진입점에서 필요한 검증을 처리하고, 실제 취소/승급 작업은 `ReservationCancellationService.cancel()`에 위임하도록 정리했다.

- [x] **10. `ReservationService`의 private 메서드가 책임 분리 대신 가독성 분리만 하고 있는지 점검한다.**
  - 받은 리뷰: private 메서드가 많아지는 것이 클래스의 책임 과다 신호일 수 있음을 신경써라. private 메서드로 숨긴 로직을 다른 객체로 옮길 수 있는지 검토하라.
  - 판단 기준: private 메서드가 같은 추상화 수준의 세부 단계라면 괜찮지만, 다른 도메인 개념의 규칙을 숨기고 있다면 분리해야 한다.
  - 해결 내용: `cancelReservation`, `promoteFirstWaiting`, `savePromotedReservation`, `deleteReservation`은 취소/승급 유스케이스 책임으로 보고 `ReservationCancellationService`로 분리했다. `createNewReservation`, `updateReservationDateAndTime`, `updateReservationSlot`, 도메인 예외 변환은 예약 객체 생성/변경 보조 책임으로 보고 `ReservationFactory`로 분리했다. `findExistingSlot`, `validateCancelable`, `validateUpdatable`은 현재 요청 흐름의 조회/검증 단계라 `ReservationService`에 남겼다.
  - 리뷰 답변: private 메서드들을 다시 역할별로 나눠보니 단순히 메서드를 작게 만든 것이 아니라, 예약 취소 후 대기 승급이라는 별도 유스케이스와 예약 객체 생성/변경 보조 책임이 `ReservationService` 안에 함께 들어와 있었습니다. 그래서 취소/승급 흐름은 `ReservationCancellationService`로 분리하고, `Reservation.createNew`, `reservation.withSlot` 호출과 예외 변환은 `ReservationFactory`로 분리했습니다. `ReservationService`에는 요청 대상 조회, 이름 확인, 과거 예약 검증, 슬롯 조회처럼 현재 요청 흐름을 조립하는 책임만 남기도록 정리했습니다.

- [ ] **11. 예약 생성 흐름에 트랜잭션이 필요한지 전체적으로 확인한다.**
  - 받은 리뷰: 예약 생성 메서드에 트랜잭션이 불필요한지 전체적으로 확인하라.
  - 판단 기준: 예약 생성은 슬롯 조회, 중복 예약 조회, 예약 저장을 함께 수행한다. UNIQUE 제약으로 최종 중복은 막지만, 읽기와 쓰기 사이 상태 변화가 있을 수 있다.
  - 해결 방향: 트랜잭션이 없어도 DB 제약으로 정합성이 보장되는지, 트랜잭션을 붙였을 때 얻는 이점이 있는지 정리한다. 단순 원자성보다 "읽은 상태와 저장 시점의 상태가 달라져도 결과가 안전한가"를 기준으로 판단한다.

- [x] **12. `LocalDateTime.now()` 호출 시점을 요청 기준으로 통일한다.**
  - 받은 리뷰: 현재 시각이 요청이 온 시각인지, 각 메서드가 `now()`를 호출한 시각인지 전체적으로 확인하라.
  - 판단 기준: 하나의 요청 안에서 여러 번 `now()`를 호출하면 예약 가능 검증, 생성 시각, 수정 가능 검증의 기준 시각이 미세하게 달라질 수 있다.
  - 해결 내용: `ReservationService`의 예약 생성, 취소, 수정 흐름에서는 public 메서드 초입에서 `requestedAt`을 한 번 만들고 검증, 슬롯 변경, 대기 승급 예약 생성까지 같은 값을 전달하도록 수정했다. `ReservationTimeService.findAvailableTimes()`와 예약 페이지 슬롯 상태 조립도 목록 처리 중 반복해서 `now()`를 호출하지 않도록 기준 시각을 한 번만 잡도록 정리했다.
  - 리뷰 답변: 말씀해주신 것처럼 하나의 요청 안에서 `LocalDateTime.now()`가 여러 private 메서드에 흩어져 있으면 검증 기준과 생성 기준이 아주 미세하게 달라질 수 있다고 판단했습니다. 그래서 public 유스케이스 초입에서 `requestedAt`을 한 번 만들고 하위 메서드에 전달하도록 정리했습니다. 이번 수정에서는 구조 변경 비용을 고려해 `Clock` 주입까지는 진행하지 않았고, 우선 요청 단위 기준 시각을 통일하는 수준으로 반영했습니다.

### 조회 성능과 Repository 사용

- [ ] **13. 예약 가능 시간 조회에서 N+1 쿼리가 발생하는지 확인한다.**
  - 받은 리뷰: `findAvailableTimes`에서 쿼리가 몇 번 발생하는지 살펴보라.
  - 판단 기준: 현재 슬롯 목록을 가져온 뒤 각 슬롯마다 `reservationRepository.findBySlot(slot)`을 호출하면 슬롯 수만큼 추가 쿼리가 발생할 수 있다.
  - 해결 방향: 특정 날짜/테마의 슬롯과 예약 여부를 한 번에 조회하는 Repository 메서드 또는 전용 조회 모델을 도입한다. 단순 컬렉션처럼 쓰는 Repository 원칙과 조회 최적화 사이의 균형을 함께 판단한다.

- [ ] **14. 예약 시간 삭제 시 모든 예약을 가져오는 방식이 적절한지 검토한다.**
  - 받은 리뷰: 예약 시간 삭제 가능 여부를 확인하기 위해 모든 예약을 가져올 필요가 있는지 검토하라.
  - 판단 기준: 전체 예약을 메모리로 가져와 필터링하는 것은 데이터가 늘어날수록 비용이 커진다. 삭제 가능 여부는 DB에서 존재 여부만 확인하는 쪽이 효율적이다.
  - 해결 방향: `ReservationRepository`에 `existsByTimeId` 같은 메서드를 다시 둘지, 슬롯 기준으로 `findByTime` 또는 `existsReservationByTime` 같은 의도를 드러내는 조회를 둘지 결정한다. Repository를 컬렉션처럼 쓰려는 원칙과 성능 요구를 같이 비교한다.

### 기타 도메인 검증

- [x] **15. `Reservation`에서 검증용으로만 쓰는 값 객체를 필드로 가질 수 있는지 검토한다.**
  - 받은 리뷰: 이름 값 객체를 검증용으로만 사용하는지 확인하라. 도메인 필드로 가질 수는 없는지 검토하라.
  - 판단 기준: 값 객체가 단순 검증 도구가 아니라 도메인 의미를 가진다면 필드로 보관하는 편이 일관적이다. 다만 응답/저장 계층에서 문자열이 반복적으로 필요해지는 비용도 고려해야 한다.
  - 해결 내용: `ReservationWaiting`과 동일하게 `Reservation`도 `ReservationName`을 필드로 가지도록 변경했다. 응답 DTO와 Repository에서는 문자열 값이 필요하므로 `getName()`은 기존처럼 `String`을 반환하되, 도메인 내부의 상태는 값 객체로 보관하도록 정리했다.
  - 리뷰 답변: 말씀해주신 것처럼 `ReservationName`을 생성 시 검증에만 사용하고 다시 `String`으로 풀어 저장하고 있어 값 객체를 검증 도구처럼 사용하고 있었습니다. 예약자 이름은 예약과 대기 모두에서 같은 도메인 의미를 가지므로, `Reservation`도 `ReservationName`을 필드로 보관하도록 수정했습니다. 다만 응답과 저장 계층에서는 문자열 값이 필요해 `getName()`은 기존처럼 문자열을 반환하도록 유지했습니다.
