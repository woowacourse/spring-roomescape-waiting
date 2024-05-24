package roomescape.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import roomescape.model.theme.Theme;

import java.time.LocalDate;

@Embeddable
public class ReservationInfo {

    @NotNull
    private LocalDate date;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public ReservationInfo(LocalDate date, ReservationTime time, Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    protected ReservationInfo() {
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
