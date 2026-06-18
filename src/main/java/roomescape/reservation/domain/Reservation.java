package roomescape.reservation.domain;

import java.time.LocalDateTime;
import roomescape.common.exception.RoomEscapeException;
import roomescape.reservation.exception.ReservationErrorCode;

public class Reservation {

    private static final long NAME_MAX_LENGTH = 20L;

    private final Long id;
    private final Slot slot;
    private final String name;
    private final ReservationStatus status;
    private final LocalDateTime createdAt;

    private Reservation(Long id, Slot slot, String name, ReservationStatus status, LocalDateTime createdAt) {
        validateSlot(slot);
        validateName(name);
        validateStatus(status);
        validateCreatedAt(createdAt);
        this.id = id;
        this.slot = slot;
        this.name = name;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static Reservation create(Slot slot, String name, ReservationStatus status, LocalDateTime createdAt) {
        return new Reservation(null, slot, name, status, createdAt);
    }

    public static Reservation of(Long id, Slot slot, String name, ReservationStatus status, LocalDateTime createdAt) {
        validateId(id);
        return new Reservation(id, slot, name, status, createdAt);
    }

    private static void validateId(Long id) {
        if (id == null) {
            throw new IllegalStateException("ID는 필수값입니다.");
        }
        if (id < 1) {
            throw new IllegalStateException("ID는 1 이상의 숫자여야 합니다. (입력값: " + id + ")");
        }
    }

    private static void validateSlot(Slot slot) {
        if (slot == null) {
            throw new IllegalStateException("슬롯은 필수값입니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank() || name.length() > NAME_MAX_LENGTH) {
            throw new RoomEscapeException(ReservationErrorCode.INVALID_NAME);
        }
    }

    private static void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalStateException("예약 상태는 필수값입니다.");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalStateException("생성 시각은 필수값입니다.");
        }
    }

    public Reservation confirm() {
        if (!status.isPending()) {
            throw new IllegalStateException("결제 대기 상태에서만 확정으로 전환할 수 있습니다. (현재: " + status + ")");
        }
        return new Reservation(id, slot, name, ReservationStatus.CONFIRMED, createdAt);
    }

    public Reservation promote() {
        if (!status.isWaiting()) {
            throw new IllegalStateException("대기 상태에서만 확정으로 승급할 수 있습니다. (현재: " + status + ")");
        }
        return new Reservation(id, slot, name, ReservationStatus.CONFIRMED, createdAt);
    }

    public void validateNotPast(LocalDateTime now) {
        if (slot.isPast(now)) {
            throw new RoomEscapeException(ReservationErrorCode.RESERVATION_PAST_TIME);
        }
    }

    public boolean isConfirmed() {
        return status.isConfirmed();
    }

    public boolean isPending() {
        return status.isPending();
    }

    public boolean isWaiting() {
        return status.isWaiting();
    }

    public boolean isOccupying() {
        return status.isOccupying();
    }

    public boolean isSameName(String otherName) {
        return name.equals(otherName);
    }

    public Long getId() {
        return id;
    }

    public Slot getSlot() {
        return slot;
    }

    public Long getSlotId() {
        return slot.getId();
    }

    public String getName() {
        return name;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
