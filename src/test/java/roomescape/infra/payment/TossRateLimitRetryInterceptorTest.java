package roomescape.infra.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import roomescape.exception.server.PaymentRateLimitExceededException;

class TossRateLimitRetryInterceptorTest {

    @Test
    void 토스가_429를_반환하면_Retry_After만큼_대기한_후_재시도한다() throws IOException {
        List<Duration> sleeps = new ArrayList<>();
        TossRateLimitRetryInterceptor interceptor = new TossRateLimitRetryInterceptor(
                3,
                Duration.ofSeconds(1),
                sleeps::add
        );
        AtomicInteger attempts = new AtomicInteger();

        var response = interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.POST, "/v1/payments/confirm"),
                new byte[0],
                (request, body) -> {
                    if (attempts.incrementAndGet() == 1) {
                        MockClientHttpResponse tooManyRequests = new MockClientHttpResponse(new byte[0],
                                HttpStatus.TOO_MANY_REQUESTS);
                        tooManyRequests.getHeaders().add(HttpHeaders.RETRY_AFTER, "2");
                        return tooManyRequests;
                    }
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attempts.get()).isEqualTo(2);
        assertThat(sleeps).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    void Retry_After가_없으면_기본_대기_시간으로_재시도한다() throws IOException {
        List<Duration> sleeps = new ArrayList<>();
        TossRateLimitRetryInterceptor interceptor = new TossRateLimitRetryInterceptor(
                2,
                Duration.ofMillis(300),
                sleeps::add
        );
        AtomicInteger attempts = new AtomicInteger();

        interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.POST, "/v1/payments/confirm"),
                new byte[0],
                (request, body) -> {
                    if (attempts.incrementAndGet() == 1) {
                        return new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
                    }
                    return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
                }
        );

        assertThat(sleeps).containsExactly(Duration.ofMillis(300));
    }

    @Test
    void 최대_시도_횟수를_넘어도_429이면_결제_한도_초과_예외를_던진다() {
        TossRateLimitRetryInterceptor interceptor = new TossRateLimitRetryInterceptor(
                2,
                Duration.ofSeconds(1),
                duration -> {
                }
        );
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.POST, "/v1/payments/confirm"),
                new byte[0],
                (request, body) -> {
                    attempts.incrementAndGet();
                    return new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
                }
        )).isInstanceOf(PaymentRateLimitExceededException.class);
        assertThat(attempts.get()).isEqualTo(2);
    }
}
