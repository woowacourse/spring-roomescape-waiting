package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 10, message = "이름은 10자 이하로 입력해주세요.")
        String name,
        @NotBlank(message = "로그인 ID를 입력해주세요.")
        @Size(max = 255, message = "로그인 ID는 255자 이하로 입력해주세요.")
        String loginId,
        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(max = 255, message = "비밀번호는 255자 이하로 입력해주세요.")
        String password,
        @NotBlank(message = "비밀번호 확인을 입력해주세요.")
        @Size(max = 255, message = "비밀번호 확인은 255자 이하로 입력해주세요.")
        String passwordConfirm
) {
}
