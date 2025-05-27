package roomescape.reservationTime.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeRequest(@NotNull @JsonFormat(pattern = "HH:mm") LocalTime startAt) {
}
