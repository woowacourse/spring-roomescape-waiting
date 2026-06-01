package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public record Slot(
        LocalDate date,
        ReservationTime time,
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
