package roomescape.auth.controller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank String name,
        @Email(message = "이메일 형식에 맞지 않습니다.") @NotBlank String email,
        @NotBlank String password
) {
}
