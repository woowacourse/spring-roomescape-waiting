package roomescape.domain;

import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public abstract class ReservationEntry {

    private Long id;
    private String name;
    private LocalDateTime createdAt;

    protected ReservationEntry(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static ReservationEntry reserve(String name) {
        return new ReservedEntry(null, name, LocalDateTime.now());
    }

    public static ReservationEntry waiting(String name) {
        return new WaitingEntry(null, name, LocalDateTime.now());
    }

    public static ReservationEntry from(Long id, String name, ReservationStatus status, LocalDateTime createdAt) {
        return switch (status) {
            case RESERVED -> new ReservedEntry(id, name, createdAt);
            case WAITING -> new WaitingEntry(id, name, createdAt);
            case DELETED -> new DeletedEntry(id, name, createdAt);
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
        return this.name.equals(name);
    }

    public boolean matches(String name, ReservationStatus status) {
        return this.name.equals(name) && this.getStatus() == status;
    }
}
