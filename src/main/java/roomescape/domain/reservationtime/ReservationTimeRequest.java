package roomescape.domain.reservationtime;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public record ReservationTimeRequest(@JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTimeRequest(LocalTime startAt) {
        this.startAt = startAt;
    }

    @Override
    public LocalTime startAt() {
        return startAt;
    }
}
