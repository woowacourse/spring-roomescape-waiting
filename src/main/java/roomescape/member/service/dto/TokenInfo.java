package roomescape.member.service.dto;

import roomescape.member.domain.MemberRole;

public record TokenInfo(long id, MemberRole role) {

    public boolean isNotAdmin() {
        return role != MemberRole.ADMIN;
    }
}
