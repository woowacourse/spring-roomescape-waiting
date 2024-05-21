package roomescape.web.controller.request;

import jakarta.validation.constraints.NotNull;

public record ReservationWaitingStatusWebRequest(
        @NotNull(message = "거절 여부는 필수입니다.") boolean isDenied
) {
}
