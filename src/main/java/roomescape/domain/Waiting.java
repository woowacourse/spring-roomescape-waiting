package roomescape.domain;

import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

public record Waiting(Long id, Member owner, Slot slot, LocalDateTime createdAt) {

    public static Waiting forNew(Member owner, Slot slot, LocalDateTime createdAt) {
        return new Waiting(null, owner, slot, createdAt);
    }

    public static Waiting create(long id, Member owner, Slot slot, LocalDateTime createdAt) {
        return new Waiting(id, owner, slot, createdAt);
    }

    public void validateNotStarted(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 시작된 게임의 예약대기는 취소할 수 없습니다.");
        }
    }

    public void validateOwnedBy(Member member) {
        if (!isOwnedBy(member)) {
            throw new ForbiddenException("타인의 예약대기는 취소할 수 없습니다.");
        }
    }

    public boolean isSameSlot(Waiting other) {
        return slot.isSameSlot(other.slot);
    }

    public boolean isAheadOf(Waiting other) {
        int byCreatedAt = createdAt.compareTo(other.createdAt);
        if (byCreatedAt != 0) {
            return byCreatedAt < 0;
        }
        return id < other.id;
    }

    public boolean isOwnedBy(Member member) {
        return owner.equals(member);
    }

    private boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }
}
