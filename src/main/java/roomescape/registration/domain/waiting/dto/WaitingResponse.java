package roomescape.registration.domain.waiting.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.registration.domain.reservation.domain.Reservation;
import roomescape.registration.domain.waiting.domain.Waiting;

public record WaitingResponse(
        long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime startAt
) {
    public static WaitingResponse from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();

        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getReservationTime().getStartAt()
        );
    }
}
