package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.payment.domain.PaymentStatus;
import roomescape.reservation.domain.ReservationStatus;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeSimpleResponse theme,
        ReservationStatus status,
        Long waitRank,
        String orderId,
        String paymentKey,
        Long paymentAmount,
        PaymentStatus paymentStatus
) {
}
