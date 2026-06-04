package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MyReservationRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name
) {
}
