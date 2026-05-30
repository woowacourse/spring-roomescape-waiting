package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Reservation {

    private final Long id;
    private final Member owner;
    private final Slot slot;

    public static Reservation create(long id, Member owner, Slot slot) {
        return new Reservation(id, owner, slot);
    }

    public void validateNotStarted(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 시작된 예약은 변경할 수 없습니다.");
        }
    }

    public void validateOwnedBy(Member member) {
        if (!isOwnedBy(member)) {
            throw new ForbiddenException("본인의 예약만 변경할 수 있습니다.");
        }
    }

    public Reservation withSlot(Slot newSlot) {
        return new Reservation(id, owner, newSlot);
    }

    public Member owner() {
        return owner;
    }

    public Slot slot() {
        return slot;
    }

    public Long id() {
        return id;
    }

    private boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean isOwnedBy(Member member) {
        return this.owner.equals(member);
    }
}
