package roomescape.reservation.dto;

import java.time.format.DateTimeFormatter;
import roomescape.reservation.domain.Reservation;

public record MemberReservationResponse(
        Long reservationId,
        String themeName,
        String date,
        String reservationTime,
        String status
) {

    public MemberReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                reservation.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                "예약"
        );
    }
}
