package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record Slot(LocalDate date, ReservationTime time, Theme theme) {

    public boolean isPast(LocalDateTime now) {
        return time.isPast(date, now);
    }

    public void validateNotPast(LocalDateTime now) {
        time.validateNotPast(date, now);
    }

    public boolean isSameSlot(Slot other) {
        return date.equals(other.date)
                && time.id().equals(other.time.id())
                && theme.id().equals(other.theme.id());
    }
}
