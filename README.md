## 로그인 계정

- 로그인 가능한 계정
  - 관리자 계정 - id : admin@abc.com / pw : 1234 / name : 관리자
  - 일반 유저 계정 - id : bri@abc.com / pw : 1234 / name : 브리
  - 일반 유저 계정 - id : brown@abc.com / pw : 1234 / name : 브라운
  - 일반 유저 계정 - id : duck@abc.com / pw : 1234 / name : 오리

## 1단계 요구사항

- 엔티티 매핑
  - [x] Theme
  - [x] Time
  - [x] Member
- 연관관계 매핑
  - [x] Reservation
- DAO -> CrudRepository (JpaRepository) 로 전환
  - [x] Theme
  - [x] Time
  - [x] Member
  - [x] Reservation
- 추가적인 리팩터링
  - [x] 멤버에 패스워드 추가

## 2단계 요구사항

- [x] 자신의 예약 목록을 조회할 수 있다.
- [x] 자신의 예약 목록 화면을 응답할 수 있다.
  - [x] "/reservation-mine" URL 요청 시 `reservation-mine.html` 가 응답된다.
  - [x] js, html 파일 추가

## 3단계 요구사항

- 예약 대기 기능
  - [ ] 같은 날짜와 시간과 테마에 대해 이미 예약이 존재하면 예약 대기를 할 수 있다.
  - [ ] 사용자는 같은 날짜와 시간과 테마에 중복으로 예약 및 예약 대기 할 수 없다.

- 자신의 예약 대기 목록 조회 기능
  - [ ] 자신의 예약 및 예약 대기 목록을 조회할 수 있다.
  - [ ] 자신의 예약 대기가 몇번째 대기인지 알 수 있다.

- 자신의 예약 대기 취소 기능
  - [ ] 자신의 예약 대기를 취소할 수 있다.

## 추가 기능

- [ ] 사용자가 예약을 취소할 수 있다.
