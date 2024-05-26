package roomescape.reservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;

@Embeddable
public record Slot(
        @Column(nullable = false)
        LocalDate date,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(nullable = false)
        ReservationTime reservationTime,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(nullable = false)
        Theme theme
) {
    public String getThemeName() {
        return theme.getName();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }
}
