## 기능 구현 목록

### 1단계 - 서비스 정책 적용

- [x] 지나간 날짜에 대한 예약 생성 불가
- [x] 같은 날짜 + 시간 + 테마에 이미 예약이 있으면 중복 예약 거부
- [x] 예약이 존재하는 시간 삭제 불가 (DB 외래키 RESTRICT 설정)
- [x] 유효하지 않은 입력값 거부 (빈 이름, null 날짜/시간/테마)

### 2단계 - 에러 응답 설계

- [x] `@RestControllerAdvice`로 전역 예외 처리 추가
- [x] 500 에러가 사용자에게 노출되지 않도록 처리
- [x] 에러 응답 본문을 JSON 형식으로 통일
- [x] 브라우저에서 에러 발생 시 사용자에게 의미 있는 메시지 표시

### 3단계 - 내 예약 조회/변경/취소

- [x] 이름으로 본인의 예약 목록 조회
- [x] 본인의 예약 취소
- [x] 본인의 예약 날짜·시간 변경 API 추가
- [x] 변경 화면 구현 (프론트엔드)
- [x] 변경·취소 에러 케이스 처리 (이미 지난 예약, 변경하려는 시간이 이미 예약된 경우 등)

### 4단계 - 예약 대기 / 대기 승인

- [x] 예약된 슬롯에 대기 등록 (본인 예약/중복 대기 거부)
- [x] 슬롯별 대기 순번 제공
- [x] **자동 전환**: 예약 취소·슬롯 변경 시 해당 슬롯의 대기 1번이 예약으로 승격
- [x] 전환·취소 시 슬롯의 대기 순번 재정렬
- [x] 대기가 없는 슬롯은 예약 취소 시 함께 삭제
- [x] 취소·승격·대기 등록 경합을 DB 제약 + rowcount 가드로 방어, 충돌 시 409로 재시도 유도

---

## 도메인 구조

`slot = (날짜, 테마, 시간)` 한 칸을 의미하며, 예약과 대기열이 모두 이 슬롯을 참조한다.

```
slot (UNIQUE(date, theme_id, time_id))
 ├─ reservation  (UNIQUE(slot_id))        : 한 슬롯에 예약 1건
 └─ waiting      (UNIQUE(slot_id, name))  : 한 슬롯에 같은 사람 중복 대기 방지
```

- 모든 외래키는 `ON DELETE RESTRICT`.
- 대기 순번(`sequence`)은 컬럼으로 저장하지 않고 **조회 시 `ROW_NUMBER() OVER (PARTITION BY slot_id ORDER BY created_at, id)` 로 파생 계산**한다. 따라서 승격·취소가 일어나면 다음 조회에서 자동으로 1, 2, 3…으로 재정렬된다(별도 재정렬 코드 불필요).

---

## API 명세

### 예약

| 메서드 | URL | 설명 | 성공 응답 |
|---|---|---|---|
| GET | /reservations | 전체 예약 조회 | 200 |
| GET | /reservations/mine?name={name} | 이름으로 내 예약 조회 | 200 |
| GET | /reservations/{id} | 예약 단건 조회 | 200 |
| POST | /reservations | 예약 생성 | 201 |
| PUT | /reservations/{id} | 예약 날짜·시간·테마 변경 (전체 교체) | 200 |
| DELETE | /reservations/{id} | 예약 취소 | 204 |

### 예약 대기

| 메서드 | URL | 설명 | 성공 응답 |
|---|---|---|---|
| GET | /reservations/waitings | 전체 대기 조회 | 200 |
| GET | /reservations/waitings/mine?name={name} | 이름으로 내 대기 조회 | 200 |
| POST | /reservations/waitings | 대기 등록 | 201 |
| DELETE | /reservations/waitings/{id} | 대기 취소 | 204 |

### 테마

| 메서드 | URL | 설명 | 성공 응답 |
|---|---|---|---|
| GET | /themes | 전체 테마 조회 | 200 |
| GET | /themes/popular | 인기 테마 상위 10개 (최근 1주일 예약 기준) | 200 |
| POST | /admin/themes | 테마 생성 | 201 |
| DELETE | /admin/themes/{id} | 테마 삭제 | 204 |

### 시간

| 메서드 | URL | 설명 | 성공 응답 |
|---|---|---|---|
| GET | /times | 전체 시간 조회 | 200 |
| GET | /times/available?date={date}&themeId={themeId} | 예약 가능 시간 조회 | 200 |
| POST | /admin/times | 시간 생성 | 201 |
| PUT | /admin/times/{id} | 시간 변경 | 200 |
| DELETE | /admin/times/{id} | 시간 삭제 | 204 |

---

## 에러 응답 설계

### 응답 형식

```json
{
  "errorCode": "DUPLICATE_RESERVATION",
  "message": "이미 예약된 시간입니다."
}
```

### 에러 코드

| 상황 | errorCode | 상태코드 |
|---|---|---|
| 중복 예약 | DUPLICATE_RESERVATION | 409 |
| 존재하지 않는 예약/대기 | RESERVATION_NOT_FOUND | 404 |
| 존재하지 않는 시간 | TIME_NOT_FOUND | 404 |
| 존재하지 않는 테마 | THEME_NOT_FOUND | 404 |
| 지나간 날짜/시간 | INVALID_DATE_OR_TIME | 400 |
| 예약이 존재하는 시간/테마 삭제 | DIFFERENCE_DATA_EXISTS | 400 |
| 유효하지 않은 입력값 | INVALID_INPUT | 400 |
| 데드락/락 획득 실패 (일시적) | LOCK_CONFLICT | 409 |
| 데이터 충돌 (대기열 변경 등) | DATA_INTEGRITY_VIOLATION | 409 |

