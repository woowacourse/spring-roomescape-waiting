package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @NotBlank(message = "이메일에 빈값을 입력할 수 없습니다.")
        String email,
        @NotBlank(message = "비밀번호에 빈값을 입력할 수 없습니다.")
        String password
) {
}
