package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.BookedStatus;
import roomescape.time.domain.ReservationTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeServiceResponse(
        ReservationTime time,
        BookedStatus bookedStatus
) {

    public AvailableReservationTimeServiceResponse {
        validate(time, bookedStatus);
    }

    private void validate(final ReservationTime time, final BookedStatus bookedStatus) {
        Validator.of(AvailableReservationTimeServiceResponse.class)
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.bookedStatus, bookedStatus, DomainTerm.BOOKED_STATUS.label());
    }
}
