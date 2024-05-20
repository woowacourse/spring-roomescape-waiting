package roomescape.domain.member.dto;

import roomescape.domain.member.domain.Member;
import roomescape.domain.member.domain.Role;

public record MemberResponse(Long id, String name, String email, Role role) {

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail(), member.getRole());
    }
}
