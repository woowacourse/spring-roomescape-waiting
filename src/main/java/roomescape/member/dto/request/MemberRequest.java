package roomescape.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

public record MemberRequest(
        @Email(message = "잘못된 이메일 형식입니다.") String email,
        @NotBlank(message = "password 값이 없습니다.") String password,
        @NotBlank(message = "name 값이 없습니다.") String name
) {

    public Member toMember() {
        return new Member(null, name, email, password, Role.MEMBER);
    }
}
