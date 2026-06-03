package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;

public class Reservation {
    private final long id;
    private final ReservationName name;
    private final Slot slot;
    private final Status status;
    private final LocalDateTime createdAt;

    private Reservation(long id, ReservationName name, Slot slot, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.slot = Objects.requireNonNull(slot);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Reservation load(long id, ReservationName reservationName, Slot slot, Status status,
                                   LocalDateTime createdAt) {
        return new Reservation(id, reservationName, slot, status, createdAt);
    }

    public static Reservation reserve(ReservationName reservationName, Slot slot, Status status, LocalDateTime now) {
        Objects.requireNonNull(now);
        Reservation reservation = new Reservation(0L, reservationName, slot, status, now);
        reservation.ensureNotPast(now);
        return reservation;
    }

    public void ensureNotPast(LocalDateTime now) {
        LocalDateTime requestDateTime = LocalDateTime.of(slot.getDate().getValue(), slot.getTime().getStartAt());

        if (requestDateTime.isBefore(now)) {
            throw new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    public long getId() {
        return id;
    }

    public ReservationName getName() {
        return name;
    }

    public Slot getSlot() {
        return slot;
    }

    public ReservationDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(slot, that.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, slot);
    }
}
