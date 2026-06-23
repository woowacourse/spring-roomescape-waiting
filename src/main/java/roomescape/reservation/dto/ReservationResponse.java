package roomescape.reservation.dto;

import java.util.List;
import roomescape.reservationtime.dto.ReservationTimeResponse;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
import roomescape.reservationtime.ReservationTime;

public record ReservationResponse(
        Long id,
        Long memberId,
        String date,
        ReservationTimeResponse time,
        Long themeId,
        Long storeId,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        ReservationTime reservationTime = reservation.getTime();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMemberId(),
                reservation.getDate().toString(),
                ReservationTimeResponse.from(reservationTime),
                reservation.getThemeId(),
                reservation.getStoreId(),
                reservation.getStatus()
        );
    }

    public static List<ReservationResponse> fromAll(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }
}
