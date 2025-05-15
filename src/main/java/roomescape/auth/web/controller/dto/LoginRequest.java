package roomescape.auth.web.controller.dto;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull(message = "이메일을 입력해주세요.")
        String email,
        @NotNull(message = "비밀번호를 입력해주세요.")
        String password
) {

}
