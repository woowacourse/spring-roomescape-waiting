## 기능 목록
### 1단계
- [x] gradle 의존성 추가
- [x] 엔티티 매핑
  - [x] Theme
  - [x] ReservationTime
  - [x] Member
  - [x] Reservation
- [x] 연관관계 매핑

### 2단계
- [x] 내 예약 목록 조회 API 구현
  - [x] 예약 상태 추가

### 3단계
- [x] 예약 대기 요청 기능
  * ```'POST /reservations-waiting'```
- [x] 예약 대기 취소 기능
  * ```'DELETE /reservations-mine/{waiting-id}'```
- [x] 내 예약 목록 조회 시, 예약 대기 목록도 함께 조회
  * ```'GET /reservations-mine'```
- [x] 중복 예약 처리
  - [x] 확정된 예약에 동일한 사용자가 대기를 거는 경우
  - [x] 대기를 건 예약에 또 대기를 거는 경우  


**예약 확정 테이블**

| 필드명      | 데이터 타입 | 연관관계                     |
|-------------|-------------|--------------------------|
| id          | BIGINT      | 기본 키                     |
| date        | DATE        | 날짜                       |
| time_id     | BIGINT      | ReservationTime 테이블과 다대일 |
| theme_id    | BIGINT      | Theme 테이블과 다대일           |
| member_id   | BIGINT      | Member 테이블과 다대일          |

**예약 대기 테이블**  

| 필드명            | 데이터 타입 | 연관관계                 |
|----------------|-------------|----------------------|
| id             | BIGINT      | 기본 키                 |
| member_id      | BIGINT      | Member 테이블과 다대일      |
| reservation_id | BIGINT      | Reservation 테이블과 다대일 |

### 4단계
- [x] 어드민에서 예약 대기 관리 기능
  - [x] 예약 대기 관리 페이지 이동
    *  ```'GET /admin/waiting'```
  - [x] 전체 예약 대기 목록을 조회
    * ``` 'GET /admin/waitings'```
  - [x] 예약 대기를 취소
    * ```'DELETE /admin/waitings/{waiting-id}'``` <br><br>
- [x] 예약 대기 승인 기능
   -[x] 자동으로 승인
     - 예약 취소가 발생하는 경우, 예약 대기가 있을 때 우선순위에 따라 자동으로 예약
     - ```'DELETE /admin/reservations/{reservation-id}'```
       - 대기 존재하는 경우: 취소된 예약의 멤버를 첫번째 대기 멤버로 수정
       - 대기 없는 경우: 예약 취소

