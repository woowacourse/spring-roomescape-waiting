package roomescape.exception;

import org.springframework.http.HttpStatus;

public class ArgumentNullException extends CustomException {

    private static final String MESSAGE = "값이 존재하지 않습니다.";
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public ArgumentNullException(String value) {
        super(String.format("%s의 %s", value, MESSAGE), STATUS);
    }
}
