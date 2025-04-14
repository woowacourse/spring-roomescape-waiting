# 방탈출 사용자 예약

## 예외 처리

- 예외 처리 후 적절한 응답 반환 (404, 403.. 등)
- 예외 처리
    - 회원
        - 이름 : not null, not blank
        - 이메일 : not null, not blank
        - 비밀번호 : not null, not blank

## 신규 기능

- 회원 entity 추가된다.
    - users 테이블이 추가된다.
- 회원 가입 기능이 추가된다.
- 로그인 기능이 추가된다.
  - 로그인 하기 위해선 회원 가입이 선행되어야 한다.

## Api

- 회원 가입
  - `/signup` 요청하면 `signup.html` 반환
- 로그인
  - `/login` 요청하면 `login.html` 반환
- API 명세
  - 회원 가입
    - request
    ```
    POST /members HTTP/1.1
    ```
    response
    ```
    HTTP/1.1 201
    Content-Type: application/json
    ```

  - 로그인
    - request
    ```
    POST /login HTTP/1.1
    content-type: application/json
    host: localhost:8080

    {
    "password": "password",
    "email": "admin@email.com"
    }
    ```
    response
    ```
    HTTP/1.1 200 OK
    Content-Type: application/json
    Keep-Alive: timeout=60
    Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.cwnHsltFeEtOzMHs2Q5-ItawgvBZ140OyWecppNlLoI; Path=/; HttpOnly
    ```
  - 인증 정보 조회
      - request
    ```
    GET /login/check HTTP/1.1
    cookie: _ga=GA1.1.48222725.1666268105; _ga_QD3BVX7MKT=GS1.1.1687746261.15.1.1687747186.0.0.0; Idea-25a74f9c=3cbc3411-daca-48c1-8201-51bdcdd93164; token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
    host: localhost:8080
    ```
    response
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
