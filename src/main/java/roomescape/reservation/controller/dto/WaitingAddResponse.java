package roomescape.reservation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.service.dto.WaitingInfo;

public record WaitingAddResponse(
        long id,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime time,
        String status
) {
        public WaitingAddResponse(WaitingInfo waitingInfo) {
                this(waitingInfo.id(),
                        waitingInfo.themeInfo().name(),
                        waitingInfo.date(),
                        waitingInfo.timeInfo().startAt(),
                        "대기"
                );
        }
}
