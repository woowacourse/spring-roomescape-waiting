package roomescape.controller.dto.response;

import roomescape.service.dto.UserReservation;

import java.time.LocalDate;

public record UserReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status,
        Long rank
) {

    public static UserReservationResponse from(UserReservation userReservation) {
        return new UserReservationResponse(
                userReservation.id(),
                userReservation.name(),
                userReservation.date(),
                ReservationTimeResponse.from(userReservation.time()),
                ThemeResponse.from(userReservation.theme()),
                userReservation.status(),
                userReservation.rank()
        );
    }
}
