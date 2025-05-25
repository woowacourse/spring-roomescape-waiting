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
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@FieldNameConstants
@Entity
@Table(name = "waiting_reservations")
public class WaitingReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "waiting_order")
    private int waitingOrder;

    @Embedded
    @AttributeOverride(
            name = ReservationDate.Fields.value,
            column = @Column(name = Reservation.Fields.date))
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public WaitingReservation(final Long userId,
                              final int waitingOrder,
                              final ReservationDate date,
                              final ReservationTime time,
                              final Theme theme
    ) {
        validate(userId, waitingOrder, date, time, theme);
        this.userId = userId;
        this.waitingOrder = waitingOrder;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public WaitingReservation(final Long id,
                              final Long userId,
                              final int waitingOrder,
                              final ReservationDate date,
                              final ReservationTime time,
                              final Theme theme
    ) {
        validate(id);
        validate(userId, waitingOrder, date, time, theme);
        this.id = id;
        this.userId = userId;
        this.waitingOrder = waitingOrder;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static WaitingReservation withId(final Long id,
                                            final Long userId,
                                            final int waitingOrder,
                                            final ReservationDate date,
                                            final ReservationTime time,
                                            final Theme theme
    ) {
        return new WaitingReservation(id, userId, waitingOrder, date, time, theme);
    }

    public static WaitingReservation withoutId(final Long userId,
                                               final int waitingOrder,
                                               final ReservationDate date,
                                               final ReservationTime time,
                                               final Theme theme
    ) {
        return new WaitingReservation(userId, waitingOrder, date, time, theme);
    }

    private static void validate(final Long userId,
                                 final int waitingOrder,
                                 final ReservationDate date,
                                 final ReservationTime time,
                                 final Theme theme
    ) {
        Validator.of(WaitingReservation.class)
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.waitingOrder, waitingOrder, DomainTerm.RESERVATION_WAITING_ORDER.label())
                .validateNotNull(Fields.date, date, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.time, time, DomainTerm.RESERVATION_TIME.label())
                .validateNotNull(Fields.theme, theme, DomainTerm.THEME.label());
    }

    private static void validate(final Long id) {
        Validator.of(WaitingReservation.class)
                .validateNotNull(Fields.id, id, DomainTerm.RESERVATION_ID.label());
    }
}
