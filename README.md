# 🚀 사이클2 - 미션 (예약 대기 승인)

## 1단계 - 예약 대기 승인 [기능 목록]

- [x] 예약 취소 시 대기 1번이 자동으로 예약으로 전환 (자동 전환 방식 선택)
  - [x] 과거 슬롯이면 승격 생략
  - [x] 이중 취소 시 이중 승격 방지
- [x] 대기 전환 또는 취소 시 해당 슬롯의 대기 순번 재정렬
- [x] 관리자/매니저 대기 목록 조회 및 강제 취소

## API 명세
### 사용자 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| POST | /waitings | 대기 신청 | 로그인 |
| GET | /waitings | 내 대기 목록 조회 | 로그인 |
| DELETE | /waitings/{id} | 본인 대기 취소 | 로그인 |

**POST /waitings 요청 본문**
```json
{
  "date": "2025-06-10",
  "timeId": 1,
  "themeId": 1,
  "storeId": 1
}
```

**GET /waitings 응답 예시**
```json
[
  {
    "id": 1,
    "name": "홍길동",
    "date": "2025-06-10",
    "status": "WAITING",
    "rank": 1,
    "theme": { "id": 1, "name": "미스터리", "description": "...", "thumbnail": "..." },
    "time": { "id": 1, "startAt": "10:00" }
  }
]
```

### 관리자 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /admin/waitings | 전체 대기 목록 조회 | 관리자 |
| DELETE | /admin/waitings/{id} | 대기 강제 취소 | 관리자 |

### 매니저 API

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | /manager/waitings | 소속 매장 대기 목록 조회 | 매니저 |
| DELETE | /manager/waitings/{id} | 소속 매장 대기 강제 취소 | 매니저 |

