package roomescape.dto.reservation;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservationOrder.OrderStatus;
import roomescape.domain.reservationOrder.ReservationOrder;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

public record ReservationWithPaymentResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        boolean paid,
        String paymentStatus,
        String orderId,
        Long amount,
        String paymentKey
) {

    public static ReservationWithPaymentResponse of(Reservation reservation, ReservationOrder order) {
        ReservationTimeResponse timeResponse = ReservationTimeResponse.from(reservation.getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservation.getTheme());

        if (order == null) {
            OrderStatus status = OrderStatus.PENDING;
            if (reservation.isPaid()) {
                status = OrderStatus.CONFIRMED;
            }
            return new ReservationWithPaymentResponse(reservation.getId(), reservation.getName(), reservation.getDate(),
                    timeResponse, themeResponse, reservation.isPaid(), status.name(), null, null, null);
        }

        return new ReservationWithPaymentResponse(reservation.getId(), reservation.getName(), reservation.getDate(),
                timeResponse, themeResponse, reservation.isPaid(), order.getStatus().name(),
                order.getId(), order.getAmount(), order.getPaymentKey());
    }
}
