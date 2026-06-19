package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.ReservationRank;
import roomescape.domain.ReservationStatus;

public record ReservationRankResponse(Long id, String name, LocalDate date, ReservationTimeResponse timeResponse,
                                      ThemeResponse themeResponse, ReservationStatus status, Long order,
                                      String orderId, String paymentKey, Long amount) {
    public static ReservationRankResponse from(ReservationRank reservation) {
        return new ReservationRankResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus(),
                reservation.getRank(),
                reservation.getOrderId(),
                reservation.getPaymentKey(),
                reservation.getAmount()
        );
    }
}
