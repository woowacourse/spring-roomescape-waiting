package roomescape.domain;

import java.time.LocalDate;

public class ReservationSlot {

    private final Long id;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public ReservationSlot(Long id, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
