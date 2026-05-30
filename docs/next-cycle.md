# 다음 사이클 키워드

> 완료된 항목은 ~~취소선~~, 진행 중은 🔄, 미완료는 그대로

---

## 코드 적용

- [x] ~~OncePerRequestFilter로 인증 필터 구현~~
  → AuthFilter, AdminFilter, ManagerFilter 구현 완료. ProblemDetail JSON 직접 직렬화 방식 적용.
  → 출처: log_14, log_15

- [x] ~~OncePerRequestFilter에서 REQUEST와 FORWARD 모두 처리하려면?~~
  → JSP/정적 리소스 forward 시 Interceptor 미동작. Spring Security도 Filter 기반인 이유. 이 프로젝트는 REST API라 forward 없어서 실질적 차이 없음. 인증/인가는 Filter가 적절한 위치.
  → 출처: log_15

---

## 개념 이해 (Filter/직렬화)

- [x] ~~Filter에서 ObjectMapper를 직접 쓰는 이유~~
  → Filter는 DispatcherServlet 앞단. HttpMessageConverter 선택 메커니즘 동작 안 함. ObjectMapper 직접 주입해서 직렬화.
  → 출처: log_21

- [x] ~~HttpServletRequest 역직렬화 시점~~
  → HttpServletRequest는 raw InputStream. 역직렬화는 ArgumentResolver 단계에서 발생.
  → 출처: log_21

---

## 흐름 파악

- [x] ~~Interceptor 구현이 AOP 기반인가?~~
  → AOP는 프록시 기반. Interceptor는 DispatcherServlet이 HandlerExecutionChain에서 직접 호출. 프록시 없음.
  → 출처: log_15

---

## 실전 판단

- [x] ~~도메인 예외와 HTTP 예외 분리 후 실제 프로젝트 적용 회고~~
  → 의미는 명확해짐. 단, ExceptionHttpStatusMapper가 관리 포인트가 됨 — 매핑 누락 시 조용히 500 반환. isAssignableFrom으로 서브클래스 커버는 했으나 누락 자체를 막는 방법은 없음.
  → 출처: log_14

---

## 개념 이해

- [x] ~~JWT 구조와 동작 방식~~
  → 헤더.페이로드.서명 구조. 복호화 아닌 재계산 후 비교로 검증. DB 조회 없음. 스케일 아웃에 유리. 로그아웃/동시 로그인 방지는 서버 저장 필요.
  → 출처: log_16, log_22

- [ ] **JWT 코드 적용**
  → 맥락: 개념 이해 완료. Spring에서 JWT 발급/검증 구현
  → 종류: 코드 적용

- [ ] **JWT Refresh Token 전략**
  → 맥락: Access Token 만료 처리, Refresh Token으로 갱신하는 흐름
  → 종류: 흐름 파악

---

## 흐름 파악 (DB 락)

- [ ] **B-Tree에서 갭락이 물리적으로 어떻게 구현되는가**
  → 맥락: 다음 키 삽입 공간을 어떻게 막는지 — 인덱스 구조 레벨에서
  → 출처: log_17

---

## 실전 판단 (DB 락)

- [ ] **SELECT ... FOR SHARE가 필요한 시나리오**
  → 맥락: 읽은 값 기반 계산 중 다른 트랜잭션의 수정을 막아야 하는 경우
  → 출처: log_17

---

## 흐름 파악 (Session)

- [x] ~~EventListener는 어떻게 작동하는가~~
  → 옵저버 패턴. Tomcat이 리스너 목록을 순회해 직접 호출. 같은 스레드에서 동기 실행.
  → 출처: log_18, log_20

- [x] ~~Session 작동 흐름~~
  → JSESSIONID 쿠키로 Tomcat이 세션 매핑. getSession(false/true) 차이 학습.
  → 출처: log_18, log_19

---

## 흐름 파악 (JVM)

- [ ] **Java GC 동작 방식 심화**
  → 맥락: GC 루트란 무엇인가, Minor GC / Major GC 차이
  → 출처: log_20

---

## 개념 이해 (디자인 패턴)

- [ ] **옵저버, 프록시, 전략 패턴 구조 비교**
  → 맥락: 옵저버 패턴 이름이 바로 안 나옴. 자주 쓰는 패턴 3개 구조로 비교
  → 출처: log_20

---

## 개념 이해 (객체지향 설계)

- [ ] **composition vs association vs aggregation (has-a의 종류)**
  → 맥락: Waiting은 Reservation을 소유하진 않지만 Slot은 가진다. 둘 다 "가진다"인데 무슨 기준으로 갈리나
  → 출처: log_23

---

## 코드 적용 (도메인 리팩토링)

- [ ] **WaitingService에 퍼진 규칙을 도메인으로**
  → 맥락: "이미 대기 신청한 슬롯"을 Slot.equals로? "동일 사용자 예약"을 Waiting 생성 규칙으로?
  → 출처: log_23

- [ ] **DTO 변환을 Slot 단위로**
  → 맥락: getter 위임은 1단계. DTO가 slot을 직접 받도록 점진 개선해 외부 노출 줄이기
  → 출처: log_23
