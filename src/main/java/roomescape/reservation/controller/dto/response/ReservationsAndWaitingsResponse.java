package roomescape.reservation.controller.dto.response;

import roomescape.wating.controller.dto.response.WaitingResponse;

import java.util.List;

public record ReservationsAndWaitingsResponse(
        List<ReservationPaymentResponse> reservations,
        List<WaitingResponse> waitings
) {


    public static ReservationsAndWaitingsResponse from(
            final List<ReservationPaymentResponse> reservations,
            final List<WaitingResponse> waitingsWithRank
    ) {
        return new ReservationsAndWaitingsResponse(
                reservations,
                waitingsWithRank
        );
    }
}
