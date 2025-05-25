package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Embeddable
public class ReservationInfo {

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public ReservationInfo(final LocalDate date, final ReservationTime time, final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationInfo() {
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
