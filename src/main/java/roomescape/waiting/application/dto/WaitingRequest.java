package roomescape.waiting.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingRequest(
        @NotNull(message = "날짜는 필수 입력값입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull(message = "시간은 필수 입력값입니다.") Long timeId,
        @NotNull(message = "테마는 필수 입력값입니다.") Long themeId
) {
}
