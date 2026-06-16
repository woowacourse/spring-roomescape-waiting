package roomescape.domain;

import java.time.LocalDateTime;

public class ReservedEntry extends ReservationEntry {

    private ReservedEntry(Long id, String name, LocalDateTime createdAt) {
        super(id, name, createdAt);
    }

    public static ReservedEntry restore(Long id, String name, LocalDateTime createdAt) {
        return new ReservedEntry(id, name, createdAt);
    }

    public static ReservedEntry of(String name, LocalDateTime createdAt) {
        return new ReservedEntry(null, name, createdAt);
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
        return DeletedEntry.restore(getId(), getReserverName(), getCreatedAt());
    }

    @Override
    public ReservationEntry promote() {
        throw new UnsupportedOperationException("이미 예약되었습니다.");
    }
}
