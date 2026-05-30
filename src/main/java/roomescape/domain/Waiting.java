package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Waiting {
    private final Long id;
    private final Member owner;
    private final Slot slot;
    private final LocalDateTime createdAt;

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

    public Long id() {
        return id;
    }

    public Member owner() {
        return owner;
    }

    public Slot slot() {
        return slot;
    }

    public LocalDateTime createAt() {
        return createdAt;
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
        return this.owner.equals(member);
    }

    private boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }
}
