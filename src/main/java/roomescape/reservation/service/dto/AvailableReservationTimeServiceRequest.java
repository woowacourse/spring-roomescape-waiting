package roomescape.reservation.service.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeServiceRequest(
        LocalDate date,
        Long themeId
) {

    public AvailableReservationTimeServiceRequest {
        validate(date, themeId);
    }

    private void validate(final LocalDate date, final Long themeId) {
        Validator.of(AvailableReservationTimeServiceRequest.class)
                .notNullField(Fields.date, date)
                .notNullField(Fields.themeId, themeId);
    }
}
