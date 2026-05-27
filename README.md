## 방탈출 API 명세
| 기능              | 메서드 / URL                                      | 요청                               | 응답                                                           | 상태 코드 |
|-----------------|------------------------------------------------|----------------------------------|--------------------------------------------------------------|-------|
| 사용자 예약 등록       | POST `/reservations`                           | `{name, date, timeId, themeId}`  | `{id, name, date, time: {id, startAt}, theme: {id, name}}`   | 201   |
| 사용자 본인 예약 조회    | GET `/reservations?name=브라운`                 | —                                | `[{id, name, date, time: {id, startAt}, theme: {id, name}}, ...]` | 200   |
| 사용자 본인 예약 변경    | PUT `/reservations/{id}`                       | `{name, date, timeId}`           | `{id, name, date, time: {id, startAt}, theme: {id, name}}`   | 200   |
| 사용자 본인 예약 취소    | DELETE `/reservations/{id}?name=브라운`          | —                                | —                                                            | 204   |
| 관리자 예약 조회       | GET `/admin/reservations`                      | —                                | `[{id, name, date, time: {id, startAt}, theme: {id, name}}, ...]` | 200   |
| 관리자 예약 등록       | POST `/admin/reservations`                     | `{name, date, timeId, themeId}`  | `{id, name, date, time: {id, startAt}, theme: {id, name}}`   | 201   |
| 관리자 예약 삭제       | DELETE `/admin/reservations/{id}`              | —                                | —                                                            | 204   |
| 관리자 시간 조회       | GET `/admin/times`                             | —                                | `[{id, startAt}, ...]`                                       | 200   |
| 관리자 시간 등록       | POST `/admin/times`                            | `{startAt}`                      | `{id, startAt}`                                              | 201   |
| 관리자 시간 삭제       | DELETE `/admin/times/{id}`                     | —                                | —                                                            | 204   |
| 사용자 테마 조회       | GET `/themes`                                  | —                                | `[{id, name, description, thumbnail}, ...]`                  | 200   |
| 관리자 테마 조회       | GET `/admin/themes`                            | —                                | `[{id, name, description, thumbnail}, ...]`                  | 200   |
| 관리자 테마 등록       | POST `/admin/themes`                           | `{name, description, thumbnail}` | `{id, name, description, thumbnail}`                         | 201   |
| 관리자 테마 삭제       | DELETE `/admin/themes/{id}`                    | —                                | —                                                            | 204   |
| 예약 가능 시간 조회     | GET `/themes/{id}/times?date=2026-05-08`       | —                                | `[{time, available}, ...]`                                   | 200   |
| 인기 테마 상위 10개 조회 | GET `/themes/popular`                          | —                                | `[{id, name, description, thumbnail, reservationCount}, ...]` | 200   |

## 에러 응답 명세

모든 에러 응답은 JSON 객체로 반환한다.

```json
{
  "code": "ERROR_CODE",
  "detail": "에러 상세 설명"
}
```

`code`는 클라이언트가 분기하기 위한 안정적인 값이다. `detail`은 API 소비자가 에러 원인을 이해하기 위한 설명이며, 클라이언트는 `detail`이 아닌 `code`를 기준으로 처리한다.

| error code | detail | 상태 코드 | 상황 |
|------------|--------|----------|------|
| INVALID_INPUT | name은 비어 있을 수 없습니다. | 400 | 요청 본문의 필수 문자열 값이 비어 있음 |
| INVALID_INPUT | name은 255자를 넘을 수 없습니다. | 400 | 요청 본문의 문자열 길이가 허용 범위를 초과함 |
| INVALID_INPUT | timeId는 비어 있을 수 없습니다. | 400 | 요청 본문의 필수 ID 값이 누락됨 |
| INVALID_INPUT | timeId는 양수이어야 합니다. | 400 | 요청 본문의 ID 값이 양수가 아님 |
| INVALID_INPUT | themeId는 비어 있을 수 없습니다. | 400 | 요청 본문의 필수 ID 값이 누락됨 |
| INVALID_INPUT | themeId는 양수이어야 합니다. | 400 | 요청 본문의 ID 값이 양수가 아님 |
| INVALID_INPUT | startAt은 비어 있을 수 없습니다. | 400 | 예약 시간 생성 요청의 시작 시간이 누락됨 |
| INVALID_INPUT | description은 255자를 넘을 수 없습니다. | 400 | 테마 설명 길이가 허용 범위를 초과함 |
| INVALID_INPUT | thumbnail은 255자를 넘을 수 없습니다. | 400 | 테마 썸네일 경로 길이가 허용 범위를 초과함 |
| INVALID_INPUT | id는 양수이어야 합니다. | 400 | 경로 변수 ID 값이 양수가 아님 |
| INVALID_INPUT | 요청 본문 형식이 올바르지 않습니다. | 400 | JSON 형식이 잘못됐거나 요청 본문 타입 변환에 실패함 |
| INVALID_INPUT | date 형식이 올바르지 않습니다. | 400 | 요청 파라미터의 날짜 형식이 올바르지 않음 |
| INVALID_INPUT | id 형식이 올바르지 않습니다. | 400 | 경로 변수 ID 형식이 올바르지 않음 |
| INVALID_INPUT | date는 필수입니다. | 400 | 예약 가능 시간 조회 요청의 date 파라미터가 누락됨 |
| INVALID_INPUT | date는 비어 있을 수 없습니다. | 400 | 예약 생성 요청의 date 값이 누락됨 |
| INVALID_INPUT | name는 필수입니다. | 400 | 내 예약 조회·취소 요청의 이름 파라미터가 누락됨 |
| INVALID_INPUT | 변경할 날짜 또는 시간이 필요합니다. | 400 | 예약 변경 요청에 날짜와 시간 ID가 모두 누락됨 |
| PAST_RESERVATION | 이미 지난 시간으로는 예약할 수 없습니다. | 400 | 사용자가 지난 날짜·시간으로 예약 생성·변경을 요청함 |
| FORBIDDEN_RESERVATION | 본인의 예약만 변경하거나 취소할 수 있습니다. | 403 | 예약은 존재하지만 요청 이름과 예약 이름이 일치하지 않음 |
| NOT_FOUND | 존재하지 않는 예약 시간입니다. | 404 | 존재하지 않는 예약 시간 ID로 요청함 |
| NOT_FOUND | 존재하지 않는 테마입니다. | 404 | 존재하지 않는 테마 ID로 요청함 |
| NOT_FOUND | 존재하지 않는 예약입니다. | 404 | 존재하지 않는 예약 ID로 변경·취소를 요청함 |
| NOT_FOUND | 존재하지 않는 리소스입니다. | 404 | 존재하지 않는 URL로 요청함 |
| DUPLICATE_RESERVATION | 이미 예약된 시간입니다. | 409 | 같은 날짜·시간·테마에 이미 다른 예약이 존재함 |
| PAST_RESERVATION_LOCKED | 이미 지난 예약은 변경하거나 취소할 수 없습니다. | 409 | 이미 지난 예약 변경·취소를 요청함 |
| UNCHANGED_RESERVATION | 기존 예약과 같은 날짜·시간으로는 변경할 수 없습니다. | 409 | 기존 날짜·시간과 동일한 예약 변경을 요청함 |
| RESOURCE_IN_USE | 예약이 존재하는 시간은 삭제할 수 없습니다. | 409 | 예약이 연결된 예약 시간 삭제를 요청함 |
| RESOURCE_IN_USE | 예약이 존재하는 테마는 삭제할 수 없습니다. | 409 | 예약이 연결된 테마 삭제를 요청함 |
| INTERNAL_SERVER_ERROR | 서버에 문제가 발생했습니다. | 500 | 예상하지 못한 서버 오류가 발생함 |

---

## 구현할 기능 목록

## 사이클1

### 1단계 - 예약 대기 신청/취소
- [x] 예약 대기 신청 기능 추가
- [x] 예약 대기 조회 기능 추가
- [x] 예약 대기 삭제 기능 추가
- 예약 대기 예외 처리 추가
  - [x] 본인이 예약한 슬롯에 대기 신청 불가
  - [x] 예약 가능한 슬롯에 대기 신청 불가
  - [x] 존재하지 않는 예약 시간에 대기 신청 불가
  - [x] 존재하지 않는 테마에 대기 신청 불가
  - [x] 지난 날짜/시간에 대기 신청 불가
  - [x] 중복 대기 신청 불가

### 2단계 - 내 예약 목록 조회 (상태 구분)
- [ ] 내 예약 목록 조회 확장 
  - [ ] 예약과 대기 함께 조회 
  - [ ] 대기에는 순번도 함께 조회

---

### 예약 더미 데이터
2026-05-14 기준 -5일 ~ +5일 범위인 2026-05-09 ~ 2026-05-19 예약 데이터 200개를 구성했습니다.
날짜/시간/테마 조합은 중복되지 않도록 구성했습니다.

| 순위 | theme_id | 테마명 | 예약 횟수 |
|------|----------|--------|----------|
| 1위 | 12 | 한밤중의 도서관 | 25회 |
| 2위 | 7 | 9회말 2사 만루 | 23회 |
| 3위 | 4 | 녹화된 마지막 하루 | 21회 |
| 4위 | 1 | 시간조작자 연구소 | 20회 |
| 5위 | 9 | 잊혀진 기억의 숲 | 18회 |
| 6위 | 2 | 사라진 개발자 | 17회 |
| 7위 | 8 | 새벽 2시의 증류소 | 16회 |
| 8위 | 3 | 404호의 비밀 | 15회 |
| 9위 | 6 | 버그 추적자: 죽음의 디버깅 | 14회 |
| 10위 | 11 | 인공지능의 반란 | 12회 |
| 11위 | 5 | VIP 전용 금고 | 10회 |
| 12위 | 10 | 명탐정의 마지막 조각 | 9회 |
