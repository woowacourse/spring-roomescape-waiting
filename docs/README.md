##페어 프로그래밍
### 1단계 - 예약 대기 신청/취소
- [x] 이미 다른 사용자에 의해 예약된 슬롯(날짜+시간+테마)에 대기를 신청할 수 있다.
- [x] 같은 슬롯에 대한 대기는 신청 순서대로 순번이 부여된다.
- [x] 같은 사용자가 같은 슬롯에 중복 대기할 수 없다.
- [x] 사용자는 본인의 대기를 취소할 수 있다.

### 2단계 - 내 예약 목록 조회 (상태 구분)
- [x] 이전 미션의 내 예약 목록 조회를 확장한다.
- [x] 사용자의 예약과 대기가 상태로 구분되어 함께 표시된다.
- [x] 대기에는 본인의 대기 순번도 함께 보여준다.

### 기능 목록
- [x] 에약 대기 기능 추가
- [x] 예약 대기 순번 계산
- [x] 예약 대기 취소 기능 추가
- [x] 예약 및 대기 조회 기능 추가
- [x] 예약 가능한 시간에 대기 인원 정보 추가
- [x] 예약 수정 기능
- [x] 예약 취소 기능

### 예외 처리
- [x] 같은 슬롯에 대한 중복 대기 불가 예외처리
- [x] 본인의 예약이 아닌 경우 대기 취소 불가
- [x] 이미 날짜가 지난 예약 대기는 취소 불가 

### 질문

### 1. 예약 생성 API와 예약 대기 API를 분리해야 할까?

```text
1) 분리하는 경우
- 예약 생성 실패 시 “예약 대기를 하시겠습니까?”를 응답
- 이후 별도의 예약 대기 API 호출

2) 하나로 합치는 경우
- `POST /reservations` 하나만 호출
- 예약 가능 시 예약 확정
- 이미 예약이 있으면 자동으로 예약 대기 등록
```

저는 사용자의 행동 자체는 둘 다 “예약 요청”이라고 생각해서 하나의 API에서 처리했습니다.   
또한 예약 가능 여부에 따라 어떤 동작을 수행할지는 클라이언트보다는 서버가 현재 상태를 기반으로 판단하는 것이 더 자연스럽다고 느꼈습니다.

다만 REST 관점에서는 하나의 API가 두 가지 동작(예약 생성, 예약 대기 생성)을 수행하는 구조가 어색할 수도 있을 것 같아 고민입니다.    
반대로 API를 분리하면 책임은 명확하지만, 예약 요청 → 실패 응답 → 예약 대기 요청처럼 하나의 흐름에서 API 요청이 두 번 발생하게 됩니다.

이 상황에서,
```text
- 클라이언트가 흐름을 분기하는 방식
- 서버가 상태에 따라 처리하는 방식 
```
중 어떤 설계를 더 선호하시는지와, 그 판단 기준이 궁금합니다.

---

### 2. 예약 확정과 예약 대기를 분리해야 할까?
```text
1) 하나의 Reservation 테이블만 사용  
2) Reservation / Waiting 테이블 분리   
3) ReservationSlot을 만들고, 모든 예약 요청을 Reservation에 저장
```

1)의 경우 theme, time, date 같은 데이터가 많이 중복될 것 같다고 느꼈습니다.   
2)의 경우 의미는 명확하지만, 예약 취소 시 Waiting → Reservation으로 데이터를 이동시켜야 하거나,    
두 테이블 간 상태가 어긋날 오류(ex. reservation에는 예약이 없는데 waiting에는 있는 경우)가 발생할 수 있어 데이터 동기화 측면에서 로직이 복잡해질 것 같았습니다.

그래서 저는 3번 방식을 선택했습니다.
```text
ReservationSlot: 예약 가능한 슬롯 자체
Reservation: 해당 슬롯에 대한 사용자의 예약 요청
```
으로 보고,
theme + date + time 조합을 ReservationSlot으로 관리하고 있습니다.

또한 ReservationSlot에는 unique 제약을 두어 동일 슬롯이 중복 생성되지 않도록 했습니다.   
현재는 예약 확정과 예약 대기를 별도 테이블이나 상태로 구분하지 않고, Reservation 내부 순서를 기준으로 예약 확정 여부와 대기 순번을 계산하고 있습니다.

이처럼 예약 확정과 예약 대기를 같은 엔티티로 관리하는 방식이 자연스러운지, 혹은 Waiting을 별도 엔티티로 분리하는 것이 더 적절한지 의견이 궁금합니다.

---

### 3. 예약 상태를 어떻게 관리할까?   
   현재는 `RESERVED`, `CANCELED` 두 상태만 사용하고 있습니다.   
   예약 확정 여부는:
- 가장 먼저 생성된 RESERVED → 예약 확정
- 이후 RESERVED → 예약 대기
  로 해석하고 있습니다.

처음에는 예약 확정과 예약 대기 상태를 구분하기 위해 `WAITING` 상태를 따로 둘지도 고민했지만,    
예약 취소 시 다음 대기자를 WAITING → RESERVED로 승격시키는 과정에서 상태 전이 로직과 정합성 문제가 생길 수 있다고 생각했습니다.

반면 현재 방식은 상태 전이가 없어 단순하지만,
매번 조회 시, 매번 조회 시 예약 순서를 계산해야 한다는 단점이 있습니다.

매트는
```text
1) WAITING 상태를 명시적으로 두는 방식
2) 현재처럼 조회 시 계산하는 방식
```
  중 어떤 설계를 더 선호하시는지 궁금합니다.
  
-----

안녕하세요 매트 🙂
정성스러운 리뷰 너무 감사합니다.
달아주신 리뷰에 대해 저의 생각을 커멘트로 달아두었습니다!
잘 부탁드립니다:)

### 변경사항 (260531)
- [x] test: 컨트롤러 테스트의 Context 초기화 범위를 메서드 단위로 변경
- @DirtiesContext를 클래스 레벨에서 메서드 레벨로 변경
- [x] 서비스 테스트에 @Transactional 적용
- 테스트 종료 후 자동 롤백되도록 수정
- @DirtiesContext 제거
- [x] Validation 별로 에러 메세지 띄우도록 수정
- [x] 자바 컨벤션에 따른 수정
- 불필요한 공백 제거
- 코드 포맷팅 적용
- [x] Reservation에 대한 단위 테스트
- [x] Optional.get() 제거 및 orElseGet 적용

### 질문     
추가 질문은 커멘트에 작성해두었습니다!     

1. 도메인 객체의 equals()와 hashCode() 기준     
https://github.com/woowacourse/spring-roomescape-waiting/pull/357#discussion_r3329806804

2. Reservation 단위 테스트     
   https://github.com/woowacourse/spring-roomescape-waiting/pull/357#discussion_r3329860389

3. DAO에서 Optional 처리     
https://github.com/woowacourse/spring-roomescape-waiting/pull/357#discussion_r3329888651

4. Repository와 DAO, Entity와 Domain     
https://github.com/woowacourse/spring-roomescape-waiting/pull/357#discussion_r3329982595

------

안녕하세요 매트!
생각해볼 만한 리뷰 주셔서 감사합니다. 
아직 학습중이라 헷갈리는 개념들도 있지만, 리뷰 주신 내용을 토대로 제가 생각하는 바를 적어봤습니다.
혹시 잘못 생각하고 있는 점이 있다면 소중한 피드백 부탁드립니다. 🙇🏻‍♀️

추가로 현재 Service - DAO 구조를 사용하고 있는데, 리뷰 주신 내용을 토대로 생각해보니,
레벨 1에서 했던 대로 도메인에 더 책임을 부여해서 Domain - Repository로 바꾸고 싶다는 생각이 들었습니다.
사이클 2를 보니 예약 대기에 대한 자동 전환 + 순번 재정렬 기능이 이미 구현이 되어있어서, 사이클 2 때 위의 변경사항을 시도해보려고 하는데 괜찮을까요?

리뷰 잘 부탁드립니다!

### 변경사항 (260603)
- [x] GlobalExceptionHandler에 Exception 처리 로직 추가
- MethodArgumentTypeMismatchException 예외 처리 추가
- HttpMessageNotReadableException 예외 처리 추가

-----

# 사이클 2

안녕하세요 매트.
기존 사이클 1의 코드 구조로는 예약 자동 승인이 이미 구현이 되어있어서, 이번 사이클 2에서는 다른 설계를 시도해보았습니다.
```text
#변경사항
1. DAO 대신 Respository 인터페이스를 사용
2. SQL 쿼리와 서비스에서 수행하던 일부 책임을 도메인으로 이동
3. ReservationSlot을 Aggregate Root로 사용(ReservationSlot을 통해서만 Reservation 추가 및 수정 가능)
4. 기존 : Reserved만 사용 → 변경 : Reserved와 Waiting을 구분
```

이번 사이클의 목표는 크게 두 가지였습니다.

1. 비즈니스 규칙을 Service나 SQL이 아닌 도메인으로 옮겨보기 (레벨 1처럼)
2. Reserved와 Waiting을 구분하는 구조를 직접 구현해보며 장단점을 경험해보기

사이클 1에서 ReservationSlot과 Reservation에 대해 객체 생명주기 관점에서 리뷰를 주신 내용을 적용해보고 싶어서,
ReservationSlot이 aggregate root가 되어 Reservation을 관리하는 구조를 사용해보았습니다.
(기존 구조보다 aggregate root를 사용하는 것이 코드 복잡성이 커진다고 생각하지만, 장단점을 체감해보고 싶어서 시도해보았습니다!)

사이클 1에서는 예약 확정과 예약 대기를 모두 Reserved 상태로 저장한 뒤, 순서로만 구분했습니다.
1번째 예약 -> 예약 확정
2번째 예약 -> 대기 1번
3번째 예약 -> 대기 2번
이 방식에서는 ReservationSlot을 거치지 않고 Reservation을 직접 추가하더라도 데이터 정합성이 크게 깨지지 않았습니다.

반면 이번에는 Reserved와 Waiting을 별도의 상태로 구분하면서,
첫 번째 예약은 반드시 Reserved여야 한다.
Waiting은 Reserved 뒤에만 존재할 수 있다.
수정 및 취소 시 Waiting -> Reserved 승격이 발생한다.
와 같은 규칙들이 생겼고, 이러한 규칙을 보장하기 위해 ReservationSlot을 Aggregate Root로 사용해볼만 하다고 생각했습니다.

또한 레벨 1에서 고민했던 객체 간 관계와 책임을 다시 한번 적용해보고 싶다는 의도도 있었습니다.

변경된 구조로 제가 느낀 점은 아래와 같습니다.
장점 :
1. Reservation은 ReservationSlot을 통해서만 생성/수정될 수 있기 때문에,
`Reservation이 존재하려면 ReservationSlot이 먼저 존재해야 한다.`와 같은 규칙을 도메인에서 표현할 수 있었습니다.
2. 기존에 SQL이나 Service가 수행하던 일부 비즈니스 로직을 도메인 객체 내부로 이동시킬 수 있었습니다.
3. Waiting → Reserved 승격 규칙을 ReservationSlot 내부에서 관리할 수 있어 관련 책임이 한 곳으로 모였습니다.

단점 : 
1. ReservationSlot을 aggreagate root로 사용하면서, 객체를 저장하고 수정하는 과정이 이전보다 복잡해졌습니다.
2. Reserved와 Waiting을 분리하면서 자동 승격 로직, 취소 로직, 순서 관리 로직 등이 추가되어 전체 구현 난이도가 많이 올라갔습니다.

이번에는 불편함과 복잡함을 많이 경험해보려고 구조를 많이 변경했는데, 그러다보니 코드가 아직 부족한 점이 많은 것 같습니다. 😅
리뷰 잘 부탁드립니다. 🙂

### 변경 사항 (260608)
- [x] refactor: 예약 관련 기능 리팩토링
- DAO에서 Respository 사용하는 구조로 변경
- ReservationSlot을 aggregate root로 사용
- [x] 응답 형식에 name 필드 추가

### 질문 사항
1. 구조에 대한 피드백
Repository를 사용하면서 DAO의 SQL이나 Service에 있던 일부 책임을 도메인으로 이동시켜 보았습니다.
그러다 보니 "어떤 로직은 SQL에서 수행하고, 어떤 로직은 도메인에서 수행해야 하는가?"에 대한 고민이 생겼습니다.
현재 Reservation 관련 규칙은 최대한 도메인으로 이동하려고 했지만, 아래와 같은 기능은 여전히 SQL에서 수행하고 있습니다.
```text
예약 가능한 시간 조회
인기 있는 테마 조회
```
이 기능들은 집계가 중심이기 때문에 DB가 계산하는 것이 더 자연스럽다고 판단했습니다.

반면 아래와 같은 규칙은 도메인에서 처리하고 있습니다.
```text
첫 번째 예약은 Reserved여야 한다.
Waiting 취소 시 다음 Waiting을 승격한다.
같은 사용자는 동일 슬롯에 중복 예약할 수 없다.
```
현재는 "복잡한 집계는 SQL에서, 비즈니스 규칙은 도메인에서" 정도로 생각하고 있는데, 이 기준이 적절한지 궁금합니다.
매트는 `도메인에서 계산할 것인가?`, `SQL에서 계산할 것인가?`를 판단하시는 기준이 있으신지 궁금합니다!

또한, 현재 구조에서 Reservation, ReservationSlot, ReservationService가 각각 책임 분리가 제대로 되어있는지, 
ReservationSlot이 aggregate로 적절하게 사용되고 있는건지 피드백 받고 싶습니다.

2. Aggregate Root와 동시성
ReservationSlot을 Aggregate Root로 사용하면 Reservation과 ReservationSlot을 각각 관리하는 대신 ReservationSlot 하나를 중심으로 상태를 관리할 수 있다는 장점이 있습니다.
그래서, 예약 생성, 취소, Waiting 승격 등의 작업이 모두 ReservationSlot을 통해 수행되도록 변경했습니다.

여기서 궁금한 점은, Aggregate Root를 사용하면 실제로 동시성 문제를 다루기가 상대적으로 쉬워진다고 볼 수 있는지 궁금합니다.

제가 이해한 바로는
```text
기존:
Reservation + ReservationSlot 둘 다 고려

현재:
ReservationSlot 하나를 기준으로 관리
```
정도로 생각하고 있는데, 이 이해가 맞는지 궁금합니다!
이처럼 Aggregate Root를 사용하면 일관성을 관리해야 하는 범위가 더 좁고 명확해지는데, 실제로도 동시성 문제를 다루는 데 도움이 된다고 볼 수 있을까요?

또한, 동시성 처리를 위해 락을 적용해보려고 했으나, 아직 락에 대한 이해가 부족해 구현해해보지는 못했습니다.
현재 구조에서는 ReservationSlot을 기준으로 락을 걸면 될 것 같다고 생각했는데, 이러한 접근이 적절한지 궁금합니다.

추가로 현재 구조에서 가장 단순하게 시도해볼 수 있는 동시성 제어 방식이나 테스트 방식이 있다면 조언 부탁드립니다! (특히 테스트가 어렵습니다. 🥲)

-----

안녕하세요 매트.     
여러가지로 고민하다보니, 리뷰 요청이 늦어졌네요. 🥲     
리뷰 주신 내용을 바탕으로 동시성 처리 로직을 추가하고, CountDownLatch를 이용해서 테스트해봤습니다.    
(CountDownLatch는 처음 접하다보니, AI로 테스트를 작성하고 이해하는 방식으로 진행했습니다.)    
또한, 저의 생각을 정리해서 코멘트에 적어두었는데, 혹시 잘못 생각한 점이 있다면 코멘트 부탁드립니다!   
리뷰 잘 부탁드립니다!

### 변경 사항 (260611)

- [x] 예약 저장 관련 동시성 처리 추가
- [x] 예약 수정 및 취소 동시성 처리 추가
- [x] CountDownLatch를 이용한 동시성 테스트 추가 
- [x] 계층별 long/Long 타입 사용 기준 적용

### 질문
1. 모니터링
https://github.com/woowacourse/spring-roomescape-waiting/pull/510#discussion_r3393603770

------

# 선택 미션
### 1단계 - JPA 전환
- [x] build.gradle 설정에 jpa로 수정
- [x] Time과 Theme의 영속성 관리를 JPA로 전환
- [x] ReservationSlot과 Reservation의 영속성 관리를 JPA로 전환
- [x] Reservation과 ReservationSlot을 양방향 매핑으로 수정