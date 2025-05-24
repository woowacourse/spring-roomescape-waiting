package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationWithRank {
    private final Reservation reservation;
    private final Long rank;

    public ReservationWithRank(Reservation reservation, Long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }

    public Long getRank() {
        return rank;
    }

    public Long getReservationId(){
        return reservation.getId();
    }

    public LocalDate getReservationDate(){
        return reservation.getDate();
    }

    public String getThemeName(){
        return reservation.themeName();
    }

    public LocalTime getReservationTime(){
        return reservation.reservationTime();
    }

    public boolean isWaitingReservation(){
        return reservation.isWaitingStatus();
    }
}
