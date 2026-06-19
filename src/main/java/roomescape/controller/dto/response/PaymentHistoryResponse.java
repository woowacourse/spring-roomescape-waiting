package roomescape.controller.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record PaymentHistoryResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        String orderId,
        Long amount,
        String paymentKey
) {

    public static PaymentHistoryResponse from(Reservation reservation) {
        return new PaymentHistoryResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().name(),
                reservation.getOrderId(),
                reservation.getAmount(),
                reservation.getPaymentKey()
        );
    }
}
