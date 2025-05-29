package roomescape.dto.response;

import java.util.List;
import roomescape.domain.Reservation;
import roomescape.dto.business.WaitingWithRank;

public record ReservationStatusResponse(
        List<ReservationProfileResponse> reservationResponses,
        List<WaitingWithRankResponse> waitingWithRankResponses
) {

    public static ReservationStatusResponse createReservationStatusResponses(
            List<Reservation> reservations,
            List<WaitingWithRank> waitingWithRanks
    ) {
        List<ReservationProfileResponse> reservationResponses =
                reservations.stream().map(ReservationProfileResponse::new).toList();
        List<WaitingWithRankResponse> waitingResponses =
                waitingWithRanks.stream().map(WaitingWithRankResponse::new).toList();
        return new ReservationStatusResponse(reservationResponses, waitingResponses);
    }
}
