package roomescape.waiting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationWaitingRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotNull(message = "대기할 테마는 필수로 입력해야 합니다.")
        Long themeId,
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "대기할 시간은 필수로 입력해야 합니다.")
        Long timeId
) {
}
