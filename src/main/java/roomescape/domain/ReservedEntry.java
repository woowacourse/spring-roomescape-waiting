package roomescape.domain;

import java.time.LocalDateTime;

public class ReservedEntry extends ReservationEntry {

    public ReservedEntry(Long id, String name, LocalDateTime createdAt) {
        super(id, name, createdAt);
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.RESERVED;
    }

    @Override
    public boolean isReserved() {
        return true;
    }

    @Override
    public boolean isWaiting() {
        return false;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public ReservationEntry cancel() {
        return new DeletedEntry(getId(), getName(), getCreatedAt());
    }

    @Override
    public ReservationEntry promote() {
        return this;
    }
}
