package roomescape.web.dto.request.time;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import roomescape.domain.ReservationTime;

public record ReservationTimeRequest(
        @NotNull(message = "시간은 빈값을 허용하지 않습니다.") LocalTime startAt) {

    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }

    @Override
    @JsonFormat(shape = Shape.STRING, pattern = "HH:mm")
    public LocalTime startAt() {
        return startAt;
    }
}
