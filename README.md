### 관리자 페이지

- 메인 페이지: `/admin` GET 요청 시 응답한다.
- 예약 관리 페이지: `/admin/reservation` GET 요청 시 응답한다.
- 예약 시간 관리 페이지: `/admin/time` GET 요청 시 응답한다.
- 테마 관리 페이지: `/admin/theme` GET 요청 시 응답한다.
- 예약 대기 관리 페이지: `/admin/waiting` GET 요청 시 응답한다.

### 사용자 페이지
- 메인 페이지: `/` GET 요청 시 인기 테마 페이지를 응답한다.
- 예약 페이지: `/reservation` GET 요청 시 응답한다.
- 내 예약 목록 페이지: `/reservation-mine` GET 요청 시 응답한다.

### 로그인 페이지
- `/login` GET 요청 시 로그인 페이지를 응답한다.
  - 관리자 ID: `nyangin@email.com` / PW: `1234`
  - 사용자 ID: `mia@email.com` / PW: `1234` 

<hr>

### 예약 API
#### 예약 생성
- Request
    ```http
    POST /reservations HTTP/1.1
    content-type: application/json
    cookie: token={token}
    
    {
        "date": "2023-08-05",
        "timeId": 1,
        "themeId": 1,
        "status": "RESERVED" 
    }
    ```
  |파라미터|타입|필수여부|설명|
  |-----|---|------|---|
  |token|String|true|사용자 인증 토큰|

  - `status` 필드는 `"RESERVED"`고정

- Response
    ```http
    HTTP/1.1 201
    Content-Type: application/json
    
    {
        "id": 1,
        "name": "브라운",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "15:00"
        },
        "theme": {
            "id": 1,
            "name": "추리"
        },
        "status": "RESERVED"
    }
    ```
####  예약 목록 조회
- Request
    ```http
    GET /reservations HTTP/1.1
    ```

- Response
    ```http  
    HTTP/1.1 200
    Content-Type: application/json
      
    [
        {
            "id": 1,
            "name": "브라운",
            "date": "2023-08-05",
            "time": {
                "id": 1,
                "startAt": "15:00"
            },
            "theme": {
                "id": 1,
                "name": "추리"
            },
            "status": "RESERVED"
        },
        {
            "id": 2,
            "name": "냥인",
            "date": "2023-08-05",
            "time": {
                "id": 2,
                "startAt": "16:00"
            },
            "theme": {
                "id": 1,
                "name": "추리"
            },
            "status": "RESERVED"
        }
    ]
    ```

#### 예약 삭제
- Request
    ```http
    DELETE /reservations/{id} HTTP/1.1
    ```
  | 파라미터 | 타입   |필수여부| 설명       |
    |-----|------|------|----------|
  | id  | Long |true| 예약 고유 id |
- Response
    ```http
    HTTP/1.1 204
    ```

#### 내 예약 목록 조회
- Request
    ```http
    GET /reservations/mine HTTP/1.1
    ```
- Response
    ```http
    HTTP/1.1 200
    Content-Type: application/json
    [
        {
          "reservationId": 1,
          "theme": "호러",
          "date": "2024-08-05",
          "time": "13:00",
          "status": "예약",
          "rank": 0
        },
        {
          "reservationId": 2,
          "theme": "호러",
          "date": "2024-08-05",
          "time": "13:00",
          "status": "대기",
          "rank": 1
        }
    ]
    ```
  
<br>

### 예약 시간 API
#### 예약 시간 생성
- Request
    ```http
    POST /times HTTP/1.1
    content-type: application/json
    
    {
        "startAt": "15:00"
    }
    ```
- Response
    ```http 
    HTTP/1.1 201
    Content-Type: application/json
    
      {
         "id": 1,
         "startAt": "15:00"
      }
    ```
#### 예약 시간 목록 조회
- Request
    ```http  
    GET /times HTTP/1.1
    ```
- Response
    ```http  
    HTTP/1.1 200
    Content-Type: application/json
      
    [
        {
            "id": 1,
            "startAt": "15:00"
        },
        {
            "id": 2,
            "startAt": "16:00"
        }
    ]
    ```

#### 예약 시간 삭제
- Request
    ```http   
    DELETE /times/{id} HTTP/1.1
    ```
  | 파라미터 | 타입   |필수여부| 설명          |
  |-----|------|------|-------------|
  | id  | Long |true| 예약 시간 고유 id |
- Response
    ```http
    HTTP/1.1 200
    ```

#### 해당 시간대 예약 가능 여부 조회
- Request
    ```http   
    GET /times/available?date={date}&themeId={themeId} HTTP/1.1
    ```
  | 파라미터 | 타입        |필수여부| 설명          |
  |-----|-----------|------|-------------|
  | date  | LocalDate |true| 예약 날짜       |
  | themeId  | Long      |true| 예약 시간 고유 id |

- Response
    ```http  
    HTTP/1.1 200
    Content-Type: application/json
      
    [
        {
            "id": 1,
            "startAt": "15:00",
            "isReserved": true
        },
        {
            "id": 2,
            "startAt": "16:00"
            "isReserved": false
        }
    ]
    ```
<br>

### 테마 API
#### 테마 추가
- Request
    ```http
    POST /themes HTTP/1.1
    content-type: application/json
    
    {
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
    ```
- Response
    ```http  
    HTTP/1.1 201
    Content-Type: application/json
    
      {
         "id": 1,
         "name": "레벨2 탈출",
         "description": "우테코 레벨2를 탈출하는 내용입니다.",
         "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
      }
    ```

#### 테마 목록 조회
- Request
    ```http  
    GET /themes HTTP/1.1
    ```
- Response
    ```http
    HTTP/1.1 200
    Content-Type: application/json
      
    [
        {
            "id": 1,
            "name": "레벨2 탈출",
            "description": "우테코 레벨2를 탈출하는 내용입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        },
        {
            "id": 2,
            "name": "레벨3 탈출",
            "description": "우테코 레벨3을 탈출하는 내용입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        }
    ]
    ```

#### 인기 테마 조회
- Request
    ```http 
    GET /themes/popular HTTP/1.1
    ```
- Response
  ```http  
  HTTP/1.1 200
  Content-Type: application/json
      
  [
     {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
     },
     {
        "id": 2,
        "name": "호러",
        "description": "매우 무섭습니다.",
        "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
     }
  ]
  ```

#### 테마 삭제
- Request
    ```http
    DELETE /themes/{id} HTTP/1.1
    ```
  | 파라미터 | 타입   |필수여부| 설명       |
  |-----|------|------|----------|
  | id  | Long |true| 테마 고유 id |
- Response
    ```http
    HTTP/1.1 204
    ```
<br>

### 예약 대기 API
#### 예약 대기 등록
- Request
    ```http
    POST /waitings HTTP/1.1
    content-type: application/json
    cookie: token={token}
    
    {
        "date": "2023-08-05",
        "timeId": 1,
        "themeId": 1,
        "status": "WAITING"
    }
    ```

  |파라미터|타입|필수여부|설명|
  |-----|---|------|---|
  |token|String|true|사용자 인증 토큰|
  - `status` 필드는 `"WAITING"` 고정
- Response
    ```http
    HTTP/1.1 201
    Content-Type: application/json
    
    {
        "id": 1,
        "name": "냥인",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "15:00"
        },
        "theme": {
            "id": 1,
            "name": "추리"
        },
        "status": "WAITING"
    }
    ```
 
<br>

### 사용자 인증 API
#### 로그인
- Request
  ```http
  POST /login HTTP/1.1
  content-type: application/json
  
  {
      "email": "admin@email.com",
      "password": "password",
  }
  ```
- Response
   ``` http 
   HTTP/1.1 200 OK
   Content-Type: application/json
   Keep-Alive: timeout=60
   Set-Cookie: token={token}; Path=/; HttpOnly
   ```

#### 인증 정보 조회
- Request
  ```http
  GET /login/check HTTP/1.1
  cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token={token}
  ```
  |파라미터|타입|필수여부|설명|
    |-----|---|------|---|
  |token|String|true|사용자 인증 토큰|

- Response
    ```http  
    HTTP/1.1 200 OK
    Connection: keep-alive
    Content-Type: application/json
    Date: Sun, 03 Mar 2024 19:16:56 GMT
    Keep-Alive: timeout=60
    Transfer-Encoding: chunked

    {
        "name": "어드민"
    }
    ```

#### 로그아웃
- Request
  ```http  
  POST /logout HTTP/1.1
  cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token={token}
  ```
- Response
  ```http  
  HTTP/1.1 200 OK
  ```
  |파라미터|타입|필수여부|설명|
  |-----|---|------|---|
  |token|String|true|사용자 인증 토큰|

<br>

### 사용자 API
#### 사용자 목록 조회
- Request
    ```http  
    GET /members HTTP/1.1
    ```
- Response
    ```http
    HTTP/1.1 200
    Content-Type: application/json
      
    [
        {
            "id": 1,
            "name": "냥인",
            "email": "nyangin@email.com",
            "role": "ADMIN"
        },
        {
            "id": 2,
            "name": "미아",
            "email": "mia@email.com",
            "role": "USER"
        }
    ]
    ```

<br>

### 관리자 API
#### 예약 생성
- Request
    ```http
    POST /admin/reservations HTTP/1.1
    content-type: application/json
    cookie: token={token}
    
    {
        "memberId": 1,
        "date": "2023-08-05",
        "timeId": 1,
        "themeId": 1,
        "status": "RESERVED"
    }
    ```

  |파라미터|타입|필수여부|설명|
    |-----|---|------|---|
  |token|String|true|사용자 인증 토큰|
  - `status` 필드는 `"RESERVED"` 고정

- Response
    ```http
    HTTP/1.1 201
    Content-Type: application/json
    
    {
        "id": 1,
        "name": "냥인",
        "date": "2023-08-05",
        "time": {
            "id": 1,
            "startAt": "15:00"
        },
        "theme": {
            "id": 1,
            "name": "추리"
        },
        "status": "RESERVED"
    }
    ```

####  예약 목록 조회
- Request
    ```http
    GET /reservations?themeId={themeId}&memberId={memberId}&dateFrom={dateFrom}&dateTo={dateTo} HTTP/1.1
    ```
  | 파라미터     | 타입        | 필수여부  | 설명        |
    |----------|-----------|-------|-----------|
  | themeId  | Long      | false | 테마 고유 id  |
  | memberId | Long      | false  | 사용자 고유 id |
  | dateFrom | LocalDate | false  | 조회 시작 날짜  |
  | dateTo   | LocalDate | false  | 조회 종료 날짜   |

- Response
    ```http  
    HTTP/1.1 200
    Content-Type: application/json
      
    [
        {
            "id": 1,
            "name": "냥인",
            "date": "2023-08-05",
            "time": {
                "id": 1,
                "startAt": "15:00"
            },
            "theme": {
                "id": 1,
                "name": "추리"
            },
            "status": "RESERVED"
        },
        {
            "id": 2,
            "name": "냥인",
            "date": "2023-08-05",
            "time": {
                "id": 2,
                "startAt": "16:00"
            },
            "theme": {
                "id": 1,
                "name": "추리"
            },
            "status": "RESERVED"
        }
    ]
    ```
####  예약 대기 목록 조회
- Request
    ```http
    GET /admin/waitings HTTP/1.1
    ```

- Response
  ```http  
  HTTP/1.1 200
  Content-Type: application/json

  [
    {
      "id": 1,
      "name": "냥인",
      "date": "2023-08-05",
      "time": {
          "id": 1,
          "startAt": "15:00"
      },
      "theme": {
          "id": 1,
          "name": "추리"
      },
      "status": "WAITING"
    },
    {
      "id": 2,
      "name": "냥인",
      "date": "2023-08-05",
      "time": {
          "id": 2,
          "startAt": "16:00"
      },
      "theme": {
          "id": 1,
          "name": "추리"
      },
      "status": "WAITING"
    }
  ]
  ```
####  예약 대기 승인
- Request
    ```http
    PUT /admin/waitings/{id} HTTP/1.1
    ```
   | 파라미터 | 타입   |필수여부| 설명          |
   |-----|------|------|-------------|
    | id  | Long |true| 예약 대기 고유 id |
- Response
    ```http
    HTTP/1.1 200
    ```

####  예약 대기 거절
- Request
    ```http
    DELETE /admin/waitings/{id} HTTP/1.1
    ```
  | 파라미터 | 타입   |필수여부| 설명          |
  |-----|------|------|-------------|
  | id  | Long |true| 예약 대기 고유 id |
- Response
    ```http
    HTTP/1.1 204
    ```
