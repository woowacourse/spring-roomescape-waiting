package roomescape.service.dto.response.wait;

import java.time.LocalDate;
import java.time.LocalTime;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationWait;

public record AdminWaitResponse(String waitId,
                                String memberName,
                                String themeName,
                                LocalDate date,
                                LocalTime time) {

    public static AdminWaitResponse from(ReservationWait wait) {
        Reservation reservation = wait.getReservation();
        return new AdminWaitResponse(
                wait.getId().toString(),
                wait.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt());
    }
}
