package roomescape.domain;

import roomescape.exception.ForbiddenException;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

public record Reservation(Long id, Member owner, Slot slot, ReservationStatus status) {

    public static Reservation forNew(Member owner, Slot slot) {
        return new Reservation(null, owner, slot, ReservationStatus.CONFIRMED);
    }

    public static Reservation forPendingPayment(Member owner, Slot slot) {
        return new Reservation(null, owner, slot, ReservationStatus.PENDING_PAYMENT);
    }

    public static Reservation create(long id, Member owner, Slot slot) {
        return new Reservation(id, owner, slot, ReservationStatus.CONFIRMED);
    }

    public static Reservation create(long id, Member owner, Slot slot, ReservationStatus status) {
        return new Reservation(id, owner, slot, status);
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
        return new Reservation(id, owner, newSlot, status);
    }

    public Reservation confirm() {
        return new Reservation(id, owner, slot, ReservationStatus.CONFIRMED);
    }

    public boolean isOwnedBy(Member member) {
        return owner.equals(member);
    }

    private boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }
}
