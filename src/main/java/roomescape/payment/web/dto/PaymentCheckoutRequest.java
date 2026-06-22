package roomescape.payment.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PaymentCheckoutRequest(
        @NotBlank(message = "예약자 이름은 비어 있을 수 없습니다.")
        @Size(max = 255, message = "예약자 이름은 255자를 넘을 수 없습니다.")
        String name,

        @NotNull(message = "예약 날짜는 비어 있을 수 없습니다.")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @NotNull(message = "예약 시간 id는 비어 있을 수 없습니다.")
        @Positive(message = "예약 시간 id는 1 이상의 숫자여야 합니다.")
        Long timeId,

        @NotNull(message = "테마 id는 비어 있을 수 없습니다.")
        @Positive(message = "테마 id는 1 이상의 숫자여야 합니다.")
        Long themeId
) {
}
