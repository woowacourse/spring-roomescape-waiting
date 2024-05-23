package roomescape.service.mapper;

import roomescape.domain.Reservation;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationWaitingResponse;

public class LoginMemberReservationResponseMapper {
    public static LoginMemberReservationResponse toResponse(Reservation reservation) {
        return new LoginMemberReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getTime(),
                "예약");
    }

    public static LoginMemberReservationResponse from(ReservationWaitingResponse waitingResponse) {
        return new LoginMemberReservationResponse(
                waitingResponse.id(),
                waitingResponse.themeName(),
                waitingResponse.date(),
                waitingResponse.startAt(),
                "%d번째 예약 대기".formatted(waitingResponse.priority())
        );
    }
}
