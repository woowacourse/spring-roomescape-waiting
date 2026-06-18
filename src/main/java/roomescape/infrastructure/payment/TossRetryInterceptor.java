package roomescape.infrastructure.payment;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class TossRetryInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(TossRetryInterceptor.class);
    private static final int DEFAULT_RETRY_AFTER_SECONDS = 1;

    private final int maxAttempts;

    public TossRetryInterceptor(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        int attempts = 0;
        ClientHttpResponse response = execution.execute(request, body);

        while (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS && attempts < maxAttempts) {
            attempts++;
            long waitSeconds = getRetryAfterSeconds(response);
            log.warn("Toss API rate limited (429). Attempt {}/{}. Retrying after {} seconds.", attempts, maxAttempts, waitSeconds);
            
            try {
                Thread.sleep(waitSeconds * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Retry interrupted", e);
            }
            
            response = execution.execute(request, body);
        }

        return response;
    }

    private long getRetryAfterSeconds(ClientHttpResponse response) {
        String retryAfter = response.getHeaders().getFirst("Retry-After");
        if (retryAfter == null || retryAfter.isBlank()) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
        try {
            return Long.parseLong(retryAfter);
        } catch (NumberFormatException e) {
            return DEFAULT_RETRY_AFTER_SECONDS;
        }
    }
}
