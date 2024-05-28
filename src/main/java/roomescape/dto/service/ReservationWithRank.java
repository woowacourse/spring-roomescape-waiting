package roomescape.dto.service;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.Theme;

public class ReservationWithRank {
    private Reservation reservation;
    private Long rank;

    public ReservationWithRank(Reservation reservation, Long rank) {
        this.reservation = reservation;
        this.rank = rank;
    }

    public long getId() {
        return reservation.getId();
    }

    public LocalDate getDate() {
        return reservation.getDate();
    }

    public LocalTime getTime() {
        return reservation.getTime();
    }

    public Theme getTheme() {
        return reservation.getTheme();
    }

    public String getStatusMessage() {
        return reservation.getStatus().makeStatusMessage(rank);
    }
}
