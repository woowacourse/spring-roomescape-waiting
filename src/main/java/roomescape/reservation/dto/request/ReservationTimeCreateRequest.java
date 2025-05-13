package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import roomescape.global.exception.InvalidInputException;

public record ReservationTimeCreateRequest(
    @JsonFormat(pattern = "HH:mm") @NotNull(message = "시간이 입력되지 않았다.") LocalTime startAt) {

}
