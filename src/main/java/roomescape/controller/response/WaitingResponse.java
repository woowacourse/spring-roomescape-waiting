package roomescape.controller.response;

import roomescape.service.result.WaitingResult;

import java.time.LocalDate;

public record WaitingResponse(
        LoginMemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static WaitingResponse from(WaitingResult waitingResult) {
        return new WaitingResponse(
                LoginMemberResponse.from(waitingResult.member()),
                waitingResult.date(),
                ReservationTimeResponse.from(waitingResult.time()),
                ThemeResponse.from(waitingResult.theme())
        );
    }
}
