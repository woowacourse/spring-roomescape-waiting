## 기능 요구사항
- [x] 예외 처리
  - [x] 시간 생성 시 시작 시간에 유효하지 않은 값이 입력되었을 때
    - null, "", HH:mm이 아닌 경우
  - [x] 예약 생성 시 예약자명, 날짜, 시간에 유효하지 않은 값이 입력 되었을 때
    - 예약자명: null, ""
    - 날짜: null, "", yyyy-MM-dd이 아닌 경우
    - 시간: null, "", HH:mm이 아닌 경우
  - [x] 특정 시간에 대한 예약이 존재하는데, 그 시간을 삭제하려 할 때
  - [x] 존재하지 않는 id를 가진 데이터에 접근하려 할 때
  - [x] 지나간 날짜와 시간에 대한 예약 생성 불가능
  - [x] 중복 예약 불가능
  - [x] 예약 시간 중복 불가능

- [x] '테마' 도메인 추가
  - [x] 모든 테마는 시작 시간과 소요 시간이 동일
- [x] 테마 관리 페이지 조회
- [x] 테마 추가 API
- [x] 테마 삭제 API
- [x] 테마 조회 API

- [x] (관리자가 아닌) 사용자가 예약 가능한 시간을 조회하고, 예약할 수 있도록 기능을 추가/변경
  - [x] 테마와 날짜를 선택하면 예약 가능한 시간 조회
  - [x] 예약 추가
  - [X] /reservation 요청 시 사용자 예약 페이지 조회
- [x] 인기 테마 조회 기능을 추가
  - [x] 최근 일주일을 기준으로 하여 해당 기간 내에 방문하는 예약이 많은 테마 10개를 확인
  - [x] / 요청 시 인기 테마 페이지 조회

- [x] 사용자 도메인 추가
- [x] 로그인 기능 구현
  - [x] 로그인 후 Cookie를 이용하여 사용자의 정보를 조회하는 API 구현
    - [x] 응답 Cookie에 "token"값으로 토큰이 포함
    - [x] `GET /login/check` 요청 시 Cookie를 이용하여 로그인 사용자의 정보를 확인
  - [x] `GET /login` 요청 시 로그인 폼이 있는 페이지를 응답
  - [x] `POST /login` 요청 시 로그인 폼에 입력한 email, password 값을 body에 포함
  
- [x] 사용자 정보를 조회하는 기능 리팩터링
  - [x] Cookie에 담긴 인증 정보를 이용해서 멤버 객체를 만드는 로직 분리(HandlerMethodArgumentResolver을 활용)
  
- [x] 예약 생성기능 변경 - 사용자
  - [x] 사용자가 예약 생성 시, 로그인한 사용자 정보를 활용

- [x] 예약 생성 기능 변경 - 관리자
  - [x] 관리자가 예약 생성 시, 유저를 조회하여 선택 후 예약 생성
  - [x] 관리자가 맞는지 확인

- [x] 접근 권한 제어
  - [x] Member의 Role이 ADMIN인 사람만 /admin으로 시작하는 페이지에 접근 가능
  - [x] HandlerInterceptor를 활용하여 권한을 확인하고 권한이 없는 경우 요청에 대한 거부 응답

- [x] 예약 목록 검색
  - [x] 예약이 많아질 경우 관리가 용이하도록 예약 검색 기능을 추가
  - [x] 예약자별, 테마별, 날짜별 검색 조건을 사용해 예약 검색이 가능하도록 기능을 추가

- [x] 엔티티 매핑
  - [x] 다른 클래스를 의존하지 않는 클래스 먼저 엔티티 설정

- [x] 연관관계 매핑
  - [x] 다른 클래스에 의존하는 클래스는 연관관계 매핑을 추가

- [x] 내 예약 목록을 조회하는 API를 구현


- [x] 예약 대기 요청 기능
- [ ] 예약 대기 취소 기능
- [x] 내 예약 목록 조회 시 예약 대기 목록 포함
  - [ ] 예약 목록의 예약 대기 상태에 몇 번째 대기인지도 함께 표시
- [ ] 중복 예약이 불가능 하도록 구현하세요.


