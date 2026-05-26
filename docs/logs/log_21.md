# 학습 로그 #21

**학습 범위**: PR 리뷰 반영, Filter와 DispatcherServlet 경계, 역직렬화 시점

## 1. 막힌 것의 종류

- [ ] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [x] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

Filter에서 ObjectMapper를 직접 쓰는 게 맞는 방식인지, JacksonMapper 선택이 안 되는 게 아닌지 의문이 생겼다.

## 2. 이번 타임의 학습 내용

### PR 리뷰 반영

| 항목 | 수정 내용 |
|---|---|
| `RoleCheckFilter` | `parseMemberId` null 반환 시 401 처리 추가 |
| `SessionRegistry.sessionDestroyed` | 무효화된 세션의 `getAttribute` 호출 시 `IllegalStateException` 방어 처리 |
| `ExceptionHttpStatusMapper` | `getClass()` 대신 `isAssignableFrom`으로 서브클래스 매핑 지원 |
| `SessionUtils` | 실제로 사용되지 않는 String 브랜치 및 `NumberFormatException` catch 제거 |

#### sessionDestroyed의 IllegalStateException 발생 경로

```
attributeAdded()
  └─ existing.invalidate()
       └─ Tomcat이 리스너 순회 (동기, 같은 스레드)
            └─ SessionRegistry.sessionDestroyed()
                 └─ session.getAttribute()  ← 세션이 이미 무효화 중이면 IllegalStateException
```

- Tomcat은 리스너 내부 예외를 **삼키고** 호출자에게 전파하지 않음
- 따라서 `attributeAdded`의 try-catch는 `sessionDestroyed` 내부 예외를 못 잡음
- `sessionDestroyed` 안에서 직접 try-catch 필요

### Filter와 DispatcherServlet 경계

- Filter는 DispatcherServlet **앞단**에서 동작
- DispatcherServlet 내부의 `HttpMessageConverter` 선택 메커니즘이 동작하지 않음
- Filter에서 에러 응답을 만들려면 `ObjectMapper`를 직접 주입받아 직접 직렬화해야 함
- `response.setContentType()`도 직접 지정해야 하는 이유도 같음

### 역직렬화 시점

- `HttpServletRequest`는 raw 바이트 스트림(`InputStream`)을 들고 있음 — Java 객체 아님
- 실제 역직렬화는 `ArgumentResolver` 단계에서 일어남
  - `@RequestBody` 확인 → `MappingJackson2HttpMessageConverter` 선택 → `InputStream` 읽어서 Java 객체로 변환
- `HttpServletRequest` 생성 시점에 역직렬화가 일어나지 않는 이유: 어떤 타입으로 변환할지 알 수 없음

## 3. 전략 평가

- 효과적이었던 것: "같은 요청을 다른 타입으로 받는다면 어떤 타입으로 만들어야 하는가?" 반례로 HttpServletRequest의 역할을 빠르게 수정함
- 아쉬운 것: sessionDestroyed IllegalStateException 발생 경로를 처음에 동시성 문제로만 생각했고, Tomcat이 리스너 예외를 삼킨다는 점을 바로 떠올리지 못함

## 4. 이전 "바꿀 것" 적용 여부

- 바꿀 것: 패턴 이름과 구조를 연결해서 기억하기
- 적용: 이번 타임에서 패턴 이름을 떠올릴 상황이 없었음 — 미적용

## 5. 다음 타임에 바꿀 것

- 유지할 것: 반례로 오개념을 빠르게 수정하는 방식
- 바꿀 것: 패턴 이름과 구조 연결 — 계속 미뤄지고 있음. 다음 타임에 디자인 패턴 키워드를 진행할 때 반드시 적용

## 다음 사이클 키워드

```
✅ Filter와 DispatcherServlet 경계 — ObjectMapper 직접 사용 이유
   → 맥락: Filter에서 JacksonMapper 선택이 왜 안 되는지 이해
   → 종류: 개념 이해 (완료)

✅ HttpServletRequest 역직렬화 시점
   → 맥락: InputStream을 들고 있고, 역직렬화는 ArgumentResolver에서 발생
   → 종류: 흐름 파악 (완료)
```
