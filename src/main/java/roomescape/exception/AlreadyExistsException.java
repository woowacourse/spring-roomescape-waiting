package roomescape.exception;

public class AlreadyExistsException extends IllegalArgumentException {

    public AlreadyExistsException(final ExceptionDomainType exceptionDomainType, final String alreadyExistInfo) {
        super(String.format("%s에 해당하는 %s이 있습니다.", alreadyExistInfo, exceptionDomainType.getMessage()));
    }

    public AlreadyExistsException(final String message) {
        super(message);
    }
}
