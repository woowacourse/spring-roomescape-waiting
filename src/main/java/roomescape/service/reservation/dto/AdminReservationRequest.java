package roomescape.service.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull(message = "회원 ID를 입력해주세요.") Long memberId,
        @NotNull(message = "시간 ID를 입력해주세요.") Long timeId,
        @NotNull(message = "테마 ID를 입력해주세요.") Long themeId) {
}
