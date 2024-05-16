package roomescape.service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import roomescape.domain.Member;
import roomescape.domain.Role;

public record SignupRequest(@Email(message = "잘못된 이메일 형식입니다.")
                            @NotNull(message = "이메일을 입력해주세요") String email,
                            @NotNull(message = "비밀번호를 입력해주세요") String password,
                            @NotBlank(message = "이름을 입력해주세요.") String name) {

    public Member toEntity(SignupRequest request) {
        return new Member(request.name(), request.email(), request.password(), Role.USER);
    }
}
