package roomescape.domain.exception;

public class RoomEscapeException extends RuntimeException {

    private static final Object[] EMPTY = new Object[0];

    private final DomainErrorCode code;
    private final transient Object[] args;

    public RoomEscapeException(DomainErrorCode code, Object... args) {
        super(code.name());
        this.code = code;
        this.args = normalizeArgs(args);
    }

    private Object[] normalizeArgs(Object... args) {
        if (args == null || args.length == 0) {
            return EMPTY;
        }
        return args.clone();
    }

    public DomainErrorCode code() {
        return code;
    }

    public Object[] args() {
        return args.clone();
    }
}
