package roomescape.reservation.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.reservation.domain.BookedStatus;
import roomescape.reservation.time.domain.ReservationTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeResponse(
        ReservationTime time,
        BookedStatus bookedStatus
) {

    public AvailableReservationTimeResponse {
        validate(time, bookedStatus);
    }

    private void validate(final ReservationTime time, final BookedStatus bookedStatus) {
        Validator.of(AvailableReservationTimeResponse.class)
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.bookedStatus, bookedStatus, DomainTerm.BOOKED_STATUS.label());
    }
}
