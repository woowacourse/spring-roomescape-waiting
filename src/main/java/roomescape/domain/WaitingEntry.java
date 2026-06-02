package roomescape.domain;

import java.time.LocalDateTime;

public class WaitingEntry extends ReservationEntry {

    private WaitingEntry(Long id, String name, LocalDateTime createdAt) {
        super(id, name, createdAt);
    }

    public static WaitingEntry restore(Long id, String name, LocalDateTime createdAt) {
        return new WaitingEntry(id, name, createdAt);
    }

    public static WaitingEntry of(String name, LocalDateTime createdAt) {
        return new WaitingEntry(null, name, createdAt);
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.WAITING;
    }

    @Override
    public boolean isReserved() {
        return false;
    }

    @Override
    public boolean isWaiting() {
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
