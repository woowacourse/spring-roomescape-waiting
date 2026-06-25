package roomescape.reservation.dto;

import java.util.List;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.dto.ReservationTimeResponse;

public record ReservationResponse(
        Long id,
        Long memberId,
        String date,
        ReservationTimeResponse time,
        Long themeId,
        Long storeId,
        ReservationStatus status,
        String orderId,
        String paymentKey,
        Long amount,
        PaymentOrderStatus paymentStatus
) {
    public static ReservationResponse from(Reservation reservation) {
        return from(reservation, null);
    }

    public static ReservationResponse from(Reservation reservation, PaymentOrder paymentOrder) {
        ReservationTime reservationTime = reservation.getTime();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservationTime),
                reservation.getThemeId(),
                reservation.getStoreId(),
                reservation.getStatus(),
                paymentOrder == null ? null : paymentOrder.getOrderId(),
                paymentOrder == null ? null : paymentOrder.getPaymentKey(),
                paymentOrder == null ? null : paymentOrder.getAmount(),
                paymentOrder == null ? null : paymentOrder.getStatus()
        );
    }

    public static List<ReservationResponse> fromAll(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
