package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record WaitingResponse(

        Long id,
        int waitingNumber,
        String memberName,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime
) {
    public WaitingResponse(Reservation reservation, int waitingNumber) {
        this(
                reservation.getId(),
                waitingNumber,
                reservation.getMemberName(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt()
        );
    }
}
