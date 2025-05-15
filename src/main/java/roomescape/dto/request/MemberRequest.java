package roomescape.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.auth.Role;
import roomescape.domain.Member;

public record MemberRequest(
        @Email String email,
        @NotBlank String password,
        @NotBlank String name
) {

    public Member toMember() {
        return new Member(null, name, email, password, Role.MEMBER);
    }
}
