package roomescape.global.exception;

public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
