package roomescape.dto.waiting;

import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        String themeName,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public static WaitingResponse from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();

        return new WaitingResponse(
                waiting.getId(),
                waiting.getMember().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
