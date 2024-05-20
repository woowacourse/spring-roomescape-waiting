package roomescape.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record TimeSaveRequest(@NotNull @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}
