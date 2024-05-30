package roomescape.application.dto.request.member;

import jakarta.validation.constraints.NotBlank;
import roomescape.application.dto.validator.EmailConstraint;
import roomescape.domain.member.Member;

public record SignupRequest(
        @NotBlank(message = "이름은 공백일 수 없습니다.") String name,
        @EmailConstraint String email,
        @NotBlank(message = "비밀번호는 공백일 수 없습니다.") String password
) {

    public Member toMember() {
        return new Member(name, email, password);
    }
}
