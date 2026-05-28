package roomescape.controller.dto.response;

import java.util.List;
import roomescape.service.dto.result.ReservationDetailResults;

public record ReservationDetailResponses(
        List<ReservationDetailResponse> reservationDetailResponses
) {

    public static ReservationDetailResponses from(ReservationDetailResults results) {
        return new ReservationDetailResponses(
                results.details().stream()
                        .map(ReservationDetailResponse::from)
                        .toList()
        );
    }
}