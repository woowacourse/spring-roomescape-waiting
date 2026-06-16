package roomescape.domain;

import java.time.LocalDate;
import roomescape.domain.exception.DomainValidationException;

public class Reservation {

    private static final int MAX_NAME_LENGTH = 30;

    private final Long id;
    private final String name;
    private final ReservationSlot slot;
    private final ReservationStatus status;

    public Reservation(Long id, String name, ReservationSlot slot) {
        this(id, name, slot, ReservationStatus.CONFIRMED);
    }

    public Reservation(Long id, String name, ReservationSlot slot, ReservationStatus status) {
        validateNameLength(name);
        this.id = id;
        this.name = name;
        this.slot = slot;
        this.status = status;
    }

    private static void validateNameLength(String name) {
        if (name.length() > MAX_NAME_LENGTH) {
            throw new DomainValidationException("예약자 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    public ReservationStatus getStatus() {
        return status;
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
