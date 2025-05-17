package roomescape.member.controller.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank(message = "이메일을 입력해주세요.") String email,
        @NotBlank(message = "패스워드를 입력해주세요.") String password,
        @NotBlank(message = "이름을 입력해주세요.") String name
) {
}
