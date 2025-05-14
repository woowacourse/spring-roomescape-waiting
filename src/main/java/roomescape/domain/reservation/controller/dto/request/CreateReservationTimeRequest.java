package roomescape.domain.reservation.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.reservation.application.dto.request.CreateReservationTimeServiceRequest;

public record CreateReservationTimeRequest(
        @NotNull(message = "시간을 필수로 입력해야 합니다.")
        LocalTime startAt
) {

    public CreateReservationTimeServiceRequest toServiceRequest() {
        return new CreateReservationTimeServiceRequest(startAt);
    }
}
