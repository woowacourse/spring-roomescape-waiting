package roomescape.member.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.Reservation;

public record FindReservationResponse(Long reservationId,
                                      String theme,
                                      LocalDate date,
                                      LocalTime time,
                                      String status) {
    public static FindReservationResponse from(final Reservation reservation) {
        return new FindReservationResponse(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt(),
                "예약");
    }
}
