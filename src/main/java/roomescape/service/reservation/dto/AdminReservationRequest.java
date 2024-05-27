package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record AdminReservationRequest(
        @NotNull(message = "날짜를 입력해주세요.") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull(message = "회원 정보를 입력해주세요.") Long memberId,
        @NotNull(message = "시간을 입력해주세요.") Long timeId,
        @NotNull(message = "테마를 입력해주세요.") Long themeId) {
}
