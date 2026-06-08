package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.ReservationSlotErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.WaitingErrorCode;

public class Reservation {

    private static final long NAME_MAX_LENGTH = 20L;
    private final Long id;
    private final String name;
    private final ReservationSlot reservationSlot;

    private Reservation(Long id, String name, ReservationSlot reservationSlot) {
        validateName(name);
        validateReservationSlot(reservationSlot);

        this.id = id;
        this.name = name;
        this.reservationSlot = reservationSlot;
    }

    public static Reservation create(String name, ReservationSlot reservationSlot) {
        return new Reservation(null, name, reservationSlot);
    }

    public static Reservation of(Long id, String name, ReservationSlot reservationSlot) {
        validateId(id);
        return new Reservation(id, name, reservationSlot);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalStateException("ID는 필수값입니다.");
        }
        if (id < 1) {
            throw new IllegalStateException("ID는 1 이상의 숫자여야 합니다. (입력값: " + id + ")");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_NAME);
        }
    }

    private static void validateReservationSlot(ReservationSlot reservationSlot) {
        if (reservationSlot == null) {
            throw new RoomEscapeException(ReservationSlotErrorCode.INVALID_RESERVATION_SLOT);
        }
    }

    public void validateNotMyReservation(String inputName) {
        if (name.equals(inputName)) {
            throw new RoomEscapeException(WaitingErrorCode.CANNOT_WAITLIST_CONFIRMED_SLOT);
        }
    }

    public void validateNotPastTime(LocalDateTime now) {
        reservationSlot.validateNotPastTime(now);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ReservationSlot getReservationSlot() {
        return reservationSlot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", reservationSlot=" + reservationSlot +
                '}';
    }
}
