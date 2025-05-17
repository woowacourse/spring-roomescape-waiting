package roomescape.reservation.ui.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceResponse;

import java.time.LocalTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeWebResponse(
        LocalTime startAt,
        Long timeId,
        Boolean isBooked
) {

    public AvailableReservationTimeWebResponse {
        validate(startAt, timeId, isBooked);
    }

    public static AvailableReservationTimeWebResponse from(
            final AvailableReservationTimeServiceResponse serviceResponse) {
        return new AvailableReservationTimeWebResponse(
                serviceResponse.time().getStartAt(),
                serviceResponse.time().getId().getValue(),
                serviceResponse.bookedStatus().isBooked());
    }

    private void validate(final LocalTime startAt, final Long timeId, final Boolean isBooked) {
        Validator.of(AvailableReservationTimeWebResponse.class)
                .validateNotNull(Fields.startAt, startAt, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.timeId, timeId, DomainTerm.RESERVATION_TIME_ID.label())
                .validateNotNull(Fields.isBooked, isBooked, DomainTerm.BOOKED_STATUS.label());
    }
}
