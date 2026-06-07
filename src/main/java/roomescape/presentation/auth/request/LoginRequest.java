package roomescape.presentation.auth.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이름은 필수 입력값 입니다.")
        String name,

        @NotBlank(message = "비밀번호는 필수 입력값 입니다.")
        String password
) {
}
