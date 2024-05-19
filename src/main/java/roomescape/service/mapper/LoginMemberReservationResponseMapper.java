package roomescape.service.mapper;

import roomescape.domain.Reservation;
import roomescape.dto.LoginMemberReservationResponse;

public class LoginMemberReservationResponseMapper {
    public static LoginMemberReservationResponse toResponse(Reservation reservation) {
        return new LoginMemberReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime(),
                "예약");
    }
}
