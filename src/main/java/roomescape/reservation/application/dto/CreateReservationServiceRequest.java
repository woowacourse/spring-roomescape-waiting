package roomescape.reservation.application.dto;

import lombok.AccessLevel;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

@FieldNameConstants(level = AccessLevel.PRIVATE)
public record CreateReservationServiceRequest(UserId userId,
                                              ReservationDate date,
                                              ReservationTimeId timeId,
                                              ThemeId themeId) {

    public CreateReservationServiceRequest {
        validate(userId, date, timeId, themeId);
    }

    public Reservation toDomain(final ReservationTime time, final Theme theme) {
        return Reservation.withoutId(
                userId,
                date,
                time,
                theme);
    }

    private void validate(final UserId userId,
                          final ReservationDate date,
                          final ReservationTimeId timeId,
                          final ThemeId themeId) {
        Validator.of(CreateReservationServiceRequest.class)
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.timeId, timeId, DomainTerm.RESERVATION_TIME_ID.label())
                .validateNotNull(Fields.themeId, themeId, DomainTerm.THEME_ID.label());
    }
}
