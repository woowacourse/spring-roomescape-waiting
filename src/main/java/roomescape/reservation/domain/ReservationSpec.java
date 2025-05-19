package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class ReservationSpec {
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public ReservationSpec(LocalDate date, ReservationTime time, Theme theme) {
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
    }
}
