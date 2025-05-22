package roomescape.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
public record ReservationSpec(

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "member_id", nullable = false)
        Member member,

        LocalDate date,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "time_id", nullable = false)
        ReservationTime time,

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "theme_id", nullable = false)
        Theme theme
) {
    public boolean isPast(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return reservationDateTime.isBefore(now);
    }

    public long calculateMinutesUntilStart(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return Duration.between(now, reservationDateTime).toMinutes();
    }
}
