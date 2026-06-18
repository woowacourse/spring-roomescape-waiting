## API 명세서

### 공통

- [x] 요청 값 검증에 실패한 경우 (필드 누락, 빈 값 등): `Http Status: 400 Bad Request`

    ```text
    {
        "status": 400,
        "errorType": "BAD_REQUEST",
        "message": "(~ 이유)"
    }
    ```

- [x] 요청 본문(JSON)의 형식이 올바르지 않은 경우: `Http Status: 400 Bad Request`

    ```text
    {
        "status": 400,
        "errorType": "BAD_REQUEST",
        "message": "요청 본문(JSON)의 형식이 올바르지 않거나 읽을 수 없습니다."
    }
    ```

- [x] 경로 변수 또는 쿼리 파라미터의 타입이 올바르지 않은 경우: `Http Status: 400 Bad Request`

    ```text
    {
        "status": 400,
        "errorType": "BAD_REQUEST",
        "message": "요청 파라미터 또는 경로 변수의 타입이 올바르지 않습니다."
    }
    ```

- [x] DB 제약 조건 위반이 발생한 경우 (중복 데이터 등): `Http Status: 400 Bad Request`

    ```text
    {
        "status": 400,
        "errorType": "BAD_REQUEST",
        "message": "이미 존재하는 데이터이거나 유효하지 않은 값이 포함되어 있습니다."
    }
    ```

- [x] 요청 url이 잘못된 경우: `Http Status: 404 Not Found`

    ```text
    {
        "status": 404,
        "errorType": "NOT_FOUND",
        "message": "잘못된 경로입니다."
    }
    ```

- [x] 요청 메서드가 잘못된 경우: `Http Status: 405 Method Not Allowed`

    ```text
    {
        "status": 405,
        "errorType": "METHOD_NOT_ALLOWED",
        "message": "지원하지 않는 메서드입니다."
    }
    ```

- [x] 서버 내부 오류가 발생한 경우: `Http Status: 500 Internal Server Error`

    ```text
    {
        "status": 500,
        "errorType": "INTERNAL_SERVER_ERROR",
        "message": "서버 내부에서 에러가 발생했습니다."
    }
    ```

### Theme

- 관리자의 테마 추가
    - Http Method: POST
    - URL: /admin/themes
    - Request
        ```text
        {
            "name": "피즈의 모험",
            "description": "피즈가 모험을 떠나는 이야기입니다.",
            "thumbnailUrl": "http://localhost:8080/images/fizz.jpg"
        }
        ```
    - Response
        - [x] 정상적으로 추가된 경우: `Http Status: 201 Created`
          ```text
          {
              "id": 1,
              "name": "피즈의 모험",
              "description": "피즈가 모험을 떠나는 이야기입니다.",
              "thumbnailUrl": "http://localhost:8080/images/fizz.jpg"
          }
          ```

- 관리자의 테마 삭제
    - Http Method: DELETE
    - URL: /admin/themes/{id}
    - Response
        - [x] 정상적으로 삭제된 경우: `Http Status: 204 No Content`

        - [x] 해당 테마를 사용하는 예약 또는 대기가 존재할 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "예약/대기에서 사용 중인 테마는 삭제할 수 없습니다."
          }
          ```

- 전체 테마 조회
    - Http Method: GET
    - URL: /themes
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "items": [
                  {
                      "id": 1,
                      "name": "피즈의 모험",
                      "description": "피즈가 모험을 떠나는 이야기입니다.",
                      "thumbnailUrl": "http://localhost:8080/images/fizz.jpg"
                  },
                  {
                      "id": 2,
                      "name": "나무의 일대기",
                      "description": "나무가 살아온 인생을 보여주는 이야기입니다.",
                      "thumbnailUrl": "http://localhost:8080/images/tree.jpg"
                  }
              ]
          }
          ```

- 인기 테마 조회
    - Http Method: GET
    - URL: /themes/ranking?startDate={startDate}&endDate={endDate} / date 형식: yyyy-mm-dd
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "items": [
                  {
                      "id": 1,
                      "name": "잃어버린 왕국",
                      "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                      "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                  },
                  {
                      "id": 2,
                      "name": "심야의 연구소",
                      "description": "한밤중 폐쇄된 연구소에서 탈출 단서를 찾는 스릴러 테마",
                      "thumbnailUrl": "https://example.com/images/midnight-lab.jpg"
                  },
                  {
                      "id": 3,
                      "name": "해적선의 저주",
                      "description": "저주받은 해적선에서 보물을 찾아 탈출하는 테마",
                      "thumbnailUrl": "https://example.com/images/pirate-curse.jpg"
                  }
              ]
          }
          ```

        - [x] 시작 날짜 또는 종료 날짜가 오늘 이후인 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "랭킹 조회는 오늘 날짜 이전까지만 가능합니다."
          }
          ```

        - [x] 종료 날짜가 시작 날짜보다 먼저 올 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "랭킹 조회 기간의 종료 날짜는 시작 날짜보다 빠를 수 없습니다."
          }
          ```

        - [x] 조회 기간이 366일을 초과할 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "랭킹 조회 기간이 최대 기간(366일)을 초과했습니다."
          }
          ```

### Reservation

- 예약/대기 추가
    - Http Method: POST
    - URL: /reservations
    - Request Header: `Member-Id: {memberId}`
    - Request Body
        ```text
        {
            "date": "2026-05-02",
            "timeId": 1,
            "themeId": 1
        }
        ```
    - Response
        - [x] 예약으로 정상적으로 추가된 경우: `Http Status: 201 Created`
          ```text
          {
              "id": 1,
              "name": "fizz",
              "date": "2026-05-02",
              "time": {
                  "id": 1,
                  "startAt": "10:00:00"
              },
              "theme": {
                  "id": 1,
                  "name": "피즈의 모험",
                  "description": "피즈가 모험하는 이야기",
                  "thumbnailUrl": "1.jpg"
              },
              "status": "CONFIRMED"
          }
          ```

        - [x] 대기로 정상적으로 추가된 경우: `Http Status: 201 Created`
          ```text
          {
              "id": 1,
              "name": "fizz",
              "date": "2026-05-02",
              "time": {
                  "id": 1,
                  "startAt": "10:00:00"
              },
              "theme": {
                  "id": 1,
                  "name": "피즈의 모험",
                  "description": "피즈가 모험하는 이야기",
                  "thumbnailUrl": "1.jpg"
              },
              "status": "WAITING",
              "order": 1,
              "createdAt": "2026-05-02T10:00:00"
          }
          ```

        - [x] 테마 ID로 테마를 찾을 수 없는 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "해당 테마가 존재하지 않습니다."
          }
          ```

        - [x] 예약 시간 ID로 예약 시간을 찾을 수 없는 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "해당 예약 시간이 존재하지 않습니다."
          }
          ```

        - [x] 같은 사용자가 이미 같은 슬롯에 예약한 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "이미 해당 슬롯에 예약을 신청했습니다."
          }
          ```

        - [x] 같은 사용자가 이미 같은 슬롯에 대기 중인 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "이미 해당 슬롯에 예약 대기를 신청했습니다."
          }
          ```

        - [x] 해당 슬롯의 대기 인원이 마감된 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "해당 슬롯에 대기 인원이 마감되었습니다."
          }
          ```

        - [x] 지나간 날짜, 시간일 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "지나간 시간의 예약은 생성할 수 없습니다."
          }
          ```

- 내 예약/대기 조회
    - Http Method: GET
    - URL: /reservations/mine
    - Request Header: `Member-Id: {memberId}`
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "reservations": {
                  "items": [
                      {
                          "id": 1,
                          "name": "예약자01",
                          "date": "2026-05-01",
                          "time": {
                              "id": 1,
                              "startAt": "10:00:00"
                          },
                          "theme": {
                              "id": 1,
                              "name": "잃어버린 왕국",
                              "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                              "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                          },
                          "status": "CONFIRMED"
                      }
                  ]
              },
              "waits": {
                  "items": [
                      {
                          "id": 1,
                          "name": "예약자01",
                          "date": "2026-05-02",
                          "time": {
                              "id": 2,
                              "startAt": "11:00:00"
                          },
                          "theme": {
                              "id": 1,
                              "name": "잃어버린 왕국",
                              "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                              "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                          },
                          "status": "WAITING",
                          "order": 1,
                          "createdAt": "2026-05-01T09:00:00"
                      }
                  ]
              }
          }
          ```

- 사용자 이름으로 예약/대기 조회
    - Http Method: GET
    - URL: /reservations?name={name}
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "reservations": {
                  "items": [
                      {
                          "id": 1,
                          "name": "예약자01",
                          "date": "2026-05-01",
                          "time": {
                              "id": 1,
                              "startAt": "10:00:00"
                          },
                          "theme": {
                              "id": 1,
                              "name": "잃어버린 왕국",
                              "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                              "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                          },
                          "status": "CONFIRMED"
                      }
                  ]
              },
              "waits": {
                  "items": [
                      {
                          "id": 1,
                          "name": "예약자01",
                          "date": "2026-05-02",
                          "time": {
                              "id": 2,
                              "startAt": "11:00:00"
                          },
                          "theme": {
                              "id": 1,
                              "name": "잃어버린 왕국",
                              "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                              "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                          },
                          "status": "WAITING",
                          "order": 1,
                          "createdAt": "2026-05-01T09:00:00"
                      }
                  ]
              }
          }
          ```

- 전체 예약/대기 조회
    - Http Method: GET
    - URL: /reservations
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "reservations": {
                  "items": [
                      {
                          "id": 1,
                          "name": "예약자01",
                          "date": "2026-05-01",
                          "time": {
                              "id": 1,
                              "startAt": "10:00:00"
                          },
                          "theme": {
                              "id": 1,
                              "name": "잃어버린 왕국",
                              "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                              "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                          },
                          "status": "CONFIRMED"
                      }
                  ]
              },
              "waits": {
                  "items": [
                      {
                          "id": 1,
                          "name": "예약자02",
                          "date": "2026-05-02",
                          "time": {
                              "id": 2,
                              "startAt": "11:00:00"
                          },
                          "theme": {
                              "id": 1,
                              "name": "잃어버린 왕국",
                              "description": "사라진 고대 왕국의 비밀을 추적하는 모험 테마",
                              "thumbnailUrl": "https://example.com/images/lost-kingdom.jpg"
                          },
                          "status": "WAITING",
                          "order": 1,
                          "createdAt": "2026-05-01T09:00:00"
                      }
                  ]
              }
          }
          ```

- 예약 삭제
    - Http Method: DELETE
    - URL: /reservations/{id}
    - Response
        - [x] 정상적으로 삭제된 경우: `Http Status: 204 No Content`

        - [x] 예약 ID로 기존 예약을 찾을 수 없는 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "해당 예약이 존재하지 않습니다."
          }
          ```

        - [x] 이미 지나간 시간의 예약을 삭제할 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "지나간 시간의 예약은 삭제할 수 없습니다."
          }
          ```

- 대기 취소
    - Http Method: DELETE
    - URL: /reservations/waits/{id}
    - Response
        - [x] 정상적으로 취소된 경우: `Http Status: 204 No Content`

        - [x] 대기 ID로 기존 대기를 찾을 수 없는 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "해당 대기가 존재하지 않습니다."
          }
          ```

        - [x] 이미 지나간 시간의 대기를 취소할 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "지나간 시간의 대기는 삭제할 수 없습니다."
          }
          ```

### Reservation Time

- 예약 시간 추가
    - Http Method: POST
    - URL: /times
    - Request
        ```text
        {
            "startAt": "10:00:00"
        }
        ```
    - Response
        - [x] 정상적으로 추가된 경우: `Http Status: 201 Created`
          ```text
          {
              "id": 1,
              "startAt": "10:00:00"
          }
          ```

        - [x] 존재하는 예약 시간과 동일한 시간일 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "이미 중복된 예약 시간이 존재합니다."
          }
          ```

- 전체 예약 시간 조회
    - Http Method: GET
    - URL: /times
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "items": [
                  {
                      "id": 1,
                      "startAt": "10:00:00"
                  },
                  {
                      "id": 2,
                      "startAt": "11:00:00"
                  },
                  {
                      "id": 3,
                      "startAt": "12:00:00"
                  }
              ]
          }
          ```

- 예약 가능 여부 조회
    - Http Method: GET
    - URL: /times/available?date={date}&themeId={themeId} / date 형식: yyyy-mm-dd
    - Response
        - [x] 정상적으로 조회된 경우: `Http Status: 200 OK`
          ```text
          {
              "items": [
                  {
                      "time": {
                          "id": 1,
                          "startAt": "10:00:00"
                      },
                      "availability": "RESERVATION_AVAILABLE"
                  },
                  {
                      "time": {
                          "id": 2,
                          "startAt": "11:00:00"
                      },
                      "availability": "WAITING_AVAILABLE"
                  },
                  {
                      "time": {
                          "id": 3,
                          "startAt": "12:00:00"
                      },
                      "availability": "NOTHING_AVAILABLE"
                  }
              ]
          }
          ```
          `availability` 값의 의미:
            - `RESERVATION_AVAILABLE`: 예약 가능
            - `WAITING_AVAILABLE`: 예약 마감, 대기 가능
            - `NOTHING_AVAILABLE`: 예약 및 대기 모두 마감

        - [x] 테마 ID로 테마를 찾을 수 없는 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "해당 테마가 존재하지 않습니다."
          }
          ```

        - [x] 이미 지나간 날짜로 조회할 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "지나간 날짜의 예약 가능 시간은 조회할 수 없습니다."
          }
          ```

- 예약 시간 삭제
    - Http Method: DELETE
    - URL: /times/{id}
    - Response
        - [x] 정상적으로 삭제된 경우: `Http Status: 204 No Content`

        - [x] 해당 예약 시간을 사용하는 예약 또는 대기가 존재할 경우: `Http Status: 400 Bad Request`
          ```text
          {
              "status": 400,
              "errorType": "BAD_REQUEST",
              "message": "예약/대기에서 사용 중인 예약 시간은 삭제할 수 없습니다."
          }
          ```

---

# 기능 구현 목록

방탈출 사용자 예약 미션까지는 한 슬롯(날짜+시간+테마)에 한 명만 예약할 수 있었고, 이미 예약된 슬롯은 사용자에게 보이지 않았다.  
이번 사이클부터는 이미 예약된 슬롯에 대기를 신청할 수 있고, 사용자는 본인의 예약과 대기를 함께 조회할 수 있다.

이번 사이클의 작업도 백엔드 API 추가와 사용자가 보는 화면을 만드는 것 두 가지를 함께 진행한다.  
API에 맞춰 페어가 함께 사용자가 사용하는 클라이언트 화면을 만들고, 각 단계의 화면이 브라우저에서 정상 동작하는 것까지 확인한다. 화면 작성에는 AI를 활용해도 좋다.

## 1단계 - 예약 대기 신청/취소

- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
    - [x] 예약이 존재하면 예약 대기 가능 여부를 확인하고 대기를 추가한다.
- [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
    - [x] 예약 대기 인원은 최대 3명으로 제한한다.
- [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
    - [x] 예약이 존재할 때, 기존 예약과 새 예약의 요청자가 같으면 예외를 발생시킨다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.
    - [x] 예약이 취소되었을 때 예약 대기 1번을 예약에 추가한다.

## 2단계 - 내 예약 목록 조회 (상태 구분)

- [x] 이전 미션의 내 예약 목록 조회를 확장한다.
- [x] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [x] 대기에는 본인의 대기 순번도 함께 보여준다.
