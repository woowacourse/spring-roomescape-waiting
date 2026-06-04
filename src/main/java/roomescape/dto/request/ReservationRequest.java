package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotNull(message = "날짜는 필수입니다.")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate date,

        @Min(value = 1, message = "유효한 시간 ID가 필요합니다.")
        long timeId,

        @Min(value = 1, message = "유효한 테마 ID가 필요합니다.")
        long themeId
) {
}
