package roomescape.waiting.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.waiting.service.dto.WaitingInfo;

public record WaitingResponse(
        long id,
        @JsonProperty(value = "name") String memberName,
        String theme,
        LocalDate date,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt
) {

    public WaitingResponse(final WaitingInfo waitingInfo) {
        this(waitingInfo.id(),
                waitingInfo.member().name(),
                waitingInfo.theme().name(),
                waitingInfo.date(),
                waitingInfo.time().startAt()
        );
    }
}
