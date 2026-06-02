package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ReservationWaitingRequest(

        @NotBlank(message = "name은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "name은 255자를 넘을 수 없습니다.")
        String name,

        @NotNull(message = "date는 비어 있을 수 없습니다.")
        LocalDate date,

        @NotNull(message = "timeId는 비어 있을 수 없습니다.")
        @Positive(message = "timeId는 양수이어야 합니다.")
        Long timeId,

        @NotNull(message = "themeId는 비어 있을 수 없습니다.")
        @Positive(message = "themeId는 양수이어야 합니다.")
        Long themeId
) {
}
