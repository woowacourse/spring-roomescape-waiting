## 기능 요구사항

### 예외 처리
- [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
  - [x] null, "", HH:mm이 아닌 경우
- [x] 사용자의 예약 생성 시 날짜, 시간 id, 테마 id에 유효하지 않은 값이 입력 되었을 때
  - [x] 날짜: null, "", yyyy-MM-dd이 아닌 경우
  - [x] 시간 id: null, 1 이상이 아닌 경우
  - [x] 테마 id: null, 1 이상이 아닌 경우
- [x] 관리자의 예약 생성 시 날짜, 시간 id, 테마 id, 예약자 id에 유효하지 않은 값이 입력 되었을 때
  - [x] 날짜: null, "", yyyy-MM-dd이 아닌 경우
  - [x] 시간 id: null, 1 이상이 아닌 경우
  - [x] 테마 id: null, 1 이상이 아닌 경우
  - [x] 예약자 id: null, 1 이상이 아닌 경우
- [x] 특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때
- [x] 특정 테마에 대한 예약이 존재하는데, 그 테마를 삭제하려 할 때
- [x] 존재하지 않는 id를 가진 데이터에 접근하려 할 때
- [x] 지나간 날짜와 시간에 대한 예약을 생성하려 할 때
- [x] 중복 예약을 생성하려 할 때
- [x] 중복 예약 시간을 생성하려 할 때 
- [x] 이름이 중복된 테마를 생성하려 할 때 

### 예약 시간
- [x] 관리자 시간 관리 페이지 조회
- [x] 시간 추가
- [x] 시간 삭제
- [x] 시간 조회

### 테마
- [x] 관리자 테마 관리 페이지 조회
- [x] 테마 추가
- [x] 테마 삭제
- [x] 테마 조회

### 사용자
- [x] 로그인 페이지 조회
- [x] 로그인
- [x] 로그아웃
- [x] 사용자 정보 조회
- [x] 사용자 목록 조회
- [x] 접근 권한 제어
  - [x] 관리자 페이지(/admin/**) 진입은 권한이 있는 사람만 할 수 있도록 제한

### 예약
- [x] 관리자의 예약 추가
  - [x] 관리자 예약 페이지 조회
- [x] 사용자의 예약 추가
  - [X] 사용자 예약 페이지 조회
  - [x] 테마와 날짜 선택 시 예약 가능 시간 조회
  - [x] 로그인 사용자 정보 활용
  - [x] 예약 추가
- [x] 인기 테마 조회 기능
  - [x] 인기 테마 페이지 조회
  - [x] 최근 일주일 기준, 해당 기간 내 예약이 많은 테마 10개 확인
- [x] 예약 목록 검색
  - [x] 관리자가 조건에 따라 예약 검색
  - [x] 예약자별, 테마별, 날짜별 검색 조건
- [x] 사용자의 예약 목록 조회
  - [x] 로그인 사용자 정보 활용
  - [x] 예약 목록
  - [ ] 예약 대기 목록
- [x] 예약 대기 요청
- [ ] 예약 대기 취소
