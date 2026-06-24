package roomescape.dto;

import roomescape.domain.Payment;
import roomescape.domain.Reservation;

import java.time.LocalDate;

public record ReservationCreateResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String orderId,
        long amount
) {
    public static ReservationCreateResponse of(Reservation reservation, Payment payment) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                payment.getOrderId(),
                payment.getAmount()
        );
    }
}
