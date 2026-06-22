package roomescape.infra.toss;

import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.global.ratelimit.TossRetryProperties;

import java.io.IOException;
import org.springframework.http.HttpRequest;

@Component
public class RetryAfterInterceptor implements ClientHttpRequestInterceptor {

    private final TossRetryProperties properties;

    public RetryAfterInterceptor(TossRetryProperties properties) {
        this.properties = properties;
    }

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
