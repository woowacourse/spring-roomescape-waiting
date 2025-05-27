package roomescape.reservation.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeRequest(
        @NotNull @JsonFormat(pattern = "HH:mm", timezone = "Asia/Seoul") LocalTime startAt) {

}
