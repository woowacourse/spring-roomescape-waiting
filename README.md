## 방탈출 사용자 예약

### 요구사항

- **1 단계**
  - 발생할 수 있는 예외 상황에 대한 처리를 하여, 사용자에게 적절한 응답을 합니다.
      - 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
    - 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
    - 특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때 등등
  - 아래와 같은 서비스 정책을 반영합니다.
    - 지나간 날짜와 시간에 대한 예약 생성은 불가능하다.
    - 중복 예약은 불가능하다.
      - ex. 이미 4월 1일 10시에 예약이 되어있다면, 4월 1일 10시에 대한 예약을 생성할 수 없다.
  - 어드민의 시간 관리 페이지, 방탈출 예약 페이지에서 모든 기능이 정상적으로 동작하는지 확인합니다.

- **2 단계**
  - 관리자가 테마를 관리할 수 있도록 기능을 추가합니다.
  - 관리자가 방탈출 예약 시, 테마 정보를 포함할 수 있도록 기능을 변경합니다.

- **3 단계**
  - 관리자가 아닌 사용자가 예약 가능한 시간을 조회하고, 예약할 수 있도록 기능을 추가/변경 합니다.
  - 인기 테마 조회 기능을 추가합니다.

- **4 단계**
  - 사용자 도메인 추가
  - 로그인 기능 추가 구현
  - 로그인 후 Cookie를 이용하여 사용자의 정보를 조회하는 API를 추가 구현

### 세부 요구 사항

#### 관리자 기능

1. 사용자 요청에 대한 예외 처리를 적용한다.

- [x] 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
  - [x] 각 입력에 대하여 빈 입력값을 시도하였을 때
  - [x] 예약자명 예외 처리 
    - [x] 특수문자가 포함된 이름 처리 
  - [x] 날짜 예외 처리
    - [x] 과거 날짜 입력 예외 처리 

2. 관리자 요청에 대한 예외 처리를 적용한다.

- [x] 시작 시간에 유효하지 않은 값이 입력되었을 때 예외로 처리한다.
    - [x] 비어있는 입력값 처리
    - [x] 운영 시간 외의 예약 시간 입력 처리
    - [x] 이미 존재하는 시간에 대한 입력 처리
- [x] 존재하는 예약에 해당하는 시간 삭제 요청 예외 처리

3. 테마를 관리하는 기능을 구현한다.

- [x] 테마 관리 페이지를 반환한다.
- [x] 방탈출 테마를 관리하는 기능을 구현한다.
  - [x] 테마를 조회한다.
  - [x] 테마를 생성한다.
  - [x] 테마를 삭제한다.

#### 사용자 기능
0. 사용자 페이지
- [x] 사용자 로그인 페이지 요청시 반환 기능
  - [x] 로그인 후 우측 상단의 Login 버튼이 사용자 이름으로 변경
  - [x] 로그아웃 시 다시 Login 버튼이 노출
- [x] 사용자 예약 페이지 요청시 반환 기능

1. 예약 조회
- [x] 모든 예약 시간을 보여준다.
  - [x] 예약이 이미 존재하는 시간을 구별해서 보여줘야한다.

2. 시간 조회
- [x] 예약 가능한 시간을 조회한다.

3. 테마 목록 조회
- [x] 예약 많은 순으로 테마 순위를 오름차순으로 보여준다.

4. 사용자 로그인
- [x] emali과 비밀번호를 입력하여 로그인한다.
  - [x] 토큰 발행 방식으로 로그인 정보 인증
  - [x] 인증 정보 클라이언트 Cookie에 저장


## 4단계 추가 API 명세서

## Login API 명세

### Login 정보 요청

#### Request
```http request

POST /login HTTP/1.1

content-type: application/json
host: localhost:8080

{
    "password": "password",
    "email": "admin@email.com"
}

```

### Response
```http request
HTTP/1.1 200 OK
Content-Type: application/json
Keep-Alive: timeout=60
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
```

### Login 인증 정보 조회

#### Request
```http request

GET /login/check HTTP/1.1
cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080


```

### Response
```http request
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


