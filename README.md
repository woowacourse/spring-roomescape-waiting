## API 명세서

### 어드민 메인 페이지
- http method: GET
- uri: /admin
- file path: templates/admin/index.html

### 어드민 예약 페이지 접근
- http method: GET
- uri: /admin/reservation
- file path: templates/admin/reservation-new.html

### 어드민 시간 페이지 접근
- http method: GET
- uri: /admin/time
- file path: templates/admin/time.html

### 어드민 테마 페이지 접근
- http method: GET
- uri: /admin/theme
- file path: templates/admin/theme.html

### 사용자 예약 페이지 접근
- http method: GET
- uri: /reservation
- file path: templates/reservation.html

### 사용자 기본 페이지 접근
- http method: GET
- uri: /
- file path: templates/index.html

### 사용자 로그인 페이지 접근
- http method: GET
- uri: /login
- file path: templates/login.html

### 내 예약 페이지 접근
- http method: GET
- uri: /member/reservation
- file path: templates/reservation-mine.html

### 권한 없는 페이지 접근 시도
- 회원이 아닌 사용자: 로그인 페이지, 회원가입 페이지 접근 가능
- 일반 회원: 어드민 권한 페이지 외 접근 가능
- 어드민: 모든 페이지 접근 가능
- 사용자는 권한 없음
  ```
    HTTP/1.1 403

    {
    "message": "권한이 없는 접근입니다."
    }
  ```

### 모든 예약 조회
- http method: GET
- uri: /reservations
  - response
    ```
    HTTP/1.1 200 
    Content-Type: application/json
  
    [
        {
            "id": 1,
            "date": "2023-01-01",
            "time": {
              "id": 1.
              "startAt": "10:00"
            },
            "theme": {
              "id": 1,
              "name": "레벨2 탈출",
              "description": "우테코 레벨2를 탈출하는 내용입니다.",
              "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
            },
            "member": {
              "id": 1,
              "name": "lini",
              "email": "lini@email.com",
              "role": "GUEST"
            }
        }
    ]
    ```

### 조건별 예약 조회 - 어드민
- http method: GET
- uri: /reservations/search?memberId=1&themeId=1&dateFrom=2024-11-11&dateTo=2024-11-12
  - memberId: 필수 아님
  - themeId: 필수 아님
  - dateFrom: 필수 아님
  - dateTo: 필수 아님
- response
  ```
  HTTP/1.1 200 
  Content-Type: application/json
  
  [
      {
          "id": 1,
          "date": "2023-01-01",
          "time": {
            "id": 1.
            "startAt": "10:00"
          },
          "theme": {
            "id": 1,
            "name": "레벨2 탈출",
            "description": "우테코 레벨2를 탈출하는 내용입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
          },
          "member": {
            "id": 1,
            "name": "lini",
            "email": "lini@email.com",
            "role": "GUEST"
          }
      }
  ]
  ```

### 예약 추가 - 어드민
- http method: POST
- uri: /reservations
- request
  ```
  POST /admin/reservations HTTP/1.1
  content-type: application/json
  
  {
      "date": "2023-08-05",
      "timeId": 1,
      "themeId": 1,
      "memberId": 1,
  }
  ```
  
### 예약 및 예약 대기 추가 - 사용자
- http method: POST
- uri: /reservations
- description: 예약이 이미 존재한다면, 자동으로 예약 대기로 추가된다.
- request
  ```
  POST /reservations HTTP/1.1
  content-type: application/json
  cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI
  host: localhost:8080

  {
    "date": "2024-03-01",
    "themeId": 1,
    "timeId": 1
  }
  ```
- response
  - 추가 성공
    ```
    HTTP/1.1 201 
    Location: /reservations/1
    Content-Type: application/json
  
    {
        "id": 1,
        "date": "2023-08-05",
        "status": "예약",
        "time" : {
            "id": 1.
            "startAt": "10:00"
        },
        "theme": {
            "id": 1,
            "name": "레벨2 탈출",
            "description": "우테코 레벨2를 탈출하는 내용입니다.",
            "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
        },
        "member": {
          "id": 1,
          "name": "lini",
          "email": "lini@email.com"
        },
        "status": "예약"
    }
    ```
  - 추가 실패: 중복 예약 불가능 오류
    ```
    HTTP/1.1 400
  
    {
      "message": "선택하신 테마와 일정은 이미 예약이 존재합니다."
    }
    ```

  - 추가 실패 : 일정 오류
    ```
    HTTP/1.1 400 
    Content-Type: application/json
  
    {
      "message": "현재보다 이전으로 일정을 설정할 수 없습니다."
    }
    ```
  - 추가 실패 : 날짜 오류
    ```
    HTTP/1.1 400
  
    {
      "message": "올바르지 않은 날짜입니다."
    }
    ```
  - 추가 실패 : 존재하지 않는 시간 오류
    ```
    HTTP/1.1 400
    Content-Type: application/json

    {
    "message": "더이상 존재하지 않는 시간입니다."
    }
    ```
  - 추가 실패 : 존재하지 않는 테마 오류
    ```
    HTTP/1.1 400

    {
    "message": "더이상 존재하지 않는 테마입니다."
    }
    ```
  - 추가 실패 : 이미 예약 혹은 예약 대기가 존재 오류
    ```
    HTTP/1.1 400

    {
    "message": "이미 예약(대기) 상태입니다."
    }
    ```

### 예약 삭제
- http method: DELETE
- uri: /reservations/{id}?waiting={waiting}
  - query string
    - waiting: 대기 여부
      - default: false
      - true: 대기에 대한 요청
      - false: 예약에 대한 요청
  - path variable
    - id: 예약 정보 식별자
- response
  - 존재하는 id로 삭제 요청
    ```
    HTTP/1.1 204
    ```
  - 삭제 실패: 어드민 권한이 아닌 일반 사용자가 본인 예약 외의 것을 삭제 시도
    ```
    HTTP/1.1 401

    {
    "message": "예약을 삭제할 권한이 없습니다."
    }
    ```
  - 삭제 실패: 일반 사용자가 일정이 지난 예약/예약 대기을 삭제 시도
    ```
    HTTP/1.1 401

    {
    "message": "이미 지난 예약(대기)은 삭제할 수 없습니다."
    }
    ```
  - 삭제 실패: 일반 사용자가 이미 예약으로 전환된 예약 대기를 시도한다.
    - waiting=true 
    ```
    HTTP/1.1 401

    {
    "message": "이미 예약으로 전환되었습니다."
    }
    ```
  - 삭제 실패: 예약 대기에 대해 예약 삭제를 시도한다.
    - waiting=true
    ```
    HTTP/1.1 401

    {
    "message": "예약 대기 상태입니다. 예약 대기 삭제로 요청해주세요."
    }
    ```
    
### 시간 추가
- http method: POST
- uri: /times
- request
  ```
  POST /times HTTP/1.1
  Content-Type: application/json
   
  {
      "startAt": "10:00"
  }
  ```
- response
  - 추가 성공
    ```
    HTTP/1.1 201 
    Location: /times/1
    Content-Type: application/json
  
    {
        "id": 1,
        "startAt": "10:00"
    }
    ```
  - 추가 실패 : 시간 오류
    ```
    HTTP/1.1 400
  
    {
      "message": "올바르지 않은 시간입니다."
    }
    ```
  - 추가 실패: 중복 시간 오류
    ```
    HTTP/1.1 400
  
    {
      "message": "이미 같은 시간이 존재합니다."
    }
    ```

### 시간 조회
- http method: GET
- uri: /times
- response
   ```
  HTTP/1.1 200 
  Content-Type: application/json
  [
   {
        "id": 1,
        "startAt": "10:00"
    }
  ]
  ```

### 예약 가능한 시간 조회
- http method: GET
- uri: /times/available?date=2023-01-01&themeId=1
- request parameter
  - date: 날짜: 필수
  - themeId: 테마 식별자: 필수
- response
   ```
  HTTP/1.1 200 
  Content-Type: application/json
  [
   {
        "id": 1,
        "startAt": "10:00"
        "isBooked": true
    },
   {
        "id": 2,
        "startAt": "11:00"
        "isBooked": false
    }
  ]
  ```

### 시간 삭제
- http method: DELETE
- uri: /times/{id}
  - path variable
    - id: 시간 정보 식별자
- response
  - 성공: 존재하는 id로 삭제 요청
    ```
    HTTP/1.1 204
    ```
  - 삭제 실패: 이미 예약이 존재하는 시간 삭제 시도 오류
    ```
    HTTP/1.1 400

    {
      "message": "해당 시간에 예약이 존재해서 삭제할 수 없습니다."
    }
    ```

### 테마 추가
- http method: POST
- uri: /themes
- request
  ```
  POST /themes HTTP/1.1
  content-type: application/json
  
  {
      "name": "레벨2 탈출",
      "description": "우테코 레벨2를 탈출하는 내용입니다.",
      "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
  ```
- response
  - 추가 성공
    ```
    HTTP/1.1 201
    Location: /themes/1
    Content-Type: application/json

    {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    }
    ```
  - 추가 실패 : 이름 길이 오류
    ```
    HTTP/1.1 400 
    Content-Type: application/json
  
    {
      "message": "이름은 1자 이상, 20자 이하여야 합니다."
    }  
    ```
  - 추가 실패: 중복 이름 오류
    ```
    HTTP/1.1 400

    {
      "message": "이미 존재하는 테마 이름입니다."
    }
    ```
  - 추가 실패: 썸네일 형식 오류
    ```
    HTTP/1.1 400

    {
      "message": "올바르지 않은 썸네일 형식입니다."
    }
    ```
  - 추가 실패: 설명 길이 오류
    ```
    HTTP/1.1 400

    {
      "message": "설명은 100자를 초과할 수 없습니다."
    }
    ```

### 테마 조회
- http method: GET
- uri: /themes
- response
   ```
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
  
### 인기 테마 조회
- http method: GET
- uri: /themes/popular
- description: 최근 일주일 기준 예약이 많은 테마 10개 조회 (today: 4/8 -> 조회 기간: 4/1~4/7)
- response
   ```
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

### 테마 삭제
- http method: DELETE
- uri: /themes/{id}
  - path variable
    - id: 테마 정보 식별자
- response
  - 성공: 존재하는 id로 삭제 요청
    ```
    HTTP/1.1 204
    ```
  - 삭제 실패: 이미 예약이 존재하는 테마 삭제 시도 오류
    ```
    HTTP/1.1 400

    {
      "message": "해당 테마로 예약이 존재해서 삭제할 수 없습니다."
    }
    ```
    
### 사용자 회원가입
- http method: POST
- uri: /members
- request
  - 회원가입 성공
    ```
    POST /members HTTP/1.1
    content-type: application/json
    host: localhost:8080
    
    {
       "name": "사용자이름",
       "email": "admin@email.com",
       "password": "lini123"
    } 
    ```
  - 회원가입 실패 - 중복된 이메일 오류
    ```
    HTTP/1.1 400

    {
      "message": "이미 가입된 이메일입니다."
    }
    ``` 
  - 추가 실패 : 이름 길이 오류
    ```
    HTTP/1.1 400 
    Content-Type: application/json
  
    {
      "message": "이름은 1자 이상, 20자 이하여야 합니다."
    }  
    ```
  - 추가 실패 : 이메일 형식 오류
    ```
    HTTP/1.1 400 
    Content-Type: application/json
  
    {
    "message": "유효하지 않은 이메일입니다."
    }  
    ```
  - 추가 실패 : 비밀번호 약식 오류
    ```
    HTTP/1.1 400 
    Content-Type: application/json
  
    {
    "message": "비밀번호는 6자 이상 12자 이하여야 합니다."
    }  
    ```
- response
  ```
  HTTP/1.1 201 OK
  Content-Type: application/json'
  location: /members/1
  ```
### 사용자 로그인
- http method: POST
- uri: /login
- request
  - 로그인 성공
    ```
    POST /login HTTP/1.1
    content-type: application/json
    host: localhost:8080
    
    {
       "password": "비밀번호",
       "email": "admin@email.com",
    } 
    ```
  - 로그인 실패: 비밀번호 미입력 오류
    ```
    HTTP/1.1 400

    {
      "message": "비밀번호를 입력해주세요."
    }
    ```  
  - 로그인 실패: 이메일 미입력 오류
    ```
    HTTP/1.1 400

    {
      "message": "이메일을 입력해주세요."
    }
    ```
  - 로그인 실패: 이메일 또는 비밀번호 오류
    ```
    HTTP/1.1 401

    {
      "message": "이메일 또는 비밀번호가 잘못되었습니다."
    }
    ```  
- response
  ```
  HTTP/1.1 200 OK
  Content-Type: application/json
  Keep-Alive: timeout=60
  Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
  ```

### 사용자 로그아웃
- http method: POST
- uri: /logout
- response
  - 로그아웃 성공
    ```
    POST /login HTTP/1.1
    content-type: application/json
    Keep-Alive: timeout=60
    Set-Cookie: 

    ```

### 인증 정보 조회
- http method: GET
- uri: /login/check
- request
  ```
  GET /login/check HTTP/1.1
  cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
  host: localhost:8080
  ```
- response
  ```
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

### 사용자 조회
- http method: GET
- uri: /members
- response
  ```
  HTTP/1.1 200 
  Content-Type: application/json
  [
    {
      "id": 1,
      "name": "lini",
      "email": "lini@email.com",
      "role": "GUEST"
    }
  ]
  ```
  - 사용자는 권한 없음
    ```
      HTTP/1.1 403

      {
      "message": "권한이 없는 접근입니다."
      }
    ```

### 사용자 예약 조회
- http method: GET
- uri: /reservations-mine
- request
```
GET /members/reservations HTTP/1.1
cookie: token={token}
host: localhost:8080
```

- response
```
HTTP/1.1 200
Content-Type: application/json

[
  {
    "reservationId": 1,
    "theme": "테마1",
    "date": "2024-03-01",
    "time": "10:00",
    "status": "예약"
  },
  {
    "reservationId": 2,
    "theme": "테마2",
    "date": "2024-03-01",
    "time": "12:00",
    "status": "예약"
  },
  {
    "reservationId": 3,
    "theme": "테마3",
    "date": "2024-03-01",
    "time": "14:00",
    "status": "예약"
  }
]
```
