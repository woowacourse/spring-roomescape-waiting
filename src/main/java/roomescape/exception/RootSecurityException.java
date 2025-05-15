package roomescape.exception;

public class RootSecurityException extends RuntimeException {

    private final SecurityErrorCode code;

    public RootSecurityException(SecurityErrorCode code) {
        super(code.clientMessage());
        this.code = code;
    }

    public String codeName() {
        return code.name();
    }

    public String detailMessage() {
        return code.detailMessage();
    }

    public String clientMessage() {
        return code.clientMessage();
    }
}
