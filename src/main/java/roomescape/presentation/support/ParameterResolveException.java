package roomescape.presentation.support;

public class ParameterResolveException extends RuntimeException {

    public ParameterResolveException(String message) {
        super(message);
    }

    public ParameterResolveException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class AuthInfoResolveException extends ParameterResolveException {

        public AuthInfoResolveException(String message) {
            super(message);
        }

        public AuthInfoResolveException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
