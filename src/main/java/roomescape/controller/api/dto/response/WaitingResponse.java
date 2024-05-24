package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.WaitingOutput;

public record WaitingResponse(long id, ThemeResponse theme, String date,
                              ReservationTimeResponse time, MemberResponse member) {
    public static WaitingResponse from(final WaitingOutput output) {
        return new WaitingResponse(
                output.id(),
                ThemeResponse.from(output.theme()),
                output.date(),
                ReservationTimeResponse.from(output.time()),
                MemberResponse.from(output.member())
        );
    }
}
