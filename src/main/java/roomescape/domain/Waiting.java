package roomescape.domain;

import roomescape.exception.InvalidOwnershipException;

public class Waiting {

    private final Long id;
    private final String name;
    private final Slot slot;
    private final Integer waitingNumber;

    public Waiting(Long id, String name, Slot slot, Integer waitingNumber) {
        validateFields(name, slot);
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.waitingNumber = waitingNumber;
    }

    public static Waiting transientOf(String name, Slot slot) {
        return new Waiting(null, name, slot, null);
    }

    public void validateModifiable(String requesterName) {
        if (!this.name.equals(requesterName)) {
            throw new InvalidOwnershipException();
        }
    }

    private void validateFields(String name, Slot slot) {
        if (name == null || name.isBlank() || slot == null) {
            throw new IllegalArgumentException("필수 대기 정보가 누락되었습니다.");
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

    public Integer getWaitingNumber() {
        return waitingNumber;
    }
}
