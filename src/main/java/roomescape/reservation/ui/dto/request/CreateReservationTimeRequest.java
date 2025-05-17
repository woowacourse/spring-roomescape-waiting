package roomescape.reservation.ui.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CreateReservationTimeRequest(
        @JsonFormat(pattern = "HH:mm")
        @NotNull
        LocalTime startAt
) {

}
