package roomescape.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import roomescape.service.dto.request.MemberCreationRequest;

public record SignupRequest(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(min = 1, max = 255, message = "이메일은 1자 이상 255자 이하로 입력해주세요.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
        @Size(min = 1, max = 255, message = "비밀번호는 1자 이상 255자 이하로 입력해주세요.")
        String password,

        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(min = 1, max = 30, message = "이름은 1자 이상 30자 이하로 입력해주세요.")
        String name
) {

    public MemberCreationRequest toMemberCreationRequest() {
        return new MemberCreationRequest(email, password, name);
    }
}
