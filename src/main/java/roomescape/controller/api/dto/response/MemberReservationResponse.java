package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

public record MemberReservationResponse(long id, String themeName, String date, String time, int order) {
    private static final int RESERVATION_COMPLETE_ORDER = 0;
    public static MemberReservationResponse toResponse(final ReservationOutput output) {
        return new MemberReservationResponse(
                output.id(),
                output.theme().name(),
                output.date(),
                output.time().startAt(),
                RESERVATION_COMPLETE_ORDER
        );
    }
    public static MemberReservationResponse toResponse(final WaitingOutput output) {
        return new MemberReservationResponse(
                output.id(),
                output.theme().name(),
                output.date(),
                output.time().startAt(),
                output.order()
        );
    }
}
