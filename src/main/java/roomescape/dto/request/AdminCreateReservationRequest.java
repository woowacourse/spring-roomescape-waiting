package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminCreateReservationRequest(
        @NotNull(message = "멤버 ID는 필수입니다.") Long memberId,
        LocalDate date,
        @NotNull(message = "예약 시간 ID는 필수입니다.") Long timeId,
        @NotNull(message = "테마 ID는 필수입니다.") Long themeId
) {
}
