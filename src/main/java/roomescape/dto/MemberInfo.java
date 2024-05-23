package roomescape.dto;

import roomescape.domain.Member;
import roomescape.domain.Role;

public record MemberInfo(long id, String name, Role role) {
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public static MemberInfo from(Member member) {
        return new MemberInfo(member.getId(), member.getName(), member.getRole());
    }
}
