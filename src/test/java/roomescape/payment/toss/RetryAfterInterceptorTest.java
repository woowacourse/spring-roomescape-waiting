package roomescape.payment.toss;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    @Test
    void Retry_After_초만큼_대기하고_재시도한다() throws IOException {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofSeconds(1), delays::add);
        ArrayDeque<MockClientHttpResponse> responses = new ArrayDeque<>();
        MockClientHttpResponse tooManyRequests = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
        tooManyRequests.getHeaders().set(HttpHeaders.RETRY_AFTER, "2");
        responses.add(tooManyRequests);
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        var response = interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> responses.removeFirst());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(delays).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    void Retry_After가_없으면_폴백_간격으로_재시도한다() throws IOException {
        List<Duration> delays = new ArrayList<>();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, Duration.ofMillis(100), delays::add);
        ArrayDeque<MockClientHttpResponse> responses = new ArrayDeque<>();
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS));
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.OK));

        interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> responses.removeFirst());

        assertThat(delays).containsExactly(Duration.ofMillis(100));
    }

    @Test
    void 최대_시도_횟수까지만_요청한다() throws IOException {
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, Duration.ZERO, delay -> {
        });
        ArrayDeque<MockClientHttpResponse> responses = new ArrayDeque<>();
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS));
        responses.add(new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS));

        var response = interceptor.intercept(new MockClientHttpRequest(HttpMethod.POST, "/"), new byte[0],
                (request, body) -> responses.removeFirst());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(responses).isEmpty();
    }
}
