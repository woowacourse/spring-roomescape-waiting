package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Waiting {
    private final Long id;
    private final String name;
    private final Slot slot;
    private final LocalDateTime createdAt;

    public static Waiting create(long id, String name, Slot slot, LocalDateTime createdAt) {
        return new Waiting(id, name, slot, createdAt);
    }

    public void validateNotStarted(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 시작된 게임의 예약대기는 취소할 수 없습니다.");
        }
    }

    public void validateOwnedBy(String name) {
        if (!isOwnedBy(name)) {
            throw new ForbiddenException("타인의 예약대기는 취소할 수 없습니다.");
        }
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Slot slot() {
        return slot;
    }

    public LocalDateTime createAt() {
        return createdAt;
    }

    private boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    private boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }
}
