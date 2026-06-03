package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.domain.ReservationException;

public class Reservation {

    private static final int RESERVATION_CHANGE_DEADLINE_PASSED = 1;

    private Long id;
    private Slot slot;
    private String name;

    public static Reservation createFutureReservation(String name, Slot slot, LocalDateTime now) {
        validateNotPastDateTime(slot, now);
        return new Reservation(null, slot, name);
    }

    private static void validateNotPastDateTime(Slot slot, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(slot.getDate(), slot.getTime().getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new ReservationException(ReservationErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    public Reservation(Slot slot, String name) {
        this(null, slot, name);
    }

    public Reservation(Long id, Slot slot, String name) {
        this.id = id;
        this.slot = slot;
        this.name = name;
    }

    public Reservation createWithId(long id) {
        return new Reservation(id, this.slot, this.name);
    }

    public boolean isNotModifiableAt(LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(
                slot.getDate(),
                slot.getTime().getStartAt()
        );
        LocalDateTime cancelDeadline = reservationDateTime.minusDays(RESERVATION_CHANGE_DEADLINE_PASSED);
        return now.isAfter(cancelDeadline);
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        if (id == null) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
