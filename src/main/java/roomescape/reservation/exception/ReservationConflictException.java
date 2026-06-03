package roomescape.reservation.exception;

import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;

public class ReservationConflictException extends DomainException {
    public ReservationConflictException(ErrorPolicy errorPolicy) {
        super(errorPolicy);
    }
}
