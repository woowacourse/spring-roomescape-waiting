package roomescape.exception;

public class InsufficientRoleException extends BusinessException {

    public InsufficientRoleException() {
        super(ErrorType.INSUFFICIENT_ROLE);
    }
}
