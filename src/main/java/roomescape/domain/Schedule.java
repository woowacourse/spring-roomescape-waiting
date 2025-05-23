package roomescape.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

@Embeddable
public class Schedule {

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public Schedule() {

    }

    public Schedule(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
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
