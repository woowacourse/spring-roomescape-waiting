## 🚀 1단계 - JPA 전환

### 요구사항

- build.gradle 파일을 이용하여 다음 의존성을 대체한다.
    - **as is: spring-boot-starter-jdbc**
    - **to be: spring-boot-starter-data-jpa**

- 엔티티 매핑
  각 엔티티의 연관관계를 매핑한다.

## 🚀 2단계 - 내 예약 목록 조회 기능

### 요구사항
- 내 예약 목록을 조회하는 API를 구현한다.

### Request
```http request
GET /reservations-mine HTTP/1.1
cookie: token=eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwibmFtZSI6IuyWtOuTnOuvvCIsInJvbGUiOiJBRE1JTiJ9.vcK93ONRQYPFCxT5KleSM6b7cl1FE-neSLKaFyslsZM
host: localhost:8080
```

### response
```http request
HTTP/1.1 200
Content-Type: application/json

[
{
"reservationId": 1,
"theme": "테마1",
"date": "2024-03-01",
"time": "10:00",
"status": "예약"
},
{
"reservationId": 2,
"theme": "테마2",
"date": "2024-03-01",
"time": "12:00",
"status": "예약"
},
{
"reservationId": 3,
"theme": "테마3",
"date": "2024-03-01",
"time": "14:00",
"status": "예약"
}
]
```
## 🚀 3단계 - 예약 대기 기능

### 요구사항
- 예약 대기 요청 기능을 구현한다.
- 예약 대기 취소 기능도 함께 구현한다.
- 내 예약 목록 조회 시 예약 대기 목록도 함께 포함하도록 구현한다.
- 중복 예약이 불가능 하도록 구현한다.


## 고민중

- WaitingWithRank를 dto로 볼 것인지 domain 볼 것인지 고민입니다. 우선은 데이터를 가져오기만한다 생각하여 dto로 두었습니다.
- 상속을 사용할지 조합을 사용할지 Reservation 예약 
  예약 테이블과 대기 테이블을 나누는 방식과 Dtype을 주어서 예약테이블에서 모두 관리하는 방식을 고민하였는데
  
  우선 예약 테이블과 대기 테이블을 나누는 가정하에 기존의 예약을 취소하는 과정을 본다면
     1. 예약을 삭제한다.
     2. 예약 대기에서 첫번째 순서를 찾아서 가져온다.
     3. 가져온 값을 예약 테이블에 저장한다.
     4. 예약 대기에서 예약 테이블에 넣은 데이터를 삭제한다.
  
  라는 과정의 흐름이 있을텐데, 삭제라는 과정이 너무 복잡하게 느껴졌습니다.
  
  SingleTable 전략의 상속을 사용하여
     1. 예약을 삭제한다.
     2. 예약 대기에서 첫번쨰 순서를 찾아서 타입을 update 한다.
  
  이 과정에서 추가로 고민한 부분이 SingleTable을 사용하기 때문에 Reservation 테이블에 너무 많은 데이터가 존재할 수 있어 
  select 처리하는 부분에서 성능상 문제가 있을 수 있다는 고민을 하게되었습니다.
  
  정리하면
  
  1. ReservationInfo를 통한 조합으로 Waiting과 Reservation 으로 나누는 방식
  2.  상속의 SingleTable을 이용하여 WaitingReservation과 ConfirmedReservation으로 나누는 방식 중에
  
  로직 처리 과정에서 상속의 SingleTable을 이용하는 것이 더 깔끔하다 생각하여 2 번을 선택
