package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.WaitingOutput;

public record WaitingResponse(long id, ThemeResponse theme, String date,
                              ReservationTimeResponse time, MemberResponse member) {
    public static WaitingResponse toResponse(final WaitingOutput output) {
        return new WaitingResponse(
                output.id(),
                ThemeResponse.toResponse(output.theme()),
                output.date(),
                ReservationTimeResponse.toResponse(output.time()),
                MemberResponse.toResponse(output.member())
        );
    }
}
