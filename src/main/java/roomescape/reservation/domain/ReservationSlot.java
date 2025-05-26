package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.theme.domain.Theme;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationSlot {

    @Column(nullable = false)
    private LocalDate date;

    @JoinColumn(name = "time_id", nullable = false)
    @ManyToOne
    private ReservationTime time;

    @JoinColumn(name = "theme_id", nullable = false)
    @ManyToOne
    private Theme theme;

    private ReservationSlot(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);

        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static ReservationSlot of(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme
    ) {
        return new ReservationSlot(date, time, theme);
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null이면 안됩니다.");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null이면 안됩니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 null이면 안됩니다.");
        }
    }
}
