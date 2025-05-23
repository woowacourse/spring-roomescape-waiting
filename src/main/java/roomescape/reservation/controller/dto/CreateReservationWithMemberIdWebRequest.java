package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationWithMemberIdWebRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public CreateReservationWithMemberIdWebRequest {
        validate(memberId, date, timeId, themeId);
    }

    private void validate(final Long memberId, final LocalDate date, final Long timeId, final Long themeId) {
        Validator.of(CreateReservationWithMemberIdWebRequest.class)
                .notNullField(Fields.memberId, memberId)
                .notNullField(Fields.date, date)
                .notNullField(Fields.timeId, timeId)
                .notNullField(Fields.themeId, themeId);
    }
}
