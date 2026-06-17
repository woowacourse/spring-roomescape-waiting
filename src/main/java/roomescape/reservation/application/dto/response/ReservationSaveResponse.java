package roomescape.reservation.application.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationSaveResponse(
        Long id,
        Long memberId,
        Long slotId,
        String orderId,
        int amount,
        ReservationStatus status,
        String orderName
) {
    public ReservationSaveResponse(Long id, Long memberId, Long slotId) {
        this(id, memberId, slotId, null, 0, ReservationStatus.CONFIRMED, null);
    }

    public static ReservationSaveResponse from(Reservation reservation) {
        return new ReservationSaveResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getSlotId(),
                reservation.getOrderId(),
                reservation.getAmount(),
                reservation.getStatus(),
                buildOrderName(reservation)
        );
    }

    private static String buildOrderName(Reservation reservation) {
        return reservation.getSlot().getTheme().getName() + " " + reservation.getSlot().getStartAt();
    }
}
