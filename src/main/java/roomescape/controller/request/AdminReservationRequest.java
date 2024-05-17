package roomescape.controller.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record AdminReservationRequest(
        @NotNull
        LocalDate date,
        @Positive(message = "[ERROR] themeId의 값이 1보다 작을 수 없습니다.")
        long themeId,
        @Positive(message = "[ERROR] timeId의 값이 1보다 작을 수 없습니다.")
        long timeId,
        @Positive(message = "[ERROR] memberId의 값이 1보다 작을 수 없습니다.")
        long memberId) {
}
