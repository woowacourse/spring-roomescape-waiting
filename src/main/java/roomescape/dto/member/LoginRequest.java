package roomescape.dto.member;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "[ERROR] 비밀번호가 없습니다.") String password,
        @NotBlank(message = "[ERROR] 이메일이 없습니다.") String email
) {
}
