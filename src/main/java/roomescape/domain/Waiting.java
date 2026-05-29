package roomescape.domain;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Waiting {
    private final Long id;
    private final String name;
    private final Slot slot;
    private final LocalDateTime createAt;

    public static Waiting from(long id, String name, Slot slot, LocalDateTime createdAt) {
        return new Waiting(id, name, slot, createdAt);
    }

    public static Waiting create(String name, Slot slot, LocalDateTime now) {
        slot.validateAvailableTime(now);
        return new Waiting(null, name, slot, now);
    }

    public void validateCancelable(LocalDateTime now) {
        slot.validateAvailableTime(now);
    }

    public void validateOwnedBy(String name) {
        if (!this.name.equals(name)) {
            throw new ForbiddenException("타인의 예약대기는 취소할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getWaitingDate() {
        return slot.getDate();
    }

    public ReservationTime getWaitingTime() {
        return slot.getTime();
    }

    public Theme getWaitingTheme() {
        return slot.getTheme();
    }

    public LocalDateTime createAt() {
        return createAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Waiting waiting = (Waiting) o;
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
