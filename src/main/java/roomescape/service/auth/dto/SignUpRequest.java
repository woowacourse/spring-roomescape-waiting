package roomescape.service.auth.dto;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.member.Email;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.Password;

public record SignUpRequest(
        @NotBlank(message = "이름을 입력해주세요.") MemberName name,
        @NotBlank(message = "이메일을 입력해주세요.") Email email,
        @NotBlank(message = "비밀번호를 입력해주세요.") Password password
) {
}
