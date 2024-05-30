package roomescape.application.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import roomescape.application.dto.validator.EmailConstraint;

public record LoginRequest(
        @EmailConstraint String email,
        @NotBlank(message = "이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.") String password
) {
}
