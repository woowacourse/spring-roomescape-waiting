package roomescape.presentation.support;

public class ParameterResolveException extends RuntimeException {

    public ParameterResolveException(String message) {
        super(message);
    }

    public ParameterResolveException(String message, Throwable cause) {
        super(message, cause);
    }
}
