package roomescape.dto.response;

import roomescape.domain.Reservation;

import java.time.LocalDate;

public record ReservationPaymentResponse(
        Long reservationId,
        String name,
        LocalDate date,
        CreateReservationTimeResponse time,
        ThemeResponse theme,
        String orderId,
        long amount
) {
    public static ReservationPaymentResponse from(Reservation reservation, String orderId, long amount) {
        return new ReservationPaymentResponse(
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
                orderId,
                amount
        );
    }
}
