package roomescape.domain.exception;

public final class UniqueConstraintViolationException extends RuntimeException {

    public UniqueConstraintViolationException() {
        super("유니크 제약이 위반되었습니다.");
    }

    public UniqueConstraintViolationException(Throwable cause) {
        super("유니크 제약이 위반되었습니다.", cause);
    }
}
