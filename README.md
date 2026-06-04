### 📝 기능 구현 목록

#### 1. 서비스 정책 검증 (예외 발생)

- [x] 지나간 날짜·시간에 대한 예약 생성 검증 로직 추가
- [x] 같은 날짜, 시간, 테마에 대한 중복 예약 검증 로직 추가
- [x] 예약이 존재하는 시간을 삭제하려는 경우의 검증 로직 추가
- [x] 유효하지 않은 입력값(빈 이름, 잘못된 날짜 형식 등) 검증 로직 추가

#### 2. 에러 응답 및 예외 처리

- [x] 글로벌 예외 처리기(`@RestControllerAdvice`) 구현
- [x] 예외 상황별 커스텀 에러 응답 본문 및 상태 코드 정의
- [x] 500 Internal Server Error 발생 시 사용자에게 내부 구조가 노출되지 않도록 에러 응답 처리

#### 3. 예약 관리 기능 (조회/변경/취소)

- [x] 사용자가 자신의 이름으로 예약 목록을 조회하는 API 및 로직 구현
- [x] 사용자가 본인의 예약을 취소하는 API 및 로직 구현
- [x] 사용자가 본인의 예약 날짜·시간을 변경하는 API 및 로직 구현
- [x] 변경/취소 시 발생하는 엣지 케이스(이미 지난 예약 취소, 변경하려는 시간이 이미 차 있는 경우 등) 예외 처리

#### 4. 클라이언트 연동

- [x] 에러 발생 시 사용자에게 의미 있는 메시지가 표시되도록 브라우저 화면 연동
- [x] 내 예약 조회/변경/취소 화면 동작 구현

#### 5. 회원 인증/인가

- [x] 회원가입 API 및 로직 구현
- [x] 로그인/로그아웃 API 및 세션 기반 인증 구현
- [x] 인터셉터를 통한 인증 필요 엔드포인트 보호 (`/reservations/**`, `/waitings/**`)
- [x] 본인 예약/대기만 취소 가능하도록 인가 처리

#### 6. 예약 대기 기능

- [x] 이미 예약이 찬 슬롯에 대기 신청 API 및 로직 구현
- [x] 대기 신청 시 해당 슬롯에 예약 존재 여부 서버 검증
- [x] 같은 회원이 동일 슬롯에 중복 대기 불가 검증
- [x] 내 대기 목록 조회 API 구현 (대기 순번 포함)
- [x] 본인 대기 취소 API 및 로직 구현

#### 7. 예약 대기 승인 · 동시성 · 인가

- [x] 예약/대기 생성 API 통합 (`POST /reservations` — 슬롯 상태에 따라 서버가 예약/대기 결정)
- [x] 예약 취소 시 해당 슬롯 1순위 대기를 예약으로 자동 전환(승격)
- [x] 전환/취소 시 대기 순번 재정렬
- [x] 비관적 락 + UNIQUE 제약으로 동시 요청 정합성 보장
- [x] 회원 `role`(USER/ADMIN) 도입 및 관리자 API(`/admin/**`) 서버 측 인가 (비인증 401 / 비관리자 403)
- [x] 프론트에서 role 기반으로 관리자 페이지 노출 제어 및 로그인 사용자 이름 배너 표시

---

### 에러 응답 명세

#### 1. 공통 에러 응답 포맷

```json
{
  "errorCode": "ERROR_CODE_STRING",
  "message": "사용자가 이해할 수 있는 에러 메시지"
}
```

#### 2. 에러 코드 리스트

> `errorCode`는 비즈니스 예외의 경우 **HTTP 상태 이름**(예: `CONFLICT`)을, 입력값 검증 실패의 경우 `INVALID_INPUT`을 사용한다.

| Http Status               | Error Code              | Message                    | 발생 상황                             |
|:--------------------------|:------------------------|:---------------------------|:----------------------------------|
| 400 Bad Request           | `INVALID_INPUT`         | 검증 실패 메시지                  | `@Valid` 검증 실패, 도메인 생성 검증, 잘못된 인자 |
| 400 Bad Request           | `INVALID_INPUT`         | 요청 본문을 읽을 수 없습니다.          | 요청 JSON 파싱 실패                     |
| 400 Bad Request           | `INVALID_INPUT`         | 요청 값의 타입이 올바르지 않습니다.       | 경로/파라미터 타입 변환 실패                  |
| 400 Bad Request           | `INVALID_INPUT`         | {파라미터}가 누락됐습니다.            | 필수 요청 파라미터 누락                     |
| 400 Bad Request           | `INVALID_INPUT`         | 지원하지 않는 Content-Type입니다.   | 잘못된 Content-Type                  |
| 400 Bad Request           | `BAD_REQUEST`           | 이미 지난 시간에는 예약할 수 없습니다.     | 과거 날짜·시간으로 예약 생성                  |
| 400 Bad Request           | `BAD_REQUEST`           | 이미 지난 예약은 변경할 수 없습니다.      | 이미 지난 예약을 변경                      |
| 400 Bad Request           | `BAD_REQUEST`           | 이미 지난 시간으로 변경할 수 없습니다.     | 변경하려는 날짜·시간이 과거                   |
| 400 Bad Request           | `BAD_REQUEST`           | 이미 지난 예약은 취소할 수 없습니다.      | 이미 지난 예약을 취소                      |
| 400 Bad Request           | `BAD_REQUEST`           | 이미 지난 시간에는 대기 신청할 수 없습니다.  | 과거 날짜·시간으로 대기 신청                  |
| 400 Bad Request           | `BAD_REQUEST`           | 이미 지난 시간에는 대기를 취소할 수 없습니다. | 이미 지난 대기를 취소                      |
| 400 Bad Request           | `BAD_REQUEST`           | 비밀번호가 올바르지 않습니다.           | 로그인 시 비밀번호 불일치                    |
| 401 Unauthorized          | `UNAUTHORIZED`          | 로그인이 필요합니다.                | 비로그인 상태로 인증 필요 API 접근             |
| 403 Forbidden             | `FORBIDDEN`             | 접근 권한이 없습니다.               | 타인의 예약/대기를 취소하려는 경우               |
| 403 Forbidden             | `FORBIDDEN`             | 관리자만 접근할 수 있습니다.           | 일반 회원이 관리자 API(`/admin/**`)에 접근   |
| 404 Not Found             | `NOT_FOUND`             | 존재하지 않는 예약입니다.             | 존재하지 않는 예약 ID로 요청                 |
| 404 Not Found             | `NOT_FOUND`             | 존재하지 않는 대기입니다.             | 존재하지 않는 대기 ID로 요청                 |
| 404 Not Found             | `NOT_FOUND`             | 존재하지 않는 시간대입니다.            | 존재하지 않는 시간 ID로 요청                 |
| 404 Not Found             | `NOT_FOUND`             | 존재하지 않는 테마입니다.             | 존재하지 않는 테마 ID로 요청                 |
| 404 Not Found             | `NOT_FOUND`             | 존재하지 않는 회원입니다.             | 존재하지 않는 이메일로 로그인                  |
| 404 Not Found             | `NOT_FOUND`             | 존재하지 않는 경로입니다.             | 매핑되지 않은 경로 요청                     |
| 405 Method Not Allowed    | `METHOD_NOT_ALLOWED`    | 지원하지 않는 HTTP 메서드입니다.       | 허용되지 않은 메서드로 요청                   |
| 409 Conflict              | `CONFLICT`              | 이미 예약된 시간입니다.              | 같은 날짜·시간·테마에 예약이 존재 (생성/변경)       |
| 409 Conflict              | `CONFLICT`              | 이미 예약한 슬롯입니다.              | 본인이 이미 예약한 슬롯에 다시 신청              |
| 409 Conflict              | `CONFLICT`              | 이미 대기 중인 슬롯입니다.            | 본인이 이미 대기 중인 슬롯에 다시 신청            |
| 409 Conflict              | `CONFLICT`              | 예약이 존재하는 시간은 삭제할 수 없습니다.   | 예약이 있는 시간 삭제 시도                   |
| 409 Conflict              | `CONFLICT`              | 예약이 존재하는 테마는 삭제할 수 없습니다.   | 예약이 있는 테마 삭제 시도                   |
| 500 Internal Server Error | `INTERNAL_SERVER_ERROR` | 일시적인 오류가 발생했습니다.           | 예상치 못한 서버 오류                      |

---

### API 명세

#### 예약/대기 생성 (통합)

| Method | URL             |
|:-------|:----------------|
| `POST` | `/reservations` |

**Request Body**

```json
{
  "date": "2026-08-05",
  "timeId": 1,
  "themeId": 1
}
```

- 해당 슬롯에 예약이 없으면 **예약**(`RESERVED`), 이미 예약이 있으면 **대기**(`WAITING`)로 생성한다.
- 예약/대기 중 무엇으로 생성됐는지는 응답의 `status`로 구분한다.

**Response Body**

```json
{
  "id": 1,
  "status": "RESERVED",
  "memberName": "현미밥",
  "date": "2026-08-05",
  "time": {
    "id": 1,
    "startAt": "10:00:00"
  },
  "themeId": 1,
  "themeName": "테마A"
}
```

| Status           | 설명                                                                           |
|:-----------------|:-----------------------------------------------------------------------------|
| 201 Created      | 예약 또는 대기 생성 성공 (`Location`: 예약이면 `/reservations/{id}`, 대기면 `/waitings/{id}`) |
| 400 Bad Request  | 입력 오류, 과거 날짜·시간                                                              |
| 401 Unauthorized | 비로그인                                                                         |
| 409 Conflict     | 본인이 이미 예약했거나 대기 중인 슬롯                                                        |

> 슬롯이 비었는지 차 있는지에 대한 판단을 서버가 한 트랜잭션 안에서 처리하므로, 클라이언트는 예약/대기를 구분해 요청하지 않고 단일 엔드포인트로 신청한다.

---

#### 내 예약 목록 조회

| Method | URL             |
|:-------|:----------------|
| `GET`  | `/reservations` |

| Status           | 설명                   |
|:-----------------|:---------------------|
| 200 OK           | 로그인한 회원 본인의 예약 목록 반환 |
| 401 Unauthorized | 비로그인                 |

**Response Body**

```json
[
  {
    "id": 1,
    "memberName": "현미밥",
    "date": "2026-08-05",
    "time": {
      "id": 1,
      "startAt": "10:00:00"
    },
    "themeId": 1,
    "themeName": "테마A"
  }
]
```

> 조회 대상은 세션의 로그인 회원으로 식별한다. 별도 `name` 파라미터를 받지 않아, 동명이인이나 타인 예약 노출 문제가 없다.

---

#### 예약 변경

| Method  | URL                  |
|:--------|:---------------------|
| `PATCH` | `/reservations/{id}` |

**Request Body**

```json
{
  "date": "2026-09-01",
  "timeId": 2
}
```

| Status          | 설명                            |
|:----------------|:------------------------------|
| 200 OK          | 변경된 예약 반환                     |
| 400 Bad Request | 유효하지 않은 날짜 등 입력 오류, 지나간 예약/시간 |
| 404 Not Found   | 존재하지 않는 id                    |
| 409 Conflict    | 변경하려는 시간에 이미 예약 존재            |

> `PUT`은 리소스 전체를 교체하는 의미로 예약자 이름까지 변경하는 것은 의도하지 않음. 날짜·시간 일부만 수정하는 부분 업데이트이므로 `PATCH` 사용.

**Response Body**

```json
{
  "id": 1,
  "memberName": "현미밥",
  "date": "2026-09-01",
  "time": {
    "id": 2,
    "startAt": "14:00:00"
  },
  "themeId": 1,
  "themeName": "테마A"
}
```

---

#### 예약 취소

| Method   | URL                  |
|:---------|:---------------------|
| `DELETE` | `/reservations/{id}` |

| Status           | 설명             |
|:-----------------|:---------------|
| 204 No Content   | 삭제 성공          |
| 400 Bad Request  | 이미 지난 예약 취소 시도 |
| 401 Unauthorized | 비로그인           |
| 403 Forbidden    | 타인 예약 취소 시도    |
| 404 Not Found    | 존재하지 않는 id     |

---

#### 회원가입

| Method | URL       |
|:-------|:----------|
| `POST` | `/signup` |

**Request Body**

```json
{
  "name": "user1",
  "email": "user1@test.com",
  "password": "1234"
}
```

| Status | 설명      |
|:-------|:--------|
| 200 OK | 회원가입 성공 |

---

#### 로그인

| Method | URL      |
|:-------|:---------|
| `POST` | `/login` |

**Request Body**

```json
{
  "email": "user1@test.com",
  "password": "1234"
}
```

| Status          | 설명            |
|:----------------|:--------------|
| 200 OK          | 로그인 성공, 세션 발급 |
| 400 Bad Request | 비밀번호 불일치      |
| 404 Not Found   | 존재하지 않는 회원    |

---

#### 로그아웃

| Method | URL       |
|:-------|:----------|
| `POST` | `/logout` |

| Status | 설명    |
|:-------|:------|
| 200 OK | 세션 만료 |

---

#### 대기 신청

> 별도 엔드포인트가 아니라 예약/대기 생성 통합 API(`POST /reservations`)로 처리한다.
> 이미 예약이 찬 슬롯에 신청하면 응답 `status`가 `WAITING`으로 내려온다. (위 예약/대기 생성 참고)

---

#### 내 대기 목록 조회

| Method | URL         |
|:-------|:------------|
| `GET`  | `/waitings` |

**Response Body**

```json
[
  {
    "id": 1,
    "memberName": "user1",
    "date": "2026-08-05",
    "startAt": "10:00:00",
    "themeName": "테마A",
    "turn": 2
  }
]
```

> `turn`: 해당 슬롯 내 대기 순번 (신청 시각 기준)

| Status           | 설명       |
|:-----------------|:---------|
| 200 OK           | 대기 목록 반환 |
| 401 Unauthorized | 비로그인     |

---

#### 대기 취소

| Method   | URL              |
|:---------|:-----------------|
| `DELETE` | `/waitings/{id}` |

| Status           | 설명             |
|:-----------------|:---------------|
| 204 No Content   | 취소 성공          |
| 400 Bad Request  | 이미 지난 대기 취소 시도 |
| 401 Unauthorized | 비로그인           |
| 403 Forbidden    | 타인 대기 취소 시도    |
| 404 Not Found    | 존재하지 않는 id     |

---

#### 관리자 - 테마 관리

> `/admin/**`은 **ADMIN 권한**이 필요하다. 비로그인 시 401, 일반 회원 접근 시 403을 반환한다.

| Method   | URL                  | 설명       |
|:---------|:---------------------|:---------|
| `POST`   | `/admin/themes`      | 테마 생성    |
| `GET`    | `/admin/themes`      | 전체 테마 조회 |
| `DELETE` | `/admin/themes/{id}` | 테마 삭제    |

**테마 생성 Request Body**

```json
{
  "name": "테마A",
  "description": "테마 설명",
  "imageUrl": "https://image.com/a.png"
}
```

| Status                                | 설명                |
|:--------------------------------------|:------------------|
| 201 Created / 200 OK / 204 No Content | 요청 성공             |
| 401 Unauthorized                      | 비로그인              |
| 403 Forbidden                         | 관리자가 아닌 회원의 접근    |
| 409 Conflict                          | 예약이 존재하는 테마 삭제 시도 |
