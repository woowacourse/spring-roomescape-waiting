# 학습 로그 #19

**학습 범위**: Session 전체 흐름 (JSESSIONID, Tomcat 관리, getSession 옵션)

## 1. 막힌 것의 종류

- [x] 개념 자체를 모르겠다
- [ ] 개념은 알겠는데 코드로 어떻게 쓰는지 모르겠다
- [ ] 코드는 돌아가는데 이게 맞는 건지 모르겠다
- [ ] 기타: ___

Tomcat이 요청에 세션을 어떻게 매핑하는지 — IP/Port 기반이라고 추측했다가 반례로 수정.

## 2. 이번 타임의 학습 내용

### Session 전체 흐름

```
1. 최초 로그인 요청
   → Tomcat이 세션 생성 + 고유한 JSESSIONID 발급
   → 응답: Set-Cookie: JSESSIONID=abc123

2. 이후 모든 요청
   → 브라우저가 자동으로 Cookie: JSESSIONID=abc123 첨부
   → Tomcat이 JSESSIONID로 세션 풀에서 매핑
   → HttpServletRequest에 해당 세션 바인딩
```

- Session 전체 정보는 Tomcat이 관리
- 하나의 요청에는 하나의 세션만 담김
- 세션 식별자는 헤더가 아닌 **쿠키**에 담김 (JSESSIONID)

### getSession(true) vs getSession(false)

| 옵션 | 세션 있을 때 | 세션 없을 때 |
|---|---|---|
| `true` (기본값) | 기존 세션 반환 | 새 세션 생성 |
| `false` | 기존 세션 반환 | null 반환 |

- 로그인: `getSession()` → 세션을 만들어야 하므로 true
- 인터셉터: `getSession(false)` → 인증 여부 확인만 하므로 false
  - true를 쓰면 인증되지 않은 요청마다 빈 세션이 불필요하게 쌓임

### 로그인한 사용자 목록 관리

- Request에 세션이 하나뿐이라 memberId → 세션 역방향 조회 불가
- HttpSessionListener + HttpSessionAttributeListener로 Servlet 이벤트를 감지해서 별도 관리

## 3. 전략 평가

- 효과적이었던 것: "같은 IP인데 두 탭이 다른 계정으로 로그인한 경우" 반례가 IP/Port 오개념을 빠르게 깼다
- 아쉬운 것: 없음 — 추측 → 반례 → 수정 흐름이 자연스러웠다

## 4. 이전 "바꿀 것" 적용 여부

- 바꿀 것: 오개념처럼 보일 때 AI가 반례 시나리오를 먼저 제시
- 적용: IP/Port 추측에서 두 탭 반례를 바로 제시했고 빠르게 수정됐다. ✅

## 5. 다음 타임에 바꿀 것

- 유지할 것: 반례 시나리오로 오개념 빠르게 수정하는 방식
- 바꿀 것: 정리할 때 "코드와 개념을 연결해서" 설명하기 — 오늘 흐름 설명에서 코드 연결이 조금 약했다

## 다음 사이클 키워드

```
✅ EventListener는 어떻게 작동하는가
   → 맥락: @WebListener가 이벤트를 받을 때 계속 대기(blocking)하고 있는 건가,
            아니면 다른 메커니즘인가
   → 종류: 흐름 파악 (구현은 했으니 내부 동작 방식으로)
```
