package roomescape.payment.infra.toss;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;

class RetryAfterInterceptorTest {

    @Test
    void Retry_After만큼_대기한_뒤_재시도한다() throws IOException {
        List<Long> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(
                3,
                Duration.ofSeconds(1),
                sleeps::add
        );
        ClientHttpResponse limited = response(HttpStatus.TOO_MANY_REQUESTS, "2");
        ClientHttpResponse success = response(HttpStatus.OK, null);
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) ->
                attempts.getAndIncrement() == 0 ? limited : success;

        ClientHttpResponse response = interceptor.intercept(
                mock(HttpRequest.class),
                new byte[0],
                execution
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(attempts).hasValue(2);
        assertThat(sleeps).containsExactly(2_000L);
    }

    @Test
    void Retry_After가_없으면_기본_간격으로_재시도한다() throws IOException {
        List<Long> sleeps = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(
                2,
                Duration.ofMillis(250),
                sleeps::add
        );
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> attempts.getAndIncrement() == 0
                ? response(HttpStatus.TOO_MANY_REQUESTS, null)
                : response(HttpStatus.OK, null);

        interceptor.intercept(mock(HttpRequest.class), new byte[0], execution);

        assertThat(sleeps).containsExactly(250L);
    }

    @Test
    void 최대_시도_횟수까지_429면_도메인_예외로_실패한다() {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(
                2,
                Duration.ZERO,
                millis -> {
                }
        );
        AtomicInteger attempts = new AtomicInteger();
        ClientHttpRequestExecution execution = (request, body) -> {
            attempts.incrementAndGet();
            return response(HttpStatus.TOO_MANY_REQUESTS, "0");
        };

        assertThatThrownBy(() -> interceptor.intercept(
                mock(HttpRequest.class),
                new byte[0],
                execution
        ))
                .isInstanceOf(PaymentException.class)
                .extracting(exception -> ((PaymentException) exception).errorCode())
                .isEqualTo(PaymentErrorCode.GATEWAY_RATE_LIMIT_EXCEEDED);
        assertThat(attempts).hasValue(2);
    }

    private ClientHttpResponse response(HttpStatus status, String retryAfter) throws IOException {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        if (retryAfter != null) {
            headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        given(response.getStatusCode()).willReturn(status);
        given(response.getHeaders()).willReturn(headers);
        return response;
    }
}
