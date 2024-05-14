package roomescape.exception;

public class NotExistException extends IllegalArgumentException {

    public NotExistException(final ExceptionDomainType exceptionDomainType, final long id) {
        super(String.format("%s ID %d에 해당하는 값이 없습니다", exceptionDomainType.getMessage(), id));
    }

    public NotExistException(final String message){
        super(message);
    }
}
