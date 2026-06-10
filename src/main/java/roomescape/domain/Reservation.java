package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.custom.InvalidDomainValueException;

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

    public Reservation(String name, Slot slot) {
        this(null, name, slot);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, name, slot);
    }

    public boolean isSameUser(String name) {
        return this.name.equals(name);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
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

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(name, that.name) && Objects.equals(slot, that.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, slot);
    }

    private void validate(String name, Slot slot) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainValueException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (slot == null) {
            throw new InvalidDomainValueException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }
}
