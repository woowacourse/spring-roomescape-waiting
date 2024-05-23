package roomescape.web.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservationWaitingWebRequest(
        @NotBlank(message = "예약 날짜는 필수입니다.") String date,
        @NotNull(message = "시간 정보는 필수입니다.") @Positive Long timeId,
        @NotNull(message = "테마 정보는 필수입니다.") @Positive Long themeId
) {
}
