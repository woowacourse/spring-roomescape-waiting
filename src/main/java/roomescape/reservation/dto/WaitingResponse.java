package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record WaitingResponse(
        int waitingNumber,
        String memberName,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime
) {
    public WaitingResponse(Reservation reservation) {
        this(
                reservation.getWaitingNumber(),
                reservation.getMemberName(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt()
        );
    }
}
