package roomescape.service.reservation.strategy;

import roomescape.domain.reservation.Reservation;

public interface ReservationValidateStrategy {

    void addReservationValidate(Reservation reservation);
}
