package roomescape.global.dto;

import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;

public record SessionMember(Long id, MemberName name, MemberRole role) {
    
    public boolean isSameMember(final Long otherId) {
        return this.id.equals(otherId);
    }

    public boolean isAdmin() {
        return role == MemberRole.ADMIN;
    }
}
