package roomescape.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "이메일은 형식에 맞아야 합니다.")
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        String email,
        @NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
        String password
) {
}
