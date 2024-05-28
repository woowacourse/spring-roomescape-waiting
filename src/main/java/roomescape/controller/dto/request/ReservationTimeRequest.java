package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.service.dto.request.ReservationTimeCreationRequest;

public record ReservationTimeRequest(
        @NotNull(message = "예약 시작 시간을 입력해주세요.")
        LocalTime startAt
) {

    public ReservationTimeCreationRequest toReservationTimeCreationRequest() {
        return new ReservationTimeCreationRequest(startAt);
    }


}
