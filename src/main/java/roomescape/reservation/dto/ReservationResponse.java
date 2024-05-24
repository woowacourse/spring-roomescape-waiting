package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long id,
        String memberName,
        ReservationDetailResponse detailResponse
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(),
                reservation.getMemberName(),
                ReservationDetailResponse.from(reservation.getDetail())
        );
    }
}
