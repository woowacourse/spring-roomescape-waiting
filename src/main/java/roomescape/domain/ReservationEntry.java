package roomescape.domain;

import java.time.LocalDateTime;
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

    public static ReservationEntry restore(Long id, String reserverName, ReservationStatus status,
                                           LocalDateTime createdAt) {
        return of(id, reserverName, status, createdAt);
    }

    private static ReservationEntry of(Long id, String reserverName, ReservationStatus status,
                                       LocalDateTime createdAt) {
        return switch (status) {
            case RESERVED -> new ReservedEntry(id, reserverName, createdAt);
            case WAITING -> new WaitingEntry(id, reserverName, createdAt);
            case DELETED -> new DeletedEntry(id, reserverName, createdAt);
        };
    }

    public abstract ReservationStatus getStatus();

    public abstract boolean isReserved();

    public abstract boolean isWaiting();

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
