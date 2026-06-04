package roomescape.domain;

import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

public record Reservation(Long id, Member owner, Slot slot) {

    public static Reservation forNew(Member owner, Slot slot) {
        return new Reservation(null, owner, slot);
    }

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

    public boolean isOwnedBy(Member member) {
        return owner.equals(member);
    }

    private boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }
}