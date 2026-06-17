# JPA 연결 미션 리뷰 노트

## 목적

우아한테크코스 방탈출 예약/대기 미션에 JPA를 연결한 뒤, 요구사항과 코드 구조를 빠르게 이해하기 위한 리뷰 노트다.

## 먼저 읽을 흐름

1. `README.md`의 구현 기능 목록과 API 명세로 외부 동작을 확인한다.
2. `src/main/java/roomescape/*/domain`의 JPA 엔티티를 읽어 DB 테이블과 도메인 규칙의 연결을 확인한다.
3. `src/main/java/roomescape/*/application/port/out`의 저장소 포트와 `adapter/out/persistence` 구현체를 비교한다.
4. `ReservationService`, `WaitingService`, `ReservationTimeService`에서 유스케이스 흐름과 트랜잭션 경계를 확인한다.
5. `src/test/java/roomescape/**/*RepositoryTest.java`, API 통합 테스트, `ReservationTransactionIntegrationTest`로 실제 DB 검증 범위를 확인한다.

## 확인된 좋은 점

- 애플리케이션 서비스가 `JpaRepository`를 직접 사용하지 않고 저장소 포트를 통해 의존한다.
- JPA 어댑터와 Spring Data 인터페이스가 분리되어, 포트 구현 책임과 Spring Data 쿼리 선언 책임이 구분된다.
- 예약/대기/슬롯의 핵심 유일성 규칙이 DB unique constraint로도 보호된다.
- 예약 취소와 대기 승격이 하나의 트랜잭션으로 묶여 있다.
- 대기 승격과 대기 취소 충돌은 명령용 `PESSIMISTIC_WRITE` 조회로 방어한다.
- 없는 대기 취소는 `WAITING_404`로 실패한다는 정책이 서비스/API/동시성 테스트에 반영되어 있다.

## 주의할 점

- `ReservationService.findMyReservations()`는 예약 응답과 대기 응답을 단순 concat한다. 화면 정렬 요구가 있으면 기준을 추가해야 한다.
- 대기 순번은 저장 컬럼이 아니라 조회 시 계산 값이다. 대기 삭제나 승격 뒤 별도 순번 갱신 로직을 찾으면 안 된다.
- `FOR UPDATE`는 H2 테스트에서 검증되어 있지만, 운영 DB가 바뀌면 락 범위와 인덱스 사용을 재확인해야 한다.
- `deleteById` 계열 중 예약 취소와 대기 취소는 없는 리소스 처리 정책이 다르다. 예약은 없는 id를 성공 처리하고, 대기는 404로 실패한다.
- ADR 일부는 과거 JDBC 명칭을 포함할 수 있다. 현재 구현 기준은 `Jpa*Repository` 어댑터와 `SpringData*Repository` 인터페이스다.

## 요구사항-코드 매핑

| 요구사항 | 핵심 코드 | 검증 위치 |
| --- | --- | --- |
| 빈 슬롯만 직접 예약 가능 | `ReservationService.throwIfSlotUnavailableForReservation`, `SlotOccupancy.isReservable` | `ReservationApiIntegrationTest`, `ReservationServiceTest` |
| 예약/대기가 있는 슬롯만 대기 가능 | `WaitingService.validateWaitingTargetExists`, `SlotOccupancy.isWaitable` | `WaitingServiceTest`, `WaitingApiIntegrationTest` |
| 같은 사용자의 중복 대기 방지 | `waiting` unique constraint, `WaitingService.validateWaitingByMemberNotExists` | `JpaWaitingRepositoryTest`, `WaitingApiIntegrationTest` |
| 예약 취소 시 첫 대기 자동 승격 | `ReservationService.cancelReservation`, `WaitingPromotionPolicy` | `ReservationServiceTest`, `ReservationApiIntegrationTest` |
| 대기 순번 재계산 | `WaitingLine`, `WaitingLines` | `WaitingLineTest`, `WaitingLinesTest`, `ReservationApiIntegrationTest` |
| 승격/취소 충돌 방어 | `findAllBySlotIdOrderByIdForUpdate`, `findByIdForUpdate` | `ReservationTransactionIntegrationTest` |

## 다음 리뷰 질문

- 조회 응답 정렬 기준이 사용자에게 충분히 예측 가능한가?
- 없는 예약 취소 성공 정책을 관리자/사용자 API 모두에서 유지할 것인가?
- 운영 DB 전환 시 비관적 락 쿼리와 unique constraint 이름이 그대로 동작하는가?
- projection 반환 타입을 application port 밖으로 더 숨길 필요가 있는가?

## 우선순위별 패치 후보

### High

- README/API 문서에서 삭제 정책을 엔드포인트별로 분리한다. 현재 예약 삭제는 없는 id를 no-op 성공으로 처리하고, 대기 삭제는 `WAITING_404`로 실패한다.
- 운영 DB를 전제한다면 `Theme.name`, `ReservationTime.startAt`에 DB unique constraint를 추가하고 `DataIntegrityViolationException`을 명시적인 409 응답으로 매핑한다.
- `application.yml`의 `ddl-auto: create-drop`과 `data.sql` 초기화가 로컬/미션 검증용이라는 점을 프로파일 문서에 명시한다.

### Medium

- `ThemeSaveRequest`, `ReservationTimeSaveRequest` 등 요청 DTO에 필수값/blank/길이 검증을 추가하고 README 요청 계약에 반영한다.
- `findMyReservations()`의 예약+대기 병합 응답 정렬 기준을 요구사항으로 확정하고 테스트로 고정한다.
- JPA/data 예외 중 사용자 입력 또는 충돌로 볼 수 있는 예외는 `GlobalExceptionHandler`에서 API 오류 포맷으로 변환한다.

### Low

- 과거 ADR의 `Jdbc*Repository` 경로/명칭을 현재 `Jpa*Repository`와 `SpringData*Repository` 기준으로 후속 ADR에서 보정한다.
- 삭제 메서드 이름에 `deleteIfExists`, `deleteOrThrow`처럼 정책이 드러나도록 정리할지 검토한다.
