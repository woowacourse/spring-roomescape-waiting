# 학습 로그 #15

**시간**: 05/20 (약 __ 분)
**학습 범위**: Filter vs Interceptor, OncePerRequestFilter

## 1. 막힌 것의 종류

- [ ] 개념 자체를 모르겠다
- [x] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [ ] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

크루와 토의 중 "공식문서에서 Interceptor를 인증/인가에 추천하지 않는다"는 말을 듣고 탐구 시작.
Interceptor의 실행 위치에서 문제를 유추하고, 그 이전에 실행되는 키워드(Filter)를 스스로 떠올렸다.

## 2. 이번 타임의 학습 전략

### 학습 과정

#### 실행 순서

```
HTTP 요청 → Tomcat (HttpServletRequest 변환) → Filter → DispatcherServlet → Interceptor → Controller
```

Interceptor는 DispatcherServlet **내부**에서 실행된다.

---

#### Interceptor의 한계

내부 포워딩(RequestDispatcher.forward) 시 Interceptor가 재실행되지 않는다.

```
/public 요청
  → Interceptor ✅ 실행 (통과)
  → Controller 내부에서 forward("/admin/reservations")
  → Interceptor ❌ 실행 안 됨 → AdminController 인증 없이 실행됨
```

---

#### Filter의 DispatcherType

Filter는 실행 시점을 `DispatcherType`으로 설정할 수 있다:
- `REQUEST` (기본값): 일반 HTTP 요청
- `FORWARD`: 내부 포워딩 시에도 실행

→ Interceptor에서 불가능했던 포워딩 인증 체크가 Filter에서는 가능하다.

---

#### OncePerRequestFilter

`DispatcherType.FORWARD`를 추가하면 포워딩 시에도 Filter가 실행되지만, 원래 요청 + 포워딩 두 번 실행될 수 있다.
`OncePerRequestFilter`는 포워딩이 몇 번 일어나도 해당 요청에서 Filter를 **한 번만** 실행하도록 보장한다.

→ Spring Security의 내부 Filter들도 모두 `OncePerRequestFilter`를 상속한다.

## 3. 전략 평가

- 효과적이었던 것: 크루 토의에서 나온 키워드를 스스로 코드와 연결했다. 포워딩이라는 개념을 시나리오로 이해했다.
- 아쉬운 것: 딱히 없었음.

## 4. AI 피드백

중간에 의문 두 개가 생겼을 때 바로 파고들지 않고 다음 키워드로 남긴 것이 좋았다. 사이클을 유지한 것이다. 의문이 생기면 바로 해결하려는 충동을 참고 키워드로 남기는 습관이 체득되고 있다.

## 5. 다음 타임에 바꿀 것

- 유지할 것: 중간 의문을 키워드로 남기는 방식
- 바꿀 것: 딱히 없음

## 다음 사이클 키워드

```
✅ OncePerRequestFilter로 인증 필터 구현
   → 맥락: Interceptor의 포워딩 한계를 Filter로 해결
   → 종류: 코드 적용

✅ Interceptor 구현이 AOP 기반인가?
   → 맥락: AOP(프록시)와 Interceptor(DispatcherServlet 내부 호출)의 차이가 궁금함
   → 종류: 흐름 파악

✅ OncePerRequestFilter에서 REQUEST와 FORWARD 모두 처리하려면?
   → 맥락: 들어올 때도, 포워딩할 때도 Filter를 실행하고 싶을 때 어떻게 설정하는가
   → 종류: 코드 적용
```
