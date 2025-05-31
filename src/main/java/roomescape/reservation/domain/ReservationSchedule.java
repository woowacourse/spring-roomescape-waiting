package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.theme.domain.Theme;

@Embeddable
public class ReservationSchedule {

    private LocalDate date;

    @JoinColumn(name = "theme_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Theme theme;

    @JoinColumn(name = "reservation_time_id")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ReservationTime reservationTime;

    protected ReservationSchedule() {}

    public ReservationSchedule(LocalDate date, Theme theme, ReservationTime reservationTime) {
        this.date = date;
        this.theme = theme;
        this.reservationTime = reservationTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public String getThemeName() {
        return theme.getName();
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Long getReservationTimeId() {
        return reservationTime.getId();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }
}
