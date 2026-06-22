# Step 2 — ErrorCode 추가 정리

---

## 무엇을 만들었는가

Rate Limit 관련 에러 코드 3개를 `ErrorCode` enum에 추가했다.

```
src/main/java/roomescape/global/exception/ErrorCode.java
```

추가된 코드:

```java
RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
OUTBOUND_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "외부 API 호출 한도를 초과했습니다."),
TOSS_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "결제 서버의 요청 한도를 초과했습니다. 잠시 후 다시 시도해 주세요.");
```

---

## 세 코드가 각각 어떤 상황에서 쓰이는가

| ErrorCode | 방향 | 발생 위치 | 의미 |
|-----------|------|----------|------|
| `RATE_LIMIT_EXCEEDED` | 들어오는 요청 | `RateLimitInterceptor` | 우리 서버가 클라이언트 요청을 429로 거부 |
| `OUTBOUND_RATE_LIMIT_EXCEEDED` | 나가는 호출 | `OutboundRateLimitInterceptor` | 우리가 토스 호출 전에 스스로 차단 |
| `TOSS_RATE_LIMIT_EXCEEDED` | 나가는 호출 응답 | `RetryAfterInterceptor` | 토스가 우리에게 429 반환, 재시도 소진 |

```
클라이언트 → [RateLimitInterceptor] → 우리 서버 → [OutboundRateLimitInterceptor] → 토스
                    ↑                                            ↑                      ↓
          RATE_LIMIT_EXCEEDED                    OUTBOUND_RATE_LIMIT_EXCEEDED    429 응답
                                                                          ↓
                                                              [RetryAfterInterceptor]
                                                                          ↓ 재시도 소진
                                                              TOSS_RATE_LIMIT_EXCEEDED
```

---

## 왜 HTTP 상태 코드 429인가

HTTP 표준(RFC 6585)에서 정의한 "Too Many Requests"다.
클라이언트가 "요청을 거부당한 게 아니라, 너무 많이 보냈다"는 신호다.

```
400 Bad Request    — 요청 자체가 잘못됨
401 Unauthorized   — 인증 없음
403 Forbidden      — 권한 없음
404 Not Found      — 리소스 없음
429 Too Many Requests — 요청 횟수 초과  ← 이번 단계
503 Service Unavailable — 서버 일시 불가
```

429와 함께 `Retry-After` 헤더를 보내는 게 관례다.
"N초 후에 다시 시도해라"는 힌트를 클라이언트에게 준다.

---

## 기존 에러 코드와 비교해서 보는 설계 패턴

```java
// 같은 HTTP 429지만 ErrorCode를 나눈 이유
RATE_LIMIT_EXCEEDED          // 우리가 클라이언트에게
OUTBOUND_RATE_LIMIT_EXCEEDED // 우리가 스스로 차단
TOSS_RATE_LIMIT_EXCEEDED     // 토스가 우리에게
```

세 상황 모두 HTTP 상태 코드는 429지만, **발생 원인과 처리 방법이 다르다.**

- `RATE_LIMIT_EXCEEDED` → 클라이언트에게 429 + Retry-After 응답
- `OUTBOUND_RATE_LIMIT_EXCEEDED` → 우리 서버 내부에서 처리 (GlobalExceptionHandler → 클라이언트에게 503 or 429)
- `TOSS_RATE_LIMIT_EXCEEDED` → 재시도 소진 후 클라이언트에게 429 응답

에러 코드를 세분화하면:
1. 로그에서 "왜 실패했는지" 구분할 수 있다
2. 에러 코드별로 모니터링 알람을 다르게 설정할 수 있다
3. 클라이언트에게 다른 메시지를 보낼 수 있다

---

## 공부해볼 개념들

### 1. HTTP 상태 코드 체계

| 범위 | 의미 | 예시 |
|------|------|------|
| 1xx | 정보 | 100 Continue |
| 2xx | 성공 | 200 OK, 201 Created, 202 Accepted |
| 3xx | 리다이렉트 | 301 Moved, 302 Found |
| 4xx | 클라이언트 오류 | 400 Bad Request, 401, 403, 404, 429 |
| 5xx | 서버 오류 | 500 Internal Server Error, 503 Service Unavailable |

**429와 503의 차이:**
- 429: 요청을 너무 많이 보냈다 (클라이언트 책임)
- 503: 서버가 현재 처리 불가 (서버 책임)

Rate Limit 초과는 클라이언트가 너무 많이 보낸 것이므로 429가 맞다.

### 2. Retry-After 헤더

RFC 7231에서 정의한 표준 HTTP 헤더다.
429, 503 응답에 함께 내려보내며, 클라이언트에게 언제 재시도할지 알려준다.

```
HTTP/1.1 429 Too Many Requests
Retry-After: 3          ← 3초 후 재시도
Content-Type: application/json

{ "message": "요청이 너무 많습니다." }
```

값 형식:
- 정수(초): `Retry-After: 30` → 30초 후 재시도
- 날짜: `Retry-After: Wed, 21 Oct 2025 07:28:00 GMT` → 특정 시각 이후 재시도

### 3. 에러 코드를 enum으로 관리하는 이유

에러 코드를 흩어진 문자열로 관리하면:

```java
// 나쁜 예: 에러 코드가 흩어짐
throw new RuntimeException("요청이 너무 많습니다.");
response.setStatus(429);
```

문자열이 여러 곳에 복사되면 오타가 생기고, 변경 시 모든 곳을 찾아야 한다.

enum으로 관리하면:
```java
// 좋은 예: 한 곳에서 관리
RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다.")

// 사용처
throw new CustomException(ErrorCode.RATE_LIMIT_EXCEEDED);
// → HTTP 상태, 메시지, 로그 키 모두 한 곳에서 결정됨
```

- 컴파일 타임에 오타를 잡을 수 있다
- HTTP 상태 코드와 메시지를 에러 코드와 함께 선언해 일관성을 보장한다
- `GlobalExceptionHandler`가 `CustomException` 하나만 처리하면 된다
