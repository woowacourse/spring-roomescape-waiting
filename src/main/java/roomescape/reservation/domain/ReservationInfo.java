package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Embeddable
public class ReservationInfo {

    @JoinColumn(nullable = false)
    @ManyToOne
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    @JoinColumn(nullable = false)
    @ManyToOne
    private ReservationTime reservationTime;

    public ReservationInfo(final Theme theme, final LocalDate date, final ReservationTime reservationTime) {
        this.theme = theme;
        this.date = date;
        this.reservationTime = reservationTime;
    }

    public ReservationInfo() {
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
