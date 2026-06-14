# Cycle 2 - PR Review 1 수정 사항

PR: https://github.com/woowacourse/spring-roomescape-waiting/pull/511

---

## ✅ 리팩토링 할 것 목록

### 대기 줄 도메인

- [x] **1. `ReservationWaitingLine.sequenceOf()`가 1부터 시작하는 순번을 반환하는 책임이 맞는지 검토한다.**
  - 받은 리뷰: `ReservationWaitingLine.sequenceOf()`에서 `index + 1`을 반환하는 것이 표현 계층의 약속인지 확인하라. 찾지 못했을 때 예외를 던지는 방식도 함께 고민하라.
  - 판단 기준: 대기 줄 내부의 위치와 사용자에게 보여줄 순번은 구분되어야 한다. 대기 줄 도메인은 줄 안에서의 위치를 계산하고, 1부터 시작하는 표시용 순번은 응답 조립 책임에 가깝다고 판단한다.
  - 해결 내용: `ReservationWaitingLine`은 0-based 위치를 `OptionalInt`로 반환하는 `indexOf()`만 제공하도록 변경했다. 히스토리 응답에 필요한 1-based 순번 변환은 히스토리 응답 조립 전용 객체인 `MyWaitingLines.sequenceOf()`에서 처리하도록 분리했다.

- [x] **2. `ReservationWaitingLine`이 `ReservationWaitingOrder`를 따로 보관해야 하는지 검토한다.**
  - 받은 리뷰: `ReservationWaitingLine`이 어떤 검증을 가질 수 있는지 고민하라. `ReservationWaitingOrder`를 별도로 둔 이유를 설명하고, `ReservationWaiting`을 필드로 가질 수 없는지 검토하라.
  - 판단 기준: 대기 줄의 검증은 개별 값 검증이 아니라 "하나의 줄에 속한 대기들이 같은 슬롯을 기다리는가", "순번 계산이 가능한 저장된 대기들인가" 같은 컬렉션 불변식이어야 한다. 기존 구조는 실제 대기 줄과 히스토리 조회 순번 계산을 하나의 객체에 몰아넣어 책임이 섞였다고 판단한다.
  - 해결 내용: `ReservationWaitingLine`은 실제 `ReservationWaiting` 목록을 보관하는 도메인 객체로 좁혔다. 히스토리 조회에서 `MyWaitingOrder`를 기반으로 표시 순번을 계산하는 책임은 `MyWaitingLines`로 분리했다. 대기 줄 생성 시 저장되지 않은 대기가 포함되거나 서로 다른 슬롯의 대기가 하나의 줄로 묶이면 예외를 던지도록 컬렉션 불변식도 추가했다.

- [x] **3. `ReservationWaitingOrder.name`이 `null`이 될 수 있는 구조를 제거한다.**
  - 받은 리뷰: `ReservationWaitingOrder.name`이 `null`이 될 수 있는 구조인지 확인하라.
  - 판단 기준: 이 문제는 `ReservationWaitingLine`을 히스토리 순번 계산에 재사용하면서 생긴 파생 문제다.
  - 해결 내용: `ReservationWaitingLine`과 `MyWaitingLines`의 책임을 분리하면서 `ReservationWaitingOrder`를 제거했다. 이에 따라 `name == null`을 허용하던 구조도 함께 사라졌다.

- [x] **4. 대기 순서 규칙을 SQL과 도메인 중 한 곳으로 모은다.**
  - 받은 리뷰: `findFirstBySlot`의 `ORDER BY requested_at, id`와 `ReservationWaitingLine`의 정렬 기준이 중복되어 있다. 대기 순서 규칙의 주인을 한 곳으로 모아라.
  - 판단 기준: 대기 순서는 도메인 규칙에 가까운 개념이다. SQL과 도메인에 같은 정렬 기준이 중복되면 변경 시 불일치가 생길 수 있다.
  - 해결 내용: `ReservationWaitingRepository.findFirstBySlot()`을 제거하고, Repository는 `findLineBySlot()`으로 해당 슬롯의 대기 목록만 조회하도록 정리했다. 첫 번째 대기 선택은 `ReservationWaitingLine.first()`에서 처리하도록 변경해 요청 시각과 ID 기준 정렬 규칙을 도메인 객체에 모았다.

### 슬롯 도메인과 테이블 승격

- [x] **5. `ReservationSlot.createNew`, `ReservationSlot.of`의 의미를 생성자로 정리한다.**
  - 받은 리뷰: `ReservationSlot.of`가 어떤 의미인지 명확하지 않다. 정적 메서드로 나누기보다 생성자를 다르게 호출하는 방식도 검토하라.
  - 판단 기준: 기존 `createNew`는 아직 저장되지 않은 슬롯, `of`는 DB 식별자가 있는 슬롯을 의미했다. 하지만 정적 팩토리 이름을 따로 두는 것보다, 식별자가 없는 생성자와 식별자가 있는 생성자를 다르게 호출하는 편이 저장 전 객체와 저장소에서 복원한 객체의 차이를 더 단순하게 드러낸다고 판단한다.
  - 해결 내용: `ReservationSlot.createNew(...)`, `ReservationSlot.of(...)` 정적 팩토리를 제거했다. 새로 열거나 조회 조건으로 사용할 저장 전 슬롯은 `new ReservationSlot(date, theme, time)`으로 생성하고, Repository에서 DB row를 복원할 때는 `new ReservationSlot(id, date, theme, time)` 생성자를 사용하도록 변경했다.

- [x] **6. `ReservationSlot`을 DB row로 둘 만큼 실체가 있는 개념인지 다시 정리한다.**
  - 받은 리뷰: `ReservationSlot`이 예약/대기처럼 사용자 요청에 따라 생성되는 데이터인지 확인하라. DB row로 쌓일 만큼 가치 있는 개념인지 근거를 정리하라.
  - 판단 기준: `ReservationSlot`은 `date`, `theme`, `time` 조합의 중복 제거만을 위한 요소가 아니다. 사용자가 특정 날짜/테마/시간에 예약하고 싶어도 관리자가 해당 슬롯을 열어두지 않으면 예약 가능한 단위로 의미가 없다고 판단한다.
  - 해결 내용: `ReservationSlot`을 "관리자가 열어둔 예약 가능 단위"로 정의했다. 관리자 슬롯 생성 API와 관리자 슬롯 생성 페이지를 추가해 슬롯 생성 생명주기를 코드에 드러냈다. 사용자 예약/대기 요청에서는 슬롯을 새로 만들지 않고, 관리자가 미리 연 슬롯을 찾아 연결하는 방향을 유지한다.

- [x] **7. `findExistingSlot()` 흐름이 슬롯의 존재 의미를 잘 드러내는지 검토한다.**
  - 받은 리뷰: 슬롯 개념이 DB에 들어오면서 혼란이 생긴다. `reservationSlotRepository.save(...)`가 실제로 호출되는지, 슬롯이 언제 생성되는지 드러내라.
  - 판단 기준: 사용자 예약/대기 요청에서 슬롯을 새로 저장하지 않는 것은 의도한 정책이다. 혼란의 핵심은 `findExistingSlot()` 자체보다 `reservationSlotRepository.save(...)`가 호출되는 명확한 생성 유스케이스가 없었다는 점이라고 판단한다.
  - 해결 내용: 관리자 슬롯 생성 API와 페이지를 추가해 `reservationSlotRepository.save(...)`가 관리자 슬롯 오픈 흐름에서 호출되도록 했다. 이로써 슬롯은 관리자에 의해 생성되고, 사용자 흐름에서는 이미 열린 슬롯을 조회해서 사용하는 구조가 되었다.

### 서비스 책임과 작업 단위

- [x] **8. 예약 취소 시 대기 승급 로직의 책임 위치를 검토한다.**
  - 받은 리뷰: 예약 취소 시 대기자가 승급되는 로직이 누구의 책임인지 정리하라. 다른 응용 계층이 등장했을 때 private 메서드가 복사되지 않도록 책임 위치를 고민하라.
  - 판단 기준: 예약 취소와 대기 승급은 같은 슬롯의 상태를 바꾸는 하나의 작업 단위다. 다만 현재 요구사항 안에서는 별도 응용 서비스로 재사용되는 지점이 아직 없으므로, 서비스를 추가하는 것보다 `ReservationService`의 트랜잭션 흐름 안에서 읽히도록 두는 편이 단순하다고 판단한다. 대신 첫 대기 선택과 승급 예약 생성처럼 도메인 객체가 말할 수 있는 책임은 도메인으로 옮긴다.
  - 해결 내용: `ReservationCancellationService`를 제거하고 예약 취소/승급 흐름을 `ReservationService`의 트랜잭션 메서드 안으로 되돌렸다. 첫 번째 대기 선택은 `ReservationWaitingLine.first()`가 담당하고, 대기자가 예약으로 승급될 때 생성되는 예약은 `ReservationWaiting.toReservation()`으로 만들도록 정리했다.

- [x] **9. 리뷰 반영으로 구조가 더 복잡해지지 않았는지 점검한다.**
  - 받은 리뷰: private 메서드를 다른 객체로 옮기는 과정에서 구조가 더 복잡해지는 방향을 경계하라. Factory나 별도 Cancel Service가 정말 필요한 상황인지 검토하라.
  - 판단 기준: 별도 객체는 역할이 분명할 때만 유지한다. `ReservationFactory`는 도메인 객체 생성과 예외 변환만 감싸고 있어 독립된 빈으로 둘 만큼의 책임이 부족하다고 판단한다. `ReservationCancellationService` 역시 현재 단계에서는 실제 재사용 지점이 없고, 오히려 흐름을 따라가기 어렵게 만들 수 있다고 판단한다.
  - 해결 내용: `ReservationFactory`와 `ReservationCancellationService`를 제거했다. 예약 생성/변경 시 필요한 예외 변환은 해당 유스케이스를 처리하는 `ReservationService` 내부에 남겼고, 대기 승급 예약 생성은 `ReservationWaiting.toReservation()`으로 옮겨 도메인 객체가 자신의 상태를 기반으로 예약을 만들 수 있도록 했다.

- [x] **10. 예약 생성 흐름에 트랜잭션이 필요한지 전체적으로 확인한다.**
  - 받은 리뷰: 예약 생성 메서드에 트랜잭션이 불필요한지 전체적으로 확인하라.
  - 판단 기준: 예약 생성은 테마/시간/슬롯 조회, 중복 예약 조회, 예약 저장을 수행하지만 실제 데이터 변경은 `reservation` INSERT 하나다. 읽기와 쓰기 사이에 같은 슬롯 예약이 먼저 생성될 수는 있으나, 이 경우는 `reservation.slot_id` UNIQUE 제약이 최종적으로 막는다. 트랜잭션을 붙여도 기본 격리 수준에서는 조회 이후 저장 전까지의 경쟁 상태를 막지 못하므로, 단순히 `@Transactional`을 붙이는 것만으로 추가 정합성이 생기지는 않는다고 판단한다.
  - 해결 내용: 예약 생성 메서드에는 별도 트랜잭션을 추가하지 않는다. 동시 예약 생성은 DB UNIQUE 제약과 예외 변환으로 처리하고, 이 동작은 동시성 테스트로 보호한다. 여러 데이터를 함께 변경하는 예약 취소/대기 승급 흐름만 트랜잭션으로 묶는 현재 기준을 유지한다.

- [x] **11. `LocalDateTime.now()` 호출 시점을 요청 기준으로 통일한다.**
  - 받은 리뷰: 현재 시각이 요청이 온 시각인지, 각 메서드가 `now()`를 호출한 시각인지 전체적으로 확인하라.
  - 판단 기준: 하나의 요청 안에서 여러 번 `now()`를 호출하면 예약 가능 검증, 생성 시각, 수정 가능 검증의 기준 시각이 미세하게 달라질 수 있다.
  - 해결 내용: `ReservationService`의 예약 생성, 취소, 수정 흐름에서는 public 메서드 초입에서 `requestedAt`을 한 번 만들고 검증, 슬롯 변경, 대기 승급 예약 생성까지 같은 값을 전달하도록 수정했다. `ReservationTimeService.findAvailableTimes()`와 예약 페이지 슬롯 상태 조립도 목록 처리 중 반복해서 `now()`를 호출하지 않도록 기준 시각을 한 번만 잡도록 정리했다.

### 조회 성능과 Repository 사용

- [x] **12. 예약 가능 시간 조회에서 N+1 쿼리가 발생하는지 확인한다.**
  - 받은 리뷰: `findAvailableTimes`에서 쿼리가 몇 번 발생하는지 살펴보라.
  - 판단 기준: 기존에는 슬롯 목록을 조회한 뒤 각 슬롯마다 `reservationRepository.findBySlot(slot)`을 호출하므로 슬롯 수만큼 추가 쿼리가 발생할 수 있었다. 다만 "예약 가능한 슬롯"은 Repository가 알아야 할 저장소 조건이라기보다 서비스 유스케이스에서 판단할 정책에 가깝다고 판단한다.
  - 해결 내용: Repository에는 날짜/테마에 맞는 슬롯 목록과 예약 목록을 각각 조회하는 역할만 두고, 서비스에서 두 컬렉션을 비교해 예약 가능한 시간을 판단하도록 변경했다. 이로써 쿼리 수는 슬롯 수와 무관하게 슬롯 목록 조회 1번, 예약 목록 조회 1번으로 고정된다.

- [x] **13. 예약 시간 삭제 시 모든 예약을 가져오는 방식이 적절한지 검토한다.**
  - 받은 리뷰: 예약 시간 삭제 가능 여부를 확인하기 위해 모든 예약을 가져올 필요가 있는지 검토하라.
  - 판단 기준: 전체 예약을 메모리로 가져와 필터링하는 것은 데이터가 늘어날수록 비용이 커진다. 삭제 가능 여부는 DB에서 존재 여부만 확인하는 쪽이 효율적이다.
  - 해결 내용: `ReservationTimeService.deleteById()`에서 `reservationRepository.findAll()`로 전체 예약을 조회해 필터링하던 방식을 제거했다. 삭제 대상 예약 시간을 먼저 조회한 뒤, `ReservationRepository.existsByTime()`으로 해당 시간이 예약에 사용 중인지 DB에서 존재 여부만 확인하도록 변경했다.

### 기타 도메인 검증

- [x] **14. `Reservation`에서 검증용으로만 쓰는 값 객체를 필드로 가질 수 있는지 검토한다.**
  - 받은 리뷰: 이름 값 객체를 검증용으로만 사용하는지 확인하라. 도메인 필드로 가질 수는 없는지 검토하라.
  - 판단 기준: 값 객체가 단순 검증 도구가 아니라 도메인 의미를 가진다면 필드로 보관하는 편이 일관적이다. 다만 응답/저장 계층에서 문자열이 반복적으로 필요해지는 비용도 고려해야 한다.
  - 해결 내용: `ReservationWaiting`과 동일하게 `Reservation`도 `ReservationName`을 필드로 가지도록 변경했다. 응답 DTO와 Repository에서는 문자열 값이 필요하므로 `getName()`은 기존처럼 `String`을 반환하되, 도메인 내부의 상태는 값 객체로 보관하도록 정리했다.

## 동시성 판단 메모

리뷰어가 남긴 동시성 관련 리뷰는 특정 코드 수정보다 판단 기준을 꺼내보라는 성격에 가깝다고 정리한다. 이번 구현에서는 예약 생성 중복과 같은 이름 대기 중복처럼 DB 제약으로 명확히 막을 수 있는 경합은 UNIQUE 제약으로 처리했다. 예약 취소 후 대기 승급은 여러 데이터 변경이 함께 성립해야 하므로 트랜잭션으로 묶었다. 다만 예약 취소 중 대기 신청/대기 취소가 동시에 일어나는 상황을 완전히 직렬화하지는 않았다. 이 부분은 현재 미션의 트래픽과 민감도를 고려해 정책적으로 허용하고, 추후 수동 승인이나 관리자 강제 승급처럼 같은 슬롯의 상태를 동시에 바꾸는 유스케이스가 늘어나면 슬롯 row 기준 비관적 락을 검토한다.
