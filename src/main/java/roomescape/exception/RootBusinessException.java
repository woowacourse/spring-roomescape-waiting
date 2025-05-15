package roomescape.exception;

public abstract class RootBusinessException extends RuntimeException {

    private final ErrorCode code;

    protected RootBusinessException(ErrorCode code) {
        super(code.message());
        this.code = code;
    }

    protected RootBusinessException(ErrorCode code, Object... args) {
        super(code.message().formatted(args));
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
