package roomescape.reservation.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeRequest(
        ReservationDate date,
        ThemeId themeId
) {

    public AvailableReservationTimeRequest {
        validate(date, themeId);
    }

    private void validate(final ReservationDate date, final ThemeId themeId) {
        Validator.of(AvailableReservationTimeRequest.class)
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label());
    }
}
