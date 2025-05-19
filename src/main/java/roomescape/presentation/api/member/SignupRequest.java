package roomescape.presentation.api.member;

import jakarta.validation.constraints.NotBlank;
import roomescape.application.member.command.dto.RegisterCommand;

public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.") String email,
        @NotBlank(message = "비밀번호는 필수입니다.") String password,
        @NotBlank(message = "사용자명은 필수입니다.") String name
) {

    public RegisterCommand toRegisterParameter() {
        return new RegisterCommand(email, password, name);
    }
}
