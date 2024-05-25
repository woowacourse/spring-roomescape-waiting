package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.service.dto.request.CreateReservationTimeRequest;

public record ReservationTimeRequest(
        @NotNull(message = "예약 시작 시간을 입력해주세요.")
        LocalTime startAt
) {

    public CreateReservationTimeRequest toCreateReservationTimeRequest() {
        return new CreateReservationTimeRequest(startAt);
    }


}
