package roomescape.time.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

public record TimeSaveRequest(
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startAt,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endAt
) {
}
