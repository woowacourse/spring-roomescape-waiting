package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Embedded
    @AttributeOverride(
            name = ReservationDate.Fields.value,
            column = @Column(name = Fields.date))
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public Reservation(final Long userId,
                       final ReservationDate date,
                       final ReservationTime time,
                       final Theme theme
    ) {
        validate(userId, date, time, theme);
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(final Long id,
                       final Long userId,
                       final ReservationDate date,
                       final ReservationTime time,
                       final Theme theme
    ) {
        validate(id);
        validate(userId, date, time, theme);
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation withId(final Long id,
                                     final Long userId,
                                     final ReservationDate date,
                                     final ReservationTime time,
                                     final Theme theme) {
        return new Reservation(id, userId, date, time, theme);
    }

    public static Reservation withoutId(final Long userId,
                                        final ReservationDate date,
                                        final ReservationTime time,
                                        final Theme theme) {
        return new Reservation(userId, date, time, theme);
    }

    private static void validate(final Long userId,
                                 final ReservationDate date,
                                 final ReservationTime time,
                                 final Theme theme) {
        Validator.of(Reservation.class)
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.theme, theme, DomainTerm.THEME.label());
    }

    private static void validate(final Long id) {
        Validator.of(Reservation.class)
                .validateNotNull(Fields.id, id, DomainTerm.RESERVATION_ID.label());
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
