package roomescape.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import roomescape.service.param.RegisterMemberParam;

public record RegisterMemberRequest(
        @Email
        @NotNull(message = "이메일은 필수 값입니다.")
        String email,

        @NotNull(message = "비밀번호는 필수 값입니다.")
        String password,

        @NotNull(message = "회원명은 필수 값입니다.")
        @Length(min = 2, max = 10)
        String name) {
    public RegisterMemberParam toServiceParam() {
        return new RegisterMemberParam(name, email, password);
    }
}
