package roomescape.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일은 null 또는 공백일 수 없습니다.")
        @Email(message = "이메일 형식이 일치하지 않습니다. 예시: abc123@gmail.com)")
        String email,
        @NotBlank(message = "비밀번호는 null 또는 공백일 수 없습니다.")
        String password
) {
}
