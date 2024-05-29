package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

public record ReservationResponse(long id, ThemeResponse theme, String date,
                                  ReservationTimeResponse time, MemberResponse member) {

    public static ReservationResponse from(final ReservationOutput output) {
        return new ReservationResponse(
                output.id(),
                ThemeResponse.from(output.theme()),
                output.date(),
                ReservationTimeResponse.from(output.time()),
                MemberResponse.from(output.member())
                );
    }

    public static ReservationResponse from(final WaitingOutput output) {
        return new ReservationResponse(
                output.id(),
                ThemeResponse.from(output.theme()),
                output.date(),
                ReservationTimeResponse.from(output.time()),
                MemberResponse.from(output.member())
        );
    }
}
