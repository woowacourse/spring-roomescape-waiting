package roomescape.time.controller.dto;

import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationTimeWebRequest(
        LocalTime startAt
) {

    public CreateReservationTimeWebRequest {
        validate(startAt);
    }

    private void validate(final LocalTime startAt) {
        Validator.of(CreateReservationTimeWebRequest.class)
                .notNullField(Fields.startAt, startAt);
    }
}
