package roomescape.reservation.domain;

import jakarta.persistence.Column;
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

    @ManyToOne
    private Reservation reservation;

    public WaitingReservation(final Long userId,
                              final int waitingOrder,
                              final Reservation reservation
    ) {
        validate(userId, waitingOrder, reservation);
        this.userId = userId;
        this.waitingOrder = waitingOrder;
        this.reservation = reservation;
    }

    public WaitingReservation(final Long id,
                              final Long userId,
                              final int waitingOrder,
                              final Reservation reservation
    ) {
        validate(id);
        validate(userId, waitingOrder, reservation);
        this.userId = userId;
        this.waitingOrder = waitingOrder;
        this.reservation = reservation;
    }

    public static WaitingReservation withId(final Long id,
                                            final Long userId,
                                            final int waitingOrder,
                                            final Reservation reservation) {
        return new WaitingReservation(id, userId, waitingOrder, reservation);
    }

    public static WaitingReservation withoutId(final Long userId,
                                               final int waitingOrder,
                                               final Reservation reservation) {
        return new WaitingReservation(userId, waitingOrder, reservation);
    }

    private static void validate(final Long userId,
                                 final int waitingOrder,
                                 final Reservation reservation) {
        Validator.of(WaitingReservation.class)
                .validateNotNull(Fields.userId, userId, DomainTerm.USER_ID.label())
                .validateNotNull(Fields.waitingOrder, waitingOrder, DomainTerm.RESERVATION_DATE.label())
                .validateNotNull(Fields.reservation, reservation, DomainTerm.RESERVATION.label());
    }

    private static void validate(final Long id) {
        Validator.of(WaitingReservation.class)
                .validateNotNull(Fields.id, id, DomainTerm.RESERVATION_ID.label());
    }
}
