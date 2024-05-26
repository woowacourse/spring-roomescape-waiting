package roomescape.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "이메일 형식에 맞지 않습니다.")
        @NotBlank(message = "이메일은 필수 값입니다.")
        String email,
        @NotBlank(message = "패스워드는 필수 값입니다.")
        String password
) {
}
