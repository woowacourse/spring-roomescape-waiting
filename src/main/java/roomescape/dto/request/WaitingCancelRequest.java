package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WaitingCancelRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name
) {
}
