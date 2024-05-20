package roomescape.waiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record WaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long themeId,
        Long timeId) {
}
