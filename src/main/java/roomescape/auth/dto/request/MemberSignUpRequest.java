package roomescape.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

public record MemberSignUpRequest(
    @NotBlank(message = "이름은 빈 값이 올 수 없습니다")
    String name,
    @NotBlank(message = "이메일은 빈 값이 올 수 없습니다")
    @Email(message = "이메일 형식이 아닙니다.")
    String email,
    @NotBlank(message = "패스워드는 빈 값이 올 수 없습니다")
    String password
) {
    public Member toMember() {
        return new Member(null, name, email, MemberRole.USER, password);
    }
}
