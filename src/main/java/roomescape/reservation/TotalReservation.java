package roomescape.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.theme.Theme;
import roomescape.waiting.ReservationWaiting;

public class TotalReservation {
    private final Long id;
    private final String name;
    private final String themeName;
    private final LocalDate date;
    private final LocalTime startAt;
    private final Long waitingNumber;

    public TotalReservation(Reservation reservation, Theme theme) {
        this.id = reservation.getId();
        this.name = reservation.getName();
        this.themeName = theme.getName();
        this.date = reservation.getDate();
        this.startAt = reservation.getTime().getStartAt();
        this.waitingNumber = null;
    }

    public TotalReservation(ReservationWaiting waiting, Theme theme) {
        this.id = waiting.getId();
        this.name = waiting.getName();
        this.themeName = theme.getName();
        this.date = waiting.getDate();
        this.startAt = waiting.getTime().getStartAt();
        this.waitingNumber = waiting.getWaitingNumber();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThemeName() {
        return themeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public Long getWaitingNumber() {
        return waitingNumber;
    }
}
