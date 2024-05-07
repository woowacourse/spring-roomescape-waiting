package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TokenRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password,
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "email형식에 맞지 않습니다.")
        String email) {
}
