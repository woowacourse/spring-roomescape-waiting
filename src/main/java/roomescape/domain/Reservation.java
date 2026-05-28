package roomescape.domain;

import lombok.RequiredArgsConstructor;
import roomescape.exception.PastReservationException;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class Reservation {

    private final Long id;
    private final String username;
    private final Slot slot;

    public static Reservation create(long id, String username, Slot slot) {
        return new Reservation(id, username, slot);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean isOwnedBy(String name) {
        return this.username.equals(name);
    }

    public void validateCancelable(LocalDateTime now) {
        if (isPast(now)) {
            throw new PastReservationException("이미 시작된 예약은 취소할 수 없습니다.");
        }
    }

    public Reservation withSlot(Slot newSlot) {
        return new Reservation(id, username, newSlot);
    }

    public String username() {
        return username;
    }

    public Slot slot() {
        return slot;
    }

    public Long id() {
        return id;
    }
}