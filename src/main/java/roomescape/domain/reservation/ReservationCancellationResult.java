package roomescape.domain.reservation;

import java.util.Optional;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public class ReservationCancellationResult {

    private final Reservation cancelledReservation;
    private final ReservationWaiting promotedWaiting;
    private final Reservation promotedReservation;

    private ReservationCancellationResult(
            final Reservation cancelledReservation,
            final ReservationWaiting promotedWaiting,
            final Reservation promotedReservation
    ) {
        this.cancelledReservation = cancelledReservation;
        this.promotedWaiting = promotedWaiting;
        this.promotedReservation = promotedReservation;
    }

    public static ReservationCancellationResult withoutPromotion(final Reservation cancelledReservation) {
        return new ReservationCancellationResult(cancelledReservation, null, null);
    }

    public static ReservationCancellationResult withPromotion(
            final Reservation cancelledReservation,
            final ReservationWaiting promotedWaiting,
            final Reservation promotedReservation
    ) {
        return new ReservationCancellationResult(cancelledReservation, promotedWaiting, promotedReservation);
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
