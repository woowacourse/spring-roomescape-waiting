package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
        @NotBlank(message = "이름은 필수값입니다.")
        String name
) {
}
