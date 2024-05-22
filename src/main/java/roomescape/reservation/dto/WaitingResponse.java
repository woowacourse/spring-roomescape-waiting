package roomescape.reservation.dto;

import java.time.format.DateTimeFormatter;
import roomescape.reservation.domain.Waiting;

public record WaitingResponse(Long id, String memberName, String themeName, String date, String startAt) {

    public WaitingResponse(Waiting waiting) {
        this(
                waiting.getId(),
                waiting.getMember().getName().name(),
                waiting.getTheme().getName().name(),
                waiting.getDate(DateTimeFormatter.ISO_DATE),
                waiting.getReservationTime().getStartAt(DateTimeFormatter.ofPattern("HH:mm"))
        );
    }
}
