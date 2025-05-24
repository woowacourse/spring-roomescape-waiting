package roomescape.domain.reserveticket;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

public class ReservationWithWaitingRank {

    private static final int NOT_INITIALIZED_RANK = 1;

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

    public ReservationWithWaitingRank(Long id, LocalDate date, LocalTime startAt,
                                      ReservationStatus reservationStatus, String themeName) {
        this(id, date, startAt, reservationStatus, NOT_INITIALIZED_RANK, themeName);
    }

    public ReservationWithWaitingRank(Reservation reservation) {
        this(reservation.getId(), reservation.getDate(), reservation.getStartAt(), reservation.getReservationStatus(),
                reservation.getThemeName());
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWithWaitingRank that = (ReservationWithWaitingRank) o;
        return Objects.equals(date, that.date) && Objects.equals(startAt, that.startAt)
                && Objects.equals(themeName, that.themeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, startAt, themeName);
    }
}
