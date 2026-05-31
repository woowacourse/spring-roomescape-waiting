package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationUpdateRequest(@NotNull LocalDate date, @NotNull Long timeId) {
}
