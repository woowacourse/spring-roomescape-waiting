package roomescape.domain;

import java.time.LocalDateTime;
import roomescape.exception.InvalidOwnershipException;
import roomescape.exception.PastSlotControlException;
import roomescape.exception.PastTimeException;

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
            throw new PastTimeException("지난 시간/날짜로 예약 대기를 추가하실 수 없습니다.");
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
