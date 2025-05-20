# API 명세서

### Auth

- [All - 로그인]
    - request
        - method & path: `[POST] /login`
        - body

            ```json
            {
            	"email" : "email1@naver.com"
            	"password" : "1234"	
            }
            ```

    - response
        - 정상 처리된 경우
            - status code: `200 OK`
        - 예외 처리
            - 아이디 또는 비밀번호가 틀렸을 경우
                - status code: `401 **Unauthorized**`

            ```java
            {
              "status": 401,
              "message": "[ERROR]아이디 또는 비밀번호가 일치하지 않습니다.",
              "timestamp": "2025-05-19T13:55:07.102543"
            }
            ```

- [Admin, User - 로그인 체크]
    - request
        - method & path: `[GET] /login/check`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```java
                {
                	"id" : 1L
                	"email" : "email1@naver.com"
                	"name" : "웨이드"
                	"role" : "USER"
                }
                ```

        - 예외 처리
            - 쿠키가 없을 경우
                - status code: `401 **Unauthorized**`

            ```java
            {
              "status": 401,
              "message": "[ERROR]인증을 위한 쿠키가 존재하지 않습니다.",
              "timestamp": "2025-05-19T13:57:10.936585"
            }
            ```

- [Admin, User - 로그아웃]
    - request
        - method & path: `[POST] /logout`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
        - 예외 처리
            - 로그인을 하지 않은 경우
                - status code: `401 **Unauthorized**`

            ```java
            {
              "status": 401,
              "message": "[ERROR]인증을 위한 쿠키가 존재하지 않습니다.",
              "timestamp": "2025-05-19T13:58:06.121592"
            }
            ```


### Member

- [Admin - 멤버 전체 조회]
    - request
        - method & path: `[GET] /admin/members`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```java
                [
                	{
                	  "id": 1,
                	  "name": "웨이드"
                	},
                	{
                	  "id": 2,
                	  "name": "검프"
                	}
                ]
                ```

        - 예외 처리
            - 로그인을 하지 않은 경우
                - status code: `401 **Unauthorized**`

                ```java
                {
                  "status": 401,
                  "message": "[ERROR]인증을 위한 쿠키가 존재하지 않습니다.",
                  "timestamp": "2025-05-19T13:58:06.121592"
                }
                ```

            - 관리자가 아닌 사용자가 접근한 경우
                - status cod : `403 Forbidden`

                ```java
                {
                  "status": 403,
                  "message": "[ERROR]관리자 권한이 필요합니다.",
                  "timestamp": "2025-05-19T14:01:50.079277"
                }
                ```


### Reservation

**[Reservation]**

- [Admin - 예약 생성]
    - request
        - method & path: `[POST] /admin/reservations`
        - body

            ```json
            
            {
              "memberId": 1,
              "date": "2025-06-01",
              "timeId": 3,
              "themeId": 5
            }
            ```

    - response
        - 정상 처리된 경우
            - status code: `201 Created`
            - body

                ```json
                
                {
                  "id": 10,
                  "name": "웨이드",
                  "date": "2025-06-01",
                  "startAt": "13:00",
                  "themeName": "방탈출 테마1"
                }
                ```

        - 예외 처리
            - 유효성 검증 실패 (예: 필수 값 누락)
                - status code: `400 Bad Request`
                - body 예시

                    ```json
                    
                    {
                      "status": 400,
                      "message": "예약자를 필수로 입력해야 합니다.",
                      "timestamp": "2025-05-19T14:10:32.123456"
                    }
                    ```

- [Admin - 전체 예약 목록 조회]
    - request
        - method & path: `[GET] /admin/reservations`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                  {
                    "id": 10,
                    "name": "웨이드",
                    "date": "2025-06-01",
                    "startAt": "13:00",
                    "themeName": "방탈출 테마1"
                  },
                  {
                    "id": 11,
                    "name": "검프",
                    "date": "2025-06-02",
                    "startAt": "14:00",
                    "themeName": "방탈출 테마2"
                  }
                ]
                ```

- [Admin - 예약 검색 조회]
    - request
        - method & path: `[GET] /admin/reservations/search`
        - query params
            - `themeId`
            - `memberId`
            - `dateForm`
            - `dateTo`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                  {
                    "id": 12,
                    "name": "검프",
                    "date": "2025-06-10",
                    "startAt": "15:00",
                    "themeName": "방탈출 테마1"
                  }
                ]
                ```

- [Admin - 예약 삭제]
    - request
        - method & path: `[DELETE] /admin/reservations/{id}`

          예시: `/admin/reservations/10`

    - response
        - 정상 처리된 경우
            - status code: `204 No Content`
        - 예외 처리
            - 존재하지 않는 예약 ID
                - status code: `404 Not Found`
                - body 예시

                    ```json
                    
                    {
                      "status": 404,
                      "message": "[ERROR]id에 해당하는 예약이 존재하지 않습니다.",
                      "timestamp": "2025-05-19T14:20:01.987654"
                    }
                    ```

- [User - 예약 생성]
    - request
        - method & path: `[POST] /admin/reservations`
        - body

            ```json
            
            {
              "date": "2025-06-01",
              "timeId": 3,
              "themeId": 5
            }
            
            ```

    - response
        - 정상 처리된 경우
            - status code: `201 Created`
            - body

                ```json
                
                {
                  "id": 10,
                  "name": "웨이드",
                  "date": "2025-06-01",
                  "startAt": "13:00",
                  "themeName": "방탈출 테마1"
                }
                ```

        - 예외 처리
            - 유효성 검증 실패 (예: 필수 값 누락)
                - status code: `400 Bad Request`
                - body 예시

                    ```json
                    
                    {
                      "status": 400,
                      "message": "예약자를 필수로 입력해야 합니다.",
                      "timestamp": "2025-05-19T14:10:32.123456"
                    }
                    ```



**[ReservationTheme]**

- [Admin - 테마 생성]
    - request
        - method & path: `[POST] /admin/themes`
        - body

            ```json
            
            {
              "name": 1,
              "description": "2025-06-01",
              "thumbnail": 3
            }
            ```

    - response
        - 정상 처리된 경우
            - status code: `201 Created`
            - body

                ```json
                
                {
                  "id": 10,
                  "name": "웨이드",
                  "description" : "테마 설명".
                  "thumbnail" : "썸네일"
                }
                ```

        - 예외 처리
            - 유효성 검증 실패 (예: 필수 값 누락)
                - status code: `400 Bad Request`
                - body 예시

                    ```json
                    
                    {
                      "status": 400,
                      "message": "테마의 썸네일을 빈 문자열이 아닌 값으로 입력해주세요.",
                      "timestamp": "2025-05-19T14:10:32.123456"
                    }
                    ```

- [Admin - 테마 삭제]
    - request
        - method & path: `[DELETE] /admin/themes/{id}`
    - response
        - 정상 처리된 경우
            - status code: `204 No Content`
        - 예외 처리
            - 유효성 검증 실패 (예: 필수 값 누락)
                - status code: `400 Bad Request`
                - body 예시

                    ```json
                    
                    {
                      "status": 404,
                      "message": "[ERROR]id에 해당하는 테마가 존재하지 않습니다.",
                      "timestamp": "2025-05-19T14:20:01.987654"
                    }
                    ```

- [All - 테마 전체 조회]
    - request
        - method & path: `[GET] /themes`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                	{
                	  "id": 1,
                	  "name": "10:00",
                	  "description" : false,
                	  "thumbnail" : "썸네일입니당"
                	}
                ]
                ```

- [All - 인기 테마 조회]
    - request
        - method & path: `[GET] /themes/populars`
        - query parameter
            - `limit` : 정수형
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                	{
                	  "id": 1,
                	  "name": "10:00",
                	  "description" : false,
                	  "thumbnail" : "썸네일입니당"
                	}
                ]
                ```


**[ReservationTime]**

- [Admin - 시간 생성]
    - request
        - method & path: `[POST] /admin/themes`
        - body

            ```json
            
            {
              "startAt" : "10:00"시간을 필수로 입력해야 합니다.
            }
            ```

    - response
        - 정상 처리된 경우
            - status code: `201 Created`
            - body

                ```json
                
                {
                  "id": 1,
                  "startAt": "10:00",
                  "isBooked" : false.
                }
                ```

        - 예외 처리
            - 유효성 검증 실패 (예: 필수 값 누락)
                - status code: `400 Bad Request`
                - body 예시

                    ```json
                    
                    {
                      "status": 400,
                      "message": "시간을 필수로 입력해야 합니다.",
                      "timestamp": "2025-05-19T14:10:32.123456"
                    }
                    ```

- [Admin - 시간 전체 조회]
    - request
        - method & path: `[GET] /admin/themes`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                	{
                	  "id": 1,
                	  "startAt": "10:00",
                	  "isBooked" : false
                	}
                ]
                ```

- [Admin - 시간 삭제]
    - request
        - method & path: `[DELETE] /admin/times/{id}`
    - response
        - 정상 처리된 경우
            - status code: `204 No Content`
        - 예외 처리
            - 유효성 검증 실패 (예: 필수 값 누락)
                - status code: `400 Bad Request`
                - body 예시

                    ```json
                    
                    {
                      "status": 404,
                      "message": "[ERROR]id에 해당하는 시간이 존재하지 않습니다.",
                      "timestamp": "2025-05-19T14:20:01.987654"
                    }
                    ```

- [All - 예약 가능한 상태 여부 포함한 시간 조회]
    - request
        - method & path: `[GET] /admin/themes`
        - query parameter
            - `themeId` : 정수
            - `date` : 날짜
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                	{
                	  "id": 1,
                	  "startAt": "10:00",
                	  "isBooked" : false
                	},
                	{
                	  "id": 1,
                	  "startAt": "10:00",
                	  "isBooked" : true
                	}
                
                ]
                ```


**[My]**

- [User - 내 예약 전체 조회]
    - request
        - method & path: `[GET] /reservations-mine`
    - response
        - 정상 처리된 경우
            - status code: `200 OK`
            - body

                ```json
                
                [
                	{
                	  "reservationId": 1,
                	  "theme": "10:00",
                	  "date" : false,
                	  "status" : "CONFIRMED"
                	}
                ]
                ```
