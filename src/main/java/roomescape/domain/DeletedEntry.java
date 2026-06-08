package roomescape.domain;

import java.time.LocalDateTime;

public class DeletedEntry extends ReservationEntry {

    private DeletedEntry(Long id, String name, LocalDateTime createdAt) {
        super(id, name, createdAt);
    }

    public static DeletedEntry restore(Long id, String name, LocalDateTime createdAt) {
        return new DeletedEntry(id, name, createdAt);
    }

    public static DeletedEntry of(String name, LocalDateTime createdAt) {
        return new DeletedEntry(null, name, createdAt);
    }

    @Override
    public ReservationStatus getStatus() {
        return ReservationStatus.DELETED;
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
    public boolean isActive() {
        return false;
    }

    @Override
    public ReservationEntry cancel() {
        return this;
    }

    @Override
    public ReservationEntry promote() {
        throw new IllegalStateException("삭제된 예약은 승격할 수 없습니다.");
    }
}
