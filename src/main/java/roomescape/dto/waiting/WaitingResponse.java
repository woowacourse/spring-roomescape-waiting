package roomescape.dto.waiting;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.waiting.Waiting;

public record WaitingResponse(
        Long waitingId,
        String member,
        String theme,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonFormat(pattern = "HH-mm") LocalTime startAt
) {

    public static WaitingResponse from(Waiting waiting) {
        Reservation reservation = waiting.getReservation();
        return new WaitingResponse(
                waiting.getId(),
                reservation.getMember().getName(),
                reservation.getTheme().getThemeName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}
