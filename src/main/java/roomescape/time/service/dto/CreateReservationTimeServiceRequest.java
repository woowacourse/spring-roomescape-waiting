package roomescape.time.service.dto;

import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationTimeServiceRequest(
        LocalTime startAt
) {

    public CreateReservationTimeServiceRequest {
        validate(startAt);
    }

    private void validate(final LocalTime startAt) {
        Validator.of(CreateReservationTimeServiceRequest.class)
                .notNullField(CreateReservationTimeServiceRequest.Fields.startAt, startAt);
    }
}
