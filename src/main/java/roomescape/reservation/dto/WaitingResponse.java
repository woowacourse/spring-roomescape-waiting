package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record WaitingResponse(
        Long id,
        String memberName,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime
) {
    public WaitingResponse(Reservation reservation) {
        this(
                reservation.getId(),
                reservation.getMemberName(),
                reservation.getThemeName(),
                reservation.getDate(),
                reservation.getStartAt()
        );
    }
}
