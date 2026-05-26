package roomescape.global.error.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.type.ErrorType;

public class GeneralNotFoundException extends RuntimeException {

    private final HttpStatus status;
    private final List<ParameterErrorResponseDto> parameterErrors;

    public GeneralNotFoundException(ErrorType errorType, List<ParameterErrorResponseDto> parameterErrors) {
        super(errorType.message());
        this.status = errorType.status();
        this.parameterErrors = parameterErrors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<ParameterErrorResponseDto> getParameterErrors() {
        return parameterErrors;
    }
}
