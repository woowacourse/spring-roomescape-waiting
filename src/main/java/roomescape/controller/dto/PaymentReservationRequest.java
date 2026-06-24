package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PaymentReservationRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,
        @NotNull(message = "날짜는 필수입니다.")
        LocalDate date,
        @NotNull(message = "유효한 시간대 번호가 필요합니다.")
        Long timeId,
        @NotNull(message = "유효한 테마 번호가 필요합니다.")
        Long themeId,
        @NotNull(message = "금액은 필수입니다.")
        Long amount,
        @NotBlank(message = "결제 키는 필수입니다.")
        String paymentKey,
        @NotBlank(message = "주문 ID는 필수입니다.")
        String orderId
) {
}
