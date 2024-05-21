package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record CreateReservationRequest(
    @NotNull(message = "null일 수 없습니다.")
    Long memberId,

    @NotNull(message = "null일 수 없습니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @NotNull(message = "null일 수 없습니다.")
    Long timeId,

    @NotNull(message = "null일 수 없습니다.")
    Long themeId
) { }
