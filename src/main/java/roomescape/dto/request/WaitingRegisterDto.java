package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingRegisterDto(
        @NotNull
        Long theme,

        @NotNull
        Long time,

        @NotNull
        LocalDate date
) {
}
