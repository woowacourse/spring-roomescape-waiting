package roomescape.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.service.dto.request.CreateTokenRequest;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        String password
) {

    public CreateTokenRequest toCreateTokenRequest() {
        return new CreateTokenRequest(email, password);
    }
}
