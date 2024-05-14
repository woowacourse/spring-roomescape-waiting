package roomescape.reservation.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record MemberReservationRequest(
        @NotNull
        long memberId,

        @NotBlank
        @Pattern(regexp = "^(?:(?:19|20)\\d{2})-(?:0[1-9]|1[0-2])-(?:0[1-9]|[1-2][0-9]|3[0-1])$")
        String date,

        @NotNull
        long timeId,

        @NotNull
        long themeId
) {
}
