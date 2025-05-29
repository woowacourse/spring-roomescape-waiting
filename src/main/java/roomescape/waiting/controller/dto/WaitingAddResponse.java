package roomescape.waiting.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.service.dto.WaitingInfo;

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
                        waitingInfo.theme().name(),
                        waitingInfo.date(),
                        waitingInfo.time().startAt(),
                        "대기"
                );
        }
}
