package roomescape.controller.dto.payment;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record PaymentOrderRequest(
        @NotNull(message = "날짜를 입력해주세요.")
        LocalDate date,
        @NotNull(message = "시간을 입력해주세요.")
        Long timeId,
        @NotNull(message = "테마를 입력해주세요.")
        Long themeId
) {
}
