package roomescape.reservation.dto.response;

import java.time.format.DateTimeFormatter;
import roomescape.reservation.domain.Waiting;

public record WaitingResponse(Long id, String memberName, String themeName, String date, String startAt) {

    public WaitingResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName().name(),
                waiting.getReservation().getTheme().getName().name(),
                waiting.getReservation().getDate(DateTimeFormatter.ISO_DATE),
                waiting.getReservation().getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
}
