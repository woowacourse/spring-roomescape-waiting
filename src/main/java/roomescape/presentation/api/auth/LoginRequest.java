package roomescape.presentation.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.application.auth.dto.LoginParam;

public record LoginRequest(
        @Email(message = "이메일 형식이 아닙니다.") String email,
        @NotBlank(message = "password는 필수입니다.") String password
) {

    public LoginParam toServiceParam() {
        return new LoginParam(email, password);
    }
}
