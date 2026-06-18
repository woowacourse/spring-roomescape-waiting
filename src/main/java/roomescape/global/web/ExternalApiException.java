package roomescape.global.web;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class ExternalApiException extends RuntimeException {

    private final HttpStatusCode status;
    private final String code;

    public ExternalApiException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

}
