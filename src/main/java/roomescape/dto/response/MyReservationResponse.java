package roomescape.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWaiting;

import java.time.LocalDate;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        CreateReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Integer order,
        PaymentOrderStatus paymentStatus,
        String orderId,
        String paymentKey,
        Long amount
) {
    public static MyReservationResponse fromReservation(Reservation reservation) {
        return fromReservation(reservation, null);
    }

    public static MyReservationResponse fromReservation(Reservation reservation, PaymentOrder paymentOrder) {
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
                ReservationStatus.RESERVED,
                null,
                paymentOrder == null ? null : paymentOrder.getStatus(),
                paymentOrder == null ? null : paymentOrder.getOrderId(),
                reservation.getPaymentKey(),
                paymentOrder == null ? null : paymentOrder.getAmount()
        );
    }

    public static MyReservationResponse fromReservationWaiting(ReservationWaiting reservationWaiting, int order) {
        return new MyReservationResponse(
                reservationWaiting.getId(),
                reservationWaiting.getName(),
                reservationWaiting.getReservationDate(),
                new CreateReservationTimeResponse(
                        reservationWaiting.getTime().getId(),
                        reservationWaiting.getTime().getStartAt()
                ),
                new ThemeResponse(
                        reservationWaiting.getTheme().getId(),
                        reservationWaiting.getTheme().getName(),
                        reservationWaiting.getTheme().getDescription(),
                        reservationWaiting.getTheme().getThumbnail()
                ),
                ReservationStatus.WAITING,
                order,
                null,
                null,
                null,
                null
        );
    }
}
