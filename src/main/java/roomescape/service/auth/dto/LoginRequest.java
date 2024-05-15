package roomescape.service.auth.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.member.Email;
import roomescape.domain.member.Password;

public record LoginRequest(
        @NotBlank(message = "비밀번호를 입력해주세요.") Password password,
        @NotBlank(message = "이메일을 입력해주세요.") Email email
) {
}
