package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Waiting;

public record MyWaitingServiceResponse(
    Long waitingId,
    String memberName,
    String themeName,
    LocalDate date,
    LocalTime time
) {

    public static MyWaitingServiceResponse from(Waiting waiting) {
        return new MyWaitingServiceResponse(
            waiting.getId(),
            waiting.getMember().getName(),
            waiting.getTheme().getName(),
            waiting.getDate(),
            waiting.getTime().getStartAt()
        );
    }
}
