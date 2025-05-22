package roomescape.dto.auth;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record LoginInfo(long id, String name, String email, Role role) {
    public static LoginInfo from(Member member) {
        return new LoginInfo(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }
}
