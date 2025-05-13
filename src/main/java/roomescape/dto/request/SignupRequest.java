package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.domain.Member;
import roomescape.domain.Role;

public record SignupRequest(
        @NotBlank(message = "이름은 비어있을 수 없습니다.") String name,
        @NotBlank(message = "이메일은 비어있을 수 없습니다.") String email,
        @NotBlank(message = "비밀번호는 비어있을 수 없습니다.") String password
) {

    public Member toMember() {
        return new Member(name, email, password, Role.USER);
    }
}
