package roomescape.time.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record CreateReservationTimeRequest(@NotNull(message = "시간을 입력해주세요.") LocalTime startAt) {

    public ReservationTime convertToReservationTime() {
        return new ReservationTime(this.startAt);
    }
}
