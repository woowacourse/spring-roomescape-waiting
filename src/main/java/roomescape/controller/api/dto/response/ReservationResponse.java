package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

public record ReservationResponse(long id, ThemeResponse theme, String date,
                                  ReservationTimeResponse time, MemberResponse member,int order) {
    private static final int RESERVATION_SUCCESS_ORDER = 0;

    public static ReservationResponse toResponse(final ReservationOutput output) {
        return new ReservationResponse(
                output.id(),
                ThemeResponse.toResponse(output.theme()),
                output.date(),
                ReservationTimeResponse.toResponse(output.time()),
                MemberResponse.toResponse(output.member()),
                RESERVATION_SUCCESS_ORDER
        );
    }
    public static ReservationResponse toResponse(final WaitingOutput output) {
        return new ReservationResponse(
                output.id(),
                ThemeResponse.toResponse(output.theme()),
                output.date(),
                ReservationTimeResponse.toResponse(output.time()),
                MemberResponse.toResponse(output.member()),
                output.order()
        );
    }
}
