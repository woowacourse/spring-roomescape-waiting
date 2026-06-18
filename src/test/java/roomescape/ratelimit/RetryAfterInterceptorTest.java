package roomescape.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    @Test
    void retries429WithRetryAfterThenReturnsSuccessTest() throws IOException {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, sleeper);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
        SequenceExecution execution = new SequenceExecution(
                response(HttpStatus.TOO_MANY_REQUESTS, "2"),
                response(HttpStatus.OK, null));

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(execution.count()).isEqualTo(2);
        assertThat(sleeper.durations()).containsExactly(Duration.ofSeconds(2));
    }

    @Test
    void usesFallbackWhen429HasNoRetryAfterTest() throws IOException {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, sleeper);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
        SequenceExecution execution = new SequenceExecution(
                response(HttpStatus.TOO_MANY_REQUESTS, null),
                response(HttpStatus.OK, null));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(sleeper.durations()).containsExactly(Duration.ofSeconds(1));
    }

    @Test
    void usesFallbackWhenRetryAfterIsInvalidTest() throws IOException {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(3, sleeper);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
        SequenceExecution execution = new SequenceExecution(
                response(HttpStatus.TOO_MANY_REQUESTS, "soon"),
                response(HttpStatus.OK, null));

        interceptor.intercept(request, new byte[0], execution);

        assertThat(sleeper.durations()).containsExactly(Duration.ofSeconds(1));
    }

    @Test
    void returnsFinal429AfterMaxAttemptsTest() throws IOException {
        RecordingSleeper sleeper = new RecordingSleeper();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2, sleeper);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://example.com"));
        SequenceExecution execution = new SequenceExecution(
                response(HttpStatus.TOO_MANY_REQUESTS, "3"),
                response(HttpStatus.TOO_MANY_REQUESTS, "4"));

        ClientHttpResponse response = interceptor.intercept(request, new byte[0], execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(execution.count()).isEqualTo(2);
        assertThat(sleeper.durations()).containsExactly(Duration.ofSeconds(3));
    }

    private static class SequenceExecution implements ClientHttpRequestExecution {

        private final ClientHttpResponse[] responses;
        private int count;

        private SequenceExecution(ClientHttpResponse... responses) {
            this.responses = responses;
        }

        @Override
        public ClientHttpResponse execute(org.springframework.http.HttpRequest request, byte[] body) {
            return responses[count++];
        }

        private int count() {
            return count;
        }
    }

    private static class RecordingSleeper implements BackoffSleeper {

        private final List<Duration> durations = new ArrayList<>();

        @Override
        public void sleep(Duration duration) {
            durations.add(duration);
        }

        private List<Duration> durations() {
            return durations;
        }
    }

    private static ClientHttpResponse response(HttpStatus status, String retryAfter) {
        MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], status);
        if (retryAfter != null) {
            response.getHeaders().set(HttpHeaders.RETRY_AFTER, retryAfter);
        }
        return response;
    }
}
