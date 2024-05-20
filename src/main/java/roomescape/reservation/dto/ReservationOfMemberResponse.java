package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

import java.time.format.DateTimeFormatter;

public record ReservationOfMemberResponse(Long reservationId, String themeName, String date, String reservationTime, String status) {

    public ReservationOfMemberResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                reservation.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                "예약"
        );
    }
}
