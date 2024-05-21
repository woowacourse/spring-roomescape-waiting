package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;

import java.util.List;

public record MemberReservationsResponse(List<MemberReservationResponse> data) {
    public static MemberReservationsResponse toResponse(final List<ReservationOutput> outputs) {
        return new MemberReservationsResponse(
                outputs.stream()
                        .map(MemberReservationResponse::toResponse)
                        .toList());
    }
}
