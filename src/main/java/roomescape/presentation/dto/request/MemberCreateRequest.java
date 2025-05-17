package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MemberCreateRequest(
        @NotBlank(message = "사용자 이름은 필수입니다.") String name,
        @NotBlank(message = "사용자 이메일은 필수입니다.") String email,
        @NotBlank(message = "사용자 비밀번호는 필수입니다.") String password
) {
}
