package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailableReservationTimeRequest(
        @NotNull(message = "날짜는 필수 선택 사항입니다.") LocalDate date,
        @NotNull(message = "테마는 필수 선택 사항입니다.") Long themeId
) {
}
