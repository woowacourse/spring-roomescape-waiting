package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

class RetryAfterInterceptorTest {

    @Test
    void Retry_After만큼_대기한_후_재시도한다() throws Exception {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor =
            new RetryAfterInterceptor(3, Duration.ofSeconds(1), delays::add);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse rateLimited = response(HttpStatus.TOO_MANY_REQUESTS, "2");
        ClientHttpResponse success = response(HttpStatus.OK, null);
        byte[] body = new byte[0];
        when(execution.execute(request, body)).thenReturn(rateLimited, success);

        ClientHttpResponse response = interceptor.intercept(request, body, execution);

        assertThat(response).isSameAs(success);
        assertThat(delays).containsExactly(Duration.ofSeconds(2));
        verify(execution, times(2)).execute(request, body);
        verify(rateLimited).close();
    }

    @Test
    void Retry_After가_없으면_기본_간격으로_재시도한다() throws Exception {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor =
            new RetryAfterInterceptor(2, Duration.ofMillis(100), delays::add);
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse rateLimited = response(HttpStatus.TOO_MANY_REQUESTS, null);
        ClientHttpResponse success = response(HttpStatus.OK, null);
        byte[] body = new byte[0];
        when(execution.execute(request, body)).thenReturn(rateLimited, success);

        interceptor.intercept(request, body, execution);

        assertThat(delays).containsExactly(Duration.ofMillis(100));
    }

    @Test
    void 최대_시도_횟수를_넘으면_도메인_예외를_던진다() throws Exception {
        RetryAfterInterceptor interceptor =
            new RetryAfterInterceptor(2, Duration.ZERO, duration -> {
            });
        HttpRequest request = mock(HttpRequest.class);
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse first = response(HttpStatus.TOO_MANY_REQUESTS, "0");
        ClientHttpResponse second = response(HttpStatus.TOO_MANY_REQUESTS, "0");
        byte[] body = new byte[0];
        when(execution.execute(request, body)).thenReturn(first, second);

        assertThatThrownBy(() -> interceptor.intercept(request, body, execution))
            .isInstanceOf(PaymentRateLimitExceededException.class);

        verify(execution, times(2)).execute(request, body);
        verify(first).close();
        verify(second).close();
    }

    private ClientHttpResponse response(HttpStatus status, String retryAfter) throws Exception {
        ClientHttpResponse response = mock(ClientHttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        if (retryAfter != null) {
            headers.set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        when(response.getStatusCode()).thenReturn(status);
        when(response.getHeaders()).thenReturn(headers);
        return response;
    }
}
