package roomescape.presentation.api.member;

import jakarta.validation.constraints.NotBlank;
import roomescape.application.member.RegisterParam;

public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다.") String email,
        @NotBlank(message = "비밀번호는 필수입니다.") String password,
        @NotBlank(message = "사용자명은 필수입니다.") String name
) {

    public RegisterParam toRegisterParameter() {
        return new RegisterParam(email, password, name);
    }
}
