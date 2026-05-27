package roomescape.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record WaitingRequest(
        @NotNull(message = "이름은 비어 있을 수 없습니다.")
        String name,
        @NotNull(message = "날짜는 비어 있을 수 없습니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,
        @NotNull(message = "시간 식별자는 필수 값입니다.")
        Long timeId,
        @NotNull(message = "테마 식별자는 필수 값입니다.")
        Long themeId,
        @NotNull(message = "대기 신청 시간은 필수 값입니다.")
        LocalDateTime createdAt
) {
}
