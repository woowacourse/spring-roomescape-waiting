package roomescape.dto.response;

import roomescape.domain.Reservation;
import roomescape.dto.result.ReservationResult;

import java.util.List;

public record ReservationResponse(
        Long id,
        Long memberId,
        String date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        StoreResponse store
) {
    public static ReservationResponse from(ReservationResult reservationResult) {
        Reservation reservation = reservationResult.reservation();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservationResult.theme()),
                StoreResponse.from(reservationResult.store())
        );
    }

    public static List<ReservationResponse> fromAll(List<ReservationResult> reservationResults) {
        return reservationResults.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
