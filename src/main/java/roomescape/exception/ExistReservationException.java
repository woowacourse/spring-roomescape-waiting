package roomescape.exception;

public class ExistReservationException extends IllegalArgumentException {

    public ExistReservationException(final ExceptionDomainType exceptionDomainType, final long id) {
        super(String.format("%s ID %d에 해당하는 예약이 존재합니다.", exceptionDomainType.getMessage(), id));
    }
}
