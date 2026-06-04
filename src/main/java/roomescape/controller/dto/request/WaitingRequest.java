package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WaitingRequest(
        @NotBlank(message = "이름은 비어있을 수 없습니다") String name,
        @NotNull(message = "예약 날짜는 비어있을 수 없습니다") LocalDate date,
        @NotNull(message = "예약 시간은 비어있을 수 없습니다") Long timeId,
        @NotNull(message = "테마는 비어있을 수 없습니다") Long themeId
) {
}
