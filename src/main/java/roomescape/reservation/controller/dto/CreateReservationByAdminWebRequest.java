package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationByAdminWebRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public CreateReservationByAdminWebRequest {
        validate(memberId, date, timeId, themeId);
    }

    private void validate(final Long memberId, final LocalDate date, final Long timeId, final Long themeId) {
        Validator.of(CreateReservationByAdminWebRequest.class)
                .notNullField(Fields.memberId, memberId)
                .notNullField(Fields.date, date)
                .notNullField(Fields.timeId, timeId)
                .notNullField(Fields.themeId, themeId);
    }
}
