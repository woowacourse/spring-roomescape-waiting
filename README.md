# 🚀 방탈출 사용자 예약 설계

### 📄 방탈출 통합 API 명세

| 도메인             | Method   | Endpoint             | Path Variable | Query Parameter                        | Request Body                                            | Response                    | Description                            |
|:----------------|:---------|:---------------------|:--------------|:---------------------------------------|:--------------------------------------------------------|:----------------------------|:---------------------------------------|
| **Reservation** | `GET`    | `/reservations`      | -             | -                                      | -                                                       | `List<ReservationResponse>` | 모든 예약 목록 조회                            |
| **Reservation** | `POST`   | `/reservations`      | -             | -                                      | `ReservationRequest`<br>*(name, date, timeId, themeId)* | `ReservationResponse`       | 새로운 예약 생성                              |
| **Reservation** | `DELETE` | `/reservations/{id}` | `id` (Long)   | -                                      | -                                                       | `200 OK` (Void)             | 식별자를 통한 예약 삭제                          |
| **Theme**       | `GET`    | `/themes`            | -             | -                                      | -                                                       | `List<ThemeResponse>`       | 모든 테마 목록 조회                            |
| **Theme**       | `GET`    | `/themes`            | -             | `topCount` (Long)<br>`during` (Long)   | -                                                       | `List<ThemeResponse>`       | 기간(`during`) 내 상위(`topCount`) 인기 테마 조회 |
| **Theme**       | `POST`   | `/themes`            | -             | -                                      | `ThemeRequest`<br>*(name, description, thumbnailUrl)*   | `ThemeResponse`             | 새로운 테마 생성                              |
| **Theme**       | `DELETE` | `/themes/{id}`       | `id` (Long)   | -                                      | -                                                       | `200 OK` (Void)             | 식별자를 통한 테마 삭제                          |
| **Time**        | `GET`    | `/time`              | -             | -                                      | -                                                       | `List<TimeResponse>`        | 모든 예약 시간 목록 조회                         |
| **Time**        | `GET`    | `/time`              | -             | `themeId` (long)<br>`date` (LocalDate) | -                                                       | `List<TimeResponse>`        | 특정 날짜, 테마의 예약 가능 시간 조회                 |
| **Time**        | `POST`   | `/time`              | -             | -                                      | `TimeRequest`<br>*(startAt)*                            | `TimeResponse`              | 새로운 예약 시간 생성                           |
| **Time**        | `DELETE` | `/time/{id}`         | `id` (Long)   | -                                      | -                                                       | `200 OK` (Void)             | 식별자를 통한 예약 시간 삭제                       |

<details><summary><h4>📜 그룹 규칙 초안</h4></summary>

규칙 정리

테스트가 보호하는 대상

프레임워크나 라이브러리 자체가 아니라, 우리가 작성한 비즈니스 규칙과 사용자에게 보이는 핵심 동작이 변경 이후에도 깨지지 않도록 보호하기 위해 테스트한다.
• (If-Then) 만약 [핵심 도메인 규칙(예: 예약 중복 방지)이나 외부 동작 계약(API 응답)이 변경되는 상황] → 우리는 [해당 스펙을 검증하는 테스트를 최우선으로 작성한다.]
→ 우리는 해당 스펙을 검증하는 테스트를 최우선으로 작성한다.

테스트 단위 선택 기준

(If-Then)

만약 외부 의존성 없이 순수 도메인 규칙만 검증 가능하다면
→ 단위 테스트로 검증한다.
(이유: 가장 빠르고, 실패 원인을 명확하게 드러낼 수 있다)

(If-Then)

만약 스프링 컨테이너, DB, HTTP, Repository 등 외부 의존성과의 결합이 검증 대상이라면
→ 통합 및 슬라이스 테스트로 검증한다.
(이유: 단위 테스트만으로는 결합 지점의 문제를 발견할 수 없다)

(If-Then)

만약 사용자 관점의 주요 흐름(예약 생성, 예약 대기, 인증 흐름 등)을 보호해야 한다면
→ E2E 테스트로 최소한의 핵심 시나리오를 검증한다.
(이유: 실제 사용 흐름이 깨지는 문제를 가장 늦게 발견하지 않기 위해서다)

(우선순위)

같은 검증을 어디서 할지 결정할 때 순서:

1. 외부 의존 없이 검증 가능한가? → 단위 테스트
2. 결합 자체가 검증 대상인가? → 통합 테스트
3. 사용자 시나리오 전체 보호가 목적인가? → E2E 테스트

(금지)

단순 위임만 수행하는 계층은 별도 테스트하지 않는다.
(이유: 동일한 내용을 여러 계층에서 중복 검증하게 된다)

DB·HTTP 의존 코드 테스트 기준

If-Then

만약 DB 저장/조회 정합성이 중요하다면
→ 우리는 테스트 DB 또는 인메모리 DB를 사용한다.
만약 비즈니스 분기만 검증하면 된다면
→ 우리는 Fake 또는 Mock을 사용할 수 있다.
(이유: 외부 프로덕션 DB 상태에 의존하지 않고, 로컬 환경에서 독립적이고 빠른 쿼리 정합성 검증이 필요하기 때문이다.)
만약 HTTP 요청/응답 전체가 중요하다면
→ 우리는 실제 요청 기반 테스트를 작성한다.
만약 외부 API 호출이 필요하다면
→ 우리는 실제 호출하지 않고 Fake 또는 Mock으로 대체한다.

(우선순위)
A. 실제 I/O 없이 협력 객체의 호출 행위(메서드 호출 여부)만 확인하면 되는가? (Mocking)
B. 실제 쿼리 문법과 ORM 매핑 확인이 필요한가? (인메모리 DB)
C. 벤더 특화 기능(MySQL Lock, 특정 함수 등) 검증이 필요한가? (실제 DB 연동)

테스트 제외 기준

If-Then

만약 단순 설정, 단순 등록, 공통 인프라, 단순 위임이라면
→ 우리는 테스트하지 않는다.
(우선순위)
A. 코드 내부에 비즈니스 분기나 데이터 가공 로직이 존재하는가? (테스트 필수 대상)
B. 우리가 직접 커스텀하게 구현한 인프라 설정인가? (통합/슬라이스 테스트로 간접 검증)
C. 프레임워크가 자체적으로 보장하는 기능인가? (테스트 생략)
(금지) 이번 사이클에서 @ControllerAdvice 등 전역 설정 파일 자체에 대한 테스트는 하지 않는다.
(이유: 전역 예외 처리나 필터는 프레임워크의 생명주기 내에서 동작하는 영역이므로, 이를 따로 떼어내어 검증하는 것은 무의미하기 때문이다)

</details>

---

## ✅ 1 단계 - 예약 대기 신청/취소

#### 스키마 추가

```text
예약(reservation)

테마(theme)

시간대(time_slot) 

++ 예약 대기(waiting) ++
ㄴ date
ㄴ theme_id
ㄴ time_id
ㄴ name
ㄴ created_at
```

<details><summary><h4>설계 과정</h4></summary>

#### 무엇을 추가할 것인가

- [x] 대기를 새로운 스키마로?
    - 대길 픽
    - 장점?
        - 예약 대기에 관에서만 관리 가능 (SRP)
        - 데이터 이관을 통한 예약 테이블도 데이터의 정합성과 제약조건 유지 가능
    - 단점?
        - 스키마 증가로 복잡성 증가.
        - 별도 조인쿼리 필요.
        - 중복 로직 존재 가능성.
- [ ] 대기를 새로운 속성으로?
    - 글렌 픽
    - 장점?
        - 예약 자체 + 대기나 순번같은 정보를 저장하고 기록할 수 있음 (통계)
        - 그 외는 스키마 방식의 단점의 역
    - 단점?
        - 스키마 방식의 장점의 역

> 장단점이 명확한 상황, 새로운 스키마로 SRP 하게 작성해보기

---

#### DB 설계 근거

- [ ] reservation_id 외래키 참조
    - 장점?
        - 중복값을 저장할 필요 X
        - 중복 조회 쿼리 제거
        - 도메인 조립 편의
    - 단점?
        - 예약 취소로 원본 예약이 변경될 경우 업데이트 필요
- [x] date, theme_id, time_id 참조
    - 장점?
        - 최초값을 유지하며 로직 구현 가능
    - 단점?
        - 중복값 다수 저장 필요

> 중복값 증가라는 단점은 크지만 변경의 전파가 훨씬 치명적인 단점이라 후자 선택

---

#### 예약 대기 컨트롤러/서비스/레포지토리 필요성

- [ ] 없음(기존 reservation 활용)
    - 장점?
        - 클래스 최소화
    - 단점?
        - 쿼리 증가
        - 컨트롤러/서비스/레포지토리 책임 증가
- [x] 있음(신규 waiting 추가)
    - 장점?
        - SRP 준수
        - 확장성 증가
    - 단점?
        - 클래스 증가
        - 변경 전파 증가

> 도메인 필요성?
> - DB 에서 쿼리로 해당 예약 대기보다 빠른 순번(created_at 이 이른)이 존재하는지, 그 개수를 순번으로 활용
> - 별도 도메인도, 서비스 로직도 필요 없이 바로 순번 계산 및 반환 가능

> 스키마를 분리한 이상, 해당 스키마에 접근하는 레이어를 명확히 할 필요가 있다

</details>

---

### 🛠️ 기능 구현 체크리스트

- [x] 스키마 추가 `table waiting`
- [x] Command 객체 추가 - `class WaitingCommand`
- [x] 레포지토리 추가 `class WaitingRepository`
    - [x] 순번 조회 `int calculateWaitingNumberByName(WaitingCommand waiting)`
    - [x] 삽입 `void insert(WaitingCommand waiting)`
    - [x] 삭제 `void delete(WaitingCommand waiting)`
- [x] 서비스 추가
    - [x] 순번 조회 `int waitingNumber(WaitingCommand waiting)`
    - [x] 삽입 `void applyWaiting(WaitingCommand waiting)`
    - [x] 삭제 `void cancelWaiting(WaitingCommand waiting)`
- [ ] Request 객체 추가 - `class WaitingRequest`
- [ ] 컨트롤러 추가
    - [ ] 순번 조회 `int waitingNumber(@RequestBody WaitingRequest waitingRequest)`
    - [ ] 삽입 `void apply(@RequestBody WaitingRequest waitingRequest)`
    - [ ] 삭제 `void cancel(@RequestBody WaitingRequest waitingRequest)`

`스키마 이름`

- [ ] `waiting_list`
    - 테이블명은 복수형 X, not reservation_list
    - 각 컬럼이 의미하는 바를 표현하는 테이블명
- [x] `waiting`

`WaitingCommand`

- 파라미터로 name 전달 시 식별 불가
- `예약`에 대한 대기 정보 == 예약 정보 포함된 요청 필요
- `예약`의 정보를 기반으로 조회/수정/삭제 요청, 쿼리 수행

## ✅ 2단계 - 내 예약 목록 조회 (상태 구분)

<details><summary><h4>설계 과정 상세</h4></summary>

</details>

## ✅ 3단계 -

<details><summary><h4>설계 과정 상세</h4></summary>

</details>
