package roomescape.controller.response;

import roomescape.service.result.WaitingResult;

import java.time.LocalDate;
import java.util.List;

public record WaitingResponse(
        Long id,
        LoginMemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static WaitingResponse from(WaitingResult waitingResult) {
        return new WaitingResponse(
                waitingResult.id(),
                LoginMemberResponse.from(waitingResult.member()),
                waitingResult.date(),
                ReservationTimeResponse.from(waitingResult.time()),
                ThemeResponse.from(waitingResult.theme())
        );
    }

    public static List<WaitingResponse> from(List<WaitingResult> waitingResults) {
        return waitingResults.stream()
                .map(WaitingResponse::from)
                .toList();
    }
}
