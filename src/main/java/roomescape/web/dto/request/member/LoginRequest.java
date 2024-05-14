package roomescape.web.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(
        @Pattern(regexp = "^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.") String email,
        @NotBlank(message = "이메일 또는 비밀번호를 형식에 맞춰 입력해주세요.") String password) {

}
