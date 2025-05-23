package roomescape.waiting.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.Waiting;

public record WaitingInfoResponse(
    Long id,
    String memberName,
    String themeName,

    @JsonFormat(pattern = "MM-dd")
    LocalDate date,

    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt
) {
    public static WaitingInfoResponse from(Waiting waiting) {
        return new WaitingInfoResponse(
            waiting.getId(),
            waiting.getMember().getNameValue(),
            waiting.getTheme().getName(),
            waiting.getDate(),
            waiting.getReservationTime().getStartAt()
        );
    }
}
