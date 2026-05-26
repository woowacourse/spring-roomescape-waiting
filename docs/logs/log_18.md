# 학습 로그 #18

**학습 범위**: 동시 로그인 방지, Session 관리 구조, JWT 무효화 한계

## 1. 막힌 것의 종류

- [ ] 개념 자체를 모르겠다
- [x] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [ ] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

"같은 계정으로 여러 기기 동시 로그인 방지"라는 요구사항에서 출발.  
처음엔 세션에 memberId가 있으니 확인할 수 있다고 생각했지만, 역방향 조회가 불가능하다는 걸 발견.

## 2. 이번 타임의 학습 내용

### Session은 Tomcat이 관리한다

- 각 HTTP 요청에는 세션 하나만 담겨 있음
- Servlet API는 "현재 요청의 세션"에만 접근 가능
  - "특정 memberId의 세션을 조회"하는 표준 API는 없음
- 직접 조회하려면 Tomcat 내부 API 필요 → 특정 WAS에 종속

```
세션 → memberId  ✅ (요청에서 꺼낼 수 있음)
memberId → 세션  ❌ (역방향 조회 API 없음)
```

### HttpSessionListener / HttpSessionAttributeListener

- `HttpSessionListener.sessionDestroyed`: 세션 만료·무효화 시 발동
- `HttpSessionAttributeListener.attributeAdded`: 세션에 속성이 추가될 때 발동
  - `sessionCreated` 시점엔 memberId가 없어서 바로 등록 불가
  - `attributeAdded("memberId", ...)` 시점에 등록해야 함

### 동시 로그인 방지 구현

```
memberId → HttpSession 매핑 유지 (ConcurrentHashMap)

로그인 시:
  attributeAdded → 기존 세션 invalidate() → 새 세션 등록

세션 종료 시:
  sessionDestroyed → 레지스트리에서 제거
```

### @WebListener vs @Component

- `@Component`만으로는 Servlet 리스너로 등록되지 않음
- `@WebListener` + `@ServletComponentScan` 으로 서블릿 컨테이너에 직접 등록

### JWT에서 동시 로그인 방지가 어려운 이유

- JWT 검증은 서명 + 유효기간 확인만 함 (DB 조회 없음)
- 이미 발급된 토큰을 서버가 회수하거나 수정할 수 없음
- 블랙리스트 방식으로 무효화하면 → 결국 서버가 상태를 저장하게 됨
- stateless 장점과 운영 요구사항이 충돌

## 3. 전략 평가

- 효과적이었던 것: "왜 직접 뒤질 수 없어?"라는 질문이 Servlet API 한계를 정확히 짚게 해줬다
- 아쉬운 것: HttpSessionAttributeListener를 힌트 없이 도달하기 어려웠음. 이 부분은 지식이 없으면 추론 자체가 안 되는 영역

## 4. 이전 "바꿀 것" 적용 여부

- 바꿀 것: 오개념이 생겼을 때 더 빠르게 자기 점검하기
- 적용: JWT 무효화 질문에서 방향이 틀렸을 때 빠르게 수정했다. 절반 정도 적용됨.

## 5. 다음 타임에 바꿀 것

- 유지할 것: 코드에서 역방향으로 "왜?"를 추적하는 방식
- 바꿀 것: 오개념처럼 보일 때, AI가 그것이 깨지는 구체적인 반례 상황을 먼저 제시한다
  → "오개념인지 스스로 점검하라"는 말은 모순 — 모르기 때문에 오개념임
  → AI가 반례 시나리오를 주면 본인이 더 빠르게 스스로 수정 가능

## 다음 사이클 키워드

```
✅ EventListener는 어떻게 작동하는가
   → 맥락: @WebListener가 이벤트를 받을 때 계속 대기(blocking)하고 있는 건가,
            아니면 다른 메커니즘인가
   → 종류: 흐름 파악 (구현은 했으니 내부 동작 방식으로)

✅ Session 작동 흐름
   → 맥락: 쿠키에 JSESSIONID가 담기고 Tomcat이 이를 매핑하는 전체 흐름
   → 종류: 흐름 파악 (Session이 Tomcat에서 관리된다는 건 알았으니 전체 라이프사이클로)
```
