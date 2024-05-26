package roomescape.dto.service;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

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

    public ReservationStatus getStatus() {
        return reservation.getStatus();
    }

    public Long getRank() {
        return rank;
    }
}
