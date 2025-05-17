package roomescape.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest (
        @NotBlank(message = "로그인 시 이메일 입력은 필수입니다.") String email,
        @NotBlank(message = "로그인 시 비밀번호 입력은 필수입니다.") String password) {
}
