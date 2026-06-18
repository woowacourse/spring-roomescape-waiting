package roomescape.domain.reservationStatus;

import roomescape.domain.Reservation;

public interface ReservationStatus {

    void cancel(Reservation reservation);
    void confirm(Reservation reservation);
    void complete(Reservation reservation);
    String getName();
}
