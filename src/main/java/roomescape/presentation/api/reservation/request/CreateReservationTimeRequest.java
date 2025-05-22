package roomescape.presentation.api.reservation.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.application.reservation.dto.CreateReservationTimeParam;

public record CreateReservationTimeRequest(
        @NotNull(message = "startAt는 필수입니다.")
        LocalTime startAt
) {

    public CreateReservationTimeParam toServiceParam() {
        return new CreateReservationTimeParam(startAt);
    }
}
