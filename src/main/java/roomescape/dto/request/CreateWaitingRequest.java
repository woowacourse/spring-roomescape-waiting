package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateWaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull(message = "예약 시간 ID는 필수입니다.") Long timeId,
        @NotNull(message = "테마 ID는 필수입니다.") Long themeId
) {
}
