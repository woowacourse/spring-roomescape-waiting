package roomescape.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "잘못된 이메일 형식입니다.") String email,
        @NotBlank(message = "password 값이 없습니다.") String password
) {
}
