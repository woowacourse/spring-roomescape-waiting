package roomescape.reservation.dto;

import lombok.Getter;
import roomescape.reservation.domain.Reservation;

@Getter
public class WaitingReservation {
        private final Reservation reservation;
        private final Long rank;

        public WaitingReservation(Reservation reservation, Long rank) {
                this.reservation = reservation;
                this.rank = rank;
        }
}
