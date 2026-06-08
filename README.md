## API

### 사용자 (`/api/*`)

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/reservations` | 예약 생성 |
| `POST` | `/api/reservations/waiting` | 대기 신청 |
| `PATCH` | `/api/reservations/entries/{id}` | 예약 변경 |
| `DELETE` | `/api/reservations/entries/{id}` | 예약/대기 취소 |
| `GET` | `/api/reservations/entries/{id}` | 예약 단건 조회 |
| `GET` | `/api/reservations` | 예약 검색 |
| `GET` | `/api/themes` | 활성 테마 목록 |
| `GET` | `/api/themes/{id}/times?date=` | 테마별 시간 슬롯 및 예약 가능 여부 조회 |
| `GET` | `/api/themes/popular` | 인기 테마 조회 |

- 사용자 이름을 입력받아 예약 목록 및 정보를 조회할 수 있다.
- 예약 단건 조회 및 취소 시 사용자 권한 검증은 추가로 진행하지 않는다. (추후 개선 예정)

### 관리자 (`/api/admin/*`)

`role: ADMIN` 헤더 필요.

| 메서드 | 경로 | 설명 |
|---|---|---|
| `POST` | `/api/admin/reservations` | 예약 생성 |
| `DELETE` | `/api/admin/reservations/entries/{id}` | 예약/대기 취소 |
| `GET` | `/api/admin/reservations` | 전체 예약 목록 |
| `POST` | `/api/admin/times` | 예약 시간 등록 |
| `PATCH` | `/api/admin/times/{id}/activate` | 예약 시간 활성화 |
| `PATCH` | `/api/admin/times/{id}/deactivate` | 예약 시간 비활성화 |
| `GET` | `/api/admin/times` | 예약 시간 목록 |
| `POST` | `/api/admin/themes` | 테마 등록 |
| `PATCH` | `/api/admin/themes/{id}/activate` | 테마 활성화 |
| `PATCH` | `/api/admin/themes/{id}/deactivate` | 테마 비활성화 |
| `GET` | `/api/admin/themes` | 전체 테마 목록 |

### 응답 status 규칙

두 API는 요청 시점의 슬롯 상태에 따라 `entry.status`가 달라진다.  
클라이언트는 요청 엔드포인트가 아닌 **응답의 `entry.status`** 를 기준으로 결과를 판단해야 한다.

**`POST /api/reservations/waiting`**

| 슬롯 상태 | entry.status | 이유 |
|---|---|---|
| 예약자 없음 | `RESERVED` | 요청 처리 시점에 빈 자리가 생기면 대기 대신 예약으로 등록 |
| 예약자 있음 | `WAITING` | 대기 등록 |

**`PATCH /api/reservations/entries/{id}`**

| 대상 슬롯 상태 | entry.status | 이유 |
|---|---|---|
| 예약자 없음 | `RESERVED` | 예약으로 이동 |
| 예약자 있음 | `WAITING` | 대기로 이동 |

---

## 구현 기능

### 예약 대기
- 날짜 및 시간이 유효한 경우, 이미 예약이 존재하는 슬롯에 대기를 신청할 수 있다.
  - 날짜 및 시간이 과거인 경우 대기가 불가능하다.
  - 대기를 신청했는데 예약이 존재하지 않는다면 예약 상태로 등록한다.

- 같은 사용자가 같은 슬롯에 중복 대기 및 예약을 신청할 수 없다.

### 예약 대기 조회
- 사용자 이름을 사용하여 식별자를 포함한 예약 목록을 조회한다.
- 내 예약 목록 조회 시 예약 상태와 대기 상태를 구분한다.
- 대기 상태의 경우 대기 순번을 함께 표시한다.


### 예약 및 대기 취소
- 사용자는 본인의 대기를 취소할 수 있다.
  - 로그인 기능이 없는 상태이므로 예약 식별자를 전달하여 해당 예약을 취소한다.
- 예약이 취소된 경우 다음 대기 순번이 자동으로 예약된다.
