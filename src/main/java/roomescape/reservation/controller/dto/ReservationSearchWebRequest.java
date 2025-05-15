package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.utils.Validator;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record ReservationSearchWebRequest(
        Long memberId,
        Long themeId,
        LocalDate from,
        LocalDate to
) {

    public ReservationSearchWebRequest {
        validate(memberId, themeId, from, to);
    }

    private void validate(final Long memberId,
                          final Long themeId,
                          final LocalDate from,
                          final LocalDate to) {
        Validator.of(ReservationSearchWebRequest.class)
                .notNullField(Fields.memberId, memberId)
                .notNullField(Fields.themeId, themeId)
                .notNullField(Fields.from, from)
                .notNullField(Fields.to, to);
    }
}
