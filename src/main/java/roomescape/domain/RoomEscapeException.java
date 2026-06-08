package roomescape.domain;

public class RoomEscapeException extends IllegalArgumentException {

    private static final Object[] EMPTY = new Object[0];

    private final DomainErrorCode code;
    private final transient Object[] args;

    public RoomEscapeException(DomainErrorCode code, Object... args) {
        super(code.name());
        this.code = code;
        this.args = (args == null || args.length == 0) ? EMPTY : args.clone();
    }

    public DomainErrorCode code() {
        return code;
    }

    public Object[] args() {
        return args;
    }
}
