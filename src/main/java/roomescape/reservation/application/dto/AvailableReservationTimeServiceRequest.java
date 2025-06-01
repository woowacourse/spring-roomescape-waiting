package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.ReservationDate;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record AvailableReservationTimeServiceRequest(
        ReservationDate date,
        Long themeId
) {

    public AvailableReservationTimeServiceRequest {
        validate(date, themeId);
    }

    private void validate(final ReservationDate date, final Long themeId) {
        Validator.of(AvailableReservationTimeServiceRequest.class)
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label());
    }
}
