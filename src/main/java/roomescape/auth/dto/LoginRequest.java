package roomescape.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "로그인 이메일은 필수입니다")
        @Email(message = "이메일 형태가 아닙니다")
        String email,
        @NotBlank(message = "로그인 비밀번호는 필수입니다") String password
) {
}
