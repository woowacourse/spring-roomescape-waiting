package roomescape.presentation.api.reservation.response;

import roomescape.application.reservation.query.dto.WaitingResult;

public record WaitingResponse(
        Long id,
        String name,
        String theme,
        String date,
        String startAt
) {

    public static WaitingResponse from(WaitingResult waitingResult) {
        return new WaitingResponse(
                waitingResult.waitingId(),
                waitingResult.memberName(),
                waitingResult.themeName(),
                waitingResult.reservationDate().toString(),
                waitingResult.reservationTime().toString()
        );
    }
}
