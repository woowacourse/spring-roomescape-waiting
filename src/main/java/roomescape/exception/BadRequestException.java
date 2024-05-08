package roomescape.exception;

public class BadRequestException extends RuntimeException {

    private static final String ERROR = "[ERROR] ";

    public BadRequestException(String message) {
        super(ERROR + message);
    }

    public BadRequestException(String value, String field) {
        super(ERROR + "%s의 값이 \"%s\"일 수 없습니다.".formatted(field, value));
    }
}
