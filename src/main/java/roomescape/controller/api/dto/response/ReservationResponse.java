package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

public record ReservationResponse(long id, ThemeResponse theme, String date,
                                  ReservationTimeResponse time, MemberResponse member) {

    public static ReservationResponse toResponse(final ReservationOutput output) {
        return new ReservationResponse(
                output.id(),
                ThemeResponse.toResponse(output.theme()),
                output.date(),
                ReservationTimeResponse.toResponse(output.time()),
                MemberResponse.toResponse(output.member())
                );
    }

    public static ReservationResponse toResponse(final WaitingOutput output) {
        return new ReservationResponse(
                output.id(),
                ThemeResponse.toResponse(output.theme()),
                output.date(),
                ReservationTimeResponse.toResponse(output.time()),
                MemberResponse.toResponse(output.member())
        );
    }
}
