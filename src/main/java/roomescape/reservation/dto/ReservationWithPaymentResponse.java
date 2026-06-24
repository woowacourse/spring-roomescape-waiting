package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.PaymentStatus;
import roomescape.reservationtime.dto.TimeResponse;

public record ReservationWithPaymentResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        String themeName,
        PaymentStatus status,
        String orderId,
        String paymentKey,
        Long amount
) {
}
