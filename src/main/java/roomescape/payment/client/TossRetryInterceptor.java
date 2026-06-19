package roomescape.payment.client;

import java.io.IOException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

class TossRetryInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TossRetryInterceptor.class);
    private static final int TOO_MANY_REQUESTS = 429;

    private final int maxAttempts;
    private final Duration fallbackWait;
    private final Sleeper sleeper;

    TossRetryInterceptor(int maxAttempts, Duration fallbackWait, Sleeper sleeper) {
        this.maxAttempts = maxAttempts;
        this.fallbackWait = fallbackWait;
        this.sleeper = sleeper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        int attempt = 0;
        while (true) {
            attempt++;
            ClientHttpResponse response = execution.execute(request, body);
            if (response.getStatusCode().value() != TOO_MANY_REQUESTS) {
                return response;
            }
            if (attempt >= maxAttempts) {
                response.close();
                throw new TossRateLimitedException();
            }
            Duration wait = parseRetryAfter(response.getHeaders().getFirst(HttpHeaders.RETRY_AFTER));
            log.warn("Toss API 429 수신 (시도 {}/{}), {}초 후 재시도: {}",
                    attempt, maxAttempts, wait.getSeconds(), request.getURI());
            response.close();
            sleeper.sleep(wait);
        }
    }

    private Duration parseRetryAfter(String header) {
        if (header == null) {
            return fallbackWait;
        }
        try {
            return Duration.ofSeconds(Long.parseLong(header.trim()));
        } catch (NumberFormatException e) {
            return fallbackWait;
        }
    }
}