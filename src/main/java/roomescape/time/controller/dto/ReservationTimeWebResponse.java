package roomescape.time.controller.dto;

import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ReservationTimeWebResponse(
        Long id,
        LocalTime startAt
) {

    public ReservationTimeWebResponse {
        validate(id, startAt);
    }

    private void validate(final Long id, final LocalTime startAt) {
        Validator.of(ReservationTimeWebResponse.class)
                .notNullField(ReservationTimeWebResponse.Fields.id, id)
                .notNullField(ReservationTimeWebResponse.Fields.startAt, startAt);
    }
}
