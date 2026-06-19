package roomescape.payment.client;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TossRetryInterceptorTest {

    private static final byte[] EMPTY_BODY = new byte[0];

    @Test
    void 정상_응답은_그대로_반환한다() throws IOException {
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(3, Duration.ofSeconds(1), slept);

        var response = interceptor.intercept(request(), EMPTY_BODY, execution(response200()));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(slept).isEmpty();
    }

    @Test
    void 첫_429_이후_재시도해서_성공하면_성공_응답을_반환한다() throws IOException {
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(3, Duration.ofSeconds(1), slept);

        var response = interceptor.intercept(request(), EMPTY_BODY,
                execution(response429("2"), response200()));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(slept).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    void Retry_After_헤더_값만큼_대기한다() throws IOException {
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(3, Duration.ofSeconds(1), slept);

        interceptor.intercept(request(), EMPTY_BODY,
                execution(response429("5"), response200()));

        assertThat(slept).containsExactly(Duration.ofSeconds(5));
    }

    @Test
    void Retry_After_헤더가_없으면_fallback_대기시간을_사용한다() throws IOException {
        Duration fallback = Duration.ofSeconds(3);
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(3, fallback, slept);

        interceptor.intercept(request(), EMPTY_BODY,
                execution(response429(null), response200()));

        assertThat(slept).containsExactly(fallback);
    }

    @Test
    void maxAttempts_횟수만큼_429가_지속되면_TossRateLimitedException을_던진다() {
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(3, Duration.ofSeconds(1), slept);

        assertThatThrownBy(() ->
                interceptor.intercept(request(), EMPTY_BODY,
                        execution(response429("1"), response429("1"), response429("1")))
        ).isInstanceOf(TossRateLimitedException.class);

        assertThat(slept).hasSize(2); // 3번 시도 → 2번 대기
    }

    @Test
    void maxAttempts가_1이면_첫_429에서_바로_예외를_던진다() {
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(1, Duration.ofSeconds(1), slept);

        assertThatThrownBy(() ->
                interceptor.intercept(request(), EMPTY_BODY, execution(response429("1")))
        ).isInstanceOf(TossRateLimitedException.class);

        assertThat(slept).isEmpty();
    }

    @Test
    void 재시도_성공시_응답_바디와_상태코드를_보존한다() throws IOException {
        List<Duration> slept = new ArrayList<>();
        TossRetryInterceptor interceptor = interceptor(3, Duration.ofSeconds(1), slept);

        MockClientHttpResponse ok = new MockClientHttpResponse(new byte[]{1, 2, 3}, HttpStatus.OK);
        var response = interceptor.intercept(request(), EMPTY_BODY,
                execution(response429("1"), ok));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().readAllBytes()).containsExactly(1, 2, 3);
    }

    // --- helpers ---

    private TossRetryInterceptor interceptor(int maxAttempts, Duration fallback, List<Duration> sleptCapture) {
        return new TossRetryInterceptor(maxAttempts, fallback, duration -> sleptCapture.add(duration));
    }

    private MockClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.POST, URI.create("https://api.tosspayments.com/v1/payments/confirm"));
    }

    private org.springframework.http.client.ClientHttpRequestExecution execution(
            MockClientHttpResponse... responses) {
        Queue<MockClientHttpResponse> queue = new ArrayDeque<>(List.of(responses));
        return (req, body) -> {
            MockClientHttpResponse next = queue.poll();
            if (next == null) {
                throw new IllegalStateException("No more responses in stub");
            }
            return next;
        };
    }

    private MockClientHttpResponse response200() {
        return new MockClientHttpResponse(EMPTY_BODY, HttpStatus.OK);
    }

    private MockClientHttpResponse response429(String retryAfter) {
        MockClientHttpResponse response = new MockClientHttpResponse(EMPTY_BODY, HttpStatus.TOO_MANY_REQUESTS);
        if (retryAfter != null) {
            response.getHeaders().add(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return response;
    }
}