package roomescape.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import roomescape.service.param.LoginMemberParam;

public record LoginMemberRequest(
        @NotNull(message = "이메일은 필수 값입니다.")
        @Email
        String email,

        @NotNull(message = "비밀번호는 필수 값입니다.")
        String password) {

    public LoginMemberParam toServiceParam() {
        return new LoginMemberParam(email, password);
    }
}
