package roomescape.ratelimit;

import org.springframework.web.client.RestClientException;

public class RetryAfterExceededException extends RestClientException {

    public RetryAfterExceededException() {
        super("결제 승인 요청이 계속 제한되고 있습니다.");
    }
}
