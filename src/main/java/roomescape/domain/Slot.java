package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
public record Slot(
        @Column(nullable = false)
        LocalDate date,

        @ManyToOne
        @JoinColumn(name = "time_id", nullable = false)
        ReservationTime time,

        @ManyToOne
        @JoinColumn(name = "theme_id", nullable = false)
        Theme theme
) {

    public Slot {
        Objects.requireNonNull(date, "슬롯에 날짜는 필수 요소입니다.");
        Objects.requireNonNull(time, "슬롯에 시간은 필수 요소입니다.");
        Objects.requireNonNull(theme, "슬롯에 테마는 필수 요소입니다.");
    }

    public boolean isPast(LocalDateTime now) {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(now);
    }
}
