package roomescape.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(
        @NotBlank(message = "이름을 입력해주세요.") @Size(max = 20, message = "이름은 20자 이하로 입력해주세요.") String name,
        @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "올바른 이메일 형식이 아닙니다.") String email,
        @NotBlank(message = "비밀번호를 입력해주세요.") String password
) {
}
