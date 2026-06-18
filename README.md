# 방탈출 예약/대기 서비스

## 1. 구현 기능 목록

### 예약
1. 사용자는 로그인 후 등록된 슬롯에 예약을 생성할 수 있다.
2. 관리자는 전체 예약 목록을 조회할 수 있다.
3. 사용자는 본인의 예약과 대기를 함께 조회할 수 있다.
4. 사용자는 본인의 예약을 취소할 수 있다.
5. 관리자는 예약을 취소할 수 있다.
6. 예약 취소 시 같은 슬롯의 첫 번째 대기가 자동으로 예약으로 전환된다.
7. 예약 수정 기능은 제공하지 않는다. 예약 변경은 기존 예약 취소 후 새 예약 생성으로 처리한다.

### 예약 대기
1. 이미 예약이 있거나 대기가 있는 슬롯에 대기를 신청할 수 있다.
2. 예약도 대기도 없는 슬롯에는 대기를 신청할 수 없다.
3. 본인이 이미 예약한 슬롯에는 대기를 신청할 수 없다.
4. 같은 사용자는 같은 슬롯에 중복 대기할 수 없다.
5. 대기 순번은 같은 슬롯의 대기 `id` 오름차순으로 계산한다.
6. 사용자는 본인의 대기를 취소할 수 있다.
7. 대기 취소는 soft delete가 아니라 hard delete로 처리한다.

### 대기 자동 전환과 순번 재정렬
1. 자동 전환 방식을 선택했다. 수동 승인 API는 제공하지 않는다.
2. 예약이 취소되면 같은 슬롯의 대기열에서 첫 번째 대기가 예약으로 승격된다.
3. 승격된 대기는 대기 목록에서 삭제된다.
4. 남은 대기는 별도 순번 컬럼을 갱신하지 않고, 조회 시 `WaitingLine`으로 다시 계산한다.
5. 예약 취소와 대기 승격은 하나의 트랜잭션으로 처리한다.
6. 승격 중 실패하면 예약 취소, 승격 예약 생성, 승격 대기 삭제가 함께 롤백된다.
7. 예약 취소로 인한 승격과 해당 대기자의 대기 취소가 동시에 발생하는 유령 예약 시나리오는 `FOR UPDATE` 기반 비관적 락으로 방어한다.

### 내 예약 목록 조회
1. 사용자의 예약과 대기를 하나의 목록으로 조회한다.
2. 예약은 `RESERVED`, 대기는 `WAITING` 상태로 구분한다.
3. 대기 응답에는 현재 대기 순번을 포함한다.
4. 예약 응답의 `waitingOrder`는 `null`이다.

### 타임대
1. 관리자는 타임대를 추가할 수 있다.
2. 사용자는 날짜와 테마 기준으로 예약 가능 시간을 조회할 수 있다.
3. 관리자는 전체 타임대를 조회할 수 있다.
4. 관리자는 특정 타임대를 삭제할 수 있다.

### 테마
1. 관리자는 테마를 생성할 수 있다.
2. 관리자는 특정 테마를 삭제할 수 있다.
3. 사용자는 테마 목록을 조회할 수 있다.
4. 사용자는 날짜 기준으로 예약 가능한 테마 목록을 조회할 수 있다.
5. 사용자는 최근 1주 인기 테마를 조회할 수 있다.

### 슬롯
1. 관리자는 슬롯을 생성할 수 있다.
2. 관리자는 슬롯 목록을 조회할 수 있다.
3. 관리자는 특정 슬롯을 삭제할 수 있다.

### 인증/인가
1. 사용자는 이름과 비밀번호로 로그인할 수 있다.
2. 로그인 성공 시 토큰을 발급한다.
3. 로그인하지 않은 사용자는 공개 API 외 요청 시 인증 예외가 발생한다.
4. 관리자 전용 API에 일반 사용자가 접근하면 인가 예외가 발생한다.

### 예외 상황
1. 등록되지 않은 슬롯으로 예약 또는 대기를 시도하면 예외가 발생한다.
2. 과거 슬롯은 예약 생성, 예약 취소, 대기 신청 대상이 될 수 없다.
3. 이미 예약 또는 대기가 있는 슬롯에는 직접 예약할 수 없다.
4. 예약 또는 슬롯에서 사용 중인 시간/테마 삭제는 제한된다.
5. 본인 소유가 아닌 예약 또는 대기 취소는 거부된다.
6. 없는 예약 취소 요청은 성공 처리한다. 없는 대기 취소 요청은 `404 Not Found`와 `WAITING_404`로 실패 처리한다.

## 2. API 명세

### 공통 응답 포맷

성공 응답 예시:

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

실패 응답 예시:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "string",
    "message": "string"
  }
}
```

삭제 API는 성공 시 `204 No Content`를 반환하며 응답 본문이 없다.

### 인증

#### 로그인

```http
POST /login
```

요청:

```json
{
  "name": "string",
  "password": "string"
}
```

응답:

- `200 OK`
- 인증 토큰 발급

#### 로그아웃

```http
POST /logout
```

응답:

- `204 No Content`

### 사용자 예약 API

#### 예약 생성

```http
POST /api/user/reservations
```

요청:

```json
{
  "date": "2026-05-05",
  "timeId": 1,
  "themeId": 1
}
```

응답:

- `201 Created`

```json
{
  "success": true,
  "data": {
    "id": 1,
    "memberId": 1,
    "slotId": 1
  },
  "error": null
}
```

#### 내 예약/대기 목록 조회

```http
GET /api/user/reservations/me
```

응답:

- `200 OK`

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "memberName": "brown",
      "date": "2026-05-05",
      "theme": {
        "id": 1,
        "name": "세기의 도둑",
        "description": "string",
        "thumbnailUrl": "string"
      },
      "time": {
        "id": 1,
        "time": "10:00"
      },
      "status": "RESERVED",
      "waitingOrder": null
    },
    {
      "id": 2,
      "memberName": "brown",
      "date": "2026-05-05",
      "theme": {
        "id": 1,
        "name": "세기의 도둑",
        "description": "string",
        "thumbnailUrl": "string"
      },
      "time": {
        "id": 2,
        "time": "11:00"
      },
      "status": "WAITING",
      "waitingOrder": 1
    }
  ],
  "error": null
}
```

#### 예약 취소

```http
DELETE /api/user/reservations/{id}
```

응답:

- `204 No Content`

처리 정책:

- 본인 예약만 취소할 수 있다.
- 같은 슬롯에 대기가 있으면 첫 번째 대기를 자동으로 예약으로 승격한다.
- 승격 후 남은 대기 순번은 조회 시 재계산된다.

### 사용자 대기 API

#### 대기 신청

```http
POST /api/user/waitings
```

요청:

```json
{
  "date": "2026-05-05",
  "timeId": 1,
  "themeId": 1
}
```

응답:

- `201 Created`

```json
{
  "success": true,
  "data": {
    "id": 1,
    "memberId": 2,
    "slotId": 1,
    "waitingOrder": 1
  },
  "error": null
}
```

#### 대기 취소

```http
DELETE /api/user/waitings/{id}
```

응답:

- `204 No Content`

처리 정책:

- 본인 대기만 취소할 수 있다.
- 취소된 대기는 hard delete로 삭제된다.
- 남은 대기 순번은 조회 시 재계산된다.

### 관리자 예약 API

#### 전체 예약 조회

```http
GET /api/manager/reservations
```

응답:

- `200 OK`
- 예약 상세 목록

#### 관리자 예약 취소

```http
DELETE /api/manager/reservations/{reservationId}
```

응답:

- `204 No Content`

처리 정책:

- 사용자 예약 취소와 동일하게 대기 자동 승격을 수행한다.

### 시간 API

#### 사용자 예약 가능 시간 조회

```http
GET /api/user/times/availability?date=2026-05-05&themeId=1
```

응답:

- `200 OK`
- 시간별 예약/대기 가능 상태 목록

#### 관리자 시간 생성

```http
POST /api/manager/times
```

#### 관리자 시간 목록 조회

```http
GET /api/manager/times
```

#### 관리자 시간 삭제

```http
DELETE /api/manager/times/{id}
```

### 테마 API

#### 테마 목록 조회

```http
GET /api/themes
GET /api/themes?date=2026-05-05
```

#### 인기 테마 조회

```http
GET /api/themes/popular
```

#### 관리자 테마 생성

```http
POST /api/manager/themes
```

#### 관리자 테마 삭제

```http
DELETE /api/manager/themes/{id}
```

### 슬롯 API

#### 관리자 슬롯 생성

```http
POST /api/manager/slots
```

#### 관리자 슬롯 목록 조회

```http
GET /api/manager/slots
```

#### 관리자 슬롯 삭제

```http
DELETE /api/manager/slots/{slotId}
```

## 3. 트랜잭션과 동시성 전략

### 자동 전환 선택 이유

수동 승인 방식은 관리자 승인/거절 API와 화면이 추가되어 구현 범위가 커진다. 이번 사이클은 대기열 정책과 트랜잭션 일관성 학습이 핵심이므로, 예약 취소 시 대기 1번을 자동으로 예약 전환하는 방식을 선택했다.

### 트랜잭션 경계

예약 취소와 대기 승격은 하나의 유스케이스다. 따라서 다음 데이터 변경은 하나의 트랜잭션 안에서 함께 처리한다.

1. 기존 예약 삭제
2. 첫 번째 대기자를 예약으로 저장
3. 승격된 대기 삭제

중간 실패 시 기존 예약과 대기 상태가 유지되어야 한다.

### 동시성 제어

모든 동시성 시나리오를 방어하지 않고, 운영 손실이 큰 유령 예약 시나리오를 우선 방어한다.

방어 대상:

1. 사용자가 대기 취소를 요청한다.
2. 같은 시점에 기존 예약 취소로 해당 대기가 자동 승격 대상이 된다.
3. 사용자는 대기가 취소되었다고 인지하지만 시스템에는 예약이 생긴다.

이를 막기 위해 예약 취소의 승격 대상 대기열 조회와 대기 취소의 단건 대기 조회에 `SELECT ... FOR UPDATE`를 사용한다.

## 4. JPA 연결 미션 이해 가이드

### 현재 구조 요약

이 프로젝트는 기존 방탈출 예약/대기 도메인을 JPA 기반 영속성으로 연결한 상태다. 읽을 때는 다음 순서로 보면 이해가 쉽다.

1. **도메인 엔티티**: `member`, `theme`, `reservationtime`, `slot`, `reservation`, `waiting` 패키지의 `domain` 클래스가 JPA `@Entity`다.
2. **애플리케이션 포트**: 각 기능의 `application.port.out` 저장소 인터페이스가 서비스가 의존하는 추상화다.
3. **JPA 어댑터**: `adapter.out.persistence.Jpa*Repository`가 포트를 구현하고, 내부에서 `SpringData*Repository`를 호출한다.
4. **유스케이스 서비스**: `ReservationService`, `WaitingService`, `SlotService` 등이 트랜잭션과 도메인 규칙 흐름을 조율한다.
5. **검증 테스트**: `Jpa*RepositoryTest`, API 통합 테스트, 동시성 통합 테스트가 JPA 매핑과 요구사항을 함께 검증한다.

### 주요 JPA 매핑 결정

- `Reservation`, `Waiting`, `Slot`은 모두 `Member`, `Slot`, `Theme`, `ReservationTime`을 식별자 필드가 아니라 객체 연관으로 가진다.
- 다대일 연관은 기본적으로 `FetchType.LAZY`를 사용해 서비스 흐름에서 필요한 데이터만 조회한다.
- 한 슬롯에는 하나의 예약만 존재해야 하므로 `reservation.slot_id`에 `uk_reservation_slot` 유니크 제약을 둔다.
- 같은 사용자는 같은 슬롯에 한 번만 대기할 수 있으므로 `waiting(member_id, slot_id)`에 `uk_waiting_member_slot` 유니크 제약을 둔다.
- 슬롯은 `date`, `time_id`, `theme_id` 조합으로 유일해야 하므로 `uk_slot_date_time_theme` 제약을 둔다.
- 대기 순번은 별도 컬럼으로 저장하지 않고, 같은 슬롯의 `waiting.id` 오름차순으로 조회 시 계산한다.

### 트랜잭션/락 정책

- 예약 취소와 첫 번째 대기 승격은 하나의 트랜잭션에서 처리한다.
- 승격 대상 대기열 조회와 대기 취소 단건 조회는 명령 흐름에서만 `PESSIMISTIC_WRITE` 락을 사용한다.
- 조회용 메서드와 락 조회 메서드를 분리해, 일반 화면 조회가 불필요하게 row lock을 잡지 않도록 했다.
- 대기 취소 요청에서 대상 대기가 없으면 성공 처리하지 않고 `WAITING_404`로 실패시킨다. 이는 승격된 대기를 뒤늦게 취소 성공으로 오해하는 상황을 막기 위한 정책이다.

### 코드 리뷰 체크리스트

JPA 연결 이후 변경을 검토할 때는 다음을 우선 확인한다.

- 엔티티 연관관계와 DB 제약이 도메인 규칙과 같은 방향인지 확인한다.
- 서비스가 Spring Data 구현체에 직접 의존하지 않고 `application.port.out` 포트에만 의존하는지 확인한다.
- `@Transactional`이 데이터 변경 유스케이스와 `FOR UPDATE` 조회를 포함하는 메서드에 걸려 있는지 확인한다.
- JPQL projection이 응답 DTO 조립을 단순화하는지, 반대로 애플리케이션 계층을 영속성 세부사항에 과하게 묶지는 않는지 확인한다.
- 대기 순번 계산, 예약 취소/승격, 없는 대기 취소 실패 정책이 테스트 이름과 문서에 함께 드러나는지 확인한다.

### 남은 개선 후보

- 운영 DB가 H2가 아닐 경우 `SELECT ... FOR UPDATE`와 `ORDER BY` 조합의 실제 락 범위를 다시 확인해야 한다.
- `findMyReservations()`는 예약 목록과 대기 목록을 합친 뒤 정렬 정책이 명확하지 않다. 화면 요구가 생기면 날짜/시간/id 기준 정렬을 명시해야 한다.
- 관리자 예약 취소에서 없는 예약을 성공 처리하는 정책은 현재 유지되지만, 대기 취소 정책과 다르므로 API 문서와 테스트에서 의도를 계속 분리해야 한다.
- `theme.name`, `reservation_time.start_at`처럼 서비스에서 중복을 검증하는 값은 동시 요청까지 막으려면 DB unique constraint와 예외 매핑을 추가로 검토해야 한다.
- 요청 DTO의 필수값/문자열 길이 검증은 API 계약과 함께 관리해야 한다. 누락 시 도메인 생성 또는 DB 제약 오류로 늦게 드러날 수 있다.
- `ddl-auto: create-drop`과 `data.sql` 초기화는 로컬/미션 검증용 설정이다. 운영 환경을 가정한다면 마이그레이션 도구와 프로파일 분리가 필요하다.
- ADR 일부의 예전 `Jdbc*Repository` 명칭은 현재 JPA 어댑터 명칭과 맞지 않을 수 있다. 새 ADR을 작성할 때는 `Jpa*Repository`/`SpringData*Repository` 기준으로 기록한다.

## 5. 테스트 전략

### 단위 테스트

- 도메인 정책 검증: `SlotOccupancy`, `WaitingLine`, `WaitingLines`, `WaitingPromotionPolicy`
- 서비스 흐름 검증: 예약 취소 시 첫 번째 대기 승격, 승격 실패 시 후속 삭제 방지

### 통합 테스트

- 대기 신청/취소 API 요구사항 검증
- 예약 취소 시 첫 번째 대기 자동 승격 검증
- 남은 대기 순번 재정렬 검증
- 승격 중 실패 시 예약 삭제 롤백 검증
