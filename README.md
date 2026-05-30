<p align="center">
    <img src="https://github.com/user-attachments/assets/eb5a8839-ed5b-453b-a764-85ffcc9d0219"/>
</p>

<h1 align="middle">방탈출 예약 서비스</h1>

![Static Badge](https://img.shields.io/badge/Java-21-green)
![Static Badge](https://img.shields.io/badge/Version-1.0.0-red)

우아한테크코스 BE 레벨 2 방탈출 예약 서비스를 구현한 저장소입니다.

# 목차

- [API 명세서](#api-명세서)
    - [Reservation](#reservation)
    - [Time](#time)
    - [Theme](#theme)
- [컨벤션](#컨벤션)
    - [예외 처리](#예외-처리)
- [클라이언트 기능](#클라이언트-기능)
    - [공통(사용자)](#공통사용자)
        - [인기 테마 조회 기능](#인기-테마-조회-기능)
        - [전체 테마 조회 기능](#전체-테마-조회-기능)
        - [예약 생성 기능](#예약-생성-기능)
    - [관리자](#관리자)
        - [예약 추가 기능](#예약-추가-기능)
        - [예약 관리 기능](#예약-관리-기능)
        - [테마 관리 기능](#테마-관리-기능)
        - [시간 추가 및 관리 기능](#시간-추가-및-관리-기능)

# API 명세서

## Reservation

### `POST /api/reservations` 예약 생성 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 예약을 생성합니다.
2. 삭제된 예약 시간 또는 삭제된 테마로는 예약을 생성할 수 없습니다.
3. 삭제되지 않고 취소되지 않은 예약 중 같은 날짜, 시간, 테마의 예약이 이미 있으면 생성할 수 없습니다.
4. 삭제된 예약과 취소된 예약은 중복 예약 검사에서 제외합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                                      | HTTP Code                 | 상위 에러 메시지             |
|-----|-----------------------------------------|---------------------------|-----------------------|
| RC1 | 요청 처리 성공                                | 201 Created               | 없음                    |
| RC2 | 요청 본문 JSON 형식 또는 필드 자료형이 올바르지 않은 경우     | 400 Bad Request           | 요청 형식이 올바르지 않습니다.     |
| RC3 | dto 필드 제약 조건을 만족하지 못한 경우                | 400 Bad Request           | 요청 값이 올바르지 않습니다.      |
| RC4 | 참조 자원이 존재하지 않는 경우                       | 404 Not Found             | 조회할 자원이 존재하지 않습니다.    |
| RC5 | date, timeId, themeId에 해당하는 예약이 존재하는 경우 | 409 Conflict              | 이미 예약된 날짜, 시간, 테마입니다. |
| RC6 | 서버 오류인 경우                               | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다.    |

### 파라미터별 에러 메시지

| ID    | 상황                   | 문제 파라미터명 | 파라미터 에러 메시지             |
|-------|----------------------|----------|-------------------------|
| RC3-1 | name이 공백이거나 null인 경우 | name     | 예약자명은 필수입니다.            |
| RC3-2 | name의 길이가 20 초과인 경우  | name     | 예약자명의 길이는 1이상 20이하 입니다. |
| RC3-3 | date가 null인 경우       | date     | 예약 날짜는 필수입니다.           |
| RC3-4 | date가 현재 날짜보다 과거인 경우 | date     | 예약 날짜가 현재보다 과거입니다.      |
| RC3-5 | timeId가 null인 경우     | timeId   | timeId는 필수입니다.          |
| RC3-6 | timeId가 양수가 아닌 경우    | timeId   | timeId의 값이 유효하지 않습니다.   |
| RC3-7 | themeId가 null인 경우    | themeId  | themeId는 필수입니다.         |
| RC3-8 | themeId가 양수가 아닌 경우   | themeId  | themeId의 값이 유효하지 않습니다.  |
| RC4-1 | timeId가 존재하지 않는 경우   | timeId   | 존재 하지 않는 시간대입니다.        |
| RC4-2 | themeId가 존재하지 않는 경우  | themeId  | 존재 하지 않는 테마입니다.         |

### Request Body

| 필드      | 타입     | 필수 여부 | 설명       | 예시         |
|---------|--------|-------|----------|------------|
| name    | String | 필수    | 예약자 이름   | 브라운        |
| date    | Date   | 필수    | 예약 날짜    | 2026-05-12 |
| timeId  | Number | 필수    | 예약 시간 ID | 1          |
| themeId | Number | 필수    | 예약 테마 ID | 1          |

### 예시

```json
{
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 1
}
```

### Response Body

#### RC1 성공 예시

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 1
}
```

### 실패 예시

#### RC2 요청 본문 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RC3 dto 필드 제약 조건 실패

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "name",
      "message": "예약자명은 필수입니다."
    },
    {
      "parameter": "date",
      "message": "예약 날짜가 현재보다 과거입니다."
    }
  ]
}
```

#### RC4 참조 자원 조회 실패

```json
{
  "message": "조회할 자원이 존재하지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "timeId",
      "message": "존재 하지 않는 시간대입니다."
    },
    {
      "parameter": "themeId",
      "message": "존재 하지 않는 테마입니다."
    }
  ]
}
```

#### RC5 중복 예약

```json
{
  "message": "이미 예약된 날짜, 시간, 테마입니다."
}
```

#### RC6 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `GET /api/admin/reservations` 관리자 예약 목록 조회 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 등록된 전체 예약 목록을 조회합니다.
2. 삭제되지 않은 예약만 조회합니다.
3. 예약이 삭제되지 않았다면 연결된 예약 시간 또는 테마가 삭제되어 있어도 예약 이력으로 조회합니다.
4. 각 예약에 상태(status), 상태 메시지(message), 대기 순번(waitingNumber)을 함께 반환합니다.
5. 조회 가능한 예약이 없으면 빈 목록을 반환합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황        | HTTP Code                 | 상위 에러 메시지          |
|-----|-----------|---------------------------|--------------------|
| RL1 | 요청 처리 성공  | 200 OK                    | 없음                 |
| RL2 | 서버 오류인 경우 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### Request Body

없음

### Response Body

#### RL1 성공 예시

```json
[
  {
    "id": 1,
    "name": "브라운",
    "date": "2026-05-04",
    "time": {
      "id": 2,
      "startAt": "12:00",
      "deleted": false
    },
    "theme": {
      "id": 1,
      "name": "피온피온",
      "description": "설명",
      "imageUrl": "https://roomescape.com/images/themes/ring-banner.png",
      "deleted": false
    },
    "status": "EDITABLE",
    "message": "",
    "waitingNumber": null
  }
]
```

### 실패 예시

#### RL2 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `DELETE /api/admin/reservations/{id}` 관리자 예약 삭제 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 예약 ID로 예약을 삭제합니다.
2. 예약 삭제는 실제 DELETE가 아니라 status를 DELETED로 변경하는 soft delete로 처리합니다.
3. 이미 삭제된 예약은 존재하지 않는 예약으로 처리합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                          | HTTP Code                 | 상위 에러 메시지          |
|-----|-----------------------------|---------------------------|--------------------|
| RD1 | 요청 처리 성공                    | 204 No Content            | 없음                 |
| RD2 | 예약 ID의 자료형이 올바르지 않은 경우      | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| RD3 | 예약 ID의 값이 제약 조건을 만족하지 못한 경우 | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| RD4 | 예약 ID가 존재하지 않는 경우           | 404 Not Found             | 예약을 찾을 수 없습니다.     |
| RD5 | 서버 오류인 경우                   | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황               | 문제 파라미터명 | 파라미터 에러 메시지      |
|-------|------------------|----------|------------------|
| RD3-1 | id의 값이 양수가 아닌 경우 | id       | id의 값은 양수여야 합니다. |

### Path Variables

| 이름 | 타입     | 필수 여부 | 설명        | 예시 |
|----|--------|-------|-----------|----|
| id | Number | 필수    | 삭제할 예약 ID | 1  |

### Request Body

없음

### Response Body

#### RD1 성공 예시

```text
응답 본문 없음
```

### 실패 예시

#### RD2 Path Variable 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RD3 Path Variable 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "id",
      "message": "id의 값은 양수여야 합니다."
    }
  ]
}
```

#### RD4 예약 조회 실패

```json
{
  "message": "예약을 찾을 수 없습니다."
}
```

#### RD5 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `GET /api/reservations?name={name}` 사용자 이름 예약 조회 API

<details>
<summary>세부 내용</summary>

### 기능 요구 사항

```text
1. 사용자가 자신의 이름으로 본인의 예약 목록을 조회할 수 있습니다.
2. 사용자 이름으로 조회된 항목이 없으면 빈 배열을 반환합니다.
3. 예약 정렬 기준은 날짜순이고, 날짜가 같은 경우엔 시간순 오름차순 정렬입니다.
4. 삭제되지 않은 예약만 조회합니다.
5. 각 예약에 수정 가능 여부, 취소 가능 여부, 대기 여부, 취소 여부, 수정 권장 여부를 나타내는 status 필드를 포함해야 합니다.
6. 각 예약에 status에 맞는 message 필드를 포함해야 합니다.
7. time, theme 필드에 삭제 여부를 포함해야 합니다.
8. status가 EDITABLE이면 수정 가능, 취소 가능, 수정 권장 아님을 의미합니다.
9. status가 EDIT_RECOMMENDED이면 수정 가능, 취소 가능, 연결된 time 또는 theme가 삭제되어 수정 권장함을 의미합니다.
10. status가 LOCKED이면 수정 불가, 취소 불가, 이미 지난 예약을 의미합니다.
11. status가 CANCELED이면 이미 취소된 예약을 의미합니다.
12. status가 WAITING이면 대기 중인 예약을 의미합니다.
13. waitingNumber는 대기 순번을 의미합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                                        | HTTP Code                 | 상위 에러 메시지          |
|-----|-------------------------------------------|---------------------------|--------------------|
| RU1 | 요청 처리 성공                                  | 200 OK                    | 없음                 |
| RU2 | name Query Parameter가 누락된 경우              | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| RU3 | name Query Parameter 값이 제약 조건을 만족하지 못한 경우 | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| RU4 | 서버 오류인 경우                                 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황                  | 문제 파라미터명 | 파라미터 에러 메시지             |
|-------|---------------------|----------|-------------------------|
| RU3-1 | name이 공백인 경우        | name     | 예약자명은 필수입니다.            |
| RU3-2 | name의 길이가 20 초과인 경우 | name     | 예약자명의 길이는 1이상 20이하 입니다. |

### Query Parameters

| 이름   | 타입     | 필수 여부 | 설명     | 예시  |
|------|--------|-------|--------|-----|
| name | String | 필수    | 예약자 이름 | 브라운 |

### Request Body

없음

### Response Body

#### RU1 성공 예시

```json
[
  {
    "id": 1,
    "name": "브라운",
    "date": "2026-05-12",
    "time": {
      "id": 2,
      "startAt": "12:00",
      "deleted": false
    },
    "theme": {
      "id": 1,
      "name": "피온피온",
      "description": "설명",
      "imageUrl": "https://roomescape.com/images/themes/ring-banner.png",
      "deleted": false
    },
    "status": "EDITABLE",
    "message": "",
    "waitingNumber": null
  }
]
```

#### RU1 조회 결과 없음 예시

```json
[]
```

### 실패 예시

#### RU2 Query Parameter 누락

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RU3 Query Parameter 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "name",
      "message": "예약자명은 필수입니다."
    }
  ]
}
```

#### RU4 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `PATCH /api/reservations/{id}` 사용자 예약 변경 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 사용자가 예약 ID로 본인의 예약 정보를 변경합니다.
2. 요청의 예약자 이름은 기존 예약의 예약자 이름과 일치해야 합니다.
3. 예약 날짜, 예약 시간, 예약 테마를 변경할 수 있습니다.
4. 삭제된 예약은 변경할 수 없습니다.
5. 이미 취소된 예약은 변경할 수 없습니다.
6. 이미 지난 예약은 변경할 수 없습니다.
7. 삭제된 예약 시간 또는 삭제된 테마로는 예약을 변경할 수 없습니다.
8. 삭제되지 않고 취소되지 않은 예약 중 변경하려는 날짜, 시간, 테마의 예약이 이미 있으면 변경할 수 없습니다.
9. 자기 자신 예약은 중복 예약 검사에서 제외합니다.
10. 삭제된 예약과 취소된 예약은 중복 예약 검사에서 제외합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID   | 상황                                      | HTTP Code                 | 상위 에러 메시지             |
|------|-----------------------------------------|---------------------------|-----------------------|
| RP1  | 요청 처리 성공                                | 200 OK                    | 없음                    |
| RP2  | 예약 ID의 자료형이 올바르지 않은 경우                  | 400 Bad Request           | 요청 형식이 올바르지 않습니다.     |
| RP3  | 예약 ID의 값이 제약 조건을 만족하지 못한 경우             | 400 Bad Request           | 요청 값이 올바르지 않습니다.      |
| RP4  | 요청 본문 JSON 형식 또는 필드 자료형이 올바르지 않은 경우     | 400 Bad Request           | 요청 형식이 올바르지 않습니다.     |
| RP5  | dto 필드 제약 조건을 만족하지 못한 경우                | 400 Bad Request           | 요청 값이 올바르지 않습니다.      |
| RP6  | 조회한 예약의 예약자 이름과 요청 예약자 이름이 다른 경우        | 403 Forbidden             | 예약을 변경할 권한이 없습니다.     |
| RP7  | 예약 ID가 존재하지 않는 경우                       | 404 Not Found             | 예약을 찾을 수 없습니다.        |
| RP8  | 참조 자원이 존재하지 않는 경우                       | 404 Not Found             | 수정할 자원이 존재하지 않습니다.    |
| RP9  | 활성된(ACTIVE) 예약이 아닌 경우                     | 409 Conflict              | 활성된 예약이 아닙니다.         |
| RP10 | 이미 지난 예약을 변경하려는 경우                      | 409 Conflict              | 지난 예약은 변경할 수 없습니다.    |
| RP11 | date, timeId, themeId에 해당하는 예약이 존재하는 경우 | 409 Conflict              | 이미 예약된 날짜, 시간, 테마입니다. |
| RP12 | 서버 오류인 경우                               | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다.    |

### 파라미터별 에러 메시지

| ID    | 상황                   | 문제 파라미터명 | 파라미터 에러 메시지             |
|-------|----------------------|----------|-------------------------|
| RP3-1 | id의 값이 양수가 아닌 경우     | id       | id의 값은 양수여야 합니다.        |
| RP5-1 | date가 null인 경우       | date     | 예약 날짜는 필수입니다.           |
| RP5-2 | timeId가 null인 경우     | timeId   | timeId는 필수입니다.           |
| RP5-3 | timeId가 양수가 아닌 경우    | timeId   | timeId의 값이 양수가 아닙니다.    |
| RP5-4 | themeId가 null인 경우    | themeId  | themeId는 필수입니다.          |
| RP5-5 | themeId가 양수가 아닌 경우   | themeId  | themeId의 값이 양수가 아닙니다.   |
| RP8-1 | timeId가 존재하지 않는 경우   | timeId   | 존재 하지 않는 시간대입니다.        |
| RP8-2 | themeId가 존재하지 않는 경우  | themeId  | 존재 하지 않는 테마입니다.         |

### Path Variables

| 이름 | 타입     | 필수 여부 | 설명        | 예시 |
|----|--------|-------|-----------|----|
| id | Number | 필수    | 변경할 예약 ID | 1  |

### Request Body

| 필드      | 타입     | 필수 여부 | 설명                     | 예시         |
|---------|--------|-------|------------------------|------------|
| name    | String | 필수    | 기존 예약자와 일치해야 하는 예약자 이름 | 브라운        |
| date    | Date   | 필수    | 변경할 예약 날짜              | 2026-05-12 |
| timeId  | Number | 필수    | 변경할 예약 시간 ID           | 1          |
| themeId | Number | 필수    | 변경할 예약 테마 ID           | 1          |

### 예시

```json
{
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 4
}
```

### Response Body

#### RP1 성공 예시

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 4
}
```

### 실패 예시

#### RP2 Path Variable 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RP3 Path Variable 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "id",
      "message": "id의 값은 양수여야 합니다."
    }
  ]
}
```

#### RP4 요청 본문 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RP5 dto 필드 제약 조건 실패

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "date",
      "message": "예약 날짜는 필수입니다."
    },
    {
      "parameter": "timeId",
      "message": "timeId는 필수입니다."
    },
    {
      "parameter": "themeId",
      "message": "themeId는 필수입니다."
    }
  ]
}
```

#### RP6 예약자 이름 불일치

```json
{
  "message": "예약을 변경할 권한이 없습니다."
}
```

#### RP7 예약 조회 실패

```json
{
  "message": "예약을 찾을 수 없습니다."
}
```

#### RP8 참조 자원 조회 실패

```json
{
  "message": "수정할 자원이 존재하지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "timeId",
      "message": "존재 하지 않는 시간대입니다."
    },
    {
      "parameter": "themeId",
      "message": "존재 하지 않는 테마입니다."
    }
  ]
}
```

#### RP9 활성된 예약이 아닌 경우

```json
{
  "message": "활성된 예약이 아닙니다."
}
```

#### RP10 지난 예약 변경

```json
{
  "message": "지난 예약은 변경할 수 없습니다."
}
```

#### RP11 중복 예약

```json
{
  "message": "이미 예약된 날짜, 시간, 테마입니다."
}
```

#### RP12 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `PATCH /api/reservations/{id}/cancel?name={name}` 사용자 예약 취소 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 사용자가 예약 ID로 본인의 예약을 취소합니다.
2. 요청의 예약자 이름은 기존 예약의 예약자 이름과 일치해야 합니다.
3. 예약 취소는 status를 CANCELED로 변경하는 방식으로 처리합니다.
4. 취소된 예약은 삭제된 예약이 아니므로 이력으로 보존합니다.
5. 활성된(ACTIVE) 예약만 취소할 수 있습니다.
6. 삭제된 예약은 존재하지 않는 예약으로 처리합니다.
7. 이미 지난 예약은 취소할 수 없습니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID   | 상황                                        | HTTP Code                 | 상위 에러 메시지          |
|------|-------------------------------------------|---------------------------|--------------------|
| RX1  | 요청 처리 성공                                  | 200 OK                    | 없음                 |
| RX2  | 예약 ID의 자료형이 올바르지 않은 경우                    | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| RX3  | 예약 ID의 값이 제약 조건을 만족하지 못한 경우               | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| RX4  | name Query Parameter가 누락된 경우              | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| RX5  | name Query Parameter 값이 제약 조건을 만족하지 못한 경우 | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| RX6  | 조회한 예약의 예약자 이름과 요청 예약자 이름이 다른 경우          | 403 Forbidden             | 예약을 취소할 권한이 없습니다.  |
| RX7  | 예약 ID가 존재하지 않는 경우                         | 404 Not Found             | 예약을 찾을 수 없습니다.     |
| RX8  | 활성된(ACTIVE) 예약이 아닌 경우                       | 409 Conflict              | 활성된 예약이 아닙니다.      |
| RX9  | 이미 지난 예약을 취소하려는 경우                        | 409 Conflict              | 지난 예약은 취소할 수 없습니다. |
| RX10 | 서버 오류인 경우                                 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황                  | 문제 파라미터명 | 파라미터 에러 메시지             |
|-------|---------------------|----------|-------------------------|
| RX3-1 | id의 값이 양수가 아닌 경우    | id       | id의 값은 양수여야 합니다.        |
| RX5-1 | name이 공백인 경우        | name     | 예약자명은 필수입니다.            |
| RX5-2 | name의 길이가 20 초과인 경우 | name     | 예약자명의 길이는 1이상 20이하 입니다. |

### Path Variables

| 이름 | 타입     | 필수 여부 | 설명        | 예시 |
|----|--------|-------|-----------|----|
| id | Number | 필수    | 취소할 예약 ID | 1  |

### Query Parameters

| 이름   | 타입     | 필수 여부 | 설명                     | 예시  |
|------|--------|-------|------------------------|-----|
| name | String | 필수    | 기존 예약자와 일치해야 하는 예약자 이름 | 브라운 |

### Request Body

없음

### Response Body

#### RX1 성공 예시

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 4
}
```

### 실패 예시

#### RX2 Path Variable 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RX3 Path Variable 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "id",
      "message": "id의 값은 양수여야 합니다."
    }
  ]
}
```

#### RX4 Query Parameter 누락

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RX5 Query Parameter 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "name",
      "message": "예약자명은 필수입니다."
    }
  ]
}
```

#### RX6 예약자 이름 불일치

```json
{
  "message": "예약을 취소할 권한이 없습니다."
}
```

#### RX7 예약 조회 실패

```json
{
  "message": "예약을 찾을 수 없습니다."
}
```

#### RX8 활성된 예약이 아닌 경우

```json
{
  "message": "활성된 예약이 아닙니다."
}
```

#### RX9 지난 예약 취소

```json
{
  "message": "지난 예약은 취소할 수 없습니다."
}
```

#### RX10 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `PATCH /api/reservations/{id}/waitings/cancel?name={name}` 사용자 대기 예약 취소 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 사용자가 예약 ID로 본인의 대기중인 예약을 취소합니다.
2. 요청의 예약자 이름은 기존 예약의 예약자 이름과 일치해야 합니다.
3. 예약 취소는 status를 Status.CANCELED로 변경하는 방식으로 진행합니다.
4. 취소된 예약은 삭제된 예약이 아니므로 이력으로 보존합니다.
5. 대기중인 예약이 아니라면 취소할 수 없습니다.
6. 이미 지난 대기중인 예약은 취소할 수 없습니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID   | 상황                                        | HTTP Code                 | 상위 에러 메시지          |
|------|-------------------------------------------|---------------------------|--------------------|
| RY1  | 요청 처리 성공                                  | 200 OK                    | 없음                 |
| RY2  | 예약 ID의 자료형이 올바르지 않은 경우                    | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| RY3  | 예약 ID의 값이 제약 조건을 만족하지 못한 경우               | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| RY4  | name Query Parameter가 누락된 경우              | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| RY5  | name Query Parameter 값이 제약 조건을 만족하지 못한 경우 | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| RY6  | 조회한 예약의 예약자 이름과 요청 예약자 이름이 다른 경우          | 403 Forbidden             | 예약을 취소할 권한이 없습니다.  |
| RY7  | 예약 ID가 존재하지 않는 경우                         | 404 Not Found             | 예약을 찾을 수 없습니다.     |
| RY8  | 대기중인 예약이 아닌 경우                            | 409 Conflict              | 대기중인 예약이 아닙니다.     |
| RY9  | 이미 지난 대기중인 예약을 취소하려는 경우                   | 409 Conflict              | 지난 예약은 취소할 수 없습니다. |
| RY10 | 서버 오류인 경우                                 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황                  | 문제 파라미터명 | 파라미터 에러 메시지             |
|-------|---------------------|----------|-------------------------|
| RY3-1 | id의 값이 양수가 아닌 경우    | id       | id의 값은 양수여야 합니다.        |
| RY5-1 | name이 공백인 경우        | name     | 예약자명은 필수입니다.            |
| RY5-2 | name의 길이가 20 초과인 경우 | name     | 예약자명의 길이는 1이상 20이하 입니다. |

### Path Variables

| 이름 | 타입     | 필수 여부 | 설명        | 예시 |
|----|--------|-------|-----------|----|
| id | Number | 필수    | 취소할 예약 ID | 1  |

### Query Parameters

| 이름   | 타입     | 필수 여부 | 설명                     | 예시  |
|------|--------|-------|------------------------|-----|
| name | String | 필수    | 기존 예약자와 일치해야 하는 예약자 이름 | 브라운 |

### Request Body

없음

### Response Body

#### RY1 성공 예시

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 4
}
```

### 실패 예시

#### RY2 Path Variable 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RY3 Path Variable 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "id",
      "message": "id의 값은 양수여야 합니다."
    }
  ]
}
```

#### RY4 Query Parameter 누락

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RY5 Query Parameter 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "name",
      "message": "예약자명은 필수입니다."
    }
  ]
}
```

#### RY6 예약자 이름 불일치

```json
{
  "message": "예약을 취소할 권한이 없습니다."
}
```

#### RY7 예약 조회 실패

```json
{
  "message": "예약을 찾을 수 없습니다."
}
```

#### RY8 대기중인 예약이 아닌 예약

```json
{
  "message": "대기중인 예약이 아닙니다."
}
```

#### RY9 지난 예약 취소

```json
{
  "message": "지난 예약은 취소할 수 없습니다."
}
```

#### RY10 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `POST /api/reservations/waitings` 사용자 예약 대기 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 대기 상태인 예약을 생성합니다.
2. 삭제된 예약 시간 또는 삭제된 테마로는 예약을 생성할 수 없습니다.
3. 대기중인 예약 중 같은 이름, 날짜, 시간, 테마의 예약이 이미 있으면 생성할 수 없습니다.
4. 같은 이름, 날짜, 시간, 테마로 활성 예약이 이미 있으면 생성할 수 없습니다.
5. 현재보다 과거의 예약은 대기를 할 수 없습니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                                                         | HTTP Code                 | 상위 에러 메시지                   |
|-----|------------------------------------------------------------|---------------------------|-----------------------------|
| RW1 | 요청 처리 성공                                                   | 201 Created               | 없음                          |
| RW2 | 요청 본문 JSON 형식 또는 필드 자료형이 올바르지 않은 경우                        | 400 Bad Request           | 요청 형식이 올바르지 않습니다.           |
| RW3 | dto 필드 제약 조건을 만족하지 못한 경우                                   | 400 Bad Request           | 요청 값이 올바르지 않습니다.            |
| RW4 | 참조 자원이 존재하지 않는 경우                                          | 404 Not Found             | 조회할 자원이 존재하지 않습니다.          |
| RW5 | name, date, timeId, themeId에 해당하는 대기 예약이 이미 존재하는 경우        | 409 Conflict              | 이미 대기 중인 이름, 날짜, 시간, 테마입니다. |
| RW6 | name, date, timeId, themeId에 해당하는 활성 예약이 이미 존재하는 경우        | 409 Conflict              | 이미 예약된 날짜, 시간, 테마입니다.       |
| RW7 | 서버 오류인 경우                                                  | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다.          |

### 파라미터별 에러 메시지

| ID    | 상황                   | 문제 파라미터명 | 파라미터 에러 메시지             |
|-------|----------------------|----------|-------------------------|
| RW3-1 | name이 공백이거나 null인 경우 | name     | 예약자명은 필수입니다.            |
| RW3-2 | name의 길이가 20 초과인 경우  | name     | 예약자명의 길이는 1이상 20이하 입니다. |
| RW3-3 | date가 null인 경우       | date     | 예약 날짜는 필수입니다.           |
| RW3-4 | date가 현재 날짜보다 과거인 경우 | date     | 예약 날짜가 현재보다 과거입니다.      |
| RW3-5 | timeId가 null인 경우     | timeId   | timeId는 필수입니다.          |
| RW3-6 | timeId가 양수가 아닌 경우    | timeId   | timeId의 값이 유효하지 않습니다.   |
| RW3-7 | themeId가 null인 경우    | themeId  | themeId는 필수입니다.         |
| RW3-8 | themeId가 양수가 아닌 경우   | themeId  | themeId의 값이 유효하지 않습니다.  |
| RW4-1 | timeId가 존재하지 않는 경우   | timeId   | 존재 하지 않는 시간대입니다.        |
| RW4-2 | themeId가 존재하지 않는 경우  | themeId  | 존재 하지 않는 테마입니다.         |

### Request Body

| 필드      | 타입     | 필수 여부 | 설명       | 예시         |
|---------|--------|-------|----------|------------|
| name    | String | 필수    | 예약자 이름   | 브라운        |
| date    | Date   | 필수    | 예약 날짜    | 2026-05-12 |
| timeId  | Number | 필수    | 예약 시간 ID | 1          |
| themeId | Number | 필수    | 예약 테마 ID | 1          |

### 예시

```json
{
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 1
}
```

### Response Body

#### RW1 성공 예시

```json
{
  "id": 1,
  "name": "브라운",
  "date": "2026-05-12",
  "timeId": 1,
  "themeId": 1
}
```

### 실패 예시

#### RW2 요청 본문 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### RW3 dto 필드 제약 조건 실패

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "name",
      "message": "예약자명은 필수입니다."
    },
    {
      "parameter": "date",
      "message": "예약 날짜가 현재보다 과거입니다."
    }
  ]
}
```

#### RW4 참조 자원 조회 실패

```json
{
  "message": "조회할 자원이 존재하지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "timeId",
      "message": "존재 하지 않는 시간대입니다."
    },
    {
      "parameter": "themeId",
      "message": "존재 하지 않는 테마입니다."
    }
  ]
}
```

#### RW5 중복 대기 예약

```json
{
  "message": "이미 대기 중인 이름, 날짜, 시간, 테마입니다."
}
```

#### RW6 활성 예약 존재

```json
{
  "message": "이미 예약된 날짜, 시간, 테마입니다."
}
```

#### RW7 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

## Time

### `GET /api/times?date={date}&themeId={themeId}` 예약 시간 가능 여부 조회 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 특정 날짜와 테마 기준으로 예약 시간별 가능 여부를 조회합니다.
2. 삭제된 테마로는 예약 가능 시간을 조회할 수 없습니다.
3. 삭제되지 않은 예약 시간만 응답에 포함합니다.
4. 삭제되지 않고 취소되지 않은 활성 예약이 존재하는 시간은 `available`을 `false`로 반환합니다.
5. 삭제된 예약은 예약 점유로 보지 않습니다.
6. 취소된 예약은 예약 점유로 보지 않습니다.
7. 대기중인 예약은 예약 점유로 보지 않습니다.
8. 삭제된 예약 시간에 연결된 기존 예약은 예약 가능 여부 계산에 반영하지 않습니다.
9. 예약 가능한 시간이 없어도 삭제되지 않은 예약 시간은 `available`을 `false`로 포함해 반환합니다.
10. 삭제되지 않은 예약 시간이 없으면 빈 목록을 반환합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                                     | HTTP Code                 | 상위 에러 메시지          |
|-----|----------------------------------------|---------------------------|--------------------|
| TA1 | 요청 처리 성공                               | 200 OK                    | 없음                 |
| TA2 | Query Parameter가 누락되었거나 형식이 올바르지 않은 경우 | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| TA3 | Query Parameter 값이 제약 조건을 만족하지 못한 경우   | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| TA4 | themeId에 해당하는 테마가 존재하지 않는 경우           | 404 Not Found             | 조회할 자원이 존재하지 않습니다. |
| TA5 | 서버 오류인 경우                              | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황                  | 문제 파라미터명 | 파라미터 에러 메시지           |
|-------|---------------------|----------|-----------------------|
| TA3-1 | themeId가 양수가 아닌 경우  | themeId  | themeId의 값은 양수여야 합니다. |
| TA4-1 | themeId가 존재하지 않는 경우 | themeId  | 존재 하지 않는 테마입니다.       |

### Query Parameters

| 이름      | 타입     | 필수 여부 | 설명       | 예시         |
|---------|--------|-------|----------|------------|
| date    | Date   | 필수    | 예약 날짜    | 2026-05-12 |
| themeId | Number | 필수    | 예약 테마 ID | 1          |

### Request Body

없음

### Response Body

#### TA1 성공 예시

```json
[
  {
    "id": 1,
    "startAt": "20:30",
    "available": false
  },
  {
    "id": 2,
    "startAt": "22:30",
    "available": true
  }
]
```

#### TA1 예약 가능한 시간이 없는 경우 예시

```json
[
  {
    "id": 1,
    "startAt": "20:30",
    "available": false
  },
  {
    "id": 2,
    "startAt": "22:30",
    "available": false
  }
]
```

#### TA1 삭제되지 않은 예약 시간이 없는 경우 예시

```json
[]
```

### 실패 예시

#### TA2 Query Parameter 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### TA3 Query Parameter 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "themeId",
      "message": "themeId의 값은 양수여야 합니다."
    }
  ]
}
```

#### TA4 참조 자원 조회 실패

```json
{
  "message": "조회할 자원이 존재하지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "themeId",
      "message": "존재 하지 않는 테마입니다."
    }
  ]
}
```

#### TA5 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `GET /api/admin/times` 관리자 예약 시간 목록 조회 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 등록된 전체 예약 시간을 조회합니다.
2. 삭제되지 않은 예약 시간만 조회합니다.
3. 조회 가능한 예약 시간이 없으면 빈 목록을 반환합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황        | HTTP Code                 | 상위 에러 메시지          |
|-----|-----------|---------------------------|--------------------|
| TL1 | 요청 처리 성공  | 200 OK                    | 없음                 |
| TL2 | 서버 오류인 경우 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### Request Body

없음

### Response Body

#### TL1 성공 예시

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

### 실패 예시

#### TL2 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `POST /api/admin/times` 관리자 예약 시간 생성 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 예약 시간을 생성합니다.
2. 삭제되지 않은 예약 시간 중 같은 startAt이 이미 있으면 생성할 수 없습니다.
3. 삭제된 예약 시간과 같은 startAt은 다시 생성할 수 있습니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                                  | HTTP Code                 | 상위 에러 메시지          |
|-----|-------------------------------------|---------------------------|--------------------|
| TC1 | 요청 처리 성공                            | 201 Created               | 없음                 |
| TC2 | 요청 본문 JSON 형식 또는 필드 자료형이 올바르지 않은 경우 | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| TC3 | dto 필드 제약 조건을 만족하지 못한 경우            | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| TC4 | 이미 등록된 예약 시간인 경우                    | 409 Conflict              | 이미 등록된 예약 시간입니다.   |
| TC5 | 서버 오류인 경우                           | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황                | 문제 파라미터명 | 파라미터 에러 메시지   |
|-------|-------------------|----------|---------------|
| TC3-1 | startAt이 null인 경우 | startAt  | 예약 시간은 필수입니다. |

### Request Body

| 필드      | 타입   | 필수 여부 | 설명                              | 예시    |
|---------|------|-------|---------------------------------|-------|
| startAt | Time | 필수    | 예약 시작 시간 (HH:mm, 00:00 ~ 23:59) | 20:30 |

### 예시

```json
{
  "startAt": "20:30"
}
```

### Response Body

#### TC1 성공 예시

```json
{
  "id": 1,
  "startAt": "20:30"
}
```

### 실패 예시

#### TC2 요청 본문 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### TC3 dto 필드 제약 조건 실패

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "startAt",
      "message": "예약 시간은 필수입니다."
    }
  ]
}
```

#### TC4 중복 예약 시간

```json
{
  "message": "이미 등록된 예약 시간입니다."
}
```

#### TC5 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `DELETE /api/admin/times/{id}` 관리자 예약 시간 삭제 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 예약 시간 ID로 예약 시간을 삭제합니다.
2. 예약 시간 삭제는 실제 DELETE가 아니라 deleted_at에 현재 시각을 기록하는 soft delete로 처리합니다.
3. 이미 삭제된 예약 시간은 존재하지 않는 예약 시간으로 처리합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                             | HTTP Code                 | 상위 에러 메시지          |
|-----|--------------------------------|---------------------------|--------------------|
| TD1 | 요청 처리 성공                       | 204 No Content            | 없음                 |
| TD2 | 예약 시간 ID의 자료형이 올바르지 않은 경우      | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| TD3 | 예약 시간 ID의 값이 제약 조건을 만족하지 못한 경우 | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| TD4 | 예약 시간 ID가 존재하지 않는 경우           | 404 Not Found             | 예약 시간을 찾을 수 없습니다.  |
| TD5 | 서버 오류인 경우                      | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황               | 문제 파라미터명 | 파라미터 에러 메시지      |
|-------|------------------|----------|------------------|
| TD3-1 | id의 값이 양수가 아닌 경우 | id       | id의 값은 양수여야 합니다. |

### Path Variables

| 이름 | 타입     | 필수 여부 | 설명           | 예시 |
|----|--------|-------|--------------|----|
| id | Number | 필수    | 삭제할 예약 시간 ID | 1  |

### Request Body

없음

### Response Body

#### TD1 성공 예시

```text
응답 본문 없음
```

### 실패 예시

#### TD2 Path Variable 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### TD3 Path Variable 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "id",
      "message": "id의 값은 양수여야 합니다."
    }
  ]
}
```

#### TD4 예약 시간 조회 실패

```json
{
  "message": "예약 시간을 찾을 수 없습니다."
}
```

#### TD5 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

## Theme

### `GET /api/themes` 테마 목록 조회 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 등록된 전체 테마를 조회합니다.
2. 삭제되지 않은 테마만 조회합니다.
3. 조회 가능한 테마가 없으면 빈 목록을 반환합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황        | HTTP Code                 | 상위 에러 메시지          |
|-----|-----------|---------------------------|--------------------|
| HL1 | 요청 처리 성공  | 200 OK                    | 없음                 |
| HL2 | 서버 오류인 경우 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### Request Body

없음

### Response Body

#### HL1 성공 예시

```json
[
  {
    "id": 1,
    "name": "링",
    "description": "이것은 링 방탈출 설명입니다.",
    "imageUrl": "https://roomescape.com/images/themes/ring.png"
  },
  {
    "id": 2,
    "name": "감옥",
    "description": "이것은 감옥 방탈출 설명입니다.",
    "imageUrl": "https://roomescape.com/images/themes/prison-room.png"
  }
]
```

### 실패 예시

#### HL2 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `GET /api/themes/rankings/last-7-days` 최근 7일 인기 테마 조회 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 오늘을 제외한 직전 7일 동안 예약 수가 많은 테마를 조회합니다.
2. 오늘 날짜는 집계 기간에서 제외합니다.
3. 예약 날짜가 오늘을 제외한 직전 7일에 포함되는 예약 수를 기준으로 테마를 조회합니다.
4. 예약 수가 많은 순서로 정렬합니다.
5. 예약 수가 같으면 테마 ID가 작은 순서로 정렬합니다.
6. 최대 10개의 테마를 반환합니다.
7. 조회 가능한 인기 테마가 없으면 빈 목록을 반환합니다.
8. 삭제되지 않은 테마만 조회 결과에 포함합니다.
9. 삭제되지 않고 취소되지 않은 예약만 인기순 집계에 반영합니다.
10. 삭제되지 않은 예약 시간에 연결된 예약만 인기순 집계에 반영합니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황        | HTTP Code                 | 상위 에러 메시지          |
|-----|-----------|---------------------------|--------------------|
| HR1 | 요청 처리 성공  | 200 OK                    | 없음                 |
| HR2 | 서버 오류인 경우 | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### Request Body

없음

### Response Body

#### HR1 성공 예시

```json
[
  {
    "id": 1,
    "name": "링",
    "description": "이것은 링 방탈출 설명입니다.",
    "imageUrl": "https://roomescape.com/images/themes/ring.png"
  },
  {
    "id": 2,
    "name": "감옥",
    "description": "이것은 감옥 방탈출 설명입니다.",
    "imageUrl": "https://roomescape.com/images/themes/prison-room.png"
  }
]
```

### 실패 예시

#### HR2 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `POST /api/admin/themes` 관리자 테마 생성 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 테마를 생성합니다.
2. 삭제되지 않은 테마 중 같은 name이 이미 있으면 생성할 수 없습니다.
3. 삭제된 테마와 같은 name은 다시 생성할 수 있습니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                                  | HTTP Code                 | 상위 에러 메시지          |
|-----|-------------------------------------|---------------------------|--------------------|
| HC1 | 요청 처리 성공                            | 201 Created               | 없음                 |
| HC2 | 요청 본문 JSON 형식 또는 필드 자료형이 올바르지 않은 경우 | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| HC3 | dto 필드 제약 조건을 만족하지 못한 경우            | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| HC4 | 같은 name의 테마가 이미 존재하는 경우             | 409 Conflict              | 이미 등록된 테마입니다.      |
| HC5 | 서버 오류인 경우                           | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황                           | 문제 파라미터명    | 파라미터 에러 메시지                    |
|-------|------------------------------|-------------|--------------------------------|
| HC3-1 | name이 공백이거나 null인 경우         | name        | 테마 이름은 필수입니다.                  |
| HC3-2 | name의 길이가 255 초과인 경우         | name        | 테마 이름의 길이는 255 이하여야 합니다.       |
| HC3-3 | description이 공백이거나 null인 경우  | description | 테마 설명은 필수입니다.                  |
| HC3-4 | description의 길이가 255 초과인 경우  | description | 테마 설명의 길이는 255 이하여야 합니다.       |
| HC3-5 | imageUrl이 공백이거나 null인 경우     | imageUrl    | 테마 이미지 URL은 필수입니다.             |
| HC3-6 | imageUrl의 길이가 2000 초과인 경우    | imageUrl    | 테마 이미지 URL의 길이는 2000 이하여야 합니다. |
| HC3-7 | imageUrl의 URL 형식이 올바르지 않은 경우 | imageUrl    | 테마 이미지 URL 형식이 올바르지 않습니다.      |

### Request Body

| 필드          | 타입     | 필수 여부 | 설명         | 예시                                                   |
|-------------|--------|-------|------------|------------------------------------------------------|
| name        | String | 필수    | 테마 이름      | 브라운                                                  |
| description | String | 필수    | 테마 설명      | 테마 설명                                                |
| imageUrl    | String | 필수    | 테마 이미지 URL | https://roomescape.com/images/themes/prison-room.png |

### 예시

```json
{
  "name": "브라운",
  "description": "테마 설명",
  "imageUrl": "https://roomescape.com/images/themes/prison-room.png"
}
```

### Response Body

#### HC1 성공 예시

```json
{
  "id": 1,
  "name": "브라운",
  "description": "테마 설명",
  "imageUrl": "https://roomescape.com/images/themes/prison-room.png"
}
```

### 실패 예시

#### HC2 요청 본문 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### HC3 dto 필드 제약 조건 실패

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "name",
      "message": "테마 이름은 필수입니다."
    },
    {
      "parameter": "imageUrl",
      "message": "테마 이미지 URL 형식이 올바르지 않습니다."
    }
  ]
}
```

#### HC4 중복 테마 이름

```json
{
  "message": "이미 등록된 테마입니다."
}
```

#### HC5 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

### `DELETE /api/admin/themes/{id}` 관리자 테마 삭제 API

<details>
<summary>세부 내용</summary>

### 기능 요구사항

```text
1. 테마 ID로 테마를 삭제합니다.
2. 테마 삭제는 실제 DELETE가 아니라 deleted_at에 현재 시각을 기록하는 soft delete로 처리합니다.
3. 이미 삭제된 테마는 존재하지 않는 테마로 처리합니다.
4. 테마가 삭제되어도 해당 테마에 연결된 기존 예약은 함께 삭제하지 않습니다.
```

### 상황별 HTTP Code 및 상위 메시지

| ID  | 상황                          | HTTP Code                 | 상위 에러 메시지          |
|-----|-----------------------------|---------------------------|--------------------|
| HD1 | 요청 처리 성공                    | 204 No Content            | 없음                 |
| HD2 | 테마 ID의 자료형이 올바르지 않은 경우      | 400 Bad Request           | 요청 형식이 올바르지 않습니다.  |
| HD3 | 테마 ID의 값이 제약 조건을 만족하지 못한 경우 | 400 Bad Request           | 요청 값이 올바르지 않습니다.   |
| HD4 | 테마 ID가 존재하지 않는 경우           | 404 Not Found             | 테마를 찾을 수 없습니다.     |
| HD5 | 서버 오류인 경우                   | 500 Internal Server Error | 예상치 못한 오류가 발생했습니다. |

### 파라미터별 에러 메시지

| ID    | 상황               | 문제 파라미터명 | 파라미터 에러 메시지      |
|-------|------------------|----------|------------------|
| HD3-1 | id의 값이 양수가 아닌 경우 | id       | id의 값은 양수여야 합니다. |

### Path Variables

| 이름 | 타입     | 필수 여부 | 설명        | 예시 |
|----|--------|-------|-----------|----|
| id | Number | 필수    | 삭제할 테마 ID | 1  |

### Request Body

없음

### Response Body

#### HD1 성공 예시

```text
응답 본문 없음
```

### 실패 예시

#### HD2 Path Variable 오류

```json
{
  "message": "요청 형식이 올바르지 않습니다."
}
```

#### HD3 Path Variable 제약 조건 오류

```json
{
  "message": "요청 값이 올바르지 않습니다.",
  "parameterErrors": [
    {
      "parameter": "id",
      "message": "id의 값은 양수여야 합니다."
    }
  ]
}
```

#### HD4 테마 조회 실패

```json
{
  "message": "테마를 찾을 수 없습니다."
}
```

#### HD5 서버 오류

```json
{
  "message": "예상치 못한 오류가 발생했습니다."
}
```

</details>

## 컨벤션

### 예외 처리

현재 예외 처리는 웹 API 중심의 정책으로 가져갑니다.

- 도메인별 ErrorType은 비즈니스 오류 식별자, 사용자에게 보여줄 메시지, HTTP Status를 함께 관리합니다.
- 예외가 발생한 지점과 API 응답으로 내려갈 메시지 및 상태 코드를 가깝게 두어 흐름을 쉽게 파악할 수 있도록 합니다.
- 최상위 에러 응답은 `message`를 기본으로 사용합니다.
- 클라이언트가 한 번에 고칠 수 있는 필드 목록이 명확한 경우에만 `parameterErrors`를 함께 내려줍니다.
- JSON 파싱 실패, 타입 불일치, 필수 Query Parameter 누락처럼 필드 단위로 안정적으로 나누기 어려운 오류는 `message`만 내려줍니다.

## 클라이언트 기능

### 공통(사용자)

#### 인기 테마 조회 기능

지난 1주간 등록된 예약 중 가장 많이 등록된 대상 테마를 조회합니다.
(예시: 2026년 5월 5일에 조회 -> 검색 범위는 2026년 4월 29일부터 2026년 5월 4일)

<img alt="인기 테마 조회" src="https://github.com/user-attachments/assets/da440d2b-318b-48ff-85fd-4b1b03cb1866" />

#### 전체 테마 조회 기능

등록된 모든 테마를 조회합니다.

<img alt="전체 테마 조회" src="https://github.com/user-attachments/assets/5a55dddc-7d70-4e64-9d01-0cca9b59d602" />

#### 예약 생성 기능

테마를 선택하고, 정해진 예약 폼에 맞춰 예약합니다.

<img alt="예약 생성" src="https://github.com/user-attachments/assets/18155750-0c47-4979-9aec-481afeab4b06" />

### 관리자

#### 예약 추가 기능

직접 예약을 추가합니다.

<img alt="관리자 예약 생성" src="https://github.com/user-attachments/assets/2b318338-bccd-4524-a1af-3dde857db0f4" />

#### 예약 관리 기능

전체 예약 내역을 관리합니다. (필터링, 정렬 기준 제공)

<img alt="관리자 예약 관리" src="https://github.com/user-attachments/assets/741f5147-0abe-4a9a-bf4c-81cd507d98bd" />

#### 테마 추가 기능

테마를 추가합니다.

<img alt="관리자 테마 추가" src="https://github.com/user-attachments/assets/f28fc0bf-a9bb-49ae-923f-c345504b0673" />

#### 테마 관리 기능

등록된 전체 테마를 관리합니다. (ID 순으로 정렬)

<img alt="관리자 테마 관리" src="https://github.com/user-attachments/assets/15da0dc8-03aa-452d-994f-3a50f7c26de4" />

#### 시간 추가 및 관리 기능

시간을 등록하고, 등록된 전체 시간을 관리합니다. (ID 순으로 정렬)

<img alt="관리자 시간 등록 및 관리" src="https://github.com/user-attachments/assets/68bf0fc6-ddf7-4661-a849-95b9f368ccaa" />
