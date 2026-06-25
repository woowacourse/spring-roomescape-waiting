# API 명세서

현재 프로젝트의 실제 컨트롤러 구현 기준 API 명세서다.

> 📌 **공통 에러 코드 규칙**
> - `COMMON4xx` (입력 검증), `AUTH401_*` (인증 실패), `AUTH403_001` (매니저 권한 없음)은 해당 조건의 모든 엔드포인트에 공통 적용된다.
> - 각 엔드포인트의 "도메인 에러" 표에는 그 엔드포인트 **고유 에러만** 적는다.
> - 전체 에러 코드 카탈로그: [`ERROR_CODE.md`](./ERROR_CODE.md)

---

## 🚀 빠른 요약

| 메서드 | 경로 | 인증 | 설명 |
| --- | --- | --- | --- |
| `POST` | `/api/v1/auth/login` | ❌ | 웹 쿠키 로그인 (HttpOnly 쿠키에 JWT 발급) |
| `POST` | `/api/v1/auth/login/token` | ❌ | 모바일 토큰 로그인 (JSON 응답에 JWT 발급) |
| `GET` | `/api/v1/auth/me` | ✅ | 현재 사용자 조회 |
| `POST` | `/api/v1/auth/logout` | ❌ | 로그아웃 (쿠키 폐기) |
| `GET` | `/api/v1/reservations` | ✅ | 내 예약·대기 목록 조회 |
| `POST` | `/api/v1/reservations` | ✅ | 예약 생성 |
| `PATCH` | `/api/v1/reservations/{id}` | ✅ | 예약 날짜·시간 변경 |
| `DELETE` | `/api/v1/reservations/{id}` | ✅ | 예약 삭제 |
| `POST` | `/api/v1/reservations/{id}/waits` | ✅ | 예약 대기 신청 |
| `DELETE` | `/api/v1/reservations/{reservationId}/waits/mine` | ✅ | 내 예약 대기 취소 |
| `GET` | `/api/v1/reservation-times` | ❌ | 예약 시간 목록 조회 |
| `GET` | `/api/v1/reservation-times/availability?date=&themeId=` | ❌ | 예약 가능 시간 조회 |
| `GET` | `/api/v1/themes` | ❌ | 테마 목록 조회 |
| `GET` | `/api/v1/themes/popular?from=&to=` | ❌ | 인기 테마 조회 |
| `GET` | `/api/v1/stores` | ❌ | 매장 목록 조회 |
| `POST` | `/api/v1/admin/reservation-times` | 🔐 MANAGER | 예약 시간 생성 |
| `DELETE` | `/api/v1/admin/reservation-times/{id}` | 🔐 MANAGER | 예약 시간 삭제 |
| `POST` | `/api/v1/admin/themes` | 🔐 MANAGER | 테마 생성 |
| `DELETE` | `/api/v1/admin/themes/{id}` | 🔐 MANAGER | 테마 삭제 |
| `GET` | `/api/v1/admin/store/reservations` | 🔐 MANAGER | 자기 매장 예약 목록 |
| `PATCH` | `/api/v1/admin/store/reservations/{id}` | 🔐 MANAGER | 자기 매장 예약 변경 |
| `DELETE` | `/api/v1/admin/store/reservations/{id}` | 🔐 MANAGER | 자기 매장 예약 삭제 |

---

## 📑 목차

1. [공통 규칙](#공통-규칙)
2. [인증·인가](#인증인가)
3. [인증 (`/api/v1/auth`)](#0-인증)
4. [예약 (`/api/v1/reservations`)](#1-예약)
5. [예약 시간 (`/api/v1/reservation-times`)](#2-예약-시간)
6. [테마 (`/api/v1/themes`)](#3-테마)
7. [관리자 예약 시간 (`/api/v1/admin/reservation-times`)](#4-관리자-예약-시간)
8. [관리자 테마 (`/api/v1/admin/themes`)](#5-관리자-테마)
9. [매장 (`/api/v1/stores`)](#6-매장)
10. [관리자 매장 예약 (`/api/v1/admin/store/reservations`)](#7-관리자-매장-예약-매니저-전용)
11. [인증 헤더 / 쿠키 사용 예시](#인증-헤더--쿠키-사용-예시)

---

## 공통 규칙

- Base URL: `/api/v1`
- Content-Type
  - 요청 본문이 있는 API: `application/json` (로그인은 `application/x-www-form-urlencoded`)
  - 응답 본문이 있는 API: `application/json`
- 날짜 형식: `yyyy-MM-dd`
- 시간 형식: `HH:mm`

### 공통 에러 응답 포맷

```json
{
  "message": "올바른 입력값 형식이 아닙니다.",
  "errorCode": "COMMON400_004"
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `message` | `String` | 백엔드 에러 메시지 |
| `errorCode` | `String` | 프론트 분기 및 문서화용 에러 코드 |

### 모든 엔드포인트에 공통 적용되는 에러

| HTTP | 에러 코드 | 의미 |
| --- | --- | --- |
| `400` | `COMMON400_001` | 유효하지 않은 요청 필드 |
| `400` | `COMMON400_003` | 쿼리 스트링 누락 |
| `400` | `COMMON400_004` | 올바르지 않은 입력값 형식 |
| `400` | `COMMON400_005` | 쿼리 스트링 형식 오류 |
| `401` | `AUTH401_002` | 비로그인 |
| `401` | `AUTH401_003` | 유효하지 않은 토큰 |
| `401` | `AUTH401_004` | 토큰 만료 |
| `403` | `AUTH403_001` | 매니저 권한 없음 (매니저 전용 API 한정) |
| `500` | `COMMON500_001` | 예기치 못한 서버 오류 |

→ 이하 각 엔드포인트는 **도메인 고유 에러만** 명시.

---

## 인증·인가

### 인증

- **JWT 기반 stateless 인증**. 서버는 세션 상태를 유지하지 않으며 매 요청마다 토큰 검증.
- 로그인 성공 시 클라이언트별 토큰 전달
  - **웹**: `Set-Cookie: access_token=<JWT>; HttpOnly; SameSite=Lax`
  - **모바일**: 응답 본문 `{ "token": "<JWT>" }`
- 이후 요청에서 서버는 다음 순서로 토큰 추출
  1. `Authorization: Bearer <JWT>` 헤더 (모바일 우선)
  2. `access_token` 쿠키 (웹 fallback)

### 인가

- 회원은 `USER`(일반 손님) / `MANAGER`(매장 매니저) 역할 중 하나
- 매니저는 `store_id`가 부여되어 자기 매장 자원만 관리 가능
- `@LoginMember(role = MANAGER)`이 붙은 엔드포인트는 USER 접근 시 `403` (`AUTH403_001`)
- 매니저가 자기 매장이 아닌 자원 조작 시 `403` (`AUTH403_002`)

### 인증·인가 정책

| 분류 | API |
| --- | --- |
| **공개** | `POST /auth/login`, `POST /auth/login/token`, `POST /auth/logout`, `GET /themes`, `GET /themes/popular`, `GET /stores`, `GET /reservation-times`, `GET /reservation-times/availability` |
| **인증 필요 (모든 역할)** | `GET /auth/me`, `GET /reservations`, `POST /reservations`, `PATCH /reservations/{id}`, `DELETE /reservations/{id}`, `POST /reservations/{id}/waits`, `DELETE /reservations/{reservationId}/waits/mine` |
| **MANAGER 권한 필요** | `POST /admin/themes`, `DELETE /admin/themes/{id}`, `POST /admin/reservation-times`, `DELETE /admin/reservation-times/{id}`, `GET /admin/store/reservations`, `PATCH /admin/store/reservations/{id}`, `DELETE /admin/store/reservations/{id}` |

> 실제 인증 적용은 `AuthenticationConfig`의 인터셉터 등록 규칙 (`addPathPatterns` + `excludePathPatterns`)을 따른다. 역할 기반 인가는 `LoginMemberArgumentResolver`가 `@LoginMember(role = ...)`를 읽어 처리한다.

---

## 0. 인증

### 0-1. 웹 쿠키 로그인

`POST /api/v1/auth/login` · Content-Type `application/x-www-form-urlencoded` · **공개**

웹 브라우저용. JWT를 HttpOnly 쿠키에 담아 발급.

#### 요청 본문 (form-encoded)

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `email` | `String` | Y | 회원 이메일 |
| `password` | `String` | Y | 회원 비밀번호 |

#### 응답

- 본문 없음
- `Set-Cookie: access_token=<JWT>; HttpOnly; Path=/; SameSite=Lax; Max-Age=3600`

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `200` | — | 로그인 성공 |
| `401` | `AUTH401_001` | 이메일 없음 또는 비밀번호 불일치 |

### 0-2. 모바일 토큰 로그인

`POST /api/v1/auth/login/token` · Content-Type `application/json` · **공개**

모바일 앱용. JWT를 JSON 응답 본문에 발급.

#### 요청 본문

```json
{
  "email": "brown@email.com",
  "password": "password"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `email` | `String` | Y | 회원 이메일 |
| `password` | `String` | Y | 회원 비밀번호 |

#### 응답 예시

```json
{ "token": "eyJhbGciOiJIUzI1NiIs..." }
```

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `200` | — | 로그인 성공 |
| `401` | `AUTH401_001` | 이메일 없음 또는 비밀번호 불일치 |

### 0-3. 현재 사용자 조회

`GET /api/v1/auth/me` · **인증 필요**

#### 응답 예시

```json
{
  "id": 1,
  "email": "brown@email.com",
  "name": "브라운",
  "role": "USER",
  "storeId": null
}
```

매니저는 `role: "MANAGER"`, `storeId`에 자기 매장 ID.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 회원 ID |
| `email` | `String` | 이메일 |
| `name` | `String` | 이름 |
| `role` | `String` | `USER` 또는 `MANAGER` |
| `storeId` | `Long` | 소속 매장 ID (USER는 `null`) |

#### 응답 코드

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |
| `401` | 비로그인 또는 무효 토큰 (공통 에러) |

### 0-4. 로그아웃

`POST /api/v1/auth/logout` · **공개 (멱등)**

- 본문 없음
- `Set-Cookie: access_token=; Max-Age=0` 헤더로 클라이언트 쿠키 폐기
- 모바일 앱은 이 헤더를 무시하고 자체적으로 Keychain의 토큰을 폐기해야 함
- JWT는 stateless이므로 서버 측 즉시 무효화는 별도 블랙리스트 없는 한 불가

#### 응답 코드

| 상태 | 상황 |
| --- | --- |
| `204` | 로그아웃 성공 (인증 없이도 호출 가능) |

---

## 1. 예약

> 모든 예약 API는 **인증 필요**.

### 1-1. 내 예약·대기 목록 조회

`GET /api/v1/reservations` · **인증 필요**

- 로그인 사용자의 **확정 예약** + **대기 중인 예약**을 한 번에 반환
- 대기는 *같은 슬롯 안에서의 신청 순서* `order` 포함 (1 = 첫 번째 대기자)

#### 응답 예시

```json
{
  "reservations": [
    {
      "id": 1,
      "memberId": 1,
      "date": "2026-05-27",
      "time": { "id": 1, "startAt": "10:00" },
      "themeId": 1,
      "storeId": 1
    }
  ],
  "waitings": [
    {
      "order": 1,
      "reservationId": 1,
      "memberId": 1,
      "createdAt": "2026-05-27T12:00:01"
    }
  ]
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `reservations[]` | `Array<ReservationResponse>` | 확정된 예약 목록 |
| `waitings[]` | `Array<WaitingResponse>` | 대기 중인 예약 목록 |
| `waitings[].order` | `Long` | 같은 슬롯에서의 대기 순번 (1부터) |
| `waitings[].reservationId` | `Long` | 대기 중인 원본 예약 ID |
| `waitings[].memberId` | `Long` | 회원 ID |
| `waitings[].createdAt` | `String` | 대기 신청 시각 (ISO-8601) |

#### 응답 코드

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |

### 1-2. 예약 생성

`POST /api/v1/reservations` · **인증 필요**

예약자(`memberId`)는 **JWT에서 추출되어 서버가 주입**하므로 요청 본문에 없음.

#### 요청 본문

```json
{
  "date": "2026-05-14",
  "timeId": 1,
  "themeId": 1,
  "storeId": 1
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 예약 날짜 |
| `timeId` | `Long` | Y | 예약 시간 ID |
| `themeId` | `Long` | Y | 테마 ID |
| `storeId` | `Long` | Y | 매장 ID |

#### 검증 규칙

- `date`: null 불가, `yyyy-MM-dd` 형식
- `timeId`, `themeId`, `storeId`: null 불가, 양수
- 지나간 날짜·시간 예약 불가
- 같은 매장 + 날짜 + 시간 + 테마 중복 예약 불가 (다른 매장이면 같은 슬롯 허용)

#### 응답 예시

```json
{
  "id": 1,
  "memberId": 1,
  "date": "2026-05-14",
  "time": { "id": 1, "startAt": "10:00" },
  "themeId": 1,
  "storeId": 1
}
```

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `201` | — | 예약 생성 성공 |
| `400` | `RESERVATION400_001` | 지나간 날짜·시간 예약 시도 |
| `409` | `RESERVATION409_001` | 같은 슬롯 중복 예약 |

### 1-3. 예약 삭제

`DELETE /api/v1/reservations/{id}` · **인증 필요** · 본인 예약만 삭제 가능

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `204` | — | 삭제 성공 |
| `400` | `RESERVATION400_002` | 이미 지난 예약 |
| `400` | `RESERVATION400_003` | 본인 예약이 아님 |
| `404` | `RESERVATION404_001` | 존재하지 않는 예약 |

### 1-4. 예약 날짜·시간 변경

`PATCH /api/v1/reservations/{id}` · **인증 필요** · 본인 예약만 변경 가능

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 요청 본문

```json
{ "date": "2026-05-15", "timeId": 2 }
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 변경할 예약 날짜 |
| `timeId` | `Long` | Y | 변경할 예약 시간 ID |

#### 검증 규칙

- `date`: null 불가, `yyyy-MM-dd` 형식
- `timeId`: null 불가, 양수
- 로그인 사용자가 예약 소유자와 일치
- 지나간 날짜·시간으로 변경 불가
- 존재하는 예약 시간이어야 함

#### 응답 예시

```json
{
  "id": 1,
  "memberId": 1,
  "date": "2026-05-15",
  "time": { "id": 2, "startAt": "11:00" },
  "themeId": 1,
  "storeId": 1
}
```

> `storeId`는 변경 불가. 매장 이동이 필요한 경우 기존 예약을 취소하고 새로 생성.

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `200` | — | 변경 성공 |
| `400` | `RESERVATION400_001` | 지나간 날짜·시간으로 변경 시도 |
| `400` | `RESERVATION400_003` | 본인 예약이 아님 |
| `404` | `RESERVATION404_001` | 존재하지 않는 예약 |
| `404` | `RESERVATION_TIME404_001` | 존재하지 않는 예약 시간 |

### 1-5. 예약 대기 신청

`POST /api/v1/reservations/{id}/waits` · **인증 필요**

신청자(`memberId`)는 **JWT에서 추출되어 서버가 주입**.

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 대기 신청 대상 예약 ID |

#### 요청 본문

- 없음

#### 검증 규칙

- 대상 예약 존재
- 다른 사용자의 예약 (본인 예약에는 대기 불가)
- 대상 슬롯이 지나간 시간이면 불가
- 같은 회원의 같은 슬롯 중복 대기 불가

#### 응답 예시

```json
{
  "id": 1,
  "reservationId": 1,
  "memberId": 1,
  "createdAt": "2026-05-27T12:00:01"
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 생성된 예약 대기 ID |
| `reservationId` | `Long` | 대기 대상 예약 ID |
| `memberId` | `Long` | 신청 회원 ID |
| `createdAt` | `String` | 신청 시각 (ISO-8601) |

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `201` | — | 대기 신청 성공 |
| `404` | `RESERVATION404_001` | 존재하지 않는 예약 |
| `409` | `RESERVATION_WAIT409_001` | 같은 슬롯에 본인 대기가 이미 존재 |
| `422` | `RESERVATION_WAIT422_001` | 지난 시간 슬롯에 대한 신청 |
| `422` | `RESERVATION_WAIT422_002` | 본인 예약에 대기 시도 |

### 1-6. 예약 대기 취소

`DELETE /api/v1/reservations/{reservationId}/waits/mine` · **인증 필요** · 본인 대기만

#### 설계 메모

대상 대기는 `(reservationId, memberId)` 조합으로 *유일하게 식별*됨 (도메인 unique key). 따라서 URL에 `waitId`를 노출할 필요 없고, `memberId`는 토큰에서 추출.

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `reservationId` | `Long` | 대기를 걸어둔 예약 ID |

- 요청 본문 없음
- 응답 본문 없음

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `204` | — | 취소 성공 |
| `404` | `RESERVATION_WAIT404_001` | 본인 대기가 존재하지 않음 |

---

## 2. 예약 시간

### 2-1. 예약 시간 목록 조회

`GET /api/v1/reservation-times` · **공개**

#### 응답 예시

```json
[
  { "id": 1, "startAt": "10:00" },
  { "id": 2, "startAt": "11:00" }
]
```

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |

### 2-2. 예약 가능 시간 조회

`GET /api/v1/reservation-times/availability?date={date}&themeId={themeId}` · **공개**

#### 쿼리 파라미터

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 조회 날짜 |
| `themeId` | `Long` | Y | 테마 ID |

#### 응답 예시

```json
[
  { "id": 1, "time": "10:00", "available": false, "reservationId": 1 },
  { "id": 2, "time": "11:00", "available": true,  "reservationId": null }
]
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 시간 ID |
| `time` | `String` | `HH:mm` |
| `available` | `Boolean` | 예약 가능 여부 (= `reservationId == null`) |
| `reservationId` | `Long` | 이미 예약된 슬롯이면 그 예약 ID, 빈 슬롯이면 `null` |

#### 응답 코드

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |
| `400` | 쿼리 누락/형식 오류 (공통 에러) |

---

## 3. 테마

### 3-1. 테마 목록 조회

`GET /api/v1/themes` · **공개**

#### 응답 예시

```json
[
  {
    "id": 1,
    "name": "이든의 공포 하우스",
    "description": "이든이 귀신으로 나오는 공포 테마",
    "imgUrl": "https://images.example.com/themes/horror-house.jpg"
  }
]
```

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |

### 3-2. 인기 테마 조회

`GET /api/v1/themes/popular?from={from}&to={to}` · **공개**

#### 쿼리 파라미터

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `from` | `String` | Y | 조회 시작일 |
| `to` | `String` | Y | 조회 종료일 |

#### 응답 예시

```json
[
  {
    "id": 1,
    "name": "이든의 공포 하우스",
    "description": "이든이 귀신으로 나오는 공포 테마",
    "imgUrl": "https://images.example.com/themes/horror-house.jpg",
    "rank": 1,
    "reservationCount": 7
  }
]
```

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |
| `400` | 쿼리 누락/형식 오류 (공통 에러) |

---

## 4. 관리자 예약 시간

> **MANAGER 권한 필요**

### 4-1. 예약 시간 생성

`POST /api/v1/admin/reservation-times` · **MANAGER**

#### 요청 본문

```json
{ "startAt": "10:00" }
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `startAt` | `String` | Y | 예약 시작 시간 (`HH:mm`) |

#### 응답 예시

```json
{ "id": 1, "startAt": "10:00" }
```

| 상태 | 상황 |
| --- | --- |
| `201` | 생성 성공 |

### 4-2. 예약 시간 삭제

`DELETE /api/v1/admin/reservation-times/{id}` · **MANAGER**

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 시간 ID |

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `204` | — | 삭제 성공 |
| `404` | `RESERVATION_TIME404_001` | 존재하지 않는 예약 시간 |
| `409` | `RESERVATION_TIME409_001` | 예약이 존재하는 예약 시간 (FK 제약) |

---

## 5. 관리자 테마

> **MANAGER 권한 필요**

### 5-1. 테마 생성

`POST /api/v1/admin/themes` · **MANAGER**

#### 요청 본문

```json
{
  "name": "이든의 공포 하우스",
  "description": "이든이 귀신으로 나오는 공포 테마",
  "imgUrl": "https://images.example.com/themes/horror-house.jpg"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | `String` | Y | 테마 이름 (2-100자) |
| `description` | `String` | Y | 테마 설명 (100자 이하) |
| `imgUrl` | `String` | Y | 테마 이미지 URL |

#### 응답 예시

```json
{
  "id": 1,
  "name": "이든의 공포 하우스",
  "description": "이든이 귀신으로 나오는 공포 테마",
  "imgUrl": "https://images.example.com/themes/horror-house.jpg"
}
```

| 상태 | 상황 |
| --- | --- |
| `201` | 생성 성공 |

### 5-2. 테마 삭제

`DELETE /api/v1/admin/themes/{id}` · **MANAGER**

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 테마 ID |

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `204` | — | 삭제 성공 |
| `404` | `THEME404_001` | 존재하지 않는 테마 |
| `409` | `THEME409_001` | 예약이 존재하는 테마 (FK 제약) |

---

## 6. 매장

### 6-1. 매장 목록 조회

`GET /api/v1/stores` · **공개**

#### 응답 예시

```json
[
  { "id": 1, "name": "강남점" },
  { "id": 2, "name": "홍대점" },
  { "id": 3, "name": "판교점" }
]
```

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |

---

## 7. 관리자 매장 예약 (매니저 전용)

> **MANAGER 권한 필요**. 매니저는 *자기 매장* 예약만 관리. 다른 매장 자원 접근은 `403` (`AUTH403_002`).

### 7-1. 자기 매장 예약 목록 조회

`GET /api/v1/admin/store/reservations` · **MANAGER** · 매니저의 `storeId`로 자동 필터링

#### 응답 예시

```json
[
  {
    "id": 1,
    "memberId": 3,
    "date": "2026-12-01",
    "time": { "id": 1, "startAt": "10:00" },
    "themeId": 1,
    "storeId": 1
  }
]
```

| 상태 | 상황 |
| --- | --- |
| `200` | 성공 |

### 7-2. 매장 예약 변경

`PATCH /api/v1/admin/store/reservations/{id}` · **MANAGER**

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 요청 본문

```json
{ "date": "2026-12-15", "timeId": 2 }
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 변경할 예약 날짜 |
| `timeId` | `Long` | Y | 변경할 예약 시간 ID |

#### 검증 규칙

- 매니저는 자기 매장(`storeId` 일치) 예약만 변경 가능
- 지나간 날짜·시간으로 변경 불가
- 존재하는 예약 시간이어야 함

#### 응답 예시

```json
{
  "id": 1,
  "memberId": 3,
  "date": "2026-12-15",
  "time": { "id": 2, "startAt": "11:00" },
  "themeId": 1,
  "storeId": 1
}
```

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `200` | — | 변경 성공 |
| `400` | `RESERVATION400_001` | 지나간 날짜·시간 |
| `403` | `AUTH403_002` | 다른 매장의 예약 |
| `404` | `RESERVATION404_001` | 존재하지 않는 예약 |
| `404` | `RESERVATION_TIME404_001` | 존재하지 않는 예약 시간 |
| `409` | `RESERVATION409_001` | 변경하려는 슬롯이 이미 예약됨 |

### 7-3. 매장 예약 삭제

`DELETE /api/v1/admin/store/reservations/{id}` · **MANAGER**

| 경로 변수 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

> 일반 사용자의 `DELETE /reservations/{id}`와 달리 **지난 예약도 삭제 가능**. 매니저는 사후 정산·정리 목적으로 과거 예약을 정리할 수 있다.

#### 응답 코드

| 상태 | 도메인 에러 코드 | 상황 |
| --- | --- | --- |
| `204` | — | 삭제 성공 |
| `403` | `AUTH403_002` | 다른 매장의 예약 |
| `404` | `RESERVATION404_001` | 존재하지 않는 예약 |

---

## 인증 헤더 / 쿠키 사용 예시

### 웹 (쿠키)

```http
GET /api/v1/reservations HTTP/1.1
Host: localhost:8080
Cookie: access_token=eyJhbGciOiJIUzI1NiIs...
```

### 모바일 (헤더)

```http
GET /api/v1/reservations HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

---

## 8. 결제

### 8-1. 결제 대기 예약 생성

`POST /api/v1/reservations` · **USER**

기존 예약 생성 요청을 받으면 `PENDING` 예약과 서버 생성 주문 정보를 먼저 저장한다. 응답의 `clientKey`, `orderId`, `amount`로 브라우저 Toss 결제창을 연다.

```json
{
  "id": 1,
  "memberId": 1,
  "reservationId": 1,
  "reservationStatus": "PENDING",
  "orderId": "order-0123456789abcdef0123456789abcdef",
  "orderName": "방탈출 예약",
  "amount": 50000,
  "clientKey": "test_ck_..."
}
```

### 8-2. 결제 승인

`POST /api/v1/payments/confirm` · **USER**

```json
{
  "paymentKey": "tgen_...",
  "orderId": "order-0123456789abcdef0123456789abcdef",
  "amount": 50000
}
```

서버는 저장된 주문 금액을 먼저 비교하고 일치할 때만 Toss `POST /v1/payments/confirm`을 호출한다. 성공하면 `paymentKey`를 저장하고 예약을 `CONFIRMED`로 바꾼다.

### 8-3. 결제 실패 정리

`POST /api/v1/payments/fail` · **USER**

```json
{
  "code": "PAY_PROCESS_CANCELED",
  "message": "사용자가 결제를 취소했습니다.",
  "orderId": null
}
```

`orderId`가 있으면 해당 `PENDING` 주문과 예약을 정리한다. 사용자가 결제창을 취소하여 `orderId`가 없는 경우에도 `204 No Content`로 안전하게 종료한다.
