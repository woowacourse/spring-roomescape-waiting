package roomescape.web.controller.request;

import jakarta.validation.constraints.NotNull;
import roomescape.domain.ReservationWaitingStatus;

public record ReservationWaitingStatusWebRequest(
        @NotNull(message = "대기 상태는 필수입니다.") ReservationWaitingStatus status
) {
}
