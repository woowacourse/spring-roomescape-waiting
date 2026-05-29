package roomescape.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReservationRequest(
        @Size(max = 255)
        @NotBlank(message = "255자 이하의 이름을 입력해주세요.")
        String name,
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,
        @NotNull(message = "시간을 입력해주세요.")
        Long timeId,
        @NotNull(message = "테마를 입력해주세요.")
        Long themeId
) {
}
