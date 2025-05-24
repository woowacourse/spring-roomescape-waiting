package roomescape.exception.resource;

public class ResourceLimitExceededException extends RuntimeException {

    public ResourceLimitExceededException(String message) {
        super(message);
    }
}
