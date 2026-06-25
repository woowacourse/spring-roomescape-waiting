package roomescape.dto;

import roomescape.domain.Payment;
import roomescape.domain.Reservation;

import java.time.LocalDate;

public record MyReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String paymentStatus,
        String orderId,
        String paymentKey,
        Long amount
) {
    public static MyReservationResponse of(Reservation reservation, Payment payment) {
        String paymentStatus = reservation.isPending() ? "PENDING" : "CONFIRMED";
        if (payment == null) {
            return new MyReservationResponse(
                    reservation.getId(),
                    reservation.getName(),
                    reservation.getDate(),
                    ReservationTimeResponse.from(reservation.getTime()),
                    ThemeResponse.from(reservation.getTheme()),
                    paymentStatus,
                    null,
                    null,
                    null
            );
        }
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                paymentStatus,
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getAmount()
        );
    }
}
