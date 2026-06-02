package roomescape.exception;

import lombok.Getter;
import org.springframework.dao.DataAccessException;

@Getter
public class KeyGenerationException extends DataAccessException {
    private final ErrorCode errorCode;

    public KeyGenerationException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
