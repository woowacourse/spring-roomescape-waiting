package roomescape.reservation.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@ToString
public class Reservation {

    private final ReservationId id;
    private final UserId userId;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    private static Reservation of(final ReservationId id,
                                  final UserId userId,
                                  final ReservationDate date,
                                  final ReservationTime time,
                                  final Theme theme) {
        validate(id, userId, date, time, theme);
        return new Reservation(id, userId, date, time, theme);
    }

    public static Reservation withId(final ReservationId id,
                                     final UserId userId,
                                     final ReservationDate date,
                                     final ReservationTime time,
                                     final Theme theme) {
        id.requireAssigned();
        return of(id, userId, date, time, theme);
    }

    public static Reservation withoutId(final UserId userId,
                                        final ReservationDate date,
                                        final ReservationTime time,
                                        final Theme theme) {
        return of(ReservationId.unassigned(), userId, date, time, theme);
    }

    private static void validate(final ReservationId id,
                                 final UserId userId,
                                 final ReservationDate date,
                                 final ReservationTime time,
                                 final Theme theme) {

        Validator.of(Reservation.class)
                .validateNotNull(Fields.id, id, DomainTerm.RESERVATION_ID.label())
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.theme, theme, DomainTerm.THEME.label());
    }

    public void validatePast(final LocalDateTime now) {
        if (date.isAfter(now.toLocalDate())) {
            return;
        }

        if (date.isBefore(now.toLocalDate())) {
            throw new PastDateReservationException(date, now);
        }

        if (time.isBefore(now.toLocalTime())) {
            throw new PastTimeReservationException(time, now);
        }
    }
}
