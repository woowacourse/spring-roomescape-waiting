package roomescape.reservation.presentation.dto.response;

import java.util.List;
import roomescape.reservation.application.dto.UserReservationResult;
import roomescape.waiting.presentation.dto.response.WaitingResponse;

public record UserReservationResponse(
        List<ReservationResponse> reservations,
        List<WaitingResponse> waitings
) {
    public static UserReservationResponse toResponse(UserReservationResult result) {
        return new UserReservationResponse(
                result.reservations().stream()
                        .map(ReservationResponse::from)
                        .toList(),
                result.waitings().stream()
                        .map(WaitingResponse::from)
                        .toList()
        );
    }
}