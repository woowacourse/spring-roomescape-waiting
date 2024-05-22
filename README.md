#  방탈출 예약 대기

## 기능 구현 목록

- [ ] 예약 기능
  - [ ] 

- [ ] 예약 취소 기능

- [x] 내 예약 목록을 조회하는 API
  - [x] 예약의 상태를 조회할 수 있다 ex) `예약`
  - [x] 현재 날짜를 포함하여 이후의 예약을 조회한다. 
  - [x] 내 예약은 최신순으로 조회한다.
  - [ ] 내 예약 대기 목록을 포함한다
    - [ ] 내 예약 대기가 몇 번째인지 파악한다

- [ ] 예약 대기 요청 기능
  - [ ] 중복 예약은 불가하다
  - [ ] 중복 예약 대기는 불가하다
  - [ ] 예약에 성공한 유저는 예약 대기를 신청할 수 없다
- [ ] 예약 대기 취소 기능
  - [ ] 예약 유저 취소 시, 1번째 대기 유저가 예약 상태가 된다



## API
-  내 예약 목록 조회 : GET `/reservations-mine`
