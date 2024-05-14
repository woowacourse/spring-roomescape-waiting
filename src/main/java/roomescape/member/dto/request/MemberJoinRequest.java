package roomescape.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public record MemberJoinRequest(
        @NotBlank(message = "이메일은 비어있을 수 없습니다.")
        String email,
        @NotBlank(message = "비밀번호는 비어있을 수 없습니다.")
        String password,
        @NotBlank(message = "이름은 비어있을 수 없습니다.")
        String name
) {

    public Member toModel(Role role) {
        return new Member(name, email, password, role);
    }
}
