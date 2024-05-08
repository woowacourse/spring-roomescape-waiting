package roomescape.controller.request;

import java.time.LocalDate;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.Positive;

import org.springframework.format.annotation.DateTimeFormat;

public record ReservationRequest(
        @Nonnull
        @DateTimeFormat
        LocalDate date,
        @Positive(message = "[ERROR] timeId의 값이 1보다 작을 수 없습니다.")
        long timeId,
        @Positive(message = "[ERROR] themeId의 값이 1보다 작을 수 없습니다.")
        long themeId) {
}
