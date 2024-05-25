package roomescape.member.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.Reservation;

public record FindReservationResponse(Long reservationId,
                                      String theme,
                                      LocalDate date,
                                      LocalTime time,
                                      String status) {
    public static FindReservationResponse from(Reservation reservation) {
        return new FindReservationResponse(
                reservation.getId(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt(),
                "예약");
    }
}
