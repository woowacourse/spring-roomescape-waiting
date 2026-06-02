# API 명세서

현재 프로젝트의 실제 컨트롤러 구현 기준 API 명세서다.

## 목차

- [공통 규칙](#공통-규칙)
- [인증](#인증)
- [인가 (Authorization)](#인가-authorization)
- [공통 에러 응답](#공통-에러-응답)
- [0. 인증](#0-인증)
  - [0-1. 웹 쿠키 로그인](#0-1-웹-쿠키-로그인)
  - [0-2. 모바일 토큰 로그인](#0-2-모바일-토큰-로그인)
  - [0-3. 현재 사용자 조회](#0-3-현재-사용자-조회)
  - [0-4. 로그아웃](#0-4-로그아웃)
- [1. 예약](#1-예약)
  - [1-1. 내 예약·대기 목록 조회](#1-1-내-예약대기-목록-조회)
  - [1-2. 예약 생성](#1-2-예약-생성)
  - [1-3. 예약 삭제](#1-3-예약-삭제)
  - [1-4. 예약 날짜·시간 변경](#1-4-예약-날짜시간-변경)
  - [1-5. 예약 대기 신청](#1-5-예약-대기-신청)
  - [1-6. 예약 대기 취소](#1-6-예약-대기-취소)
- [2. 예약 시간](#2-예약-시간)
  - [2-1. 예약 시간 목록 조회](#2-1-예약-시간-목록-조회)
  - [2-2. 예약 가능 시간 조회](#2-2-예약-가능-시간-조회)
- [3. 테마](#3-테마)
  - [3-1. 테마 목록 조회](#3-1-테마-목록-조회)
  - [3-2. 인기 테마 조회](#3-2-인기-테마-조회)
- [4. 관리자 예약 시간](#4-관리자-예약-시간)
  - [4-1. 예약 시간 생성](#4-1-예약-시간-생성)
  - [4-2. 예약 시간 삭제](#4-2-예약-시간-삭제)
- [5. 관리자 테마](#5-관리자-테마)
  - [5-1. 테마 생성](#5-1-테마-생성)
  - [5-2. 테마 삭제](#5-2-테마-삭제)
- [6. 매장](#6-매장)
  - [6-1. 매장 목록 조회](#6-1-매장-목록-조회)
- [7. 관리자 매장 예약 (매니저 전용)](#7-관리자-매장-예약-매니저-전용)
  - [7-1. 자기 매장 예약 목록 조회](#7-1-자기-매장-예약-목록-조회)
  - [7-2. 매장 예약 변경 (매니저)](#7-2-매장-예약-변경-매니저)
  - [7-3. 매장 예약 삭제 (매니저)](#7-3-매장-예약-삭제-매니저)
- [빠른 요약](#빠른-요약)
- [인증 헤더 / 쿠키 사용 예시](#인증-헤더--쿠키-사용-예시)

## 공통 규칙

- Base URL: `/api/v1`
- Content-Type
  - 요청 본문이 있는 API: `application/json` (로그인은 `application/x-www-form-urlencoded`)
  - 응답 본문이 있는 API: `application/json`
- 날짜 형식: `yyyy-MM-dd`
- 시간 형식: `HH:mm`

## 인증

- **JWT 기반 stateless 인증**을 사용한다. 서버는 세션 상태를 유지하지 않으며, 매 요청마다 토큰을 검증한다.
- 로그인 성공 시 클라이언트별로 다른 형태로 토큰을 전달한다:
  - **웹**: `Set-Cookie: access_token=<JWT>; HttpOnly; SameSite=Lax` 쿠키로 발급
  - **모바일**: 응답 본문 `{ "token": "<JWT>" }`으로 발급
- 이후 요청에서 서버는 다음 순서로 토큰을 추출한다:
  1. `Authorization: Bearer <JWT>` 헤더 (모바일 우선)
  2. `access_token` 쿠키 (웹 fallback)
- 인증 정보가 없는 요청은 `401` (`AUTH401_002`), 토큰이 위조·형식 오류로 무효이면 `401` (`AUTH401_003`), 토큰이 만료되면 `401` (`AUTH401_004`)를 반환한다.

## 인가 (Authorization)

인증된 요청에 대해서도 *권한*에 따라 거부될 수 있다. 인증 실패(`401`)와 인가 실패(`403`)는 명확히 구분된다.

- 회원은 `USER`(일반 손님)와 `MANAGER`(매장 매니저) 역할 중 하나를 갖는다.
- 매니저는 `store_id`가 부여되어 자기 매장 자원만 관리할 수 있다.
- `@LoginMember(role = MANAGER)` 어노테이션이 붙은 엔드포인트는 `MANAGER` 권한이 없으면 `403` (`AUTH403_001`)을 반환한다.
- 매니저가 *자기 매장이 아닌* 자원을 조작하려 하면 `403` (`AUTH403_002`)를 반환한다.

### 인증·인가 정책

| 분류 | API |
| --- | --- |
| **공개** | `POST /api/v1/auth/login`, `POST /api/v1/auth/login/token`, `POST /api/v1/auth/logout`, `GET /api/v1/themes`, `GET /api/v1/themes/popular`, `GET /api/v1/stores`, `GET /api/v1/reservation-times`, `GET /api/v1/reservation-times/availability` |
| **인증 필요 (모든 역할)** | `GET /api/v1/auth/me`, `GET /api/v1/reservations`, `POST /api/v1/reservations`, `PATCH /api/v1/reservations/{id}`, `DELETE /api/v1/reservations/{id}`, `POST /api/v1/reservations/{id}/waits`, `DELETE /api/v1/reservations/{reservationId}/waits/mine` |
| **MANAGER 권한 필요** | `POST /api/v1/admin/themes`, `DELETE /api/v1/admin/themes/{id}`, `POST /api/v1/admin/reservation-times`, `DELETE /api/v1/admin/reservation-times/{id}`, `GET /api/v1/admin/store/reservations`, `PATCH /api/v1/admin/store/reservations/{id}`, `DELETE /api/v1/admin/store/reservations/{id}` |

> 실제 인증 적용은 `AuthenticationConfig`의 인터셉터 등록 규칙(`addPathPatterns` + `excludePathPatterns`)을 따른다. 역할 기반 인가는 `LoginMemberArgumentResolver`가 `@LoginMember(role = ...)`를 읽어 처리한다.

## 공통 에러 응답

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

---

## 0. 인증

### 0-1. 웹 쿠키 로그인

- `POST /api/v1/auth/login`
- Content-Type: `application/x-www-form-urlencoded`
- 웹 브라우저용. JWT를 HttpOnly 쿠키에 담아 발급한다.

#### 요청 본문 (form-encoded)

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `email` | `String` | Y | 회원 이메일 |
| `password` | `String` | Y | 회원 비밀번호 |

#### 응답

- 본문 없음
- 성공 시 `Set-Cookie: access_token=<JWT>; HttpOnly; Path=/; SameSite=Lax; Max-Age=3600` 헤더 발급

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 로그인 성공 |
| `401 Unauthorized` | 이메일 없음 또는 비밀번호 불일치 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH401_001` | 이메일 또는 비밀번호가 일치하지 않습니다 |

### 0-2. 모바일 토큰 로그인

- `POST /api/v1/auth/login/token`
- Content-Type: `application/json`
- 모바일 앱용. JWT를 JSON 응답 본문에 담아 발급한다. 클라이언트는 Keychain/Keystore 등 안전한 저장소에 보관해야 한다.

#### 요청 본문

```json
{
  "email": "brown@email.com",
  "password": "password"
}
```

#### 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `email` | `String` | Y | 회원 이메일 |
| `password` | `String` | Y | 회원 비밀번호 |

#### 응답 예시

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 로그인 성공 |
| `401 Unauthorized` | 이메일 없음 또는 비밀번호 불일치 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH401_001` | 이메일 또는 비밀번호가 일치하지 않습니다 |

### 0-3. 현재 사용자 조회

- `GET /api/v1/auth/me`
- 인증 필요. `Authorization` 헤더 또는 `access_token` 쿠키로 토큰 전달.

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

매니저의 경우 `role`은 `"MANAGER"`, `storeId`는 자기 매장 ID로 채워진다.

#### 응답 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 회원 ID |
| `email` | `String` | 이메일 |
| `name` | `String` | 이름 |
| `role` | `String` | `USER` 또는 `MANAGER` |
| `storeId` | `Long` | 소속 매장 ID (USER는 `null`) |

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 현재 로그인 사용자 정보 반환 |
| `401 Unauthorized` | 비로그인 또는 무효 토큰 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH401_002` | 로그인이 필요한 요청입니다 |
| `AUTH401_003` | 유효하지 않은 토큰입니다 (위조/형식 오류) |
| `AUTH401_004` | 토큰이 만료되었습니다 |

### 0-4. 로그아웃

- `POST /api/v1/auth/logout`

#### 응답

- 본문 없음
- 응답에 `Set-Cookie: access_token=; Max-Age=0` 헤더를 포함해 클라이언트의 쿠키를 폐기한다.
- 모바일 앱은 이 헤더를 무시하고 자체적으로 Keychain의 토큰을 폐기해야 한다.
- JWT는 stateless이므로 서버 측 즉시 무효화는 별도 블랙리스트가 없는 한 불가능하다.

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 로그아웃 성공 (인증 없이도 호출 가능, 멱등) |

---

## 1. 예약

> 모든 예약 API는 **인증 필요**. 비로그인 시 `401 Unauthorized` (`AUTH401_002`).

### 1-1. 내 예약·대기 목록 조회

- `GET /api/v1/reservations`
- 인증 필요

#### 조회 규칙

- 로그인한 사용자의 **확정된 예약(`reservations`)** 과 **대기 중인 예약(`waitings`)** 을 한 번에 반환한다.
- 대기 항목은 *같은 슬롯 안에서의 신청 순서*인 `order` 를 함께 내려준다. (1 = 첫 번째 대기자)
- 쿼리 파라미터 없음.

#### 응답 예시

```json
{
  "reservations": [
    {
      "id": 1,
      "memberId": 1,
      "date": "2026-05-27",
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "이든의 공포 하우스",
        "description": "이든이 귀신으로 나오는 공포 테마",
        "imgUrl": "https://images.example.com/themes/horror-house.jpg"
      },
      "store": {
        "id": 1,
        "name": "강남점"
      }
    }
  ],
  "waitings": [
    {
      "order": 1,
      "reservation": {
        "id": 1,
        "date": "2026-05-27",
        "time": {
          "id": 1,
          "startAt": "10:00"
        },
        "theme": {
          "id": 1,
          "name": "이든의 공포 하우스",
          "description": "이든이 귀신으로 나오는 공포 테마",
          "imgUrl": "https://images.example.com/themes/horror-house.jpg"
        },
        "store": {
          "id": 1,
          "name": "강남점"
        }
      },
      "memberId": 1,
      "createdAt": "2026-05-27T12:00:01"
    },
    {
      "order": 2,
      "reservation": {
        "id": 3,
        "date": "2026-05-29",
        "time": {
          "id": 1,
          "startAt": "10:00"
        },
        "theme": {
          "id": 1,
          "name": "이든의 공포 하우스",
          "description": "이든이 귀신으로 나오는 공포 테마",
          "imgUrl": "https://images.example.com/themes/horror-house.jpg"
        },
        "store": {
          "id": 1,
          "name": "강남점"
        }
      },
      "memberId": 1,
      "createdAt": "2026-05-27T12:00:05"
    }
  ]
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `reservations[]` | `Array<ReservationResponse>` | 확정된 예약 목록 |
| `reservations[].time` | `Object` | 예약 시간 (`id`, `startAt`) |
| `reservations[].theme` | `Object` | 테마 정보 (`id`, `name`, `description`, `imgUrl`) |
| `reservations[].store` | `Object` | 매장 정보 (`id`, `name`) |
| `waitings[]` | `Array<WaitingResponse>` | 대기 중인 예약 목록 |
| `waitings[].order` | `Long` | 같은 슬롯 안에서의 대기 순번 (1부터 시작) |
| `waitings[].reservation` | `Object` | 대기 중인 원본 예약 정보 |
| `waitings[].reservation.id` | `Long` | 대기 중인 원본 예약 ID |
| `waitings[].reservation.date` | `String` | 예약 날짜 (`yyyy-MM-dd`) |
| `waitings[].reservation.time` | `Object` | 예약 시간 (`id`, `startAt`) |
| `waitings[].reservation.theme` | `Object` | 테마 정보 (`id`, `name`, `description`, `imgUrl`) |
| `waitings[].reservation.store` | `Object` | 매장 정보 (`id`, `name`) |
| `waitings[].memberId` | `Long` | 회원 ID |
| `waitings[].createdAt` | `String` | 대기 신청 시각 (ISO-8601) |

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 예약·대기 목록 조회 성공 |
| `401 Unauthorized` | 비로그인 |

### 1-2. 예약 생성

- `POST /api/v1/reservations`
- 인증 필요. 예약자(`memberId`)는 **요청의 JWT(쿠키 또는 `Authorization` 헤더)에서 추출하여 서버가 주입**하므로 요청 본문에 없다.

#### 요청 본문

```json
{
  "date": "2026-05-14",
  "timeId": 1,
  "themeId": 1,
  "storeId": 1
}
```

#### 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 예약 날짜 |
| `timeId` | `Long` | Y | 예약 시간 ID |
| `themeId` | `Long` | Y | 테마 ID |
| `storeId` | `Long` | Y | 매장 ID (예약할 매장) |

#### 검증 규칙

- `date`: null 불가, `yyyy-MM-dd` 형식
- `timeId`: null 불가, 양수
- `themeId`: null 불가, 양수
- `storeId`: null 불가, 양수
- 지나간 날짜·시간 예약 불가
- 같은 매장 + 날짜 + 시간 + 테마 중복 예약 불가 (다른 매장이면 같은 날짜·시간·테마 허용)

#### 응답 예시

```json
{
  "id": 1,
  "memberId": 1,
  "date": "2026-05-14",
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "이든의 공포 하우스",
    "description": "이든이 귀신으로 나오는 공포 테마",
    "imgUrl": "https://images.example.com/themes/horror-house.jpg"
  },
  "store": {
    "id": 1,
    "name": "강남점"
  }
}
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 예약 생성 성공 |
| `400 Bad Request` | 입력값 오류, 잘못된 날짜 형식, 지나간 날짜·시간 |
| `401 Unauthorized` | 비로그인 |
| `409 Conflict` | 같은 날짜 + 시간 + 테마 중복 예약 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `COMMON400_001` | 유효하지 않은 요청 필드 |
| `COMMON400_004` | 올바르지 않은 입력값 형식 |
| `RESERVATION400_001` | 지나간 날짜와 시간 예약 시도 |
| `RESERVATION409_001` | 이미 예약이 존재함 |
| `AUTH401_002` | 비로그인 |

### 1-3. 예약 삭제

- `DELETE /api/v1/reservations/{id}`
- 인증 필요. 본인 예약만 삭제 가능.

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 예약 삭제 성공 |
| `400 Bad Request` | 이미 지난 예약 취소 시도, 본인 예약이 아님 |
| `401 Unauthorized` | 비로그인 |
| `404 Not Found` | 존재하지 않는 예약 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `RESERVATION400_002` | 이미 지난 예약은 취소할 수 없음 |
| `RESERVATION400_003` | 본인 예약이 아님 |
| `RESERVATION404_001` | 존재하지 않는 예약 |
| `AUTH401_002` | 비로그인 |

### 1-4. 예약 날짜·시간 변경

- `PATCH /api/v1/reservations/{id}`
- 인증 필요. 본인 예약만 변경 가능.

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 요청 본문

```json
{
  "date": "2026-05-15",
  "timeId": 2
}
```

#### 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 변경할 예약 날짜 |
| `timeId` | `Long` | Y | 변경할 예약 시간 ID |

#### 검증 규칙

- `date`: null 불가, `yyyy-MM-dd` 형식
- `timeId`: null 불가, 양수
- **로그인 사용자가 예약 소유자와 일치해야 한다.**
- 존재하는 예약 시간으로만 변경할 수 있다.
- 지나간 날짜·시간으로 변경할 수 없다.

#### 응답 예시

```json
{
  "id": 1,
  "memberId": 1,
  "date": "2026-05-15",
  "time": {
    "id": 2,
    "startAt": "11:00"
  },
  "theme": {
    "id": 1,
    "name": "이든의 공포 하우스",
    "description": "이든이 귀신으로 나오는 공포 테마",
    "imgUrl": "https://images.example.com/themes/horror-house.jpg"
  },
  "store": {
    "id": 1,
    "name": "강남점"
  }
}
```

> `storeId`는 변경할 수 없다. 매장 이동이 필요한 경우 기존 예약을 취소하고 새로 생성한다.

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 예약 변경 성공 |
| `400 Bad Request` | 입력값 오류, 잘못된 날짜 형식, 지나간 날짜·시간, 본인 예약 아님 |
| `401 Unauthorized` | 비로그인 |
| `404 Not Found` | 존재하지 않는 예약 또는 예약 시간 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `COMMON400_001` | 유효하지 않은 요청 필드 |
| `COMMON400_004` | 올바르지 않은 입력값 형식 |
| `RESERVATION400_001` | 지나간 날짜와 시간으로 변경 시도 |
| `RESERVATION400_003` | 본인 예약이 아님 |
| `RESERVATION404_001` | 존재하지 않는 예약 |
| `RESERVATION_TIME404_001` | 존재하지 않는 예약 시간 |
| `AUTH401_002` | 비로그인 |

### 1-5. 예약 대기 신청

- `POST /api/v1/reservations/{id}/waits`
- 인증 필요. 신청자(`memberId`)는 **JWT(쿠키 또는 `Authorization` 헤더)에서 추출**되어 서버가 주입한다.

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 대기 신청 대상 예약 ID |

#### 요청 본문

- 없음

#### 검증 규칙

- 대상 예약이 존재해야 한다.
- 대상 예약은 *다른 사용자가* 한 예약이어야 한다 (본인 예약에는 대기 불가).
- 대상 예약 슬롯이 *지나간 시간*이면 신청 불가.
- 같은 회원이 *같은 슬롯*에 대기 신청은 1회만 가능 (중복 신청 불가).

#### 응답 예시

```json
{
  "id": 1,
  "reservationId": 1,
  "memberId": 1,
  "createdAt": "2026-05-27T12:00:01",
  "order": 3
}
```

#### 응답 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 생성된 예약 대기 ID (테이블 PK, 순번 아님) |
| `reservationId` | `Long` | 대기 대상 예약 ID |
| `memberId` | `Long` | 신청 회원 ID |
| `createdAt` | `String` | 신청 시각 (ISO-8601) |
| `order` | `Long` | 같은 슬롯 안에서의 대기 순번 (1 = 첫 번째 대기자). 방금 신청한 본인이 보통 마지막이므로 현재 대기 인원과 같다 |

> `id`는 전체 대기에 걸친 auto-increment PK라 "이 슬롯에서 몇 번째인지"를 나타내지 않는다. 본인의 순번은 `order`로 확인한다. (조회 API `GET /api/v1/reservations`의 `waitings[].order`와 동일한 기준: `reservation_id`별 `created_at` 오름차순 순위)

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 예약 대기 신청 성공 |
| `401 Unauthorized` | 비로그인 |
| `404 Not Found` | 존재하지 않는 예약 |
| `409 Conflict` | 같은 슬롯에 본인 대기가 이미 존재 |
| `422 Unprocessable Entity` | 지난 시간 슬롯에 대한 대기 신청 / 본인 예약에 대기 시도 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH401_002` | 비로그인 |
| `RESERVATION404_001` | 존재하지 않는 예약 |
| `RESERVATION_WAIT409_001` | 같은 슬롯에 본인 대기가 이미 존재함 |
| `RESERVATION_WAIT422_001` | 지난 시간 슬롯의 대기 신청 |
| `RESERVATION_WAIT422_002` | 본인 예약에 대기 시도 |

### 1-6. 예약 대기 취소

- `DELETE /api/v1/reservations/{reservationId}/waits/mine`
- 인증 필요. 본인의 대기만 취소 가능.

#### 설계 메모

대상 대기는 `(reservationId, memberId)` 조합으로 *유일하게 식별*된다 (도메인 unique key). 따라서 URL에 `waitId`를 노출할 필요가 없고, `memberId`는 인증 토큰에서 추출한다.

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `reservationId` | `Long` | 대기를 걸어둔 예약 ID |

#### 요청 본문

- 없음

#### 응답

- 본문 없음

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 예약 대기 취소 성공 |
| `401 Unauthorized` | 비로그인 |
| `404 Not Found` | 본인 대기가 존재하지 않음 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH401_002` | 비로그인 |
| `RESERVATION_WAIT404_001` | 존재하지 않는 예약 대기 |

---

## 2. 예약 시간

### 2-1. 예약 시간 목록 조회

- `GET /api/v1/reservation-times`
- 공개

#### 응답 예시

```json
[
  {
    "id": 1,
    "startAt": "10:00"
  },
  {
    "id": 2,
    "startAt": "11:00"
  }
]
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 예약 시간 목록 조회 성공 |

### 2-2. 예약 가능 시간 조회

- `GET /api/v1/reservation-times/availability?date={date}&themeId={themeId}`
- 공개

#### 쿼리 파라미터

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 조회 날짜 |
| `themeId` | `Long` | Y | 테마 ID |

#### 응답 예시

```json
[
  {
    "id": 1,
    "time": "10:00",
    "available": false,
    "reservationId": 5
  },
  {
    "id": 2,
    "time": "11:00",
    "available": true,
    "reservationId": null
  }
]
```

#### 응답 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 시간 ID |
| `time` | `String` | 시작 시간 (`HH:mm`) |
| `available` | `Boolean` | 예약 가능 여부. 해당 슬롯에 예약이 없으면 `true`, 이미 예약되어 있으면 `false` |
| `reservationId` | `Long` | 해당 슬롯을 이미 차지한 예약 ID. 예약 가능한(`available: true`) 슬롯은 `null` |

> `available: false`인(이미 예약된) 슬롯은 `reservationId`로 `POST /api/v1/reservations/{reservationId}/waits`를 호출해 **예약 대기**를 신청할 수 있다. 즉, 사용자는 예약 가능 시간 조회 화면에서 이미 찬 슬롯을 그대로 대기 신청에 활용한다.

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 예약 가능 시간 조회 성공 |
| `400 Bad Request` | 쿼리 문자열 누락, 날짜 형식 오류 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `COMMON400_003` | 쿼리 문자열 누락 |
| `COMMON400_005` | 쿼리 문자열 형식 오류 |

---

## 3. 테마

### 3-1. 테마 목록 조회

- `GET /api/v1/themes`
- 공개

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

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 테마 목록 조회 성공 |

### 3-2. 인기 테마 조회

- `GET /api/v1/themes/popular?from={from}&to={to}`
- 공개

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

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 인기 테마 조회 성공 |
| `400 Bad Request` | 쿼리 문자열 누락, 날짜 형식 오류 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `COMMON400_003` | 쿼리 문자열 누락 |
| `COMMON400_005` | 쿼리 문자열 형식 오류 |

---

## 4. 관리자 예약 시간

> **MANAGER 권한 필요**. 비로그인 시 `401` (`AUTH401_002`), USER 권한이면 `403` (`AUTH403_001`).

### 4-1. 예약 시간 생성

- `POST /api/v1/admin/reservation-times`
- 인증 필요

#### 요청 본문

```json
{
  "startAt": "10:00"
}
```

#### 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `startAt` | `String` | Y | 예약 시작 시간 |

#### 검증 규칙

- `startAt`: null 불가, `HH:mm` 형식

#### 응답 예시

```json
{
  "id": 1,
  "startAt": "10:00"
}
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 예약 시간 생성 성공 |
| `400 Bad Request` | 입력값 오류, 시간 형식 오류 |
| `401 Unauthorized` | 비로그인 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `COMMON400_001` | 유효하지 않은 요청 필드 |
| `COMMON400_004` | 올바르지 않은 입력값 형식 |
| `AUTH401_002` | 비로그인 |

### 4-2. 예약 시간 삭제

- `DELETE /api/v1/admin/reservation-times/{id}`
- 인증 필요

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 시간 ID |

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 예약 시간 삭제 성공 |
| `401 Unauthorized` | 비로그인 |
| `404 Not Found` | 존재하지 않는 예약 시간 |
| `409 Conflict` | 예약이 존재하는 예약 시간 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `RESERVATION_TIME404_001` | 존재하지 않는 예약 시간 |
| `RESERVATION_TIME409_001` | 예약이 존재하는 예약 시간 |
| `AUTH401_002` | 비로그인 |

---

## 5. 관리자 테마

> **MANAGER 권한 필요**. 비로그인 시 `401` (`AUTH401_002`), USER 권한이면 `403` (`AUTH403_001`).

### 5-1. 테마 생성

- `POST /api/v1/admin/themes`
- 인증 필요

#### 요청 본문

```json
{
  "name": "이든의 공포 하우스",
  "description": "이든이 귀신으로 나오는 공포 테마",
  "imgUrl": "https://images.example.com/themes/horror-house.jpg"
}
```

#### 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | `String` | Y | 테마 이름 |
| `description` | `String` | Y | 테마 설명 |
| `imgUrl` | `String` | Y | 테마 이미지 URL |

#### 검증 규칙

- `name`: 공백 불가, 2자 이상 100자 이하
- `description`: 공백 불가, 100자 이하
- `imgUrl`: 공백 불가, URL 형식

#### 응답 예시

```json
{
  "id": 1,
  "name": "이든의 공포 하우스",
  "description": "이든이 귀신으로 나오는 공포 테마",
  "imgUrl": "https://images.example.com/themes/horror-house.jpg"
}
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `201 Created` | 테마 생성 성공 |
| `400 Bad Request` | 입력값 오류 |
| `401 Unauthorized` | 비로그인 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `COMMON400_001` | 유효하지 않은 요청 필드 |
| `AUTH401_002` | 비로그인 |

### 5-2. 테마 삭제

- `DELETE /api/v1/admin/themes/{id}`
- 인증 필요

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 테마 ID |

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 테마 삭제 성공 |
| `401 Unauthorized` | 비로그인 |
| `404 Not Found` | 존재하지 않는 테마 |
| `409 Conflict` | 예약이 존재하는 테마 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `THEME404_001` | 존재하지 않는 테마 |
| `THEME409_001` | 예약이 존재하는 테마 |
| `AUTH401_002` | 비로그인 |

---

## 6. 매장

### 6-1. 매장 목록 조회

- `GET /api/v1/stores`
- 공개

#### 응답 예시

```json
[
  { "id": 1, "name": "강남점" },
  { "id": 2, "name": "홍대점" },
  { "id": 3, "name": "판교점" }
]
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 매장 목록 조회 성공 |

---

## 7. 관리자 매장 예약 (매니저 전용)

> **MANAGER 권한 필요**. 로그인은 했지만 `role != MANAGER`이면 `403` (`AUTH403_001`).
> 매니저는 *자기 매장* 예약만 관리할 수 있다. 다른 매장 예약 접근 시도는 `403` (`AUTH403_002`).

### 7-1. 자기 매장 예약 목록 조회

- `GET /api/v1/admin/store/reservations`
- 매니저 권한 필요. 매니저의 `storeId`로 자동 필터링되어 *본인 매장 예약만* 반환한다.

#### 응답 예시

```json
[
  {
    "id": 1,
    "memberId": 3,
    "date": "2026-12-01",
    "time": { "id": 1, "startAt": "10:00" },
    "theme": {
      "id": 1,
      "name": "이든의 공포 하우스",
      "description": "이든이 귀신으로 나오는 공포 테마",
      "imgUrl": "https://images.example.com/themes/horror-house.jpg"
    },
    "store": { "id": 1, "name": "강남점" }
  }
]
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 매장 예약 목록 조회 성공 |
| `401 Unauthorized` | 비로그인 또는 무효 토큰 |
| `403 Forbidden` | 매니저 권한 없음 (USER) |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH401_002` | 비로그인 |
| `AUTH401_003` | 유효하지 않은 토큰 |
| `AUTH403_001` | 매니저 권한 없음 |

### 7-2. 매장 예약 변경 (매니저)

- `PATCH /api/v1/admin/store/reservations/{id}`
- 매니저 권한 필요. 자기 매장의 예약 날짜·시간을 변경한다.

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 요청 본문

```json
{
  "date": "2026-12-15",
  "timeId": 2
}
```

#### 요청 필드

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `date` | `String` | Y | 변경할 예약 날짜 |
| `timeId` | `Long` | Y | 변경할 예약 시간 ID |

#### 검증 규칙

- `date`: null 불가, `yyyy-MM-dd` 형식
- `timeId`: null 불가, 양수
- **매니저는 자기 매장(`storeId` 일치) 예약만 변경할 수 있다.**
- 지나간 날짜·시간으로 변경 불가
- 변경할 예약 시간이 존재해야 함

#### 응답 예시

```json
{
  "id": 1,
  "memberId": 3,
  "date": "2026-12-15",
  "time": { "id": 2, "startAt": "11:00" },
  "theme": {
    "id": 1,
    "name": "이든의 공포 하우스",
    "description": "이든이 귀신으로 나오는 공포 테마",
    "imgUrl": "https://images.example.com/themes/horror-house.jpg"
  },
  "store": { "id": 1, "name": "강남점" }
}
```

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `200 OK` | 예약 변경 성공 |
| `400 Bad Request` | 입력값 오류, 잘못된 날짜 형식, 지나간 날짜·시간 |
| `401 Unauthorized` | 비로그인 또는 무효 토큰 |
| `403 Forbidden` | 매니저 권한 없음 또는 다른 매장 예약 |
| `404 Not Found` | 존재하지 않는 예약 또는 예약 시간 |
| `409 Conflict` | 변경하려는 시간이 이미 예약됨 |

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH403_001` | 매니저 권한 없음 |
| `AUTH403_002` | 다른 매장의 예약 |
| `RESERVATION404_001` | 존재하지 않는 예약 |
| `RESERVATION_TIME404_001` | 존재하지 않는 예약 시간 |
| `RESERVATION409_001` | 이미 예약된 시간 |
| `RESERVATION400_001` | 지나간 날짜·시간 |

### 7-3. 매장 예약 삭제 (매니저)

- `DELETE /api/v1/admin/store/reservations/{id}`
- 매니저 권한 필요. 자기 매장의 예약을 삭제한다.

#### 경로 변수

| 이름 | 타입 | 설명 |
| --- | --- | --- |
| `id` | `Long` | 예약 ID |

#### 응답 코드

| 상태 코드 | 설명 |
| --- | --- |
| `204 No Content` | 예약 삭제 성공 |
| `401 Unauthorized` | 비로그인 또는 무효 토큰 |
| `403 Forbidden` | 매니저 권한 없음 또는 다른 매장 예약 |
| `404 Not Found` | 존재하지 않는 예약 |

> 일반 사용자의 `DELETE /api/v1/reservations/{id}`와 달리 **지난 예약도 삭제 가능**하다. 매니저는 사후 정산·정리 목적으로 과거 예약을 정리할 수 있다.

#### 주요 에러 코드

| 에러 코드 | 설명 |
| --- | --- |
| `AUTH403_001` | 매니저 권한 없음 |
| `AUTH403_002` | 다른 매장의 예약 |
| `RESERVATION404_001` | 존재하지 않는 예약 |

---

## 빠른 요약

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
