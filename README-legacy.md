## 🚀 1단계 - 예외 처리와 응답

### 요구사항

- [x] 시간 관리, 예약 관리 API가 적절한 응답을 하도록 생성에 대한 API를 HttpStatus 201로 변경

- 예외 처리
    - [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
        - [x] HH:mm 형식이 아닐 때 - LocalTime에서 걸림
        - [x] 방탈출 운영 시간이 아닐 때 (12:00 ~ 23:00 -> 진행 시간 1시간이라 가정하고 22시까지 가능) - domain
    - [x] 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
        - [x] 예약자명 글자 수 제한 (2 ~ 5글자 사이) - domain
        - [x] 지난 날짜에 대한 예약 시 예외 - domain
        - [x] 당일 날짜이면서 지난 시간이나 현재 시간에 대한 예약 시 예외. - domain
            - [x] 시작 시간 10분 전까지 가능
        - [x] 특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때 - Service
    - [x] 날짜와 시간이 중복되는 예약은 불가능하다. - Service
        - ex. 이미 4월 1일 10시에 예약이 되어있다면, 4월 1일 10시에 대한 예약을 생성할 수 없다.
- [x] 테마 관리 기능 추가
    - [x] 테마 전체 조회
    - [x] 테마 생성
    - [x] 테마 삭제
        - [x] 특정 테마에 대한 예약이 존재하는데, 그 테마를 삭제하려 할 때 예외 처리- Service
    - [x] 인기 테마 조회
    - [x] 특정 테마의 특정 날짜 예약 가능 시간 조회

- 인증
  - [x] Member 추가
  - 로그인을 위한 JWT, Cookie 사용
    - JWT 검증
      - [x] 유효한 토큰인지 (만료되지 않았는지)
      - [x] null인지
    - Cookie 검증 
      - [x] null인지
  - [x] 로그아웃
  - [x] 회원 가입
  - ArgumentResolver 도입
    - [x] LoginMember 어노테이션 추가
    - [x] LoginMemberArgumentResolver 추가
    - [x] Config에 ArgumentResolver 추가
  - 예약 생성 기능 변경
    - [x] name을 원래 요청에서 제거
    - [x] name을 jwt 토큰으로 가져오도록 수정
  - 어드민 권한 검사
    - [x] interceptor 사용
    - [x] MemberRole 추가
- 예약 조회 필터링
  - [x] 필터링 js 수정
  - [x] reservationRepository 쿼리 작성
  - [x] 전체 사용자 조회 추가

---

### API 명세

[GET] 테마 조회 `/themes`
  
  
  **응답 예시**

```json
[
  {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
]
```

**id:** 식별자

**name:** 이름

**description:** 설명

**thumbnail:** 썸네일 이미지 경로

---
[POST] 테마 삭제 `/themes`

  **요청 예시**

```json
{
  "name": "레벨2 탈출",
  "description": "우테코 레벨2를 탈출하는 내용입니다.",
  "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

**name:** 이름

**description:** 설명

**thumbnail:** 썸네일 이미지 경로

**응답 예시**

```json
{
  "id": 1,
  "name": "레벨2 탈출",
  "description": "우테코 레벨2를 탈출하는 내용입니다.",
  "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
}
```

**id:** 식별자

**name:** 이름

**description:** 설명

**thumbnail:** 썸네일 이미지 경로

---
[DELETE] 테마 삭제 `/themes/{id}`
  
  
---
[GET] 예약 가능 시간 조회 `/times/available`

- Query Parameter
  - themeId: 테마의 식별자
  - date: 에약 날짜

**응답 예시**

```json
[
  {
    "timeId": 1,
    "startAt": "12:00:00",
    "booked": true
  },
  {
    "timeId": 2,
    "startAt": "13:00:00",
    "booked": false
  }
]
```

**timeId:** 예약 시간의 식별자

**startAt:** 예약 시작 시간

**booked:** 예약 여부

---
[GET] 일주일 간, 인기 테마 10개 내림차순 조회 `/themes/rank`

**응답 예시**

```json
[
  {
    "id": 7,
    "name": "The Lost City",
    "description": "Discover the secrets of the lost city and find your way out.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
  {
    "id": 11,
    "name": "The Bank Heist",
    "description": "Plan and execute the perfect bank heist to escape.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
  {
    "id": 20,
    "name": "The Wild West",
    "description": "Escape the wild west town before the showdown.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  },
  ...
]
```
---

[POST] 예약 생성 `/reservations`

**요청 예시**
```json
Content-Type: application/json
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJhZG1pbkBlbWFpbC5jb20iLCJleHAiOjE3NDY2Mzc2MjF9.WxJxnaS-fmy-VmNrIMERWlPf0TOboT5WY2nATnybbVw

{
  "date": "2025-05-11",
  "timeId": 1,
  "themeId": 1
}
```

**date**: 예약 일자

**timeId**: 예약 시간 식별자

**themeId**: 예약 테마 식별자

**응답 예시**
```json
{
  "id": 21,
  "name": "admin",
  "date": "2025-05-11",
  "time": {
    "id": 1,
    "startAt": "12:00:00"
  },
  "theme": {
    "id": 1,
    "name": "The Haunted Mansion",
    "description": "Solve the mysteries of the haunted mansion to escape.",
    "thumbnail": "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
  }
}
```

**id**: 예약 식별자

**name**: 예약자명

**date**: 예약 날짜

**time.id**: 예약 시간 식별자

**time.startAt**: 예약 시작 시간

**theme.id**: 예약 테마 식별자

**theme.name**: 예약 테마명

**theme.description**: 예약 테마 설명

**theme.thumbnail**: 예약 테마 썸네일

---

[GET] 모든 사용자 찾기 `/members`

**응답 형식**
```json
[
  {
    "id": 1,
    "name": "admin",
    "email": "admin@email.com"
  },
  {
    "id": 2,
    "name": "Alice",
    "email": "alice@example.com"
  },
...
]
```

**id**: 사용자 식별자

**email**: 사용자 이메일

**name**: 사용자 이름

---

[POST] 사용자 회원 가입 `/members`

**요청 형식**
```json
{
  "email": "abc@email.com",
  "password": "abc",
  "name": "abc"
}
```

**email**: 사용자 이메일

**password**: 로그인 패스워드

**name**: 사용자 이름

**응답 형식**
```json
{
  "id": 12,
  "name": "abc",
  "email": "abc@email.com"
}
```
**id**: 사용자 식별자

**email**: 사용자 이메일

**name**: 사용자 이름

[POST] 사용자 로그인 `/login`

**요청 예시**

```json
{
  "email": "abc@email.com",
  "password": "abc"
}
```
**email**: 사용자 이메일

**password**: 로그인 패스워드

**응답 예시**

```json
HTTP/1.1 200
Set-Cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiZXhwIjoxNzQ2ODE0NzIxfQ.yV-Ikj-XDo1c8q66kHNtf6lCKEx-y1KfImvm3kjovDE; Path=/; Max-Age=86400; Expires=Sat, 10 May 2025 17:18:41 GMT; Secure; HttpOnly; SameSite=Strict
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 09 May 2025 17:18:41 GMT

{
"id": 1,
"name": "admin",
"email": "admin@email.com"
}
```

**id:** 사용자 식별자

**name:** 사용자 이름

**email:** 사용자 이메일

---

[GET] 로그인 체크 `/login/check`

**응답 형식**
```json
{
  "name": "admin"
}
```
**name**: 사용자 이름

---
