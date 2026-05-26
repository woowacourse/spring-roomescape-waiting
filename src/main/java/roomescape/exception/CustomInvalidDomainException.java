package roomescape.exception;

public class CustomInvalidDomainException extends CustomException {

    public CustomInvalidDomainException(ErrorCode errorCode) {
        super(errorCode);
    }
}
