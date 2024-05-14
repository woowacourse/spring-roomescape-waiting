## 로그인 계정

- 로그인 가능한 계정
    - 관리자 계정 - id : admin@abc.com / pw : 1234 / name : 관리자
    - 일반 유저 계정 - id : bri@abc.com / pw : 1234 / name : 브리
    - 일반 유저 계정 - id : brown@abc.com / pw : 1234 / name : 브라운
    - 일반 유저 계정 - id : duck@abc.com / pw : 1234 / name : 오리
- 추후 패스워드 확인 기능 추가 예정

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
