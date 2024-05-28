package roomescape.service.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public record ReservationTimeSaveRequest(@NotNull(message = "예약 시간을 입력해주세요.") LocalTime startAt) {

    public ReservationTime toEntity(ReservationTimeSaveRequest request) {
        return new ReservationTime(request.startAt());
    }
}
