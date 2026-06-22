package roomescape.pg.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpResponse;

class RetryAfterInterceptorTest {

    @Test
    void 게이트웨이가_429와_RetryAfter를_주면_대기_후_재시도한다() throws IOException {
        AtomicInteger requestCount = new AtomicInteger();
        RetryAfterInterceptor interceptor = new RetryAfterInterceptor(2);
        ClientHttpRequestExecution execution = (request, body) -> {
            if (requestCount.incrementAndGet() == 1) {
                MockClientHttpResponse response = new MockClientHttpResponse(new byte[0], HttpStatus.TOO_MANY_REQUESTS);
                response.getHeaders().set(HttpHeaders.RETRY_AFTER, "0");
                return response;
            }
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        var response = interceptor.intercept(request(), new byte[0], execution);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(requestCount).hasValue(2);
    }

    private HttpRequest request() {
        return new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return HttpMethod.POST;
            }

            @Override
            public URI getURI() {
                return URI.create("/v1/payments/confirm");
            }

            @Override
            public org.springframework.http.HttpHeaders getHeaders() {
                return new org.springframework.http.HttpHeaders();
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }
        };
    }
}
