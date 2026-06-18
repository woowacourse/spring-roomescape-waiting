package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record ReservationResponse(Long id, String name, LocalDate date, ReservationTimeResponse timeResponse,
                                  ThemeResponse themeResponse, ReservationStatus status, String orderId, String paymentKey, Long amount) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus(),
                reservation.getOrderId(),
                reservation.getPaymentKey(),
                reservation.getAmount()
        );
    }
}
