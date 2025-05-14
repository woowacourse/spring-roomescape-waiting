package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import roomescape.common.domain.BaseEntity;
import roomescape.common.domain.DomainTerm;
import roomescape.common.validate.Validator;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants(level = AccessLevel.PRIVATE)
@ToString
@Entity
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    @Embedded
    @AttributeOverride(
            name = UserId.Fields.value,
            column = @Column(name = Fields.userId))
    private UserId userId;

    @Embedded
    @AttributeOverride(
            name = ReservationDate.Fields.value,
            column = @Column(name = Fields.date))
    private ReservationDate date;

    @Embedded
    @AttributeOverride(
            name = BookedStatus.Fields.sequence,
            column = @Column(name = Fields.status))
    private BookedStatus status;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public Reservation(final UserId userId,
                       final ReservationDate date,
                       final ReservationTime time,
                       final Theme theme,
                       final BookedStatus status
    ) {
        validate(userId, date, time, theme);
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public Reservation(final Long id,
                       final UserId userId,
                       final ReservationDate date,
                       final ReservationTime time,
                       final Theme theme,
                       final BookedStatus status
    ) {
        super(id);
        validate(userId, date, time, theme);
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public static Reservation withId(final ReservationId id,
                                     final UserId userId,
                                     final ReservationDate date,
                                     final ReservationTime time,
                                     final Theme theme) {
        return new Reservation(id.getValue(), userId, date, time, theme, BookedStatus.from(0));
    }

    public static Reservation withoutId(final UserId userId,
                                        final ReservationDate date,
                                        final ReservationTime time,
                                        final Theme theme) {
        return new Reservation(userId, date, time, theme, BookedStatus.from(0));
    }

    private static void validate(
            final UserId userId,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme) {
        Validator.of(Reservation.class)
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

    public ReservationId getId() {
        return ReservationId.from(id);
    }
}
