package roomescape.reservation.dto;

import roomescape.reservation.domain.Reservation;

import java.time.format.DateTimeFormatter;

public record MyReservationResponse(Long reservationId, String theme, String date, String time, String status) {

    // TODO: status를 Reservation이 갖도록 수정
    public MyReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getTheme().getName().name(),
                reservation.getDate(DateTimeFormatter.ISO_DATE),
                reservation.getTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm")),
                "예약"
        );
    }
}
