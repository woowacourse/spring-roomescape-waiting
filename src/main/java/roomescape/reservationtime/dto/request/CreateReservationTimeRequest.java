package roomescape.reservationtime.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservationtime.model.ReservationTime;

public record CreateReservationTimeRequest(
        @NotNull(message = "예약 시간은 공백 문자가 불가능합니다.")
        LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(
                null,
                startAt);
    }
}
