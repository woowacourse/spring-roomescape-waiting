package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.ReservationErrorCode;
import roomescape.exception.ReservationSlotErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.exception.WaitingErrorCode;

public class Waiting {

    private static final long NAME_MAX_LENGTH = 20L;

    private final Long id;
    private final String name;
    private final ReservationSlot reservationSlot;
    private final Long waitingNumber;

    private Waiting(Long id, String name, ReservationSlot reservationSlot, Long waitingNumber) {
        validateName(name);
        validateReservationSlot(reservationSlot);
        validateWaitingNumber(waitingNumber);
        this.id = id;
        this.name = name;
        this.reservationSlot = reservationSlot;
        this.waitingNumber = waitingNumber;
    }

    public static Waiting create(String name, ReservationSlot reservationSlot, Long waitingNumber) {
        return new Waiting(null, name, reservationSlot, waitingNumber);
    }

    public static Waiting of(Long id, String name, ReservationSlot reservationSlot,
            Long waitingNumber) {
        validateId(id);
        return new Waiting(id, name, reservationSlot, waitingNumber);
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

    private static void validateWaitingNumber(Long waitingNumber) {
        if (waitingNumber == null) {
            throw new RoomEscapeException(WaitingErrorCode.INVALID_WAITING_NUMBER);
        }
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

    public Long getWaitingNumber() {
        return waitingNumber;
    }

    public void validateNotPastTime(LocalDateTime now) {
        reservationSlot.validateNotPastTime(now);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting that = (Waiting) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Waiting{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", reservationSlot=" + reservationSlot +
                ", waitingNumber=" + waitingNumber +
                '}';
    }
}
