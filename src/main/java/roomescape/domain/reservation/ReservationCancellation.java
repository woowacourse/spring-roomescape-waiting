package roomescape.domain.reservation;

import java.util.Optional;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public class ReservationCancellation {

    private final Reservation cancelledReservation;
    private final ReservationWaiting promotedWaiting;
    private final Reservation promotedReservation;

    private ReservationCancellation(
            final Reservation cancelledReservation,
            final ReservationWaiting promotedWaiting,
            final Reservation promotedReservation
    ) {
        this.cancelledReservation = cancelledReservation;
        this.promotedWaiting = promotedWaiting;
        this.promotedReservation = promotedReservation;
    }

    public static ReservationCancellation withoutPromotion(final Reservation cancelledReservation) {
        return new ReservationCancellation(cancelledReservation, null, null);
    }

    public static ReservationCancellation withPromotion(
            final Reservation cancelledReservation,
            final ReservationWaiting promotedWaiting,
            final Reservation promotedReservation
    ) {
        return new ReservationCancellation(cancelledReservation, promotedWaiting, promotedReservation);
    }

    public Reservation cancelledReservation() {
        return cancelledReservation;
    }

    public Optional<ReservationWaiting> promotedWaiting() {
        return Optional.ofNullable(promotedWaiting);
    }

    public Optional<Reservation> promotedReservation() {
        return Optional.ofNullable(promotedReservation);
    }
}
