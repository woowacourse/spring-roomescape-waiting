package roomescape.controller.dto.response;

import roomescape.domain.ReservationStatus;
import roomescape.service.dto.UserReservation;

import java.time.LocalDate;

public record UserReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Long rank,
        String orderId,
        String paymentKey,
        Long amount
) {

    public static UserReservationResponse from(UserReservation userReservation) {
        return new UserReservationResponse(
                userReservation.id(),
                userReservation.name(),
                userReservation.date(),
                ReservationTimeResponse.from(userReservation.time()),
                ThemeResponse.from(userReservation.theme()),
                userReservation.status(),
                userReservation.rank(),
                userReservation.orderId(),
                userReservation.paymentKey(),
                userReservation.amount()
        );
    }
}
