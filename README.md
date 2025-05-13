# 사용 예시

## 접근 방법

애플리케이션 실행 후 아래의 링크로 접속할 수 있습니다.

- 관리자 페이지: [localhost:8080/admin](http://localhost:8080/admin)
    ```plaintext
      테스트 관리자 계정
      email: admin@email.com
      password: password
    ```

- 사용자 페이지: [localhost:8080/](http://localhost:8080/)
    ```plaintext
  테스트 사용자 계정
  email: brown@email.com
  password: brown
   ```

## 명세

### 사용자

| 기능       | Method | URL            | 파라미터 / Path Variable | Body                        |
|----------|--------|----------------|----------------------|-----------------------------|
| 로그인      | POST   | `/login`       | -                    | `email`, `password`         |
| 로그인 확인   | GET    | `/login/check` | -                    | -                           |
| 로그아웃     | POST   | `/logout`      | -                    | -                           |
| 회원 전체 조회 | GET    | `/members`     | -                    | -                           |
| 회원 가입    | POST   | `/members`     | -                    | `email`, `password`, `name` |

### 예약

| 기능          | Method | URL                             | 파라미터 / Path Variable                        | Body                                    |
|-------------|--------|---------------------------------|---------------------------------------------|-----------------------------------------|
| 예약 전체 조회    | GET    | `/reservations`                 | -                                           | -                                       |
| 예약 상세 조회    | GET    | `/reservations/{reservationId}` | `reservationId`                             | -                                       |
| 예약 생성       | POST   | `/reservations`                 | -                                           | `themeId`, `timeId`, `date`             |
| 예약 삭제       | DELETE | `/reservations/{reservationId}` | `reservationId`                             | -                                       |
| 조건부 예약 조회   | GET    | `/admin/reservations`           | `themeId`, `memberId`, `dateFrom`, `dateTo` | -                                       |
| 예약 생성 (관리자) | POST   | `/admin/reservations`           | -                                           | `themeId`, `memberId`, `date`, `timeId` |

### 예약 시간

| 기능          | Method | URL                  | 파라미터 / Path Variable | Body                   |
|-------------|--------|----------------------|----------------------|------------------------|
| 예약 시간 전체 조회 | GET    | `/times`             | -                    | -                      |
| 예약 가능 시간 조회 | GET    | `/times/reservation` | `date`, `themeId`    | -                      |
| 특정 예약 시간 조회 | GET    | `/times/{timeId}`    | `timeId`             | -                      |
| 예약 시간 등록    | POST   | `/times`             | -                    | `startTime`, `endTime` |
| 예약 시간 삭제    | DELETE | `/times/{timeId}`    | `timeId`             | -                      |

### 테마

| 기능       | Method | URL                 | 파라미터 / Path Variable | Body                  |
|----------|--------|---------------------|----------------------|-----------------------|
| 테마 전체 조회 | GET    | `/themes`           | -                    | -                     |
| 인기 테마 조회 | GET    | `/themes/ranks`     | -                    | -                     |
| 테마 등록    | POST   | `/themes`           | -                    | `name`, `description` |
| 테마 삭제    | DELETE | `/themes/{themeId}` | `themeId`            | -                     |

# 구현할 기능 목록

0. 이전 코드 리팩터링
    - [x] member 테이블의 제약조건 수정

1. 데이터베이스 접근 방식 교체
    - [x] 엔티티 매핑
    - [x] 연관관계 매핑
    - [ ] 레포지토리 교체

