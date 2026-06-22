package roomescape.dto.response;

import roomescape.domain.*;

import java.time.LocalDate;

public record ReservationResult(
        Long id,

        String name,
        Long amount,
        String orderId,

        String paymentKey,
        String orderStatus,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        ReservationStatus status
) {

    public static ReservationResult from(final Reservation reservation, final Order order) {
        final ReservationTime reservationTime = reservation.getTime();
        final Theme theme = reservation.getTheme();

        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                order.getAmount(),
                order.getOrderId(),
                order.getPaymentKey(),
                order.getStatus().name(),
                reservation.getReservationDate().getDate(),
                ReservationTimeResult.from(reservationTime),
                ThemeResult.from(theme),
                ReservationStatus.RESERVATION
        );
    }
}
