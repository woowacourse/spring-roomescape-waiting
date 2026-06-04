package roomescape.domain;

import java.time.LocalDate;
import roomescape.domain.exception.InvalidDomainException;

public class Waiting {
    private static final int MAX_NAME_LENGTH = 30;

    private final Long id;
    private final String name;
    private final Slot slot;
    private final int orderIndex;

    private Waiting(Long id, String name, Slot slot, int orderIndex) {
        validate(name, slot, orderIndex);
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.orderIndex = orderIndex;
    }

    public static Waiting create(String name, Slot slot, int order) {
        return new Waiting(null, name, slot, order);
    }

    public static Waiting create(String name, LocalDate date,
                                 ReservationTime time, Theme theme, int order) {
        return create(name, new Slot(date, time, theme), order);
    }

    public static Waiting withId(Long id, String name, Slot slot, int order) {
        return new Waiting(id, name, slot, order);
    }

    public static Waiting withId(Long id, String name, LocalDate date,
                                 ReservationTime time, Theme theme, int order) {
        return withId(id, name, new Slot(date, time, theme), order);
    }

    public Waiting withOrderIndex(int newOrderIndex) {
        return new Waiting(this.id, this.name, this.slot, newOrderIndex);
    }

    public boolean isSameSlot(Slot slot) {
        return this.slot.equals(slot);
    }

    private static void validate(String name, Slot slot, int order) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainException("대기자 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                    "대기자 이름은 %d자를 초과할 수 없습니다.".formatted(MAX_NAME_LENGTH));
        }
        if (slot == null) {
            throw new InvalidDomainException("슬롯은 비어 있을 수 없습니다.");
        }
        if (order < 1) {
            throw new InvalidDomainException("대기 순번은 1 이상이어야 합니다.");
        }
    }

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }

    public int getOrderIndex() {
        return orderIndex;
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
}
