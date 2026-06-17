package roomescape.domain;

import java.time.LocalDate;
import roomescape.domain.exception.InvalidDomainException;
import roomescape.domain.policy.ReservationPolicy;

public class Reservation {
    private static final int MAX_NAME_LENGTH = 30;

    private final Long id;
    private final String name;
    private final Slot slot;
    private final ReservationStatus status;

    private Reservation(Long id, String name, Slot slot, ReservationStatus status) {
        validate(name, slot);
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.status = status;
    }

    public static Reservation create(String name, Slot slot, ReservationPolicy policy) {
        policy.validateCreatable(slot.getDate(), slot.getTime().getStartAt());
        return new Reservation(null, name, slot, ReservationStatus.PENDING);
    }

    public static Reservation create(String name, LocalDate date,
                                     ReservationTime time, Theme theme,
                                     ReservationPolicy policy) {
        return create(name, new Slot(date, time, theme), policy);
    }

    public static Reservation withId(Long id, String name, Slot slot) {
        return withId(id, name, slot, ReservationStatus.CONFIRMED);
    }

    public static Reservation withId(Long id, String name, Slot slot, ReservationStatus status) {
        return new Reservation(id, name, slot, status);
    }

    public static Reservation withId(Long id, String name, LocalDate date,
                                     ReservationTime time, Theme theme) {
        return withId(id, name, new Slot(date, time, theme));
    }

    public static Reservation withId(Long id, String name, LocalDate date,
                                     ReservationTime time, Theme theme,
                                     ReservationStatus status) {
        return withId(id, name, new Slot(date, time, theme), status);
    }

    public static Reservation promote(Waiting w) {
        return new Reservation(null, w.getName(), w.getSlot(), ReservationStatus.CONFIRMED);
    }

    public Reservation confirm() {
        return new Reservation(id, name, slot, ReservationStatus.CONFIRMED);
    }

    public Reservation fail() {
        return new Reservation(id, name, slot, ReservationStatus.FAILED);
    }

    private static void validate(String name, Slot slot) {
        validateName(name);
        validateSlot(slot);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainException("예약자 이름은 비어 있을 수 없습니다.");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new InvalidDomainException(
                    "예약자 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다."
            );
        }
    }

    private static void validateSlot(Slot slot) {
        if (slot == null) {
            throw new InvalidDomainException("슬롯은 비어 있을 수 없습니다.");
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

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }
}
