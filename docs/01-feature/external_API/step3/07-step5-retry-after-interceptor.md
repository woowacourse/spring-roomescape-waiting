# Step5 — RetryAfterInterceptor 구현 (토스 429 재시도)

---

## 결론 먼저

> `ClientHttpRequestInterceptor`로 RestClient에 등록.
> 토스가 429를 반환하면 `Retry-After` 헤더를 읽어 대기 후 재시도.
> `maxAttempts` 초과 시 `TOSS_RATE_LIMIT_EXCEEDED` 예외를 던진다.

---

## 구현

```java
@Component
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final TossRetryProperties properties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        for (int attempt = 1; attempt <= properties.maxAttempts(); attempt++) {
            ClientHttpResponse response = execution.execute(request, body);

            if (response.getStatusCode().value() != 429) {
                return response;
            }

            if (attempt == properties.maxAttempts()) {
                response.close();
                throw new CustomException(ErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
            }

            long waitSeconds = parseRetryAfter(response);
            response.close();
            sleep(waitSeconds);
        }

        throw new CustomException(ErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
    }

    private long parseRetryAfter(ClientHttpResponse response) {
        String header = response.getHeaders().getFirst("Retry-After");
        if (header == null) {
            return properties.fallbackWaitSeconds();
        }
        try {
            return Long.parseLong(header);
        } catch (NumberFormatException e) {
            return properties.fallbackWaitSeconds();
        }
    }

    private void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.TOSS_RATE_LIMIT_EXCEEDED);
        }
    }
}
```

---

## 핵심 결정사항

### 왜 @Retryable(step2)와 별도의 인터셉터를 두는가?

| 구분 | read timeout (@Retryable) | 429 (RetryAfterInterceptor) |
|------|--------------------------|------------------------------|
| 처리 여부 | 모름 (불확실) | 안 됨 (확정) |
| 재시도 즉시성 | backoff 후 즉시 | Retry-After 대기 후 |
| 멱등키 필요 | 필요 (이중 승인 방지) | 없어도 안전, 이미 등록돼 있음 |

두 메커니즘은 레이어와 트리거가 다르므로 분리해서 관리하는 것이 옳다.

### 왜 loop 안에서 `response.close()`를 하는가?

`ClientHttpResponse`는 기저의 HTTP 연결을 보유한다.
다음 재시도(`execution.execute`)를 위해 이전 응답의 스트림을 닫아야 연결 풀이 고갈되지 않는다.

### 왜 `fallbackWaitSeconds`가 필요한가?

토스 명세에 따르면 429 응답에 `Retry-After` 헤더가 항상 포함되지 않을 수 있다.
헤더가 없거나 파싱에 실패할 경우 기본 대기 시간(1초)을 사용한다.
`@ConfigurationProperties`로 외부화되어 배포 환경마다 조정이 가능하다.

### InterruptedException 처리 이유

`Thread.sleep()`은 `InterruptedException`을 던진다.
인터럽트가 발생했다는 것은 상위 컨텍스트(서버 종료, 스레드 풀 shutdown)가 이 스레드를 중단하려는 신호다.
`Thread.currentThread().interrupt()`로 인터럽트 상태를 복원한 뒤 예외를 던져 스레드가 정상 종료될 수 있도록 한다.

---

## 테스트 전략

### MockRestServiceServer 테스트

```java
// 1차: 429 + Retry-After: 1
// 2차: 200 정상 응답
mockServer.expect(requestTo(CONFIRM_URL))
    .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS).header("Retry-After", "1"));
mockServer.expect(requestTo(CONFIRM_URL))
    .andRespond(withSuccess(...));
// → 최종 성공 검증

// maxAttempts 초과 시 TOSS_RATE_LIMIT_EXCEEDED 예외 검증
```
