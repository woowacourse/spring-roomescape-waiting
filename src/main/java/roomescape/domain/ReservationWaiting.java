package roomescape.domain;

import java.time.LocalDateTime;

public class ReservationWaiting {

    private final Long id;
    private final String name;
    private final ReservationSlot slot;

    public ReservationWaiting(Long id, String name, ReservationSlot slot) {
        validateName(name);
        validateSlot(slot);

        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public ReservationWaiting withId(Long id) {
        return new ReservationWaiting(id, name, slot);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name은 비어 있을 수 없습니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("name은 255자를 넘을 수 없습니다.");
        }
    }

    private void validateSlot(ReservationSlot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("slot은 비어 있을 수 없습니다.");
        }
    }
}
