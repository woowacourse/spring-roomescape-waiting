package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWaiting;

public record ReservationResponse(
        Long id,
        String name,
        ReservationDetailResponse detailResponse
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(),
                reservation.getMemberName(),
                ReservationDetailResponse.from(reservation.getDetail())
        );
    }

    public static ReservationResponse from(ReservationWaiting reservation) {
        return new ReservationResponse(reservation.getId(),
                reservation.getMemberName(),
                ReservationDetailResponse.from(reservation.getDetail())
        );
    }
}
