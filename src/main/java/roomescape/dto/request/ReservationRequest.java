package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotNull(message = "회원 ID는 필수값 입니다.")
        Long memberId,

        @NotNull(message = "예약 날짜는 필수값 입니다.")
        LocalDate date,

        @NotNull(message = "예약 시간은 필수값 입니다.")
        Long timeId,

        @NotNull(message = "예약 테마는 필수값 입니다.")
        Long themeId,

        @NotNull(message = "결제 금액은 필수값 입니다.")
        Long amount
) {
}
