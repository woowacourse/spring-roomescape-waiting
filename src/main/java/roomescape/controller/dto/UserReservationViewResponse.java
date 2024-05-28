package roomescape.controller.dto;

import roomescape.service.dto.response.UserReservationResponse;

import java.time.LocalDate;
import java.time.LocalTime;


public record UserReservationViewResponse(
        long id,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {

    public static UserReservationViewResponse from(UserReservationResponse userReservationResponse) {
        return new UserReservationViewResponse(
                userReservationResponse.id(),
                userReservationResponse.reservationSlot().getTheme().getName(),
                userReservationResponse.reservationSlot().getDate(),
                userReservationResponse.reservationSlot().getTime().getStartAt(),
                ReservationStatusMessageMapper.mapTo(userReservationResponse.status(), userReservationResponse.rank())
        );
    }
}
