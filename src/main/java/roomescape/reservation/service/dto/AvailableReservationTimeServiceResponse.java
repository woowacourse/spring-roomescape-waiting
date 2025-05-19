package roomescape.reservation.service.dto;

import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeServiceResponse(
        LocalTime startAt,
        Long timeId,
        Boolean isBooked
) {

    public AvailableReservationTimeServiceResponse {
        validate(startAt, timeId, isBooked);
    }

    private void validate(final LocalTime startAt, final Long timeId, final Boolean isBooked) {
        Validator.of(AvailableReservationTimeServiceResponse.class)
                .notNullField(Fields.startAt, startAt)
                .notNullField(Fields.timeId, timeId)
                .notNullField(Fields.isBooked, isBooked);
    }
}
