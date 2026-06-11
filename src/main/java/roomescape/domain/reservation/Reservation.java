package roomescape.domain.reservation;

import common.exception.ErrorCode;
import common.exception.RoomEscapeException;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.theme.Theme;

public class Reservation {
    private static final long TRANSIENT = 0L;
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

    public static Reservation create(ReservationName reservationName, Slot slot, Status status, LocalDateTime now) {
        Objects.requireNonNull(now);
        Reservation reservation = new Reservation(TRANSIENT, reservationName, slot, status, now);
        reservation.ensureNotPast(now);
        return reservation;
    }

    public void ensureNotPast(LocalDateTime now) {
        if (slot.isBefore(now)) {
            throw new RoomEscapeException(ErrorCode.PAST_RESERVATION_NOT_ALLOWED);
        }
    }

    public boolean isEarlierThan(Reservation target) {
        int byTime = createdAt.compareTo(target.getCreatedAt());
        if (byTime != 0) {
            return byTime < 0;
        }
        return id < target.getId();
    }

    public boolean hasSameSlot(Reservation target) {
        return slot.isSame(target.slot);
    }

    public boolean hasSameSlot(Slot slot) {
        return slot.isSame(slot);
    }

    public boolean isSameSlot(Slot target) {
        return slot.isSame(target);
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean hasSameName(ReservationName name) {
        return name.equals(name);
    }

    public Reservation withId(long id) {
        return new Reservation(id, name, slot, status, createdAt);
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
}
