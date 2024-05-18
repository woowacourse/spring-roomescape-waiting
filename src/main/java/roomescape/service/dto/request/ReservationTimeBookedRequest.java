package roomescape.service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationTimeBookedRequest(
        @NotNull
        LocalDate date,

        @NotNull
        @Positive
        Long themeId
) {
}
