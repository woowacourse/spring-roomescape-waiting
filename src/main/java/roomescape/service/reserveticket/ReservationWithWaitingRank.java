package roomescape.service.reserveticket;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationWithWaitingRank {

    private final Long id;
    private final LocalDate date;
    private final LocalTime startAt;
    private final ReservationStatus reservationStatus;
    private final int waitNumber;
    private final String themeName;

    public ReservationWithWaitingRank(Long id, LocalDate date, LocalTime startAt,
                                      ReservationStatus reservationStatus, int waitNumber, String themeName) {
        this.id = id;
        this.date = date;
        this.startAt = startAt;
        this.reservationStatus = reservationStatus;
        this.waitNumber = waitNumber;
        this.themeName = themeName;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public int getWaitRank() {
        return waitNumber;
    }

    public String getThemeName() {
        return themeName;
    }
}
