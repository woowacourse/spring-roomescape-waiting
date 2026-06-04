package roomescape.controller.dto.response;

import java.util.List;
import roomescape.service.dto.result.ReservationDetailResults;

public record ReservationDetailResponses(
        List<ReservationResponse> reservationResponses,
        List<WaitingDetailResponse> waitingDetailResponses
) {

    public static ReservationDetailResponses from(ReservationDetailResults results) {
        return new ReservationDetailResponses(
                results.reservationResults().stream()
                        .map(ReservationResponse::from)
                        .toList(),
                results.waitingResults().stream()
                        .map(WaitingDetailResponse::from)
                        .toList()
        );
    }
}
