package roomescape.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.param.RegisterMemberParam;

public record RegisterMemberRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String name
) {
    public RegisterMemberParam toServiceParam() {
        return new RegisterMemberParam(email, password, name);
    }
}
