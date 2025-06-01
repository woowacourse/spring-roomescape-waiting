package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.time.domain.ReservationTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeServiceResponse(
        ReservationTime time,
        boolean isBooked
) {

    public AvailableReservationTimeServiceResponse {
        validate(time, isBooked);
    }

    private void validate(final ReservationTime time, final boolean bookedStatus) {
        Validator.of(AvailableReservationTimeServiceResponse.class)
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.isBooked, bookedStatus, DomainTerm.BOOKED_STATUS.label());
    }
}
