package roomescape.domain;

import java.time.LocalDateTime;

public class PendingEntry extends ReservationEntry {

    private PendingEntry(Long id, String name, LocalDateTime createdAt) {
        super(id, name, createdAt);
    }

    public static PendingEntry restore(Long id, String name, LocalDateTime createdAt) {
        return new PendingEntry(id, name, createdAt);
    }

    public static PendingEntry of(String name, LocalDateTime createdAt) {
        return new PendingEntry(null, name, createdAt);
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.PENDING;
    }

    @Override
    public boolean isReserved() {
        return false;
    }

    @Override
    public boolean isWaiting() {
        return false;
    }

    @Override
    public boolean isPending() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public ReservationEntry cancel() {
        return DeletedEntry.restore(getId(), getReserverName(), getCreatedAt());
    }

    @Override
    public ReservationEntry promote() {
        return ReservedEntry.restore(getId(), getReserverName(), getCreatedAt());
    }
}
