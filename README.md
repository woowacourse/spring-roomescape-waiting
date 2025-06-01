# 🚪 방탈출 예약 관리 API 문서

## 📋 전체 API 목록

### 📄 View 페이지 (화면 렌더링)

#### 🌐 공용 페이지 (게스트)

| 메서드 | URL            | 설명       | 반환 View       |
|-----|----------------|----------|---------------|
| GET | `/`            | 홈 페이지    | `index`       |
| GET | `/reservation` | 예약 페이지   | `reservation` |
| GET | `/signup`      | 회원가입 페이지 | `signup`      |
| GET | `/login`       | 로그인 페이지  | `login`       |

#### 🔧 관리자 페이지

| 메서드 | URL                          | 설명               | 반환 View                 |
|-----|------------------------------|------------------|-------------------------|
| GET | `/admin`                     | 관리자 메인 페이지       | `admin/index`           |
| GET | `/admin/reservation`         | 관리자 예약 관리 페이지    | `admin/reservation-new` |
| GET | `/admin/time`                | 관리자 시간 관리 페이지    | `admin/time`            |
| GET | `/admin/theme`               | 관리자 테마 관리 페이지    | `admin/theme`           |
| GET | `/admin/waiting-reservation` | 관리자 예약 대기 관리 페이지 | `admin/waiting`         |

#### 👤 사용자 페이지

| 메서드 | URL                 | 설명            | 반환 View            |
|-----|---------------------|---------------|--------------------|
| GET | `/reservation-mine` | 사용자 예약 관리 페이지 | `reservation-mine` |

### 🔗 API 엔드포인트 (데이터 처리)

### 📅 예약 관리

| 권한  | 메서드    | URL                          | 설명          |
|-----|--------|------------------------------|-------------|
| 사용자 | POST   | `/reservations`              | 예약 생성       |
| 사용자 | GET    | `/my-reservations`           | 내 예약 목록 조회  |
| 관리자 | GET    | `/admin/reservations`        | 모든 예약 목록 조회 |
| 관리자 | GET    | `/admin/reservations?filter` | 예약 조건별 필터링  |
| 관리자 | POST   | `/admin/reservations`        | 관리자 예약 생성   |
| 관리자 | DELETE | `/admin/reservations/{id}`   | 예약 취소       |

### ⏰ 시간 관리

| 권한  | 메서드    | URL                 | 설명          |
|-----|--------|---------------------|-------------|
| 게스트 | GET    | `/times`            | 모든 예약 시간 조회 |
| 게스트 | GET    | `/times/available`  | 예약 가능 시간 조회 |
| 관리자 | POST   | `/admin/times`      | 예약 시간 추가    |
| 관리자 | DELETE | `/admin/times/{id}` | 예약 시간 삭제    |

### 🎭 테마 관리

| 권한  | 메서드    | URL                  | 설명       |
|-----|--------|----------------------|----------|
| 게스트 | GET    | `/themes`            | 모든 테마 조회 |
| 게스트 | GET    | `/themes/popular`    | 인기 테마 조회 |
| 관리자 | POST   | `/admin/themes`      | 테마 추가    |
| 관리자 | DELETE | `/admin/themes/{id}` | 테마 삭제    |

### 👥 회원 관리

| 권한  | 메서드  | URL              | 설명        |
|-----|------|------------------|-----------|
| 게스트 | POST | `/login`         | 로그인       |
| 게스트 | POST | `/members`       | 회원가입      |
| 게스트 | GET  | `/login/check`   | 로그인 여부 확인 |
| 사용자 | POST | `/logout`        | 로그아웃      |
| 관리자 | GET  | `/admin/members` | 모든 사용자 조회 |

### ⏳ 예약 대기 관리

| 권한  | 메서드    | URL                                | 설명          |
|-----|--------|------------------------------------|-------------|
| 사용자 | POST   | `/waiting-reservations`            | 예약 대기 생성    |
| 사용자 | DELETE | `/waiting-reservations/{id}`       | 예약 대기 삭제    |
| 관리자 | GET    | `/admin/waiting-reservations`      | 예약 대기 목록 조회 |
| 관리자 | DELETE | `/admin/waiting-reservations/{id}` | 예약 대기 삭제    |

---

## 📅 예약 관리

### 🌐 게스트 (권한 없는 API)

*예약 관리는 게스트 권한 API가 없습니다.*

### 👤 사용자

#### 📝 예약 생성

| 메서드  | URL             | 설명        |
|------|-----------------|-----------|
| POST | `/reservations` | 사용자 예약 생성 |

**요청 조건**

- 이름: null 불가, 10글자 이하
- dateTime: null 불가, 과거 날짜&시간 불가

**요청 예시**

```http
POST /reservations HTTP/1.1
Content-Type: application/json
Cookie: token=eyJhbGciOiJIUzI1NiJ9...

{
  "date": "2024-03-01",
  "themeId": 1,
  "timeId": 1
}
```

**응답 예시**

```json
HTTP/1.1 201
Content-Type: application/json

{
"id": 1,
"name": "브라운",
"date": "2023-08-05",
"time": {
"id": 1,
"startAt": "10:00"
},
"theme": {
"id": 1,
"name": "추리",
"description": "추리 with mint",
"thumbnail": "thumbnail.png"
}
}
```

#### 👤 내 예약 목록 조회

| 메서드 | URL                | 설명              |
|-----|--------------------|-----------------|
| GET | `/my-reservations` | 로그인한 사용자의 예약 목록 |

**요청 예시**

```http
GET /my-reservations HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"reservationId": 1,
"theme": "테마1",
"date": "2024-03-01",
"time": "10:00",
"status": "예약"
}
]
```

### 🔧 관리자

#### 📋 모든 예약 목록 조회

| 메서드 | URL                   | 설명          |
|-----|-----------------------|-------------|
| GET | `/admin/reservations` | 모든 예약 목록 조회 |

**요청 예시**

```http
GET /admin/reservations HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"id": 1,
"member": {
"id": 1,
"name": "Alice"
},
"date": "2025-05-05",
"time": {
"id": 1,
"startAt": "08:00:00"
},
"theme": {
"id": 12,
"name": "논리",
"description": "퍼즐 마스터",
"thumbnail": "image/thumbnail.png"
}
}
]
```

#### 🔍 예약 조건별 필터링

| 메서드 | URL                                                                                             | 설명         |
|-----|-------------------------------------------------------------------------------------------------|------------|
| GET | `/admin/reservations?themeId={themeId}&memberId={memberId}&dateFrom={dateFrom}&dateTo={dateTo}` | 조건별 예약 필터링 |

**참고**: 동적 필터링을 구현, 파라미터는 선택이며, 없을 경우 null 입력

**요청 예시**

```http
GET /reservations?themeId=12&memberId=2&dateFrom=2025-05-01&dateTo=2025-05-31 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"id": 1,
"member": {
"id": 2,
"name": "Alice"
},
"date": "2025-05-11",
"time": {
"id": 1,
"startAt": "08:00:00"
},
"theme": {
"id": 12,
"name": "논리",
"description": "퍼즐 마스터",
"thumbnail": "image/thumbnail.png"
}
}
]
```

#### 📝 관리자 예약 생성

| 메서드  | URL                   | 설명                 |
|------|-----------------------|--------------------|
| POST | `/admin/reservations` | 관리자가 특정 회원에게 예약 생성 |

**요청 예시**

```http
POST /admin/reservations HTTP/1.1
Content-Type: application/json
Cookie: token=eyJhbGciOiJIUzI1NiJ9...

{
  "date": "2024-03-01",
  "themeId": 1,
  "timeId": 1,
  "memberId": 1
}
```

**응답 예시**

```json
HTTP/1.1 201
Content-Type: application/json

{
"id": 1,
"name": "브라운",
"date": "2023-08-05",
"time": {
"id": 1,
"startAt": "10:00"
},
"theme": {
"id": 1,
"name": "추리",
"description": "추리 with mint",
"thumbnail": "thumbnail.png"
}
}
```

#### 🗑️ 예약 취소

| 메서드    | URL                        | 설명    |
|--------|----------------------------|-------|
| DELETE | `/admin/reservations/{id}` | 예약 삭제 |

**요청 예시**

```http
DELETE /reservations/1 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 204
Content-Type: application/json
```

---

## ⏰ 시간 관리

### 🌐 게스트 (권한 없는 API)

#### 📋 모든 예약 시간 조회

| 메서드 | URL      | 설명          |
|-----|----------|-------------|
| GET | `/times` | 모든 예약 시간 조회 |

**요청 예시**

```http
GET /times HTTP/1.1
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"id": 1,
"startAt": "10:00"
}
]
```

#### 🔍 예약 가능 시간 조회

| 메서드 | URL                                              | 설명                 |
|-----|--------------------------------------------------|--------------------|
| GET | `/times/available?date={date}&themeId={themeId}` | 특정 날짜/테마의 예약 가능 시간 |

**요청 예시**

```http
GET /times/available?date=2024-03-01&themeId=1 HTTP/1.1
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"timeId": 1,
"startAt": "10:00",
"alreadyBooked": "true"
},
{
"timeId": 2,
"startAt": "11:00",
"alreadyBooked": "false"
}
]
```

### 👤 사용자

*시간 관리는 사용자 권한 API가 없습니다.*

### 🔧 관리자

#### ➕ 예약 시간 추가

| 메서드  | URL            | 설명          |
|------|----------------|-------------|
| POST | `/admin/times` | 예약 가능 시간 추가 |

**요청 예시**

```http
POST /admin/times HTTP/1.1
Content-Type: application/json
Cookie: token=eyJhbGciOiJIUzI1NiJ9...

{
  "startAt": "10:00"
}
```

**응답 예시**

```json
HTTP/1.1 201
Content-Type: application/json

{
"id": 1,
"startAt": "10:00"
}
```

#### 🗑️ 예약 시간 삭제

| 메서드    | URL                 | 설명       |
|--------|---------------------|----------|
| DELETE | `/admin/times/{id}` | 예약 시간 삭제 |

**요청 예시**

```http
DELETE /times/1 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 204
```

---

## 🎭 테마 관리

### 🌐 게스트 (권한 없는 API)

#### 📋 모든 테마 조회

| 메서드 | URL       | 설명       |
|-----|-----------|----------|
| GET | `/themes` | 모든 테마 조회 |

**요청 예시**

```http
GET /themes HTTP/1.1
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"id": 1,
"name": "레벨2 탈출",
"description": "우테코 레벨2를 탈출하는 내용입니다.",
"thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
]
```

#### 🔥 인기 테마 조회

| 메서드 | URL               | 설명                      |
|-----|-------------------|-------------------------|
| GET | `/themes/popular` | 지난 일주일간 예약 건수 상위 10개 테마 |

**참고**: 오늘 기준 -7일 ~ -1일 기간의 예약 건수 기준

**요청 예시**

```http
GET /themes/popular HTTP/1.1
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"id": 1,
"name": "추리",
"description": "셜록 with Danny",
"thumbnail": "image/thumbnail.png"
},
{
"id": 2,
"name": "공포",
"description": "어둠 속의 비명",
"thumbnail": "image/thumbnail.png"
}
]
```

### 👤 사용자

*테마 관리는 사용자 권한 API가 없습니다.*

### 🔧 관리자

#### ➕ 테마 추가

| 메서드  | URL             | 설명      |
|------|-----------------|---------|
| POST | `/admin/themes` | 새 테마 추가 |

**요청 예시**

```http
POST /admin/themes HTTP/1.1
Content-Type: application/json
Cookie: token=eyJhbGciOiJIUzI1NiJ9...

{
  "name": "레벨2 탈출",
  "description": "우테코 레벨2를 탈출하는 내용입니다.",
  "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

**응답 예시**

```json
HTTP/1.1 201
Location: /admin/themes/1
Content-Type: application/json

{
"id": 1,
"name": "레벨2 탈출",
"description": "우테코 레벨2를 탈출하는 내용입니다.",
"thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

#### 🗑️ 테마 삭제

| 메서드    | URL                  | 설명    |
|--------|----------------------|-------|
| DELETE | `/admin/themes/{id}` | 테마 삭제 |

**요청 예시**

```http
DELETE /admin/themes/1 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 204
```

---

## 👥 회원 관리

### 🌐 게스트 (권한 없는 API)

#### 🔐 로그인

| 메서드  | URL      | 설명      |
|------|----------|---------|
| POST | `/login` | 사용자 로그인 |

**요청 예시**

```http
POST /login HTTP/1.1
Content-Type: application/json

{
  "password": "password",
  "email": "admin@email.com"
}
```

**응답 예시**

```http
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9...; Path=/; HttpOnly
```

#### 📝 회원가입

| 메서드  | URL        | 설명      |
|------|------------|---------|
| POST | `/members` | 새 회원 등록 |

**요청 예시**

```http
POST /members HTTP/1.1
Content-Type: application/json

{
  "email": "admin@email.com",
  "password": "password",
  "name": "admin"
}
```

**응답 예시**

```json
HTTP/1.1 201 OK
Content-Type: application/json

{
"id": 3
}
```

#### ✅ 인증 정보 조회

| 메서드 | URL            | 설명        |
|-----|----------------|-----------|
| GET | `/login/check` | 로그인 여부 확인 |

**요청 예시**

```http
GET /login/check HTTP/1.1
```

**응답 예시**

```json
HTTP/1.1 200 OK
Content-Type: application/json

{
"name": "어드민"
}
```

### 👤 사용자

#### 🚪 로그아웃

| 메서드  | URL       | 설명       |
|------|-----------|----------|
| POST | `/logout` | 사용자 로그아웃 |

**요청 예시**

```http
POST /logout HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 204 OK
```



### 🔧 관리자

#### 👥 모든 사용자 조회

| 메서드 | URL              | 설명           |
|-----|------------------|--------------|
| GET | `/admin/members` | 전체 사용자 목록 조회 |

**요청 예시**

```http
GET /admin/members HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 200 OK
Content-Type: application/json

[
{
"id": 2,
"name": "mint"
},
{
"id": 3,
"name": "danny"
}
]
```

---

## ⏳ 예약 대기 관리

### 🌐 게스트 (권한 없는 API)

*예약 대기 관리는 게스트 권한 API가 없습니다.*

### 👤 사용자

#### ➕ 예약 대기 생성

| 메서드  | URL                     | 설명       |
|------|-------------------------|----------|
| POST | `/waiting-reservations` | 예약 대기 등록 |

**요청 예시**

```http
POST /waiting-reservations HTTP/1.1
Content-Type: application/json
Cookie: token=eyJhbGciOiJIUzI1NiJ9...

{
  "date": "2024-03-01",
  "themeId": 1,
  "timeId": 1
}
```

**응답 예시**

```json
HTTP/1.1 201
Content-Type: application/json

[
{
"reservationId": 1,
"waitingId": "2024-03-01",
"waitingStatus": "WAITING"
}
]
```

#### 🗑️ 예약 대기 삭제

| 메서드    | URL                                     | 설명         |
|--------|-----------------------------------------|------------|
| DELETE | `/waiting-reservations/{reservationId}` | 내 예약 대기 취소 |

**요청 예시**

```http
DELETE /waiting-reservations/1 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 204
Content-Type: application/json
```

### 🔧 관리자

#### 📋 예약 대기 목록 조회

| 메서드 | URL                           | 설명             |
|-----|-------------------------------|----------------|
| GET | `/admin/waiting-reservations` | 모든 예약 대기 목록 조회 |

**요청 예시**

```http
GET /admin/waiting-reservations HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 200
Content-Type: application/json

[
{
"waitingId": 2,
"name": "Bob",
"themeName": "논리",
"date": "2025-05-10",
"startAt": "08:00"
},
{
"waitingId": 3,
"name": "Carol",
"themeName": "논리",
"date": "2025-05-10",
"startAt": "08:00"
}
]
```

#### 🗑️ 예약 대기 삭제

| 메서드    | URL                                       | 설명          |
|--------|-------------------------------------------|-------------|
| DELETE | `/admin/waiting-reservations/{waitingId}` | 특정 예약 대기 삭제 |

**요청 예시**

```http
DELETE /admin/waiting-reservations/2 HTTP/1.1
Cookie: token=eyJhbGciOiJIUzI1NiJ9...
```

**응답 예시**

```json
HTTP/1.1 204
Content-Type: application/json
```

---

## ❌ 예외 처리

### 에러 코드 및 상황

| 예외 타입                    | HTTP 상태 | 설명                               |
|--------------------------|---------|----------------------------------|
| `DateTimeParseException` | 400     | 날짜(yyyy-MM-dd) & 시간(HH:mm) 파싱 오류 |
| `ValidationException`    | 400     | 입력 데이터 검증 실패 (예약자명, 필수값 등)       |
| `InUseException`         | 400     | 리소스가 사용 중이어서 삭제/수정 불가 (외래키 제약)   |
| `UnAuthorizedException`  | 401     | 인증되지 않은 사용자 (로그인 필요)             |
| `ForbiddenException`     | 403     | 권한 없음 (접근 권한 부족)                 |
| `NotFoundException`      | 404     | 예약, 예약 시간, 회원, 테마가 존재하지 않음       |
| `DuplicatedException`    | 409     | 중복된 리소스 (이메일, 예약 시간 등)           |

### 🔍 검증 규칙

#### ⏰ 시간 관련

- ✅ 시간 생성 시 HH:mm 형식 준수
- ✅ 예약 생성 시 유효성 검사
  - 예약자명: 10글자 이내
  - 날짜: yyyy-MM-dd 형식
- ✅ 시간 삭제 시 해당 시간 예약 존재 여부 확인

#### 📅 예약 관련

- ✅ 과거 날짜/시간 예약 생성 불가
- ✅ 중복 예약 불가 (같은 날짜/시간 중복 예약 방지)

---

## 🔐 인증 및 권한

### Cookie 기반 인증

- **인증 방식**: JWT 토큰을 Cookie로 전송
- **Cookie 이름**: `token`
- **권한 구분**:
  - `USER`: 일반 사용자
  - `ADMIN`: 관리자

### 권한별 접근 권한

- **🌐 게스트**: 로그인, 회원가입, 테마 조회, 시간 조회, 예약 가능 시간 조회
- **👤 사용자**: 예약 생성/취소, 내 예약 조회, 예약 대기 관리, 로그아웃, 인증 정보 조회
- **🔧 관리자**: 모든 예약/대기 관리, 시간/테마 관리, 사용자 조회, 관리자 홈
