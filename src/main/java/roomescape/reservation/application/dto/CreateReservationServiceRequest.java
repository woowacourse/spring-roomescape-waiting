package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationServiceRequest(Long userId,
                                              ReservationDate date,
                                              Long timeId,
                                              Long themeId) {

    public CreateReservationServiceRequest {
        validate(userId, date, timeId, themeId);
    }

    public Reservation toDomain(final ReservationTime time, final Theme theme) {
        return Reservation.of(
                userId,
                date,
                time,
                theme);
    }

    private void validate(final Long userId,
                          final ReservationDate date,
                          final Long timeId,
                          final Long themeId) {
        Validator.of(CreateReservationServiceRequest.class)
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.timeId, timeId, DomainTerm.RESERVATION_TIME_ID.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label());
    }
}
