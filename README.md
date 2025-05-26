# 방탈출 예약 대기

## 1, 2단계 - JPA 전환

### 엔티티 매핑
- [x] 다른 클래스를 의존하지 않는 클래스 엔티티 설정


### 연관관계 매핑
- [x] 다른 클래스에 의존하는 클래스는 연관관계 매핑 추가

## 3단계 - 예약 대기 기능
- [x] 예약 대기 생성
  - [x] 같은 날짜, 같은 시간, 같은 테마에 예약이 존재하는 경우, 예약 대기를 추가할 수 있다.
- [x] 내 예약 및 예약 대기 조회
  - [x] 내 예약 목록의 예약 대기 상태에 몇 번째 대기인지도 함께 표시한다.
- [x] 내 예약 대기 취소

## 4단계 - 예약 대기 관리
- [x] 어드민 예약 대기 관리 페이지 조회
- [x] 자동 예약 대기 조정
 - [x] 예약 삭제 시 첫 예약 대기가 자동으로 예약이 되도록 한다.
- [ ] (선택사항) 어드민 예약 대기 취소
  - [ ] 예약 대기 관리 페이지에서 승인/거절을 한다.

## API 명세서

### 인증 (Authentication)

| Method | URL | Description | Request Body | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| POST | /login | 로그인 | `{ "email": "string", "password": "string" }` | 쿠키에 토큰 저장 | No |
| GET | /login/check | 로그인 상태 확인 | - | `{ "name": "string" }` | Yes |
| POST | /logout | 로그아웃 | - | 쿠키에서 토큰 삭제 | Yes |

### 회원 (Members)

| Method | URL | Description | Request Body | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| GET | /members | 모든 회원 조회 | - | `[{ "id": number, "name": "string", "email": "string", "role": "string" }]` | Admin |
| POST | /members | 회원 가입 | `{ "name": "string", "email": "string", "password": "string" }` | `{ "id": number, "name": "string", "email": "string" }` | No |

### 테마 (Themes)

| Method | URL | Description | Request Body | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| GET | /themes | 모든 테마 조회 | - | `[{ "id": number, "name": "string", "description": "string", "price": number }]` | No |
| GET | /themes/popular | 인기 테마 조회 | - | `[{ "id": number, "name": "string", "description": "string", "price": number }]` | No |
| POST | /themes | 테마 생성 | `{ "name": "string", "description": "string", "price": number }` | `{ "id": number, "name": "string", "description": "string", "price": number }` | Admin |
| DELETE | /themes/{id} | 테마 삭제 | - | - | Admin |

### 예약 시간 (Reservation Times)

| Method | URL | Description | Request Body/Params | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| GET | /times | 모든 예약 시간 조회 | - | `[{ "id": number, "time": "string" }]` | Admin |
| GET | /times/available | 특정 날짜와 테마에 대한 가능한 예약 시간 조회 | `date=YYYY-MM-DD&themeId=number` | `[{ "id": number, "time": "string", "available": boolean }]` | No |
| POST | /times | 예약 시간 생성 | `{ "time": "string" }` | `{ "id": number, "time": "string" }` | Admin |
| DELETE | /times/{id} | 예약 시간 삭제 | - | - | Admin |

### 예약 (Reservations)

| Method | URL | Description | Request Body/Params | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| GET | /reservations | 모든 예약 조회 | - | `[{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }]` | Admin |
| GET | /reservations/me | 내 예약 조회 | - | `[{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "status": "string", "waitingOrder": number }]` | Yes |
| POST | /reservations | 예약 생성 | `{ "date": "YYYY-MM-DD", "timeId": number, "themeId": number }` | `{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }` | Yes |

### 관리자 예약 (Admin Reservations)

| Method | URL | Description | Request Body/Params | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| GET | /admin/reservations | 예약된 모든 예약 조회 | - | `[{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }]` | Admin |
| POST | /admin/reservations | 관리자가 예약 생성 | `{ "date": "YYYY-MM-DD", "timeId": number, "themeId": number, "memberId": number }` | `{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }` | Admin |
| GET | /admin/reservations/search | 기간별 예약 검색 | `themeId=number&memberId=number&dateFrom=YYYY-MM-DD&dateTo=YYYY-MM-DD` | `[{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }]` | Admin |
| DELETE | /admin/reservations/{id} | 예약 삭제 | - | - | Admin |

### 예약 대기 (Reservation Waitings)

| Method | URL | Description | Request Body | Response | Auth Required |
|--------|-----|-------------|--------------|----------|---------------|
| GET | /reservations/waiting | 모든 예약 대기 조회 | - | `[{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }]` | Admin |
| POST | /reservations/waiting | 예약 대기 생성 | `{ "date": "YYYY-MM-DD", "timeId": number, "themeId": number }` | `{ "id": number, "date": "YYYY-MM-DD", "time": "string", "theme": "string", "member": "string", "status": "string" }` | Yes |
| DELETE | /reservations/waiting/{id} | 예약 대기 취소 | - | - | Yes |
