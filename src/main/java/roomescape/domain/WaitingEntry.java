package roomescape.domain;

import java.time.LocalDateTime;

public class WaitingEntry extends ReservationEntry {

    public WaitingEntry(Long id, String name, LocalDateTime createdAt) {
        super(id, name, createdAt);
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
        return new DeletedEntry(getId(), getReserverName(), getCreatedAt());
    }

    @Override
    public ReservationEntry promote() {
        return new ReservedEntry(getId(), getReserverName(), getCreatedAt());
    }
}
