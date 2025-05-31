package roomescape.exception.resource;

public class ResourceLimitExceededException extends RuntimeException {

    public ResourceLimitExceededException(final String message) {
        super(message);
    }
}
