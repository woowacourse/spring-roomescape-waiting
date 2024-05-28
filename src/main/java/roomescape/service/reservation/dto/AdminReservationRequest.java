package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.config.DateFormatConstraint;

public record AdminReservationRequest(
        @DateFormatConstraint LocalDate date,
        @NotNull(message = "회원 ID를 입력해주세요.") Long memberId,
        @NotNull(message = "시간 ID를 입력해주세요.") Long timeId,
        @NotNull(message = "테마 ID를 입력해주세요.") Long themeId) {
}
