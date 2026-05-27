package roomescape.dto.response;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

import java.util.List;

public record ReservationResponse(
        Long id,
        Long memberId,
        String date,
        ReservationTimeResponse time,
        Long themeId,
        Long storeId
) {
    public static ReservationResponse from(Reservation reservation) {
        ReservationTime reservationTime = reservation.getTime();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservationTime),
                reservation.getThemeId(),
                reservation.getStoreId()
        );
    }

    public static List<ReservationResponse> fromAll(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
