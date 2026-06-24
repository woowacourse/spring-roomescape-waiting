package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        CreateReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Integer order,
        String orderId,
        String paymentKey,
        Long amount
) {
    public static MyReservationResponse fromReservation(Reservation reservation, PaymentOrder paymentOrder) {
        ReservationStatus status = resolveStatus(reservation, paymentOrder);
        String paymentKey = (paymentOrder != null) ? paymentOrder.getPaymentKey() : null;
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                new CreateReservationTimeResponse(
                        reservation.getTime().getId(),
                        reservation.getTime().getStartAt()
                ),
                new ThemeResponse(
                        reservation.getTheme().getId(),
                        reservation.getTheme().getName(),
                        reservation.getTheme().getDescription(),
                        reservation.getTheme().getThumbnail()
                ),
                status,
                null,
                (paymentOrder != null) ? paymentOrder.getOrderId() : null,
                paymentKey,
                (paymentOrder != null) ? paymentOrder.getAmount() : null
        );
    }

    private static ReservationStatus resolveStatus(Reservation reservation, PaymentOrder paymentOrder) {
        if (reservation.getReservationStatus() == roomescape.domain.ReservationStatus.CONFIRMED) {
            return ReservationStatus.RESERVED;
        }
        if (paymentOrder != null && paymentOrder.getPaymentKey() != null) {
            return ReservationStatus.NEED_CHECK;
        }
        return ReservationStatus.PENDING_PAYMENT;
    }

    public static MyReservationResponse fromReservationWaiting(ReservationWaitingOrderResponse waitingOrderResponse) {
        ReservationWaiting waiting = waitingOrderResponse.waiting();
        return new MyReservationResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getReservationDate(),
                new CreateReservationTimeResponse(
                        waiting.getTime().getId(),
                        waiting.getTime().getStartAt()
                ),
                new ThemeResponse(
                        waiting.getTheme().getId(),
                        waiting.getTheme().getName(),
                        waiting.getTheme().getDescription(),
                        waiting.getTheme().getThumbnail()
                ),
                ReservationStatus.WAITING,
                waitingOrderResponse.order(),
                null,
                null,
                null
        );
    }
}
