package roomescape.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.param.LoginMemberParam;

public record LoginMemberRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {

    public LoginMemberParam toServiceParam() {
        return new LoginMemberParam(email, password);
    }
}
