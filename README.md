# 방탈출 예약 페이지 사용 방법

- 계정 목록
  - 사용자 계정: 포스티
    - ID: posty@woowa.com
    - PW: 12341234
  - 사용자 계정: 밍곰
    - ID: minggom@woowa.com
    - PW: 12341234
  - 관리자 계정: 로키
    - ID: roky@woowa.com
    - PW: 12341234
  - 별도로 회원 가입이 가능하며, 회원 가입 시 일반 사용자 계정
  - 이메일 형식의 ID만 가능하며 비밀번호는 8자 이상 16자 이하
- 현재 시간 이후로 예약이 불가능합니다
- 선택한 날짜와 시간에 예약이 존재하는 경우 예약 대기를 할 수 있습니다
  - 1번 째 예약 대기 때 예약이 취소되면 자동 승인됩니다

# 📌 View API

| HTTP Method 	 |   Role    	   |    Endpoint       	    | Response 	 |  Description                        	  |
|:-------------:|:-------------:|:----------------------:|:----------:|:--------------------------------------:|
|   GET     	   |       	       |     `/`         	      | 200 OK  	  | 인덱스 페이지, 8일 ~ 1일 전 동안의 인기 테마 목록을 보여줌 	 |
|   GET     	   |  Admin    	   |    `/admin`       	    | 200 OK  	  |    관리자 인덱스 페이지                    	    |
|   GET     	   |  Admin    	   | `/admin/reservation` 	 | 200 OK  	  |    관리자 예약 관리 페이지                  	    |
|   GET     	   |  Admin    	   |   `/admin/time`    	   | 200 OK  	  |   관리자 예약 시간 관리 페이지                	    |
|   GET     	   |  Admin    	   |  `/admin/theme`    	   | 200 OK  	  |   관리자 예약 테마 관리 페이지                	    |
|   GET     	   |  Admin    	   |  `/admin/waiting`   	  | 200 OK  	  |   관리자 예약 대기 관리 페이지                	    |
|   GET     	   | User, Admin 	 |  `/reservation`    	   | 200 OK  	  |   관리자 및 사용자 예약 생성 페이지             	    |
|   GET     	   | User, Admin 	 | `/reservation-mine` 	  | 200 OK  	  |  관리자 및 사용자 내 예약/예약 대기 조회 페이지       	   |
|   GET     	   |       	       |    `/login`       	    | 200 OK  	  |    로그인 페이지                       	     |
|   GET     	   |       	       |    `/singup`      	    | 200 OK  	  |    회원가입 페이지                      	     |

# 📌 Reservation API

| HTTP Method 	 |     Role    	      |     Endpoint               	      |   Response  	    |      Description                               	       |
|:-------------:|:------------------:|:---------------------------------:|:----------------:|:------------------------------------------------------:|
|   GET     	   | User, Admin      	 |     `/reservations`         	     |    200 OK   	    |        전체 예약 목록 조회                           	         |
|   GET     	   |   User, Admin 	    |    `/reservations/my`       	     |    200 OK   	    |         내 예약 목록 조회                           	         |
|   GET     	   |   User, Admin 	    | `/reservations/filtering`       	 |    200 OK   	    | 사용자, 날짜, 테마로 필터링된 예약 목록 조회                           	 |
|   GET     	   |   User, Admin 	    |         `/times`       	          |    200 OK   	    |        전체 예약 시간 목록 조회                         	        |
|   GET     	   |   User, Admin 	    |         `/themes`       	         |    200 OK   	    |         전체 테마 목록 조회                         	          |
|   GET     	   |   User, Admin 	    |     `/themes/popular`       	     |    200 OK   	    |         인기 테마 목록 조회                         	          |
|   POST    	   |   User, Admin 	    |     `/reservations`         	     |  201 Created  	  |          예약 생성                             	           |
|   POST    	   |      Admin 	       |        `/times`         	         |  201 Created  	  |         예약 시간 생성                             	         |
|   POST    	   |      Admin 	       |        `/themes`         	        |  201 Created  	  |          테마 생성                             	           |
|   POST    	   |   User, Admin 	    |   `/reservations/waiting`     	   |  201 Created  	  |          예약 대기 생성                           	          |
|  DELETE   	   |      Admin 	       |      `/reservations/{id}`  	      | 204 No Content 	 |           예약 삭제                           	            |
|  DELETE   	   |      Admin 	       |         `/times/{id}`  	          | 204 No Content 	 |         예약 시간 삭제                             	         |
|  DELETE   	   |      Admin 	       |         `/themes/{id}`  	         | 204 No Content 	 |          테마 삭제                             	           |
|  DELETE   	   |   User, Admin 	    |  `/reservations/waiting/{id}` 	   | 204 No Content 	 |          예약 대기 삭제                           	          |

# 📌 Member API

| HTTP Method 	 | Role    	 | Endpoint         	 |  Response  	   | Description         	 |
|:-------------:|:---------:|:------------------:|:--------------:|:---------------------:|
|   GET     	   |     	     |    `/members` 	    |   200 OK   	   |     전체 회원 목록 조회 	     |
|   POST    	   |     	     | `/members`      	  | 201 Created  	 |   회원 가입          	    |

# 📌 Admin API

| HTTP Method 	 | Role 	  |         Endpoint                   	         |    Response  	     | Description           	 |
|:-------------:|:-------:|:--------------------------------------------:|:------------------:|:-----------------------:|
|   GET    	    |  Admin  |       `/admin/reservations/waiting` 	        |     200 OK   	     |    전체 예약 대기 목록 조회 	     |
|  DELETE    	  | Admin 	 |  `/admin/reservations/waiting/deny/{id}` 	   | 204 No Content   	 |       예약 대기 거절 	        |

# 📌 Auth API

| HTTP Method 	 |   Role    	   |  Endpoint    	   | Response 	 | Description   	 |
|:-------------:|:-------------:|:----------------:|:----------:|:---------------:|
|   POST    	   |       	       |   `/login`  	    | 200 OK  	  |   로그인      	    |
|   GET     	   | User, Admin 	 | `/login/check` 	 | 200 OK  	  |  내 로그인 정보 확인 	  |
|  POST     	   | User, Admin 	 |   `/logout` 	    | 200 OK  	  |     로그아웃 	      |

# 📌 기능 구현 목록

- [x] 예약 생성
  - 해당 날짜, 시간, 테마에 대한 예약이 존재하면 예약할 수 없다
  - 예약 생성 시점보다 과거의 시간에 대해 예약할 수 없다
  - 예약 대기가 존재하는 경우 예약을 생성할 수 없다
  - 사용자, 관리자 모두 본인의 예약을 생성할 수 있다
  - 관리자는 사용자 예약을 생성할 수 있다
- [x] 예약 삭제
  - 존재하지 않는 예약은 삭제할 수 없다
  - 예약 삭제 후 예약 대기가 존재하면 자동 승인한다
  - 사용자는 예약을 삭제할 수 없다
  - 관리자는 예약을 삭제할 수 있다
- [x] 필터링 예약 조회
  - 설정한 사용자, 날짜, 테마에 맞는 예약 목록을 조회한다
  - 조건 값이 존재하지 않으면 생략한다. (사용자, 날짜, 테마가 모두 존재하지 않으면 전체 예약 목록을 조회한다)
- [x] 사용자 예약 조회
  - 사용자의 예약, 예약 대기 전체 목록을 조회한다
- [x] 예약 가능 시간 목록 조회
  - 날짜, 테마를 선택하면 예약 가능한 시간 목록을 오름차순으로 조회한다
- [x] 예약 시간 생성
  - 예약 시간은 중복 생성할 수 없다
  - 관리자는 예약 시간을 생성할 수 있다
- [x] 예약 시간 삭제
  - 예약 시간이 존재하지 않으면 삭제할 수 없다
  - 해당 예약 시간을 갖고 있는 예약이 존재하면 삭제할 수 없다
  - 관리자는 예약 시간을 삭제할 수 있다
- [x] 테마 생성
  - 관리자는 테마를 생성할 수 있다
- [x] 테마 삭제
  - 테마가 존재하지 않으면 삭제할 수 없다
  - 해당 테마를 갖고 있는 예약이 존재하면 삭제할 수 없다
  - 관리자는 테마를 삭제할 수 있다
- [x] 인기 테마 조회
  - 어제 기준 최근 1주일 동안 가장 예약이 많은 상위 10개 테마를 예약 개수 내림차순으로 조회한다
- [x] 예약 대기 생성
  - 같은 사용자가 중복 예약 대기를 생성할 수 없다
  - 예약 대기 생성 시점보다 과거의 시간에 대해 예약할 수 없다
  - 예약이 존재하지 않으면 예약 대기를 생성할 수 없다
  - 사용자, 관리자 모두 예약 대기를 생성할 수 있다
- [x] 예약 대기 삭제
  - 존재하지 않는 예약 대기는 삭제할 수 없다
  - 관리자가 예약 대기를 거절하면 삭제된다
- [x] 사용자 생성
  - 사용자 이름은 2자 이상 10자 이하이다
  - 이메일은 이메일 형식을 따라야 한다
  - 패스워드는 8자 이상 16자 이하이다
- [x] 이메일과 패스워드가 일치하는 사용자 조회
- [x] 사용자 로그인
  - 이메일과 패스워드가 일치하면 로그인이 가능하다
  - 토큰을 발급해 반환한다
