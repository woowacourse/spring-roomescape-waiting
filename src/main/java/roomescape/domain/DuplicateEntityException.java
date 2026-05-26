package roomescape.domain;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String message, Object... args) {
        super(message.formatted(args));
    }
}
