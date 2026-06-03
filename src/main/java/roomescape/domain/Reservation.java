package roomescape.domain;

import roomescape.exception.InvalidOwnershipException;

public class Reservation {

    private final Long id;
    private final String name;
    private final Slot slot;

    public Reservation(Long id, String name, Slot slot) {
        validateFields(name, slot);
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static Reservation transientOf(String name, Slot slot) {
        return new Reservation(null, name, slot);
    }

    public void validateModifiable(String requesterName) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
        }
    }

    private void validateFields(String name, Slot slot) {
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
