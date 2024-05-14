package roomescape.web.controller.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TokenWebRequest(
    @NotBlank(message = "이메일은 필수 입니다.")
    @Email(message = "이메일 형식이 아닙니다.")
    String email,
    @NotBlank(message = "비밀번호는 필수 입니다.")
    String password) {
}
