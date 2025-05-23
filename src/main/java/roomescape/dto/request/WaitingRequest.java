package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingRequest(
        @NotNull LocalDate date,
        @NotNull Long theme,
        @NotNull Long time
) {
}
