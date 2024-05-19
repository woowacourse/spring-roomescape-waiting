package roomescape.service.schedule.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationTimeReadRequest(
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull(message = "테마 ID를 입력해주세요.") Long themeId
) {
}
