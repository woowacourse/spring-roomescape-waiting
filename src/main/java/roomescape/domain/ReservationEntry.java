package roomescape.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

@Getter
public abstract class ReservationEntry {

    private final Long id;
    private final String reserverName;
    private final LocalDateTime createdAt;

    protected ReservationEntry(Long id, String reserverName, LocalDateTime createdAt) {
        this.id = id;
        this.reserverName = reserverName;
        this.createdAt = createdAt;
    }

    public static ReservationEntry reserve(String reserverName, LocalDateTime createdAt) {
        return of(null, reserverName, ReservationStatus.RESERVED, createdAt);
    }

    public static ReservationEntry waiting(String reserverName, LocalDateTime createdAt) {
        return of(null, reserverName, ReservationStatus.WAITING, createdAt);
    }

    public static ReservationEntry pending(String reserverName, LocalDateTime createdAt) {
        return PendingEntry.of(reserverName, createdAt);
    }

    public static ReservationEntry restore(Long id, String reserverName, ReservationStatus status,
                                           LocalDateTime createdAt) {
        return of(Objects.requireNonNull(id, "복원 시 id 값은 필수입니다"), reserverName, status, createdAt);
    }

    private static ReservationEntry of(Long id, String reserverName, ReservationStatus status,
                                       LocalDateTime createdAt) {
        return switch (status) {
            case PENDING -> PendingEntry.restore(id, reserverName, createdAt);
            case RESERVED -> ReservedEntry.restore(id, reserverName, createdAt);
            case WAITING -> WaitingEntry.restore(id, reserverName, createdAt);
            case DELETED -> DeletedEntry.restore(id, reserverName, createdAt);
        };
    }

    public abstract ReservationStatus getStatus();

    public abstract boolean isReserved();

    public abstract boolean isWaiting();

    public boolean isPending() {
        return false;
    }

    public abstract boolean isActive();

    public abstract ReservationEntry cancel();

    public abstract ReservationEntry promote();

    public boolean isSameId(long id) {
        return this.id != null && this.id.equals(id);
    }

    public boolean hasSameName(String name) {
        return this.reserverName.equals(name);
    }

    public boolean matches(String name, ReservationStatus status) {
        return this.reserverName.equals(name) && this.getStatus() == status;
    }
}
