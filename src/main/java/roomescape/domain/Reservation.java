package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastSlotControlException;
import roomescape.exception.PastTimeException;

public class Reservation {

    private final Long id;
    private final String name;
    private final Slot slot;

    public Reservation(Long id, String name, Slot slot) {
        validate(name, slot);
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static Reservation transientOf(String name, Slot slot) {
        return new Reservation(null, name, slot);
    }

    public Reservation reschedule(Slot slot, LocalDateTime currentDateTime) {
        Slot patchedSlot = Objects.requireNonNullElse(slot, this.slot);
        validateNotPast(currentDateTime);
        return new Reservation(this.id, this.name, patchedSlot);
    }

    public void validateModifiable(String requesterName, LocalDateTime currentDateTime) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
        }
        if (this.slot.isPast(currentDateTime)) {
            throw new PastSlotControlException();
        }
    }

    public void validateNotPast(LocalDateTime currentDateTime) {
        if (this.slot.isPast(currentDateTime)) {
            throw new PastTimeException("지난 시간/날짜로 예약하실 수 없습니다.");
        }
    }

    private void validate(String name, Slot slot) {
        if (name == null || name.isBlank() || slot == null) {
            throw new IllegalArgumentException("필수 예약 정보가 누락되었습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Slot getSlot() {
        return slot;
    }
}
