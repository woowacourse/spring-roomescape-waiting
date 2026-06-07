package roomescape.reservation.domain;

import java.time.LocalDateTime;

public record ReservationHistory(
        Long reservationId,
        String name,
        Slot slot,
        long requestOrder,
        LocalDateTime createdAt,
        LocalDateTime canceledAt
) {
    public ReservationHistory {
        validate(reservationId, name, slot, createdAt, canceledAt);
    }

    public ReservationEntry canceled() {
        return ReservationEntry.canceled(toReservation());
    }

    private Reservation toReservation() {
        return Reservation.reconstruct(
                reservationId,
                name,
                slot
        );
    }

    private static void validate(
            Long reservationId,
            String name,
            Slot slot,
            LocalDateTime createdAt,
            LocalDateTime canceledAt
    ) {
        validateReservationId(reservationId);
        validateName(name);
        validateSlot(slot);
        validateCreatedAt(createdAt);
        validateCanceledAt(canceledAt);
    }

    private static void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 id는 비어 있을 수 없습니다.");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 비어 있을 수 없습니다.");
        }
    }

    private static void validateSlot(Slot slot) {
        if (slot == null) {
            throw new IllegalArgumentException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }

    private static void validateCreatedAt(LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("예약 생성 시각은 비어 있을 수 없습니다.");
        }
    }

    private static void validateCanceledAt(LocalDateTime canceledAt) {
        if (canceledAt == null) {
            throw new IllegalArgumentException("예약 취소 시각은 비어 있을 수 없습니다.");
        }
    }
}
