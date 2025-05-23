### 요구사항
- [x] gradle 의존성 추가
- [x] 엔티티 매핑
  - [x] Theme나 Time 엔티티 설정을 하세요.
  - [x] 연관관계 매핑
    - [x] Reservation이 Member나 Theme, ReservationTime 객체에 의존하도록

- [x] 내 예약 목록 조회 API 추가

- [x] 예약 대기 요청 구현
  - [x] 대기 클래스 추가
- [x] 예약 대기 취소 기능 구현
   - [x] 예약 대기 보유 사용자와 삭제 요청 사용자가 다르면 예외
- [x] 예약 목록 조회 시 예약 대기 목록 포함
- [x] 중복 예약 대기 검증

- [x] 어드민 예약 대기 관리 기능
  - 승인 / 거절
  - [x] 어드민 예약 대기 목록 조회 페이지
  - [x] 어드민 예약 대기 승인 / 취소 가능
    - [x] 예약 취소 시 자동으로 승인 
      - 승인
        1. 예약 생성
        2. 대기 삭제
    


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

### 고민한 점

- 예약 대기 구현 방법
  - Waiting 테이블을 만들지, 
    - Waiting 테이블 컬럼을 고민
      - 컬럼으로 Reservation을 가진다 (ManyToOne)
        - Reservation의 메서드에 접근 가능
        - Waiting과 Reservation의 생명주기가 다른데 
      - Reservation과 동일한 컬럼 모두 가지기 (id, name, theme, ReservationTime, member) + 예약 순번 컬럼
        - 예약처럼 예약 대기를 관리
  - Reservation에서 순서와 상태 컬럼을 만들지
    - 대기가 아닌 예약 상태일 경우, 순서와 상태 컬럼은 사실상 같은 의미
  - 승인과 거절의 method