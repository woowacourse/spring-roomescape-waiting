package roomescape.presentation.error;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import roomescape.domain.exception.ErrorCode;

@Component
public class DomainErrorHttpMapper {

    public HttpStatus statusOf(ErrorCode errorCode) {
        return switch (errorCode) {
            case ;
            default:
        }
    }
}
