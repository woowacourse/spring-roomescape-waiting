package roomescape.reservation.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import roomescape.auth.dto.LoginMember;

public record ReservationRequest(
        @FutureOrPresent @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}
