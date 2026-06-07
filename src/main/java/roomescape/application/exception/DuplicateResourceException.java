package roomescape.application.exception;

public final class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException() {
        super("중복 리소스가 존재합니다.");
    }

    public DuplicateResourceException(Throwable cause) {
        super("중복 리소스가 존재합니다.", cause);
    }
}
