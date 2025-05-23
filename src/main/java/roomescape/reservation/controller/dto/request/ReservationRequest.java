package roomescape.reservation.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
    @NotNull long timeId,
    @NotNull long themeId
) {
}
