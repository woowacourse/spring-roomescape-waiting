package roomescape.service.dto.response;

import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

public record MemberResponse(Long id, String name, Role role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getRole());
    }
}
