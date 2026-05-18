# 방탈출 예약 시스템
## 팀 규칙
```aiignore
1. 리소스 식별

- 리소스 = 클라이언트에게 제공하려는 명사
- 필터·표현 조건 = 쿼리스트링 (비계층적 데이터)
- 리소스는 복수형으로 표현 (reservations, times)
- 동사는 HTTP 메서드로 표현, 경로에 담지 않는다

BAD:
POST   /reservations/create
DELETE /reservations/remove/2

GOOD:
POST   /reservations
DELETE /reservations/2


2. HTTP 메서드 선택

작업        메서드    정상 응답
조회        GET       200
생성        POST      201
부분 변경   PATCH     200
삭제        DELETE    204 No Content

- 전체 교체가 아니라면 PUT이 아니라 PATCH를 쓴다.


3. 책임 분리

3.1 서버 / 클라이언트 책임

- 서버: 리소스의 관리 범위, 개방, 검증, 비즈니스 규칙 강제
- 클라이언트: 받은 정보의 사용 방법
- 비즈니스 규칙은 반드시 서버에서 강제한다.
- UI에서 막는 건 보조 수단일 뿐이다.


3.2 관리자 / 사용자 API 분리

- 같은 자원을 다루더라도 관리자/사용자 엔드포인트는 분리한다.
- 근거: 명세 가독성 + 유지보수성


4. 좋은 API의 조건

- 명확성: URL + HTTP 메서드만 보고 무슨 일이 일어날지 알 수 있어야 한다.
- 일관성: 비슷한 리소스는 같은 패턴으로 표현한다. 예외를 외우지 않게 한다.
- RESTful은 수단이다.
- 비즈니스 의도가 리소스보다 클 때는 명확성을 우선한다.

예:
GET /themes/ranking?period=weekly

- 단순 필터가 아니라 의도를 드러내는 경우이다.


5. 에러 응답 규칙

5.1 응답 수신자

- 클라이언트(프론트엔드) 개발자
- 백엔드 개발자


5.2 기본 원칙

- 비즈니스 규칙을 위반한 요청 → 4xx + message로 위반 사유 명시
- 이유: 사용자/클라이언트가 응답만으로 다음 행동을 정할 수 있어야 한다.
- 잘못된 요청을 받은 경우 일단은 400을 고려한다.
- 더 명확하거나 상태에 적합한 상태 코드를 알고 있다면 그 코드를 사용한다.
- 5xx는 발생시키지 않도록 한다.
- 내부 예외 메시지를 그대로 담지 않는다.


5.3 상태 코드 매핑

상황                                  코드      메시지 예
필수 값 누락 / 형식 오류                  400     "name은 필수입니다"
비즈니스 규칙 위반                        400     "이미 지난 예약은 취소할 수 없습니다"
리소스가 존재하지 않음                     404     "예약을 찾을 수 없습니다"
다른 리소스와 상태 충돌                    409     "해당 시간은 이미 예약되어 있습니다"

비즈니스 규칙 위반 예:
- 지난 예약 취소/변경
- 과거 날짜 요청

다른 리소스와 상태 충돌 예:
- 중복 시간


5.4 세부 케이스별 대응 전략

- 대상 리소스(URL로 표현되는 리소스)가 존재하지 않는 경우 → 404
- 대상 리소스의 필드 참조가 잘못된 경우 (예: 존재하지 않는 FK) → 400 (존재하지 않는 FK에 대한 에러 로그 포함)
- 요청 파라미터/본문 형식이 잘못된 경우 → 400
- 변경하려는 날짜·테마에 이미 같은 시간을 가진 예약이 존재하는 경우 → 409
- 이전 날짜/시간으로 변경을 시도하는 경우 → 400 (필요시 422)
- 이전 날짜/시간에 잡힌 예약을 취소하려고 시도하는 경우 → 400


5.5 응답 본문 작성 기준

- message로 무엇이 잘못됐는지만 명시한다.
- 다음 행동을 구체적으로 명시하지 않는다.

```

## 기능 목록

### 사이클 1 - 테마 + 사용자 예약
- [x] 테마 추가 / 목록 조회 / 삭제 (관리자)
- [x] 예약 가능한 시간 조회 (날짜 + 테마 기준, 사용자)
- [x] 예약 생성 (사용자 / 관리자)
- [x] 중복 예약 방지 (동일 날짜 + 시간 + 테마)
- [x] 인기 테마 조회 (최근 1주일 예약 수 기준)

### 사이클 2 - 예약 변경/취소와 에러 처리

#### 1단계 - 서비스 정책 적용
- [x] 지나간 날짜·시간에 대한 예약 생성 불가
- [x] 같은 날짜 + 시간 + 테마 중복 예약 방지
- [x] 예약이 존재하는 시간 삭제 불가
- [x] 유효하지 않은 입력값 거부 (빈 이름, 잘못된 날짜 형식, 유효하지 않은 ID 등)

#### 2단계 - 에러 응답 설계
- [x] 서비스 정책 위반 / 잘못된 입력 / 존재하지 않는 리소스에 대한 의도된 에러 응답 반환
- [x] 500이 사용자에게 노출되지 않도록 예외 처리
- [x] 에러 발생 시 브라우저에서 사용자가 이해할 수 있는 메시지 표시

#### 3단계 - 내 예약 조회 / 변경 / 취소
- [x] 이름으로 내 예약 목록 조회
- [x] 예약 취소 (지난 예약 취소 불가)
- [x] 예약 변경 - 날짜·시간 수정 (지난 예약 변경 불가, 중복 시간 변경 불가)

---

## API 명세

### 예약 시간 (Reservation Time)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/times?date={date}&themeId={id}` | - | `200` |
| 관리자 | `GET` | `/admin/times` | - | `200` |
| 관리자 | `POST` | `/admin/times` | `{"startAt": "HH:mm"}` | `201` |
| 관리자 | `DELETE` | `/admin/times/{id}` | - | `204` |

`GET /times?date=2026-05-01&themeId=1` 응답 예시 (해당 날짜 + 테마에 예약되지 않은 시간만 반환)

```json
[
  { "id": 1, "startAt": "10:00" },
  { "id": 2, "startAt": "14:00" }
]
```

---

### 테마 (Theme)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/themes` | - | `200` |
| 사용자 | `GET` | `/themes/popular?limit={n}` | - | `200` |
| 관리자 | `GET` | `/admin/themes` | - | `200` |
| 관리자 | `POST` | `/admin/themes` | `{"name", "thumbnailUrl", "description"}` | `201` |
| 관리자 | `DELETE` | `/admin/themes/{id}` | - | `204` |

---

### 예약 (Reservation)

| 구분 | Method | URL | Request Body | Response |
|------|--------|-----|--------------|----------|
| 사용자 | `GET` | `/reservations?name={name}` | - | `200` |
| 사용자 | `POST` | `/reservations` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 사용자 | `DELETE` | `/reservations/{id}` | - | `204` |
| 사용자 | `PATCH` | `/reservations/{id}` | `{"date", "timeId"}` | `200` |
| 관리자 | `GET` | `/admin/reservations` | - | `200` |
| 관리자 | `POST` | `/admin/reservations` | `{"name", "date", "timeId", "themeId"}` | `201` |
| 관리자 | `DELETE` | `/admin/reservations/{id}` | - | `204` |

예약 생성 요청 예시

```json
{
  "name": "홍길동",
  "date": "2026-05-01",
  "timeId": 1,
  "themeId": 2
}
```

예약 응답 예시

```json
{
  "id": 1,
  "name": "홍길동",
  "date": "2026-05-01",
  "time": { "id": 1, "startAt": "10:00" },
  "theme": { "id": 2, "name": "우주 탐험대", "thumbnailUrl": "https://...", "description": "..." }
}
```

---

## 에러 응답 형식

```json
{ "message": "..." }
```

| 상황 | 코드 |
|------|------|
| 필수 값 누락 / 형식 오류 | `400` |
| 비즈니스 규칙 위반 (지난 예약 취소·변경 시도 등) | `400` |
| 존재하지 않는 FK 참조 (timeId, themeId) | `400` |
| URL로 표현되는 리소스가 존재하지 않음 | `404` |
| 중복 예약 | `409` |

---

## 미션 중 기록

### 규칙을 적용해서 변경한 코드

**`ReservationCommandService` - DB 조회와 비즈니스 로직 분리**

기존 `validatePastDateTime(date, timeId)`는 `reservationTimeDao.findById(timeId)` 호출(DB 조회)과 과거 시간 검증(비즈니스 로직)을 하나의 메서드에 담고 있었습니다.
팀 규칙 "책임 분리" 원칙에 따라 DB 조회는 `create()` 메서드로 끌어올리고, `validateNotPast(date, time)`는 순수 로직만 담도록 분리했습니다.

```java
// 변경 전: DB 조회와 검증이 섞임
private void validatePastDateTime(LocalDate date, long timeId) {
    ReservationTime time = reservationTimeDao.findById(timeId);
    if (date.atTime(time.startAt()).isBefore(LocalDateTime.now())) {
        throw new PastReservationException();
    }
}

// 변경 후: DB 조회를 호출부로 분리, 검증은 순수 로직만
private void validateNotPast(LocalDate date, ReservationTime time) {
    if (date.atTime(time.startAt()).isBefore(LocalDateTime.now())) {
        throw new PastReservationException();
    }
}

public Reservation create(String name, LocalDate date, long timeId, long themeId) {
    ReservationTime time = reservationTimeDao.findById(timeId);
    themeDao.findById(themeId);
    validateNotPast(date, time);
    validateDuplicate(date, timeId, themeId);
    return reservationDao.save(name, date, timeId, themeId);
}
```

이 구조 덕분에 `cancel()`에서도 별도 구현 없이 `validateNotPast`를 재사용할 수 있게 됐습니다.

---

### 변경·취소에서 발견한 엣지 케이스

**1. 변경(PATCH) 시 자기 자신과의 중복 충돌**

같은 날짜, 시간으로 변경 요청을 보내면 기존 중복 체크 쿼리가 자기 자신의 예약을 중복으로 인식하는 문제가 있었습니다.
`existsByDateAndTimeIdAndThemeId` 대신 자신의 id를 제외하는 `existsByDateAndTimeIdAndThemeIdExcluding`을 별도로 작성해 해결했습니다.

**2. 지난 예약 취소 시도**

취소는 사용자가 직접 요청하는 행위이므로, 이미 지난 예약에 대한 취소 요청은 비즈니스 규칙 위반으로 400을 반환합니다.

**3. 존재하지 않는 예약 변경·취소 시도**

URL에 표현된 리소스(`/reservations/{id}`)가 존재하지 않는 경우로, 404를 반환합니다.

---

### 규칙 충돌 순간

**존재하지 않는 FK 참조   404 vs 400**

`timeId`, `themeId`가 DB에 없을 때 처음에는 "해당 리소스를 찾을 수 없다"는 의미로 404를 반환했습니다.
그러나 팀 규칙 5.4.2에서 "대상 리소스의 필드 참조가 잘못된 경우(존재하지 않는 FK) → 400"으로 명시되어 있어 충돌이 발생했습니다.

- **404 논리**: timeId, themeId는 각각 독립적인 리소스(예약 시간, 테마)를 참조하므로 해당 리소스가 없다면 404가 자연스럽다.
- **400 논리**: 요청 바디의 필드 값이 유효하지 않은 입력이므로 잘못된 요청(400)이 맞다.

팀 규칙을 따라 400으로 결정했습니다. 다만 URL로 표현되는 리소스 자체(`/reservations/{id}`)가 없는 경우는 여전히 404를 유지합니다.

---

### 유지 / 수정 / 폐기 항목

| 항목 | 결과 | 이유 |
|------|------|------|
| 리소스는 복수형으로 표현 | **유지** | - |
| 동사는 HTTP 메서드로, 경로에 담지 않는다 | **유지** | - |
| 전체 교체가 아니면 PATCH 사용 | **유지** | 예약 변경 시 날짜·시간만 변경, PUT 사용하지 않음 |
| 생성은 201 반환 | **유지** | - |
| 관리자 / 사용자 엔드포인트 분리 | **유지** | - |
| 존재하지 않는 FK 참조 → 404 | **수정 → 400** | 팀 규칙 5.4.2 적용 |
