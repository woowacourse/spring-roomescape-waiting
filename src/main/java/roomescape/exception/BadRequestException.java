package roomescape.exception;

import java.time.LocalDateTime;

public class BadRequestException extends RuntimeException {

    private static final String ERROR = "[ERROR] ";

    public BadRequestException(String message) {
        super(ERROR + message);
    }

    public BadRequestException(String value, String field) {
        super(ERROR + "%s의 값이 \"%s\"일 수 없습니다.".formatted(field, value));
    }

    public BadRequestException(LocalDateTime now) {
        super(ERROR + "현재(%s) 이전 시간으로 예약할 수 없습니다.".formatted(now));
    }
}
