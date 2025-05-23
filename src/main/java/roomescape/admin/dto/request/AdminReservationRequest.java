package roomescape.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationRequest(
        @NotNull(message = "memberId 값이 없습니다.") Long memberId,
        @NotNull(message = "date 값이 없습니다.") LocalDate date,
        @NotNull(message = "timeId 값이 없습니다.") Long timeId,
        @NotNull(message = "themeId 값이 없습니다.") Long themeId
) {
}
