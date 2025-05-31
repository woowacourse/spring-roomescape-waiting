package roomescape.dto.auth;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record LoginInfo(long id, String name, String email, Role role) {

    public LoginInfo(Member loginMember) {
        this(
                loginMember.getId(),
                loginMember.getName(),
                loginMember.getEmail(),
                loginMember.getRole()
        );
    }
}
