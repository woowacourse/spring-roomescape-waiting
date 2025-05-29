package roomescape.waiting.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import roomescape.waiting.service.dto.WaitingAddCommand;

public record WaitingAddRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @JsonProperty(value = "theme") long themeId,
        @JsonProperty(value = "time")long timeId
) {
    public WaitingAddCommand toCommand(long memberId) {
        return new WaitingAddCommand(date, timeId, themeId, memberId);
    }
}
