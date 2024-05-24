package roomescape.service.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.config.DateFormatConstraint;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.service.schedule.dto.ReservationTimeResponse;
import roomescape.service.theme.dto.ThemeResponse;

public record ReservationWaitingResponse(
        long id,
        String name,
        @DateFormatConstraint LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {
    public ReservationWaitingResponse(ReservationWaiting waiting) {
        this(
                waiting.getId(),
                waiting.getMemberName(),
                waiting.getDate(),
                new ReservationTimeResponse(waiting.getReservationTime()),
                new ThemeResponse(waiting.getTheme()),
                waiting.getCreatedAt()
        );
    }
}
