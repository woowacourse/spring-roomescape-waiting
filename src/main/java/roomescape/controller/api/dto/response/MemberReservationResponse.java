package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;

public record MemberReservationResponse(long id, String themeName, String date, String time, String status) {
    public static MemberReservationResponse toResponse(final ReservationOutput output) {
        return new MemberReservationResponse(
                output.id(),
                output.theme().name(),
                output.date(),
                output.time().startAt(),
                output.reservationStatus()
        );
    }
}
