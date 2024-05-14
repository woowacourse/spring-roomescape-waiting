package roomescape.web.dto.request.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.") String email,
        @NotBlank(message = "이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.") String password) {

}
