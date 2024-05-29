package roomescape.controller.api.dto.response;

import roomescape.service.dto.output.ReservationOutput;
import roomescape.service.dto.output.WaitingOutput;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record MemberReservationsResponse(List<MemberReservationResponse> data) {
    public static MemberReservationsResponse toResponse(final List<ReservationOutput> reservationOutputs, final List<WaitingOutput> waitingOutputs) {
        return new MemberReservationsResponse(Stream.concat(
                        reservationOutputs.stream()
                                .map(MemberReservationResponse::from),
                        waitingOutputs.stream()
                                .map(MemberReservationResponse::from)
                )
                .sorted(Comparator.comparing(MemberReservationResponse::date)
                        .thenComparing(MemberReservationResponse::time))
                .toList());
    }
}
