package roomescape.domain;

import java.util.Objects;
import roomescape.exception.InvalidOwnershipException;

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

    public Reservation reschedule(Slot slot) {
        Slot patchedSlot = Objects.requireNonNullElse(slot, this.slot);
        return new Reservation(this.id, this.name, patchedSlot);
    }

    public void validateModifiable(String requesterName) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
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
